package ru.ssau.arwalls.map

import androidx.lifecycle.ViewModel
import ru.ssau.arwalls.data.MapStore
import ru.ssau.arwalls.ui.model.MapState
import kotlinx.coroutines.flow.Flow

class MapViewModel: ViewModel() {
    val newMapPointsState: Flow<MapState> =
        MapStore.newMapPointsState
}
