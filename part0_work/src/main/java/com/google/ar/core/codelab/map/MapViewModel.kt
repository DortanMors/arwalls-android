package com.google.ar.core.codelab.map

import androidx.lifecycle.ViewModel
import com.google.ar.core.codelab.data.MapStore
import com.google.ar.core.codelab.ui.model.MapState
import com.google.ar.core.codelab.ui.model.UpdateMapState
import kotlinx.coroutines.flow.Flow

class MapViewModel: ViewModel() {
    val newMapPointsState: Flow<MapState> =
        MapStore.newMapPointsState

    fun updateMapState(updateMapState: UpdateMapState) =
        MapStore.updateMapState(updateMapState)
}
