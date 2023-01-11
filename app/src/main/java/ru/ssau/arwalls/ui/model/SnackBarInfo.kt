package ru.ssau.arwalls.ui.model

sealed class SnackBarInfo(val message: String) {
    class SnackWithMessage(message: String) : SnackBarInfo(message)
    sealed class DismissibleSnack(message: String) : SnackBarInfo(message)
    class WarnSnack(message: String) : DismissibleSnack(message)
    class ErrorSnack(message: String) : DismissibleSnack(message)
    object Hidden : DismissibleSnack("")
}
