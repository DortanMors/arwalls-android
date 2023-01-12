package ru.ssau.arwalls.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.util.Log
import android.view.View
import ru.ssau.arwalls.common.Settings
import ru.ssau.arwalls.rawdepth.FloatsPerPoint
import ru.ssau.arwalls.ui.model.MapState

class MapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    private var bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)

    fun clear() {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    fun setMapState(mapState: MapState) {
        Log.d("HARDCODE", "setMapState")
        if (width <= 0) {
            return
        }
        val pointsArray = mapState.points.array()
        try {
            for (i in pointsArray.indices step FloatsPerPoint) {
                if (pointsArray[i + 1] in -Settings.scanVerticalRadius..Settings.scanVerticalRadius) { // Y
                    bitmap.setPixel(
                        (pointsArray[i] * Settings.mapScale + Settings.mapOffset).toInt(),     // X
                        (pointsArray[i + 2] * Settings.mapScale + Settings.mapOffset).toInt(), // Z
                        Settings.paintColor,
                    )
                }
            }
        }
        catch (e: Exception) {
            Log.e("HARDCODE", "setMapState", e)
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap, 0f, 0f, bitmapPaint)
        Log.d("HARDCODE", "onDraw")
    }

    companion object {
        private val paint = Paint().apply {
            color = Settings.paintColor
            style = Settings.paintStyle
            strokeWidth = Settings.strokeWidth
        }
        val bitmapPaint = Paint(paint).apply {
            flags = Paint.DITHER_FLAG
            colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }
}
