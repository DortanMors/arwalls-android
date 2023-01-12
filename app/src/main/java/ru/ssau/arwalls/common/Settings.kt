package ru.ssau.arwalls.common

import android.graphics.Color
import android.graphics.Paint

object Settings {
    var mapPointRadius = 10f
    var strokeWidth = 2f
    var paintColor = Color.RED
    var paintStyle = Paint.Style.FILL
    var minConfidence = 0.4
    var snackBackgroundColor = -0x40cdcdce
    var mapScale = 100
    var mapOffset = 150f
    var scanVerticalRadius = 0.3
    val maxNumberOfPointsToRender = 20000f
}
