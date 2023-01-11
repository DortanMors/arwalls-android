package com.google.ar.core.codelab.ui.model

import android.graphics.Path

class MapState(
    val path: Path = Path().apply {
        moveTo(0f, 0f)
    },
)
