package com.google.ar.core.codelab.data

import android.graphics.Path
import android.util.Log
import com.google.ar.core.codelab.common.Settings
import com.google.ar.core.codelab.common.tag
import com.google.ar.core.codelab.ui.model.MapState
import com.google.ar.core.codelab.ui.model.UpdateMapState
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
}
