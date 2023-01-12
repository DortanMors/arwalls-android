package ru.ssau.arwalls.common

import android.graphics.Color
import android.graphics.Paint

object Settings {
    var strokeWidth = 5f
    var markerSize = 20f
    var paintColor = Color.RED
    var markerColor = Color.GREEN
    var paintStyle = Paint.Style.FILL
    var minConfidence = 0.45
    var snackBackgroundColor = -0x40cdcdce
    var mapScale = 100
    var mapOffsetX = 0f
    var mapOffsetY = 0f
    var scanVerticalRadius = 0.15
    val maxNumberOfPointsToRender = 20000f
}
