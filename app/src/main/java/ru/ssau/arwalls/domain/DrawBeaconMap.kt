package ru.ssau.arwalls.domain

import android.graphics.Path
import ru.ssau.arwalls.common.Settings
import ru.ssau.arwalls.data.Beacon

object DrawBeaconMap {
    operator fun invoke(beacons: List<Beacon>): Path =
        beacons.sortedBy { it.id }.let { sortedBeacons ->
            val path = Path()
            sortedBeacons.firstOrNull()?.run {
                path.moveTo(point.x * Settings.mapScale + Settings.mapOffsetX, point.y * Settings.mapScale + Settings.mapOffsetY)
            }
            sortedBeacons.forEach {
                path.lineTo(it.point.x, it.point.y)
            }
            path
        }
}
