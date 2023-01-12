package ru.ssau.arwalls.data

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.ssau.arwalls.common.tag
import ru.ssau.arwalls.ui.model.MapState

object MapStore {
    private val mutableMapPointsState = MutableStateFlow(MapState())
    val newMapPointsState: Flow<MapState> = mutableMapPointsState

    private val coroutineScope = CoroutineScope(
        Dispatchers.Default +
            Job() +
            CoroutineExceptionHandler { _, throwable -> Log.e(tag, throwable.toString()) }
    )

    fun updateMapState(mapState: MapState) {
        coroutineScope.launch {
            mutableMapPointsState.emit(mapState)
        }
    }
}
