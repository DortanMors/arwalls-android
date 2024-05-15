package ru.ssau.arwalls.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View


class ScalableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {
    private var imageBitmap: Bitmap? = null
    private val matrix = Matrix()
    private val matrixValues = FloatArray(9)
    private val gestureDetector: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector
    private val scaleFactor = 1.0f
    private val minScale = 1.0f
    private val maxScale = 5.0f

    init {
        gestureDetector = GestureDetector(
            /* context = */ context,
            /* listener = */ object : SimpleOnGestureListener() {
                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    matrix.postTranslate(-distanceX, -distanceY)
                    constrainTranslation()
                    invalidate()
                    return true
                }
            }
        )
        scaleGestureDetector = ScaleGestureDetector(
            /* context = */ context,
            /* listener = */ object : SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val scaleFactor = detector.scaleFactor
                    val currentScale: Float = currentScale
                    if (currentScale < maxScale && scaleFactor > 1.0f || currentScale > minScale && scaleFactor < 1.0f) {
                        matrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                        constrainScale()
                        invalidate()
                    }
                    return true
                }
            }
        )
    }

    fun setBitmap(newBitmap: Bitmap) {
        imageBitmap = newBitmap
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        imageBitmap?.let { bitmap ->
            canvas.drawBitmap(
                /* bitmap = */ bitmap,
                /* matrix = */ matrix,
                /* paint = */ null,
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = scaleGestureDetector.onTouchEvent(event)
        if (!scaleGestureDetector.isInProgress) {
            gestureDetector.onTouchEvent(event)
        }
        if (event.action == MotionEvent.ACTION_UP) {
            performClick()
        }
        return result || super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun constrainScale() {
        val currentScale = currentScale
        if (currentScale < minScale) {
            val scale = minScale / currentScale
            matrix.postScale(scale, scale)
        } else if (currentScale > maxScale) {
            val scale = maxScale / currentScale
            matrix.postScale(scale, scale)
        }
    }

    private fun constrainTranslation() {
        val bitmapWidth = imageBitmap?.width?.toFloat() ?: 0F
        val bitmapHeight = imageBitmap?.height?.toFloat() ?: 0F
        val bounds = RectF(
            /* left = */ 0f,
            /* top = */ 0f,
            /* right = */ bitmapWidth,
            /* bottom = */ bitmapHeight,
        )
        matrix.mapRect(bounds)
        val offsetX = getOffset(bounds.left, bounds.right, width.toFloat())
        val offsetY = getOffset(bounds.top, bounds.bottom, height.toFloat())
        matrix.postTranslate(offsetX, offsetY)
    }

    private val currentScale: Float
        get() {
            matrix.getValues(matrixValues)
            return matrixValues[Matrix.MSCALE_X]
        }

    private fun getOffset(start: Float, end: Float, extent: Float): Float {
        var offset = 0f
        if (start > 0) {
            offset = -start
        } else if (end < extent) {
            offset = extent - end
        }
        return offset
    }
}
