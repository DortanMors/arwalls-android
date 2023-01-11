package com.google.ar.core.codelab.rawdepth

import android.media.Image
import android.opengl.Matrix
import com.google.ar.core.Anchor
import com.google.ar.core.CameraIntrinsics
import com.google.ar.core.Frame
import com.google.ar.core.exceptions.NotYetAvailableException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.experimental.and
import kotlin.math.ceil
import kotlin.math.sqrt


/**
 * Convert depth data from ARCore depth images to 3D point clouds. Points are added by calling the
 * Raw Depth API, and reprojected into 3D space.
 */


const val FloatsPerPoint = 4 // X, Y, Z, confidence

fun create(frame: Frame, cameraPoseAnchor: Anchor): FloatBuffer? {
    try {
        val depthImage: Image = frame.acquireRawDepthImage16Bits()
        val confidenceImage: Image = frame.acquireRawDepthConfidenceImage()

        // Retrieve the intrinsic camera parameters corresponding to the depth image to
        // transform 2D depth pixels into 3D points. See more information about the depth values
        // at
        // https://developers.google.com/ar/develop/java/depth/overview#understand-depth-values.
        val intrinsics: CameraIntrinsics = frame.camera.textureIntrinsics
        val modelMatrix = FloatArray(16)
        cameraPoseAnchor.pose.toMatrix(modelMatrix, 0)
        val points: FloatBuffer = convertRawDepthImagesTo3dPointBuffer(
            depthImage, confidenceImage, intrinsics, modelMatrix
        )
        depthImage.close()
        confidenceImage.close()
        return points
    } catch (e: NotYetAvailableException) {
        // This normally means that depth data is not available yet.
        // This is normal, so you don't have to spam the logcat with this.
    }
    return null
}

/** Apply camera intrinsics to convert depth image into a 3D pointcloud.  */
private fun convertRawDepthImagesTo3dPointBuffer(
    depth: Image,
    confidence: Image,
    cameraTextureIntrinsics: CameraIntrinsics,
    modelMatrix: FloatArray,
): FloatBuffer {
    // Java uses big endian so change the endianness to ensure
    // that the depth data is in the correct byte order.
    val depthImagePlane = depth.planes[0]
    val depthByteBufferOriginal: ByteBuffer = depthImagePlane.buffer
    val depthByteBuffer: ByteBuffer = ByteBuffer.allocate(depthByteBufferOriginal.capacity())
    depthByteBuffer.order(ByteOrder.LITTLE_ENDIAN)
    while (depthByteBufferOriginal.hasRemaining()) {
        depthByteBuffer.put(depthByteBufferOriginal.get())
    }
    depthByteBuffer.rewind()
    val depthBuffer: ShortBuffer = depthByteBuffer.asShortBuffer()
    val confidenceImagePlane = confidence.planes[0]
    val confidenceBufferOriginal: ByteBuffer = confidenceImagePlane.buffer
    val confidenceBuffer: ByteBuffer = ByteBuffer.allocate(confidenceBufferOriginal.capacity())
    confidenceBuffer.order(ByteOrder.LITTLE_ENDIAN)
    while (confidenceBufferOriginal.hasRemaining()) {
        confidenceBuffer.put(confidenceBufferOriginal.get())
    }
    confidenceBuffer.rewind()

    // To transform 2D depth pixels into 3D points, retrieve the intrinsic camera parameters
    // corresponding to the depth image. See more information about the depth values at
    // https://developers.google.com/ar/develop/java/depth/overview#understand-depth-values.
    val intrinsicsDimensions = cameraTextureIntrinsics.imageDimensions
    val depthWidth = depth.width
    val depthHeight = depth.height
    val fx = cameraTextureIntrinsics.focalLength[0] * depthWidth / intrinsicsDimensions[0]
    val fy = cameraTextureIntrinsics.focalLength[1] * depthHeight / intrinsicsDimensions[1]
    val cx = cameraTextureIntrinsics.principalPoint[0] * depthWidth / intrinsicsDimensions[0]
    val cy = cameraTextureIntrinsics.principalPoint[1] * depthHeight / intrinsicsDimensions[1]

    // Allocate the destination point buffer. If the number of depth pixels is larger than
    // `maxNumberOfPointsToRender` we uniformly subsample. The raw depth image may have
    // different resolutions on different devices.
    val maxNumberOfPointsToRender = 20000f
    val step = ceil(sqrt((depthWidth * depthHeight / maxNumberOfPointsToRender).toDouble())).toInt()
    val points = FloatBuffer.allocate(depthWidth / step * depthHeight / step * FloatsPerPoint)
    val pointCamera = FloatArray(4)
    val pointWorld = FloatArray(4)
    var y = 0
    while (y < depthHeight) {
        var x = 0
        while (x < depthWidth) {

            // Depth images are tightly packed, so it's OK to not use row and pixel strides.
            val depthMillimeters = depthBuffer.get(y * depthWidth + x) // Depth image pixels are in mm.
            if (depthMillimeters == 0.toShort()) {
                // Pixels with value zero are invalid, meaning depth estimates are missing from
                // this location.
                x += step
                continue
            }
            val depthMeters = depthMillimeters / 1000.0f // Depth image pixels are in mm.

            // Retrieve the confidence value for this pixel.
            val confidencePixelValue: Byte = confidenceBuffer.get(
                y * confidenceImagePlane.rowStride
                    + x * confidenceImagePlane.pixelStride
            )
            val confidenceNormalized: Float = (confidencePixelValue and 0xff.toByte()).toFloat() / 255.0f

            // Unproject the depth into a 3D point in camera coordinates.
            pointCamera[0] = depthMeters * (x - cx) / fx
            pointCamera[1] = depthMeters * (cy - y) / fy
            pointCamera[2] = -depthMeters
            pointCamera[3] = 1f

            // Apply model matrix to transform point into world coordinates.
            Matrix.multiplyMV(pointWorld, 0, modelMatrix, 0, pointCamera, 0)
            points.put(pointWorld[0]) // X.
            points.put(pointWorld[1]) // Y.
            points.put(pointWorld[2]) // Z.
            points.put(confidenceNormalized)
            x += step
        }
        y += step
    }
    points.rewind()
    return points
}
