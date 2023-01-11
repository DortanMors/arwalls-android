package ru.ssau.arwalls.domain

import android.util.Log
import ru.ssau.arwalls.common.tag
import ru.ssau.arwalls.data.MapPoint
import ru.ssau.arwalls.data.MapStore
import ru.ssau.arwalls.rawdepth.FloatsPerPoint
import ru.ssau.arwalls.ui.model.UpdateMapState
import java.nio.FloatBuffer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object FilterWallPointsUseCase {

    private val coroutineScope = CoroutineScope(
        Dispatchers.Default +
            Job() +
            CoroutineExceptionHandler { _, throwable -> Log.e(tag, throwable.toString()) }
    )

    operator fun invoke(points: FloatBuffer) {
        coroutineScope.launch {
            val pointsArray = points.array()
            val mapPoints = mutableListOf<MapPoint>()
            for (i in pointsArray.indices step FloatsPerPoint) {
                mapPoints.add(
                    MapPoint(
                        pointsArray[i],     // X
                        pointsArray[i + 2], // Z
                    ),
                )
            }
            MapStore.updateMapState(
                UpdateMapState(
                    points = mapPoints,
                ),
            )
        }
    }
}
