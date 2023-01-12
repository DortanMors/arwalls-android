package ru.ssau.arwalls.ui.model

import java.nio.FloatBuffer
import ru.ssau.arwalls.data.MapPoint

class MapState(
    val points: FloatBuffer = FloatBuffer.allocate(0),
    val cameraPosition: MapPoint = MapPoint(0f, 0f),
)
