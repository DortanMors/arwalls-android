package ru.ssau.arwalls.domain

import ru.ssau.arwalls.data.CurrentBeaconStore

object UpdateCurrentBeacon {
    operator fun invoke(name: String?) {
        CurrentBeaconStore.updateCurrentBeacon(name)
    }
}
