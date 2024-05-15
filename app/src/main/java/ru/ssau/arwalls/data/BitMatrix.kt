package ru.ssau.arwalls.data

import java.util.LinkedList

typealias Coordinate = Int

class BitMatrix {
    private val _filledPoints: MutableList<Pair<Coordinate, Coordinate>> = LinkedList()
    val filledPoints: List<Pair<Coordinate, Coordinate>>
        get() = _filledPoints

    fun set(x: Coordinate, y: Coordinate) {
        _filledPoints += x to y
    }

    fun remove(x: Coordinate, y: Coordinate) {
        _filledPoints.removeAll { (xPoint, yPoint) -> x == xPoint && y == yPoint }
    }
}
