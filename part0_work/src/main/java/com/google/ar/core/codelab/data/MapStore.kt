package com.google.ar.core.codelab.data

import android.graphics.Path
import android.util.Log
import com.google.ar.core.codelab.common.Settings
import com.google.ar.core.codelab.common.tag
import com.google.ar.core.codelab.rawdepth.FloatsPerPoint
import com.google.ar.core.codelab.ui.model.MapState
import com.google.ar.core.codelab.ui.model.UpdateMapState
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
            mutableMapPointsState.emit(
                mutableMapPointsState.value.apply {
                    updateMapState.points.forEach { point ->
                        path.addCircle(
                            point.x,
                            point.y,
                            Settings.mapPointRadius,
                            Path.Direction.CW,
                        )
                    }
                }
            )
        }
    }

    fun updateMapState(points: FloatBuffer) {
        coroutineScope.launch {
            val pointsArray = points.array()
            val updatedPath = mutableMapPointsState.value.path
            for (i in pointsArray.indices step FloatsPerPoint) {
                updatedPath.addCircle(
                    pointsArray[i] * Settings.mapScale,     // X
                    pointsArray[i + 2] * Settings.mapScale, // Z
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
