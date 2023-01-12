package ru.ssau.arwalls.data

import android.graphics.Path
import android.util.Log
import ru.ssau.arwalls.common.Settings
import ru.ssau.arwalls.common.tag
import ru.ssau.arwalls.rawdepth.FloatsPerPoint
import ru.ssau.arwalls.ui.model.MapState
import ru.ssau.arwalls.ui.model.UpdateMapState
import java.nio.FloatBuffer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object MapStore {
    private val mutableMapPointsState = MutableStateFlow(MapState())
    val newMapPointsState: Flow<MapState> = mutableMapPointsState

    private val coroutineScope = CoroutineScope(
        Dispatchers.Default +
            Job() +
            CoroutineExceptionHandler { _, throwable -> Log.e(tag, throwable.toString()) }
    )

    fun updateMapState(updateMapState: UpdateMapState) {
        coroutineScope.launch {
            Log.d("HARDCODE", "emit")
//            mutableMapPointsState.emit(
//                MapState(
//                    Path().apply {
//                        updateMapState.points.forEach { point ->
//                            addCircle(
//                                point.x,
//                                point.y,
//                                Settings.mapPointRadius,
//                                Path.Direction.CW,
//                            )
//                        }
//                    }
//                )
//            )
        }
    }

    fun updateMapState(points: FloatBuffer) {
        coroutineScope.launch {
            val pointsArray = points.array()
            val updatedPath = Path()
            for (i in pointsArray.indices step FloatsPerPoint) {
                updatedPath.addCircle(
                    pointsArray[i] * Settings.mapScale + Settings.mapOffset,     // X
                    pointsArray[i + 2] * Settings.mapScale + Settings.mapOffset, // Z
                    Settings.mapPointRadius,
                    Path.Direction.CW,
                )
            }
            mutableMapPointsState.emit(
                MapState(
                    path = updatedPath,
                )
            )
        }
    }
}
