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
package com.google.ar.core.codelab.common.helpers

import android.app.Activity
import android.util.Log
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.codelab.common.Settings
import com.google.ar.core.codelab.common.tag
import com.google.ar.core.codelab.ui.model.SnackBarInfo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

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

    private var lastMessage = ""

    /** Shows a snackbar with a given message.  */
    fun showMessage(message: String) {
        if (lastMessage != message) {
            lastMessage = message
            coroutineScope.launch {
                mutableSnackBarFlow.emit(SnackBarInfo.SnackWithMessage(message))
            }
        }
    }

    /** Shows a snackbar with a given message, and a dismiss button.  */
    fun showMessageWithDismiss(message: String) {
        coroutineScope.launch {
            mutableSnackBarFlow.emit(SnackBarInfo.WarnSnack(message))
        }
    }

    /**
     * Shows a snackbar with a given error message. When dismissed, will finish the activity. Useful
     * for notifying errors, where no further interaction with the activity is possible.
     */
    fun showError(errorMessage: String) {
        coroutineScope.launch {
            mutableSnackBarFlow.emit(SnackBarInfo.ErrorSnack(errorMessage))
        }
    }

    /**
     * Hides the currently showing snackbar, if there is one. Safe to call from any thread. Safe to
     * call even if snackbar is not shown.
     */
    fun hide() {
        lastMessage = ""
        coroutineScope.launch {
            mutableSnackBarFlow.emit(SnackBarInfo.Hidden)
        }
    }

    fun Activity.showSnackBar(
        snackBarInfo: SnackBarInfo,
    ) {
        if (snackBarInfo is SnackBarInfo.Hidden) {
            lastSnackBar?.dismiss()
            lastSnackBar = null
        } else {
            Snackbar.make(
                findViewById(android.R.id.content),
                snackBarInfo.message,
                Snackbar.LENGTH_INDEFINITE
            ).apply {
                view.setBackgroundColor(Settings.snackBackgroundColor)
                if (snackBarInfo is SnackBarInfo.DismissibleSnack) {
                    setAction("Dismiss") {
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
            }.show()
        }
    }
}
