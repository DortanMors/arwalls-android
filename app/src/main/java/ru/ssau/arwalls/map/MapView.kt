package ru.ssau.arwalls.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import ru.ssau.arwalls.common.Settings
import ru.ssau.arwalls.ui.model.MapState

class MapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    var path: Path = Path()

    fun setMapState(mapState: MapState) {
        Log.d("HARDCODE", "setMapState")
        path = mapState.path
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("HARDCODE", "onDraw")
        canvas.drawPath(path, paint)
    }

    companion object {
        private val paint = Paint().apply {
            color = Settings.paintColor
            style = Settings.paintStyle
            strokeWidth = Settings.strokeWidth
        }
    }
}
