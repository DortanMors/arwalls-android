package ru.ssau.arwalls.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import ru.ssau.arwalls.common.tag
import ru.ssau.arwalls.ui.model.MapState


object RawMapStore {
    private const val mapScale = 100

    private var rawMatrix = BitMatrix()
    val mutex = Mutex()
    private var path = Path()

    private val coroutineScope = CoroutineScope(
        Dispatchers.IO +
            Job() +
            CoroutineName("RawMapStore") +
            CoroutineExceptionHandler { _, throwable -> Log.e(tag, throwable.toString()) }
    )

    fun updateMapStateAsync(mapState: MapState) {
        path = mapState.path
    }

    suspend fun getBitmap(): Bitmap =
        Bitmap.createBitmap(1000, 1000, Bitmap.Config.RGB_565).apply {
            eraseColor(Color.WHITE)
            Canvas(this).drawPath(
                path,
                Paint().apply {
                    color = Color.BLACK
                    strokeWidth = 10f
                },
            )
        }

    fun clear() {
        rawMatrix = BitMatrix()
    }
}
