package ru.ssau.arwalls.common

import android.graphics.Canvas
import android.graphics.Paint
import ru.ssau.arwalls.data.MapPoint

val Any.tag: String
    get() = this::class.java.simpleName

fun Canvas.drawMarker(center: MapPoint, size: Float, paint: Paint) {
    drawLine(center.x - size, center.y, center.x + size, center.y, paint)
    drawLine(center.x, center.y - size, center.x, center.y + size, paint)
}
