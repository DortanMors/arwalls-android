package ru.ssau.arwalls.data

typealias Coordinate = Int

class BitMatrix(val rows: Int = 5000, val columns: Int = 5000) {

    private var _filledPoints = Array(rows) {
        BooleanArray(columns)
    }

    val centerOffsetX: Coordinate = columns / 2
    val centerOffsetY: Coordinate = rows / 2


    fun getFilledPoints(): List<Pair<Coordinate, Coordinate>> =
        _filledPoints.flatMapIndexed { y, row ->
            row.mapIndexed { x, point ->
                (x to y).takeIf { point }
            }
        }.filterNotNull()

    fun set(x: Coordinate, y: Coordinate) {
        val centeredX = x + centerOffsetX
        val centeredY = y + centerOffsetY
        if (centeredX < columns && centeredY < rows) {
            _filledPoints[centeredY][centeredX] = true
        }
    }

    fun remove(x: Coordinate, y: Coordinate) {
        val centeredX = x + centerOffsetX
        val centeredY = y + centerOffsetY
        if (centeredX < columns && centeredY < rows) {
            _filledPoints[centeredY][centeredX] = false
        }
    }

    fun clear() {
        _filledPoints = Array(rows) {
            BooleanArray(columns)
        }
    }
}
