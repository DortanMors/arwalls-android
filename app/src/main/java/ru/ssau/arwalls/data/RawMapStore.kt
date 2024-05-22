package ru.ssau.arwalls.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import ru.ssau.arwalls.common.Settings
import ru.ssau.arwalls.ui.model.MapState


object RawMapStore {
    private var rawMatrix = BitMatrix()
    private var path = Path()
    private val bitmapPaint = Paint().apply {
        style = Settings.paintStyle
        strokeWidth = Settings.strokeWidth
        color = Color.BLACK
    }

    fun updateMapStateAsync(mapState: MapState) {
        path = mapState.path
    }

    fun getBitmap(): Bitmap =
        Bitmap.createBitmap(1000, 1000, Bitmap.Config.RGB_565).apply {
            eraseColor(Color.WHITE)
            drawScaledPathOnBitmap(path, this)
        }

    private fun drawScaledPathOnBitmap(path: Path, bitmap: Bitmap) {
        val pathBounds = RectF()
        path.computeBounds(pathBounds, true)
        val scaleX = bitmap.width / pathBounds.width()
        val scaleY = bitmap.height / pathBounds.height()
        val scale = minOf(scaleX, scaleY)
        val dx = (bitmap.width - pathBounds.width() * scale) / 2f - pathBounds.left * scale
        val dy = (bitmap.height - pathBounds.height() * scale) / 2f - pathBounds.top * scale

        val matrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate(dx, dy)
        }
        path.transform(matrix)

        Canvas(bitmap).drawPath(path, bitmapPaint)
    }

    fun clear() {
        rawMatrix = BitMatrix()
    }
}
