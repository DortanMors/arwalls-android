package ru.ssau.arwalls.domain

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import java.io.IOException
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import ru.ssau.arwalls.common.helpers.DisplayRotationHelper
import ru.ssau.arwalls.common.helpers.SnackBarUseCase
import ru.ssau.arwalls.common.helpers.TrackingStateHelper
import ru.ssau.arwalls.common.rendering.BackgroundRenderer
import ru.ssau.arwalls.common.rendering.DepthRenderer
import ru.ssau.arwalls.common.tag
import ru.ssau.arwalls.data.Beacon
import ru.ssau.arwalls.data.MapPoint
import ru.ssau.arwalls.data.MapStore
import ru.ssau.arwalls.rawdepth.create
import ru.ssau.arwalls.ui.model.MapState


class OpenGLRendererUseCase(
    private val context: Context,
    private val displayRotationHelper: DisplayRotationHelper,
): GLSurfaceView.Renderer {

    private val backgroundRenderer = BackgroundRenderer()
    private val depthRenderer = DepthRenderer()
    var session: Session? = null

    private val coroutineScope = CoroutineScope(
        Dispatchers.Default +
            Job() +
            CoroutineExceptionHandler { _, throwable -> Log.e(tag, throwable.toString()) }
    )

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

            // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
            try {
                // Create the texture and pass it to ARCore session to be filled during update().
                backgroundRenderer.createOnGlThread(context)
                depthRenderer.createOnGlThread(context)
            } catch (e: IOException) {
                Log.e(tag, "Failed to read an asset file", e)
            }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            displayRotationHelper.onSurfaceChanged(width, height)
            GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
            // Clear screen to notify driver it should not load any pixels from previous frame.
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            // Notify ARCore session that the view size changed so that the perspective matrix and
            // the video background can be properly adjusted.
            val currentSession = session ?: return
            try {
                displayRotationHelper.updateSessionIfNeeded(currentSession)
                currentSession.setCameraTextureName(backgroundRenderer.textureId)

                // Obtain the current frame from ARSession. When the configuration is set to
                // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
                // camera framerate.
                val frame = currentSession.update()
                val camera = frame.camera

                // If frame is ready, render camera preview image to the GL surface.
                backgroundRenderer.draw(frame)

                // Retrieve the depth data for this frame.
                val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)
                val beacons = updatedAugmentedImages.map { img ->
                    Beacon(
                        id = img.index,
                        point = MapPoint(
                            x = img.centerPose.tx(),
                            y = img.centerPose.tz(),
                        ),
                    )
                }
                MapStore.updateMapState(
                    MapState(
                        path = DrawBeaconMap(beacons),
                        cameraPosition = MapPoint(
                            x = frame.camera.pose.tx(),
                            y = frame.camera.pose.tz(),
                        )
                    )
                )
                val points: FloatBuffer = frame.create(
                    cameraPoseAnchor = currentSession.createAnchor(camera.pose)
                ) ?: return
                depthRenderer.update(points)
                depthRenderer.draw(camera)
                SnackBarUseCase.hide()

                // If not tracking, show tracking failure reason instead.
                if (camera.trackingState == TrackingState.PAUSED) {
                    SnackBarUseCase.showMessage(
                        message = TrackingStateHelper.getTrackingFailureReasonString(camera)
                    )
                    return
                }
            } catch (t: Throwable) {
                Log.e(tag, "Exception on the OpenGL thread", t)
            }
    }
}
