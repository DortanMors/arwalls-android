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
import ru.ssau.arwalls.common.drawMarker
import ru.ssau.arwalls.data.MapPoint
import ru.ssau.arwalls.rawdepth.FloatsPerPoint
import ru.ssau.arwalls.ui.model.MapState

class MapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    private var bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
    private var cameraPosition = MapPoint(0f, 0f)

    fun clear() {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    fun centralize() {
        val xOffset = width / 2f - cameraPosition.x
        val yOffset = height / 2f - cameraPosition.y
        Settings.mapOffsetX = xOffset
        Settings.mapOffsetY = yOffset
        val canvas = Canvas(bitmap)
        canvas.translate(xOffset, yOffset)
    }

    fun setMapState(mapState: MapState) {
//        Log.d("HARDCODE", "setMapState")
        if (width <= 0) {
            return
        }
        cameraPosition = MapPoint(
            x = mapState.cameraPosition.x * Settings.mapScale + Settings.mapOffsetX,
            y = mapState.cameraPosition.y * Settings.mapScale + Settings.mapOffsetY,
        )
        val pointsArray = mapState.points.array()
        try {
            for (i in pointsArray.indices step FloatsPerPoint) {
                if (pointsArray[i + 1] - Settings.heightOffset in -Settings.scanVerticalRadius..Settings.scanVerticalRadius) { // Y
                    bitmap.setPixel(
                        (pointsArray[i] * Settings.mapScale + Settings.mapOffsetX).toInt(),     // X
                        (pointsArray[i + 2] * Settings.mapScale + Settings.mapOffsetY).toInt(), // Z
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
        if (Settings.markerVisibility) {
            canvas.drawMarker(cameraPosition, Settings.markerSize, markerPaint)
        }
//        Log.d("HARDCODE", "onDraw")
    }

    companion object {
        val bitmapPaint = Paint().apply {
            color = Settings.paintColor
            flags = Paint.DITHER_FLAG
            colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
        val markerPaint = Paint().apply {
            color = Settings.markerColor
            style = Settings.paintStyle
            strokeWidth = Settings.strokeWidth
        }
    }
}

