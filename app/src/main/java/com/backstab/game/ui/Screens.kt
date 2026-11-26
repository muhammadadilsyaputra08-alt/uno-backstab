package com.backstab.game.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.backstab.game.data.Feedback
import com.backstab.game.data.Prefs
import com.backstab.game.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val Faint = Color(0xFF8E97B0)

@Composable
private fun BackButton(onBack: () -> Unit) = IconCircleButton("ic_back", Modifier.atC(76, 60, 78, 78), onBack)

@Composable
private fun Title(text: String) = BText(text, 62, family = Poppins, modifier = Modifier.at(0, 20, 2400, 80), spacing = 2)

@Composable
private fun LogoText(size: Number, modifier: Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        BText("BACKSTAB", size, Color(0xFFE23B3B), family = Poppins, spacing = 3)
        BText("!", size, BS.Gold, family = Poppins)
    }
}

// ---------------------------------------------------------------- Splash

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val list = Assets.preloadList(ctx)
        withContext(Dispatchers.IO) {
            list.forEachIndexed { i, p ->
                Assets.load(ctx, p)
                if (i % 6 == 0 || i == list.lastIndex) progress = (i + 1f) / list.size
            }
        }
        progress = 1f
        kotlinx.coroutines.delay(350)
        onDone()
    }
    Stage {
        Box(Modifier.at(0, 0, 2400, 1080).background(Brush.radialGradient(listOf(Color(0x66B91C1C), Color.Transparent), center = Offset(1200f, 620f), radius = 1300f)))
        Icon("ic_stab", 320, Color.White, Modifier.atC(1200, 330, 320, 320))
        LogoText(150, Modifier.at(0, 470, 2400, 200))
        BText("TRUST NOBODY. PLAY DIRTY. WIN ANYWAY.", 36, BS.Gold, spacing = 7, modifier = Modifier.at(0, 690, 2400, 60), weight = FontWeight.SemiBold)
        Box(Modifier.atC(1200, 890, 700, 14).background(Color(0xFF2A1A20), RoundedCornerShape(percent = 50))) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(progress).background(Color(0xFFE23B3B), RoundedCornerShape(percent = 50)))
        }
        BText("Memuat aset...", 26, Faint, modifier = Modifier.at(0, 920, 2400, 40), weight = FontWeight.SemiBold)
        BText("v1.0.0", 24, Faint, modifier = Modifier.at(2180, 1020, 160, 36), align = TextAlign.End)
    }
}

// ------------------------------------------------------------------ Menu

@Composable
fun MenuScreen(onPlay: () -> Unit, onHow: () -> Unit, onSettings: () -> Unit, onExit: () -> Unit) {
    Stage {
        LogoText(130, Modifier.at(180, 110, 1280, 170))
        BText("TRUST NOBODY. PLAY DIRTY. WIN ANYWAY.", 28, BS.Gold, spacing = 5, modifier = Modifier.at(180, 285, 1280, 44), weight = FontWeight.SemiBold)
        BsButton("MAIN", BtnKind.PRIMARY, Modifier.atC(820, 455, 600, 118), icon = "ic_play", fontSize = 40, onClick = onPlay)
        BsButton("CARA BERMAIN", BtnKind.CONTROL, Modifier.atC(820, 600, 600, 118), icon = "ic_book", fontSize = 38, onClick = onHow)
        BsButton("PENGATURAN", BtnKind.GRAY, Modifier.atC(820, 745, 600, 118), icon = "ic_gear", fontSize = 38, onClick = onSettings)
        BsButton("KELUAR", BtnKind.GRAY, Modifier.atC(820, 890, 600, 118), icon = "ic_exit", fontSize = 38, onClick = onExit)
        IconCircleButton("ic_gear", Modifier.atC(2315, 65, 78, 78), onSettings)
        // kipas kartu dekoratif
        val deck = remember { Deck.build() }
        val fan = remember {
            listOf(
                deck.first { it.color == CardColor.CRIMSON && it.value == 9 }, deck.first { it.color == CardColor.AZURE && it.value == 4 },
                deck.first { it.type == CardType.IMMUNITY }, deck.first { it.type == CardType.CHAOS },
                deck.first { it.color == CardColor.EMERALD && it.value == 7 }, deck.first { it.type == CardType.WILD },
                deck.first { it.color == CardColor.AMBER && it.value == 5 },
            )
        }
        fan.forEachIndexed { i, c ->
            val t = i - 3f
            CardImage(c, Modifier.atC(1760f + t * 120f, 800f - (3 - kotlin.math.abs(t)) * 28f, 270, 410).graphicsLayer { rotationZ = t * 12f })
        }
        BText("v1.0.0", 24, Faint, modifier = Modifier.at(2180, 1020, 160, 36), align = TextAlign.End)
    }
}

// ------------------------------------------------------------ Mode select

@Composable
fun ModeSelectScreen(onBack: () -> Unit, onPick: (Mode) -> Unit) {
    BackHandler(onBack = onBack)
    Stage {
        BackButton(onBack)
        Title("PILIH MODE")
        ModeCard(Mode.FIRST_TO_ZERO, 170, onPick)
        ModeCard(Mode.LAST_STANDING, 1260, onPick)
    }
}

@Composable
private fun ModeCard(mode: Mode, x: Int, onPick: (Mode) -> Unit) {
    val ftz = mode == Mode.FIRST_TO_ZERO
    val head = if (ftz) BtnKind.PRIMARY else BtnKind.CONTROL
    Panel(Modifier.at(x, 190, 970, 760)) {
        Row(
            Modifier.at(0, 0, 970, 130).background(Brush.verticalGradient(listOf(head.top, head.bottom)), RoundedCornerShape(topStart = r(30), topEnd = r(30))).padding(horizontal = r(40)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(if (ftz) "ic_stab" else "ic_skull", 60)
            Spacer(Modifier.width(r(24)))
            BText(if (ftz) "FIRST TO ZERO" else "LAST STANDING", 52, family = Poppins)
        }
        BText(if (ftz) "Habiskan seluruh kartu di tanganmu" else "Jadilah pemain terakhir yang bertahan", 36, modifier = Modifier.at(0, 170, 970, 50))
        BText(
            if (ftz) "Umumkan STAB! saat sisa 1 kartu.\nMainkan kartu terakhir & umumkan BACKSTAB!" else "Tereliminasi saat memegang ${15} kartu.\nPemain terakhir tersisa menjadi pemenang.",
            28, Faint, weight = FontWeight.SemiBold, maxLines = 3, modifier = Modifier.at(60, 230, 850, 110),
        )
        if (ftz) {
            Pill("7 KARTU", Modifier.at(60, 390, 230, 70), size = 26)
            BText("›", 40, Faint, modifier = Modifier.at(290, 392, 50, 60))
            Pill("STAB! · 1", Modifier.at(340, 390, 250, 70), fill = BS.Gold, stroke = BS.GoldLight, color = Color(0xFF1A1206), size = 26, family = Poppins)
            BText("›", 40, Faint, modifier = Modifier.at(590, 392, 50, 60))
            Pill("BACKSTAB! · 0", Modifier.at(640, 390, 270, 70), fill = Color(0xFFB91C1C), stroke = Color(0xFFFF6B6B), size = 24, family = Poppins)
        } else {
            Box(Modifier.at(60, 400, 850, 36).background(Color(0xFF1B2547), RoundedCornerShape(percent = 50))) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(0.85f).background(Brush.horizontalGradient(listOf(Color(0xFF3F74F5), Color(0xFF1D4ED8))), RoundedCornerShape(percent = 50)))
            }
            BText("0", 24, Faint, modifier = Modifier.at(60, 446, 60, 36), align = TextAlign.Start)
            BText("15 = ELIMINASI", 24, BS.Gold, modifier = Modifier.at(610, 446, 300, 36), align = TextAlign.End)
        }
        Pill("2-6 pemain", Modifier.at(60, 560, 230, 62), stroke = Color(0xFFFF6B6B).takeIf { ftz } ?: Color(0xFF6B93FF), size = 24)
        Pill("15-30 menit", Modifier.at(310, 560, 250, 62), stroke = Color(0xFFFF6B6B).takeIf { ftz } ?: Color(0xFF6B93FF), size = 24)
        BsButton("PILIH", head, Modifier.at(640, 540, 270, 100), fontSize = 36) { onPick(mode) }
    }
}

// ----------------------------------------------------------------- Lobby

@Composable
fun LobbyScreen(mode: Mode, onBack: () -> Unit, onStart: (Int, Int) -> Unit) {
    BackHandler(onBack = onBack)
    var count by remember { mutableIntStateOf(Prefs.lastPlayers) }
    var threshold by remember { mutableIntStateOf(15) }
    val ftz = mode == Mode.FIRST_TO_ZERO
    Stage {
        BackButton(onBack)
        Title("SIAPKAN PERMAINAN")
        Panel(Modifier.at(119, 180, 880, 730)) {
            BText("MODE", 24, Faint, modifier = Modifier.at(50, 40, 300, 36), align = TextAlign.Start)
            Pill(if (ftz) "FIRST TO ZERO" else "LAST STANDING", Modifier.at(50, 84, 400, 70),
                fill = if (ftz) Color(0xFFB91C1C) else Color(0xFF1D4ED8), stroke = Color(0x55FFFFFF), size = 28, family = Poppins)
            BText("JUMLAH PEMAIN", 24, Faint, modifier = Modifier.at(50, 200, 400, 36), align = TextAlign.Start)
            (2..6).forEachIndexed { i, n ->
                val on = n == count
                Box(
                    Modifier.at(50 + i * 150, 246, 130, 120)
                        .background(if (on) Color(0xFFB91C1C) else Color(0xFF18213D), RoundedCornerShape(r(22)))
                        .border(r(3), if (on) Color(0xFFFF6B6B) else BS.SurfaceStroke, RoundedCornerShape(r(22)))
                        .clickable { Feedback.tap(); count = n; Prefs.setLastPlayers(n) },
                    contentAlignment = Alignment.Center,
                ) { BText("$n", 56, family = Poppins) }
            }
            BText("★ Disarankan 4–5 pemain · ±15–30 menit", 26, BS.Gold, weight = FontWeight.SemiBold, modifier = Modifier.at(50, 392, 800, 40), align = TextAlign.Start)
            BText("AMBANG ELIMINASI", 24, Faint, modifier = Modifier.at(50, 480, 400, 36), align = TextAlign.Start)
            Row(Modifier.at(50, 524, 460, 80), verticalAlignment = Alignment.CenterVertically) {
                Stepper("−", ftz.not() && threshold > 10) { threshold-- }
                Box(Modifier.width(r(200)).height(r(80)).background(Color(0xFF18213D), RoundedCornerShape(r(18))).border(r(2), BS.SurfaceStroke, RoundedCornerShape(r(18))), contentAlignment = Alignment.Center) {
                    BText("$threshold kartu", 32, if (ftz) Faint else Color.White)
                }
                Stepper("+", ftz.not() && threshold < 20) { threshold++ }
            }
            BText(if (ftz) "(hanya Last Standing)" else "(Last Standing)", 22, Faint, modifier = Modifier.at(530, 550, 330, 32), align = TextAlign.Start)
        }
        Panel(Modifier.at(1062, 180, 1220, 730)) {
            BText("PEMAIN", 24, Faint, modifier = Modifier.at(50, 30, 400, 36), align = TextAlign.Start)
            for (i in 0 until count) {
                val me = i == 0
                Row(
                    Modifier.at(40, 80 + i * 106, 1140, 92).background(Color(0xFF18213D), RoundedCornerShape(r(20))).border(r(2), BS.SurfaceStroke, RoundedCornerShape(r(20))).padding(horizontal = r(24)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(i, GameEngine.PLAYER_NAMES[i], 66)
                    Spacer(Modifier.width(r(24)))
                    BText(GameEngine.PLAYER_NAMES[i], 36, align = TextAlign.Start, modifier = Modifier.width(r(500)))
                    Spacer(Modifier.weight(1f))
                    Pill(if (me) "KAMU" else "BOT", Modifier.size(r(150), r(48)),
                        fill = if (me) BS.Gold else Color(0xFF2A3350), stroke = if (me) BS.GoldLight else Color(0xFF3F4A6E),
                        color = if (me) Color(0xFF1A1206) else Color(0xFFD0D6E6), size = 22, family = Poppins)
                }
            }
        }
        BsButton("MULAI", BtnKind.PRIMARY, Modifier.atC(1740, 990, 480, 105), icon = "ic_play", fontSize = 42) { onStart(count, threshold) }
    }
}

@Composable
private fun Stepper(label: String, enabled: Boolean, onClick: () -> Unit) {
    BsButton(label, BtnKind.CONTROL, Modifier.size(r(80), r(86)), enabled = enabled, fontSize = 40, onClick = onClick)
}

// -------------------------------------------------------------- How to play

@Composable
fun HowToScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val pager = rememberPagerState(pageCount = { 2 })
    val deck = remember { Deck.build() }
    fun num(c: CardColor, v: Int) = deck.first { it.color == c && it.value == v }
    fun act(t: CardType) = deck.first { it.type == t }
    Stage {
        BackButton(onBack)
        Title("CARA BERMAIN")
        HorizontalPager(pager, Modifier.at(0, 150, 2400, 800)) { page ->
            Box(Modifier.fillMaxSize()) {
                if (page == 0) {
                    HowCol(120, "1 COCOKKAN", BtnKind.CONTROL, listOf(num(CardColor.CRIMSON, 7), num(CardColor.CRIMSON, 3), num(CardColor.AZURE, 7)),
                        "WARNA · ANGKA", "Mainkan kartu dengan warna atau angka yang sama.\nTidak bisa? Ambil 1 kartu.")
                    HowCol(870, "2 KOMBINASI", BtnKind.PRIMARY, listOf(num(CardColor.CRIMSON, 7), num(CardColor.AZURE, 7), num(CardColor.EMERALD, 7)),
                        "TRIPLE 7 » +3", "PAIR +1 · TRIPLE +3 · QUAD +4.\nSTRAIGHT (3+ angka berurutan) membalik arah.")
                    HowCol(1620, "3 PLUS CHAIN", BtnKind.GOLD, listOf(num(CardColor.CRIMSON, 5), act(CardType.PLUS_TWO), num(CardColor.EMERALD, 9)),
                        "TOTAL +6 » pemain berikutnya", "Lanjutkan penalti, atau pakai IMMUNITY, DEFLECT, BOOMERANG.")
                } else {
                    HowCol(120, "4 PERTAHANAN", BtnKind.DEFENSE, listOf(act(CardType.IMMUNITY), act(CardType.DEFLECT), act(CardType.BOOMERANG)),
                        "HANYA SEBAGAI RESPONS", "IMMUNITY batalkan rantai. DEFLECT alihkan sekali. BOOMERANG kembalikan ke penyerang.")
                    HowCol(870, "5 AKSI KHUSUS", BtnKind.CONTROL, listOf(act(CardType.PICKPOCKET), act(CardType.HIJACK), act(CardType.MUTINY)),
                        "CURI · TUKAR · PUTAR", "PICKPOCKET curi 1, HIJACK curi 2 beri 1, EXCHANGE tukar tangan, MUTINY putar kartu.")
                    HowCol(1620, "6 MENANG", BtnKind.PRIMARY, listOf(act(CardType.WILD), act(CardType.MONOPOLY), act(CardType.CHAOS)),
                        "STAB! · BACKSTAB!", "First to Zero: umumkan STAB! di 1 kartu, BACKSTAB! di 0. Last Standing: hindari 15 kartu.")
                }
            }
        }
        Row(Modifier.atC(1200, 1010, 120, 24), horizontalArrangement = Arrangement.spacedBy(r(16))) {
            repeat(2) { Box(Modifier.size(r(20)).background(if (pager.currentPage == it) BS.Gold else Color(0xFF3A4152), CircleShape)) }
        }
    }
}

@Composable
private fun HowCol(x: Int, title: String, kind: BtnKind, cards: List<Card>, caption: String, text: String) {
    Panel(Modifier.at(x, 20, 660, 760)) {
        Box(Modifier.at(0, 0, 660, 100).background(Brush.verticalGradient(listOf(kind.top, kind.bottom)), RoundedCornerShape(topStart = r(30), topEnd = r(30))), contentAlignment = Alignment.Center) {
            BText(title, 42, kind.text, family = Poppins)
        }
        cards.forEachIndexed { i, c ->
            CardImage(c, Modifier.atC(330f + (i - 1) * 170f, 330f, 200, 303).graphicsLayer { rotationZ = (i - 1) * 9f })
        }
        BText(caption, 32, BS.Gold, modifier = Modifier.at(20, 520, 620, 50))
        BText(text, 26, Color.White, weight = FontWeight.SemiBold, maxLines = 4, modifier = Modifier.at(40, 585, 580, 150))
    }
}

// -------------------------------------------------------------- Settings

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Stage {
        BackButton(onBack)
        Title("PENGATURAN")
        Panel(Modifier.at(515, 180, 1370, 760)) {
            SettingRow(0, "ic_volume", "Efek Suara") { Toggle(Prefs.sound) { Prefs.setSound(it) } }
            SettingRow(1, "ic_music", "Musik") { Toggle(Prefs.music) { Prefs.setMusic(it) } }
            SettingRow(2, "ic_vibrate", "Getaran") { Toggle(Prefs.vibrate) { Prefs.setVibrate(it); if (it) Feedback.buzz(40) } }
            SettingRow(3, "ic_stab", "Kecepatan Animasi") {
                Slider(
                    value = Prefs.animSpeed, onValueChange = { Prefs.setAnimSpeed(it) }, valueRange = 0.5f..2f,
                    modifier = Modifier.width(r(420)),
                    colors = SliderDefaults.colors(thumbColor = BS.GoldLight, activeTrackColor = BS.Gold, inactiveTrackColor = Color(0xFF2A3350)),
                )
            }
            SettingRow(4, "ic_globe", "Bahasa") { Pill("INDONESIA", Modifier.size(r(220), r(52)), fill = Color(0xFF2A3350), size = 22) }
            SettingRow(5, "ic_info", "Tentang BACKSTAB!") { BText("v1.0.0", 26, Faint, weight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun SettingRow(i: Int, icon: String, label: String, trailing: @Composable () -> Unit) {
    // dipanggil di dalam Panel (koordinat panel)
    Row(
        Modifier.at(30, 30 + i * 118, 1310, 100).background(Color(0xFF18213D), RoundedCornerShape(r(20))).border(r(2), BS.SurfaceStroke, RoundedCornerShape(r(20))).padding(horizontal = r(30)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, 44, BS.Gold)
        Spacer(Modifier.width(r(24)))
        BText(label, 34, align = TextAlign.Start, weight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        trailing()
    }
}

@Composable
private fun Toggle(on: Boolean, onChange: (Boolean) -> Unit) {
    Box(
        Modifier.size(r(116), r(60)).background(if (on) Color(0xFF12A073) else Color(0xFF3A4152), RoundedCornerShape(percent = 50))
            .clickable { Feedback.tap(); onChange(!on) }.padding(r(6)),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) { Box(Modifier.size(r(48)).background(Color.White, CircleShape)) }
}

// --------------------------------------------------------------- Victory

@Composable
fun VictoryScreen(res: GameResult, onAgain: () -> Unit, onMenu: () -> Unit) {
    BackHandler(onBack = onMenu)
    val ftz = res.mode == Mode.FIRST_TO_ZERO
    val gold = BS.Gold
    Stage {
        Box(Modifier.at(0, 0, 2400, 1080).background(Brush.radialGradient(listOf(Color(0x55F59E0B), Color.Transparent), center = Offset(1000f, 420f), radius = 1000f)))
        Icon("ic_crown", 150, BS.GoldLight, Modifier.atC(1000, 175, 150, 150))
        Avatar(res.winnerId, res.winnerName, 180, Modifier.atC(1000, 390, 180, 180), glow = true)
        Row(Modifier.at(0, 480, 2000, 170), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            BText(if (ftz) "BACKSTAB!" else "BERTAHAN!", 130, gold, family = Poppins, spacing = 2)
        }
        BText(if (res.humanWon) "KAMU MENANG" else "${res.winnerName.uppercase()} MENANG", 56, family = Poppins, modifier = Modifier.at(0, 640, 2000, 80), spacing = 3)
        BText(if (ftz) "Kartu terakhir dimainkan · kemenangan tervalidasi" else "Pemain terakhir yang bertahan", 28, Faint, weight = FontWeight.SemiBold, modifier = Modifier.at(0, 722, 2000, 40))
        BsButton("MAIN LAGI", BtnKind.PRIMARY, Modifier.atC(756, 850, 400, 100), icon = "ic_play", fontSize = 34, onClick = onAgain)
        BsButton("MENU UTAMA", BtnKind.GRAY, Modifier.atC(1206, 850, 400, 100), fontSize = 34, onClick = onMenu)

        Panel(Modifier.at(1560, 130, 720, 800)) {
            BText("PERINGKAT", 36, family = Poppins, modifier = Modifier.at(0, 30, 720, 56), spacing = 2)
            res.standings.forEachIndexed { i, st ->
                val me = st.id == 0
                Row(
                    Modifier.at(30, 110 + i * 100, 660, 84).background(Color(0xFF18213D), RoundedCornerShape(r(18)))
                        .border(r(if (me) 3 else 2), if (me) gold else BS.SurfaceStroke, RoundedCornerShape(r(18))).padding(horizontal = r(20)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BText("#${i + 1}", 26, gold, family = Poppins, modifier = Modifier.width(r(70)), align = TextAlign.Start)
                    Avatar(st.id, st.name, 52)
                    Spacer(Modifier.width(r(18)))
                    BText(st.name, 30, align = TextAlign.Start, modifier = Modifier.width(r(190)))
                    Spacer(Modifier.weight(1f))
                    BText(if (st.eliminated) "TEREL." else "${st.count} kartu", 24, if (st.eliminated) Color(0xFFFF6B6B) else Faint, weight = FontWeight.SemiBold)
                }
            }
            val mm = res.durationSec / 60; val ss = res.durationSec % 60
            BText("Durasi %d:%02d · Chain terpanjang +%d".format(mm, ss, res.longestChain), 24, Faint, weight = FontWeight.SemiBold, modifier = Modifier.at(0, 730, 720, 40))
        }
    }
}
