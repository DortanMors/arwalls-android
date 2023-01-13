/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.ssau.arwalls.common.helpers

import android.app.Activity
import android.util.Log
import androidx.annotation.StringRes
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import ru.ssau.arwalls.common.Settings
import ru.ssau.arwalls.common.tag
import ru.ssau.arwalls.ui.model.SnackBarInfo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.ssau.arwalls.rawdepth.R

/**
 * Helper to manage the sample snackbar. Hides the Android boilerplate code, and exposes simpler
 * methods.
 */
object SnackBarUseCase {

    private val mutableSnackBarFlow: MutableStateFlow<SnackBarInfo> = MutableStateFlow(SnackBarInfo.Hidden)
    val snackBarFlow: Flow<SnackBarInfo> = mutableSnackBarFlow

    private var lastSnackBar: Snackbar? = null

    private val coroutineScope = CoroutineScope(
        Dispatchers.Default +
            Job() +
            CoroutineExceptionHandler { _, throwable -> Log.e(tag, throwable.toString()) }
    )

    private var lastMessageId = -1

    /** Shows a snackbar with a given message.  */
    fun showMessage(@StringRes messageId: Int) {
        if (lastMessageId != messageId) {
            lastMessageId = messageId
            coroutineScope.launch {
                mutableSnackBarFlow.emit(SnackBarInfo.SnackWithMessage(messageId))
            }
        }
    }

    /** Shows a snackbar with a given message, and a dismiss button.  */
    fun showMessageWithDismiss(@StringRes messageId: Int) {
        coroutineScope.launch {
            mutableSnackBarFlow.emit(SnackBarInfo.WarnSnack(messageId))
        }
    }

    /**
     * Shows a snackbar with a given error message. When dismissed, will finish the activity. Useful
     * for notifying errors, where no further interaction with the activity is possible.
     */
    fun showError(@StringRes errorMessageId: Int) {
        coroutineScope.launch {
            mutableSnackBarFlow.emit(SnackBarInfo.ErrorSnack(errorMessageId))
        }
    }

    /**
     * Hides the currently showing snackbar, if there is one. Safe to call from any thread. Safe to
     * call even if snackbar is not shown.
     */
    fun hide(@StringRes messageId: Collection<Int>? = null) {
        coroutineScope.launch {
            if (messageId?.contains(mutableSnackBarFlow.value.messageId) != false) {
                lastMessageId = -1
                mutableSnackBarFlow.emit(SnackBarInfo.Hidden)
            }
        }
    }

    fun Activity.showSnackBar(
        snackBarInfo: SnackBarInfo,
    ) {
        lastSnackBar?.dismiss()
        lastSnackBar = if (snackBarInfo is SnackBarInfo.Hidden) {
            null
        } else {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(snackBarInfo.messageId),
                Snackbar.LENGTH_INDEFINITE
            ).apply {
                view.setBackgroundColor(Settings.snackBackgroundColor)
                if (snackBarInfo is SnackBarInfo.DismissibleSnack) {
                    setAction(getString(R.string.dismiss)) {
                        coroutineScope.launch {
                            mutableSnackBarFlow.emit(SnackBarInfo.Hidden)
                        }
                    }
                    if (snackBarInfo is SnackBarInfo.ErrorSnack) {
                        addCallback(
                            object : BaseTransientBottomBar.BaseCallback<Snackbar?>() {
                                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                    super.onDismissed(transientBottomBar, event)
                                    finish()
                                }
                            }
                        )
                    }
                }
                show()
            }
        }
    }
}
