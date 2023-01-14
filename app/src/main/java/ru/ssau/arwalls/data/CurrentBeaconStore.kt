package ru.ssau.arwalls.data

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.ssau.arwalls.common.tag

object CurrentBeaconStore {
    private val mutableCurrentBeaconState: MutableStateFlow<String?> = MutableStateFlow(null)
    val currentBeaconState: Flow<String?> = mutableCurrentBeaconState

    private val coroutineScope = CoroutineScope(
        Dispatchers.Default +
            Job() +
            CoroutineExceptionHandler { _, throwable -> Log.e(tag, throwable.toString()) }
    )

    fun updateCurrentBeacon(name: String?) {
        coroutineScope.launch {
            mutableCurrentBeaconState.emit(name)
        }
    }
}
