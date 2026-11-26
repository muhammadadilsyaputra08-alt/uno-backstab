package com.backstab.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.backstab.game.data.Feedback
import com.backstab.game.data.Prefs
import com.backstab.game.engine.Mode
import com.backstab.game.ui.*

/** Peta navigasi layar — lihat harness/app_map.json (diverifikasi oleh harness/scripts/appmap_check.py). */
enum class Screen { SPLASH, MENU, MODE_SELECT, LOBBY, HOW_TO, SETTINGS, GAME, VICTORY }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(applicationContext)
        Feedback.init(applicationContext)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()
        setContent { BackstabApp(onExit = { finish() }) }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        val c = WindowInsetsControllerCompat(window, window.decorView)
        c.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        c.hide(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
fun BackstabApp(onExit: () -> Unit) {
    val vm: GameViewModel = viewModel()
    var screen by rememberSaveable { mutableStateOf(Screen.SPLASH) }
    var mode by rememberSaveable { mutableStateOf(Mode.FIRST_TO_ZERO) }

    // Pindah otomatis ke layar kemenangan saat game selesai
    val result = vm.result
    LaunchedEffect(result) { if (result != null && screen == Screen.GAME) screen = Screen.VICTORY }
    // Proses terbunuh/dipulihkan tanpa game aktif → kembali ke menu
    LaunchedEffect(screen) { if ((screen == Screen.GAME || screen == Screen.VICTORY) && !vm.active) screen = Screen.MENU }

    Box(Modifier.fillMaxSize().background(BS.Black)) {
        when (screen) {
            Screen.SPLASH -> SplashScreen { screen = Screen.MENU }
            Screen.MENU -> MenuScreen(
                onPlay = { screen = Screen.MODE_SELECT }, onHow = { screen = Screen.HOW_TO },
                onSettings = { screen = Screen.SETTINGS }, onExit = onExit,
            )
            Screen.MODE_SELECT -> ModeSelectScreen(onBack = { screen = Screen.MENU }) { mode = it; screen = Screen.LOBBY }
            Screen.LOBBY -> LobbyScreen(mode, onBack = { screen = Screen.MODE_SELECT }) { count, threshold ->
                vm.start(mode, count, threshold); screen = Screen.GAME
            }
            Screen.HOW_TO -> HowToScreen { screen = Screen.MENU }
            Screen.SETTINGS -> SettingsScreen { screen = Screen.MENU }
            Screen.GAME -> GameScreen(vm, onMenu = { screen = Screen.MENU })
            Screen.VICTORY -> {
                val res = vm.result
                if (res != null) VictoryScreen(
                    res,
                    onAgain = { val n = vm.ui?.players?.size ?: 5; val t = vm.ui?.threshold ?: 15; vm.start(mode, n, t); screen = Screen.GAME },
                    onMenu = { vm.clear(); screen = Screen.MENU },
                )
            }
        }
    }
}
