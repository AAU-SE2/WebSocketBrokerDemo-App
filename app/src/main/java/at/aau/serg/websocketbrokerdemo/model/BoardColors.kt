package at.aau.serg.websocketbrokerdemo.model

import androidx.core.graphics.toColorInt

object BoardColors {
    val CHARACTER_COLORS: Map<String, Int> by lazy {
        mapOf(
            "MRS_LAVENDER" to "#9B59B6".toColorInt(),
            "MRS_PINK" to "#E91E63".toColorInt(),
            "DR_RED" to "#E74C3C".toColorInt(),
            "DR_BLUE" to "#3498DB".toColorInt()
        )
    }
}