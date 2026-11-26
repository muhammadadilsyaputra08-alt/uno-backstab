package com.backstab.game.data

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Umpan balik haptik & efek suara sederhana (tanpa aset audio eksternal). */
object Feedback {
    private var vibrator: Vibrator? = null
    private var tone: ToneGenerator? = null

    fun init(ctx: Context) {
        vibrator = if (Build.VERSION.SDK_INT >= 31) {
            (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION") ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        tone = try { ToneGenerator(AudioManager.STREAM_MUSIC, 40) } catch (e: RuntimeException) { null }
    }

    fun tap() = sfx(ToneGenerator.TONE_PROP_BEEP, 40)
    fun play() = sfx(ToneGenerator.TONE_PROP_ACK, 60)
    fun alert() { sfx(ToneGenerator.TONE_PROP_NACK, 120); buzz(60) }
    fun win() { sfx(ToneGenerator.TONE_CDMA_CONFIRM, 300); buzz(120) }

    private fun sfx(t: Int, ms: Int) {
        if (!Prefs.sound) return
        try { tone?.startTone(t, ms) } catch (e: RuntimeException) { /* abaikan */ }
    }

    fun buzz(ms: Long) {
        if (!Prefs.vibrate) return
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        else @Suppress("DEPRECATION") v.vibrate(ms)
    }
}
