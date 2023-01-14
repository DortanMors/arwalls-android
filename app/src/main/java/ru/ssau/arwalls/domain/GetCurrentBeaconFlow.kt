package ru.ssau.arwalls.domain

import kotlinx.coroutines.flow.Flow
import ru.ssau.arwalls.data.CurrentBeaconStore

object GetCurrentBeaconFlow {
    operator fun invoke(): Flow<String?> =
        CurrentBeaconStore.currentBeaconState
}
