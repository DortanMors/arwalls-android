package ru.ssau.arwalls.ui.model

import androidx.annotation.StringRes
import ru.ssau.arwalls.rawdepth.R

sealed class SnackBarInfo(@StringRes val messageId: Int) {
    class SnackWithMessage(@StringRes messageId: Int) : SnackBarInfo(messageId)
    sealed class DismissibleSnack(@StringRes messageId: Int) : SnackBarInfo(messageId)
    class WarnSnack(@StringRes messageId: Int) : DismissibleSnack(messageId)
    class ErrorSnack(@StringRes messageId: Int) : DismissibleSnack(messageId)
    object Hidden : DismissibleSnack(R.string.empty)
}
