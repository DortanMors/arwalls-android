package ru.ssau.arwalls.ui.model

import android.graphics.Path
import ru.ssau.arwalls.data.MapPoint

class MapState(
    val path: Path = Path(),
    val cameraPosition: MapPoint = MapPoint(0f, 0f),
)
