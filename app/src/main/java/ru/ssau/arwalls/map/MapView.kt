package ru.ssau.arwalls.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import ru.ssau.arwalls.common.Settings
import ru.ssau.arwalls.common.drawMarker
import ru.ssau.arwalls.data.MapPoint
import ru.ssau.arwalls.ui.model.MapState

class MapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    private var path: Path = Path()
    private var cameraPosition = MapPoint(0f, 0f)

    fun clear() {
        path = Path()
    }

    fun centralize() {
        val xOffset = width / 2f - cameraPosition.x
        val yOffset = height / 2f - cameraPosition.y
        Settings.mapOffsetX = xOffset
        Settings.mapOffsetY = yOffset
        invalidate()
    }

    fun setMapState(mapState: MapState) {
        Log.d("HARDCODE", "setMapState")
        if (width <= 0) {
            return
        }
        cameraPosition = MapPoint(
            x = mapState.cameraPosition.x * Settings.mapScale + Settings.mapOffsetX,
            y = mapState.cameraPosition.y * Settings.mapScale + Settings.mapOffsetY,
        )
        path = mapState.path
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, bitmapPaint)
        if (Settings.markerVisibility) {
            canvas.drawMarker(cameraPosition, Settings.markerSize, markerPaint)
        }
        Log.d("HARDCODE", "onDraw")
    }

    companion object {
        val bitmapPaint = Paint().apply {
            style = Settings.paintStyle
            strokeWidth = Settings.strokeWidth
            color = Settings.paintColor
        }
        val markerPaint = Paint().apply {
            color = Settings.markerColor
            style = Settings.paintStyle
            strokeWidth = Settings.strokeWidth
        }
    }
}
