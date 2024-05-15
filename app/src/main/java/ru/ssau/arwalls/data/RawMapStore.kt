package ru.ssau.arwalls.data

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.ssau.arwalls.common.Settings
import ru.ssau.arwalls.common.tag
import ru.ssau.arwalls.rawdepth.FloatsPerPoint
import ru.ssau.arwalls.ui.model.MapState

object RawMapStore {
    private const val mapScale = 100

    private var rawMatrix = BitMatrix()

    private val coroutineScope = CoroutineScope(
        Dispatchers.IO +
            Job() +
            CoroutineName("RawMapStore") +
            CoroutineExceptionHandler { _, throwable -> Log.e(tag, throwable.toString()) }
    )

    fun updateMapStateAsync(mapState: MapState) {
        coroutineScope.launch {
            val pointsArray = mapState.points.array()
            with(rawMatrix) {
                for (i in pointsArray.indices step FloatsPerPoint) {
                    if (pointsArray[i + 1] - Settings.heightOffset in -Settings.scanVerticalRadius..Settings.scanVerticalRadius) { // Y
                        set(
                            x = (pointsArray[i] * mapScale).toInt(),     // X
                            y = (pointsArray[i + 2] * mapScale).toInt(), // Z
                        )
                    }
                }
            }
        }
    }

    fun getBitmap(): Bitmap =
        with(rawMatrix) {
            val maxX = filledPoints.maxOf { (x, _) -> x }
            val minX = filledPoints.minOf { (x, _) -> x }
            val maxY = filledPoints.maxOf { (_, y) -> y }
            val minY = filledPoints.minOf { (_, y) -> y }
            val offsetX = 0 - minX
            val offsetY = 0 - minY
            val width = maxX - minX + 1
            val height = maxY - minY + 1
            Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565).apply {
                filledPoints.forEach { (x, y) ->
                    setPixel(x + offsetX, y + offsetY, Color.BLACK)
                }
            }
        }

    fun clear() {
        rawMatrix = BitMatrix()
    }
}
