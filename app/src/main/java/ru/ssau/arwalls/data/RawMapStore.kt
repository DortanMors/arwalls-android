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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.ssau.arwalls.common.Settings
import ru.ssau.arwalls.common.tag
import ru.ssau.arwalls.rawdepth.FloatsPerPoint
import ru.ssau.arwalls.ui.model.MapState

object RawMapStore {
    private const val mapScale = 100

    private var rawMatrix = BitMatrix()
    val mutex = Mutex()

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

    suspend fun getBitmap(): Bitmap =
        with(rawMatrix) {
            val points = filledPoints.toMutableList().toList()
            val maxX = points.maxOf { (x, _) -> x }
            val minX = points.minOf { (x, _) -> x }
            val maxY = points.maxOf { (_, y) -> y }
            val minY = points.minOf { (_, y) -> y }
            val offsetX = 0 - minX
            val offsetY = 0 - minY
            val width = maxX - minX + 1
            val height = maxY - minY + 1
            Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565).apply {
                points.forEach { (x, y) ->
                    setPixel(x + offsetX, y + offsetY, Color.RED)
                }
            }
        }

    fun clear() {
        rawMatrix = BitMatrix()
    }
}
