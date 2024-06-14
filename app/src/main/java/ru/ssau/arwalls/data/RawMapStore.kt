package ru.ssau.arwalls.data

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.ssau.arwalls.common.Settings
import ru.ssau.arwalls.common.tag
import ru.ssau.arwalls.rawdepth.FloatsPerPoint
import ru.ssau.arwalls.ui.model.MapState

object RawMapStore {
    private const val mapScale = 100

    private val rawMatrix = BitMatrix()
    private val rawMapStateFlow = MutableStateFlow(MapState())
    private val rawResultMap = rawMapStateFlow.map { mapState ->
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
        rawMatrix
    }

    private val coroutineScope = CoroutineScope(
        Dispatchers.IO +
            Job() +
            CoroutineName("RawMapStore") +
            CoroutineExceptionHandler { _, throwable -> Log.e(tag, throwable.toString()) }
    )

    init {
        coroutineScope.launch {
            rawResultMap.collect {

            }
        }
    }

    fun updateMapStateAsync(mapState: MapState) {
        coroutineScope.launch {
            rawMapStateFlow.emit(mapState)
        }
    }

    suspend fun getBitmap(): Bitmap =
        with(rawResultMap.first()) {
            val points = getFilledPoints().toMutableList().toList()

            var maxX = Int.MIN_VALUE
            var minX = Int.MAX_VALUE
            var maxY = Int.MIN_VALUE
            var minY = Int.MAX_VALUE

            points.forEach { (x, y) ->
                when {
                    x > maxX -> maxX = x
                    x < minX -> minX = x
                }
                when {
                    y > maxY -> maxY = y
                    y < minY -> minY = y
                }
            }

            val offsetX = 0 - minX
            val offsetY = 0 - minY
            val width = maxX - minX + 1
            val height = maxY - minY + 1
            Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565).apply {
                eraseColor(Color.WHITE)
                points.forEach { (x, y) ->
                    val bitmapX = x + offsetX
                    val bitmapY = y + offsetY
                    if (bitmapX > 0 && bitmapY > 0 && bitmapX < width && bitmapY < height) {
                        setPixel(bitmapX, bitmapY, Color.BLACK)
                    }
                }
            }
        }

    fun clear() {
        rawMatrix.clear()
    }
}
