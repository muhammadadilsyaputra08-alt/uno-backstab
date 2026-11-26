package com.backstab.game.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Pengaturan pemain (disimpan lokal). Dibaca langsung oleh UI lewat state Compose. */
object Prefs {
    private lateinit var sp: SharedPreferences
    var sound by mutableStateOf(true); private set
    var music by mutableStateOf(true); private set
    var vibrate by mutableStateOf(true); private set
    var animSpeed by mutableFloatStateOf(1f); private set
    var lastPlayers by mutableStateOf(5); private set

    fun init(ctx: Context) {
        sp = ctx.getSharedPreferences("backstab", Context.MODE_PRIVATE)
        sound = sp.getBoolean("sound", true)
        music = sp.getBoolean("music", true)
        vibrate = sp.getBoolean("vibrate", true)
        animSpeed = sp.getFloat("animSpeed", 1f).coerceIn(0.5f, 2f)
        lastPlayers = sp.getInt("players", 5).coerceIn(2, 6)
    }

    fun setSound(v: Boolean) { sound = v; sp.edit().putBoolean("sound", v).apply() }
    fun setMusic(v: Boolean) { music = v; sp.edit().putBoolean("music", v).apply() }
    fun setVibrate(v: Boolean) { vibrate = v; sp.edit().putBoolean("vibrate", v).apply() }
    fun setAnimSpeed(v: Float) { animSpeed = v.coerceIn(0.5f, 2f); sp.edit().putFloat("animSpeed", animSpeed).apply() }
    fun setLastPlayers(v: Int) { lastPlayers = v; sp.edit().putInt("players", v).apply() }
}
