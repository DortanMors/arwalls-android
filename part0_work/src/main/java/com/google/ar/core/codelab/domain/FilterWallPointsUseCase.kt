package com.google.ar.core.codelab.domain

import android.util.Log
import com.google.ar.core.codelab.common.tag
import com.google.ar.core.codelab.data.MapPoint
import com.google.ar.core.codelab.data.MapStore
import com.google.ar.core.codelab.rawdepth.FloatsPerPoint
import com.google.ar.core.codelab.ui.model.UpdateMapState
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
