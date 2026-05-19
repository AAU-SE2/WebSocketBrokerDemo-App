package at.aau.serg.websocketbrokerdemo

import android.content.Context
import java.util.UUID

object UserPreferences {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_PLAYER_ID = "player_id"

    fun getOrCreatePlayerId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existingId = prefs.getString(KEY_PLAYER_ID, null)

        if (existingId != null) return existingId

        // Erste Mal → neue ID generieren und speichern
        val newId = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_PLAYER_ID, newId).apply()
        return newId
    }
}