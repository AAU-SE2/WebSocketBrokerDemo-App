package at.aau.serg.websocketbrokerdemo.model

import androidx.core.graphics.toColorInt
import kotlin.math.abs

object BoardConfig {
    const val COLS = 13
    const val ROWS = 9


    val DOOR_CELLS: Map<Pair<Int, Int>, String> = mapOf(
        Pair(0, 0) to "KITCHEN",
        Pair(12, 0) to "BALLROOM",
        Pair(0, 4) to "LOUNGE",
        Pair(12, 4) to "LIBRARY",
        Pair(0, 8) to "STUDY",
        Pair(12, 8) to "BILLIARDROOM"
    )

    val ROOM_CENTERS_PERCENT: Map<String, Pair<Float, Float>> = mapOf(
        "KITCHEN" to Pair(0.12f, 0.15f),
        "BALLROOM" to Pair(0.85f, 0.15f),
        "LOUNGE" to Pair(0.12f, 0.50f),
        "LIBRARY" to Pair(0.85f, 0.50f),
        "STUDY" to Pair(0.12f, 0.85f),
        "BILLIARDROOM" to Pair(0.85f, 0.85f)
    )

    val HIDDEN_PASSAGES: Map<String, String> = mapOf(
        "BALLROOM" to "STUDY",
        "STUDY" to "BALLROOM",
        "BILLIARDROOM" to "KITCHEN",
        "KITCHEN" to "BILLIARDROOM"
    )

    val CHARACTER_START_POSITIONS: Map<String, Pair<Int, Int>> = mapOf(
        "MRS_LAVENDER" to Pair(5, 3),
        "MRS_PINK" to Pair(7, 3),
        "DR_RED" to Pair(5, 5),
        "DR_BLUE" to Pair(7, 5)
    )

    val CHARACTER_COLORS: Map<String, Int> = mapOf(
        "MRS_LAVENDER" to "#9B59B6".toColorInt(), // purple
        "MRS_PINK" to "#E91E63".toColorInt(),      // pink
        "DR_RED" to "#E74C3C".toColorInt(),         // red
        "DR_BLUE" to "#3498DB".toColorInt()         // blue
    )

    val ALL_CHARACTERS = listOf("DR_RED", "DR_BLUE", "MRS_PINK", "MRS_LAVENDER")

    val ALL_WEAPONS = listOf("SYRINGE", "KNIFE", "SHOTGUN", "MEAT_CLEAVER", "AX")

    val ALL_ROOMS = listOf("LIBRARY", "BALLROOM", "BILLIARDROOM", "KITCHEN", "LOUNGE", "STUDY")

    val CHECKLIST_SUSPECTS = listOf("DR_RED", "DR_BLUE", "MRS_PINK", "MRS_LAVENDER")
    val CHECKLIST_WEAPONS = listOf("SYRINGE", "KNIFE", "SHOTGUN", "MEAT_CLEAVER", "AX")
    val CHECKLIST_ROOMS = listOf("LIBRARY", "BALLROOM", "BILLIARDROOM", "KITCHEN", "LOUNGE", "STUDY")

    fun isWalkable(col: Int, row: Int): Boolean {
        return col in 0 until COLS && row in 0 until ROWS
    }

    fun isAdjacent(col1: Int, row1: Int, col2: Int, row2: Int): Boolean {
        val dc = abs(col1 - col2)
        val dr = abs(row1 - row2)
        return (dc + dr) == 1
    }

    fun getRoomAtDoor(col: Int, row: Int): String? {
        return DOOR_CELLS[Pair(col, row)]
    }
}
