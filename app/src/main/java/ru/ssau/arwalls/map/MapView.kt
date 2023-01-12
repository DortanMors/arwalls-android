package ru.ssau.arwalls.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
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

    private var path: Path = Path()
    private val bitmap by lazy {
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    fun setMapState(mapState: MapState) {
        if (width <= 0) {
            return
        }
        Log.d("HARDCODE", "setMapState")
        path = mapState.path
        val canvas = Canvas(bitmap)
        draw(canvas)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap, 0f, 0f, bitmapPaint)
        canvas.drawPath(path, paint)
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
