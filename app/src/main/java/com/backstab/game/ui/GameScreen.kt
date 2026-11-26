package com.backstab.game.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.backstab.game.data.Prefs
import com.backstab.game.engine.*

enum class Seat { LEFT, TOP_LEFT, TOP_CENTER, TOP_RIGHT, RIGHT }

/** Susunan kursi lawan searah jarum jam dari pemain bawah (sesuai gambar referensi untuk 5 pemain). */
fun seatsFor(playerCount: Int): List<Seat> = when (playerCount) {
    2 -> listOf(Seat.TOP_CENTER)
    3 -> listOf(Seat.TOP_LEFT, Seat.TOP_RIGHT)
    4 -> listOf(Seat.LEFT, Seat.TOP_CENTER, Seat.RIGHT)
    5 -> listOf(Seat.LEFT, Seat.TOP_LEFT, Seat.TOP_RIGHT, Seat.RIGHT)
    else -> listOf(Seat.LEFT, Seat.TOP_LEFT, Seat.TOP_CENTER, Seat.TOP_RIGHT, Seat.RIGHT)
}

@Composable
fun GameScreen(vm: GameViewModel, onMenu: () -> Unit) {
    val ui = vm.ui ?: return
    BackHandler { vm.setPaused(true) }
    Stage {
        TableCanvas()
        Hud(ui, vm)
        val seats = seatsFor(ui.players.size)
        ui.players.filter { it.id != 0 }.forEachIndexed { i, p -> Opponent(p, seats[i], ui, vm.thinking == p.id) }
        CenterPiles(ui, vm)
        PreviewBanner(vm)
        ToastBanner(vm)
        PlayerPanel(ui, vm)
        HandFan(ui, vm.selected, vm.holdIds) { vm.toggle(it) }
        FlightLayer(vm.flights, ui.players.size)
        Dialogs(ui, vm, onMenu)
    }
}

// ----------------------------------------------------------------- table

@Composable
private fun TableCanvas() {
    Canvas(Modifier.at(0, 0, 2400, 1080)) {
        val s = size.width / 2400f
        val path = Path().apply {
            moveTo(450 * s, 110 * s); lineTo(1955 * s, 110 * s); lineTo(2285 * s, 795 * s); lineTo(115 * s, 795 * s); close()
        }
        // bayangan meja
        drawPath(path, Color(0x66000000), style = Stroke(width = 70 * s, join = StrokeJoin.Round))
        // bingkai kayu
        drawPath(path, Brush.verticalGradient(listOf(Color(0xFF8A5A30), Color(0xFF3E2512))), style = Stroke(width = 46 * s, join = StrokeJoin.Round, cap = StrokeCap.Round))
        drawPath(path, Color(0xFF9A6A3C), style = Stroke(width = 5 * s, join = StrokeJoin.Round))
        // kain felt
        drawPath(path, Brush.radialGradient(listOf(Color(0xFF1B7A63), Color(0xFF0F5A47), Color(0xFF082F27)), center = Offset(1200 * s, 450 * s), radius = 1250 * s))
        // cincin tengah
        drawOval(Color(0x1AFFFFFF), Offset(870 * s, 320 * s), Size(660 * s, 300 * s), style = Stroke(2.5f * s))
    }
    val dagger = rememberAsset("icons/ic_stab.png")
    if (dagger != null) {
        Image(dagger, null, Modifier.atC(1200, 470, 520, 520).alpha(0.07f), colorFilter = ColorFilter.tint(Color.White))
    }
}

// ------------------------------------------------------------------- HUD

@Composable
private fun Hud(ui: GameUi, vm: GameViewModel) {
    IconCircleButton("ic_back", Modifier.atC(76, 60, 78, 78)) { vm.setPaused(true) }
    IconCircleButton("ic_pause", Modifier.atC(176, 60, 78, 78)) { vm.setPaused(true) }

    val ftz = ui.mode == Mode.FIRST_TO_ZERO
    Pill(
        if (ftz) "FIRST TO ZERO" else "LAST STANDING",
        Modifier.at(995, 23, 430, 74), fill = Color(0xFF10172B), stroke = BS.Gold, color = Color.White, size = 34,
        icon = if (ftz) "ic_stab" else "ic_skull", family = Poppins,
    )
    val cw = ui.direction == Direction.CW
    Pill(if (cw) "SEARAH" else "BERLAWANAN", Modifier.at(1830, 23, 250, 74), size = 28, icon = "ic_cw")
    val lockLabel = if (ui.lockColor != null) "${ui.activeColor.label} •" else ui.activeColor.label
    Box(Modifier.at(2110, 23, 250, 74).background(Color(0xFF141B30), RoundedCornerShape(percent = 50))
        .border(r(2), if (ui.lockColor != null) BS.Gold else BS.SurfaceStroke, RoundedCornerShape(percent = 50)),
        contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ColorDot(ui.activeColor, 36)
            Spacer(Modifier.width(r(14)))
            BText(lockLabel, 28, spacing = 1)
        }
    }
}

// ------------------------------------------------------------- opponents

@Composable
private fun Opponent(p: PlayerUi, seat: Seat, ui: GameUi, thinking: Boolean) {
    val dim = p.eliminated
    val countText = if (ui.mode == Mode.LAST_STANDING) "${p.count}/${ui.threshold}" else p.count.toString()
    val danger = ui.mode == Mode.LAST_STANDING && p.count >= ui.threshold - 3
    when (seat) {
        Seat.LEFT, Seat.RIGHT -> {
            val cx = if (seat == Seat.LEFT) 140 else 2260
            Box(Modifier.alpha(if (dim) 0.4f else 1f)) {
                Avatar(p.id, p.name, 100, Modifier.atC(cx, 300, 100, 100), glow = p.isTurn)
                BText(p.name, 36, modifier = Modifier.atC(cx, 373, 220, 50))
                CountPill(countText, danger, Modifier.atC(cx, 432, 120, 46))
                if (p.stab && !dim && !thinking) Pill("STAB!", Modifier.atC(cx, 481, 126, 38), fill = BS.Gold, stroke = BS.GoldLight, color = Color(0xFF1A1206), size = 24, family = Poppins)
                if (!dim) CardFan(p.count, vertical = true, cx = if (seat == Seat.LEFT) 405f else 1995f, cy = 450f)
                if (thinking && !dim) ThinkingPill(Modifier.atC(cx, 481, 214, 38))
            }
            if (dim) Icon("ic_skull", 60, Color(0xFFFF6B6B), Modifier.atC(cx, 300, 60, 60))
        }
        else -> {
            val (ax, fanX) = when (seat) {
                Seat.TOP_LEFT -> 665f to 760f
                Seat.TOP_RIGHT -> 1545f to 1640f
                else -> 1040f to 1330f
            }
            val ay = if (seat == Seat.TOP_CENTER) 190f else 70f
            Box(Modifier.alpha(if (dim) 0.4f else 1f)) {
                Avatar(p.id, p.name, 90, Modifier.atC(ax, ay, 90, 90), glow = p.isTurn)
                BText(p.name, 34, modifier = Modifier.at(ax + 55, ay - 30, 200, 44), align = TextAlign.Start)
                CountPill(countText, danger, Modifier.at(ax + 55, ay + 8, 120, 46))
                if (p.stab && !dim && !thinking) Pill("STAB!", Modifier.at(ax + 190, ay + 12, 118, 38), fill = BS.Gold, stroke = BS.GoldLight, color = Color(0xFF1A1206), size = 22, family = Poppins)
                if (!dim) CardFan(p.count, vertical = false, cx = fanX, cy = if (seat == Seat.TOP_CENTER) 225f else 215f)
                if (thinking && !dim) ThinkingPill(Modifier.at(ax + 190, if (seat == Seat.TOP_CENTER) 105f else ay + 12, 214, 38))
            }
            if (dim) Icon("ic_skull", 50, Color(0xFFFF6B6B), Modifier.atC(ax, ay, 50, 50))
        }
    }
}

@Composable
private fun ThinkingPill(modifier: Modifier) {
    val tr = rememberInfiniteTransition(label = "think")
    val k by tr.animateFloat(0f, 3f, infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "dots")
    Pill(
        "BERPIKIR" + ".".repeat(k.toInt().coerceIn(0, 2) + 1), modifier,
        fill = Color(0xFF0F1830), stroke = BS.Gold, color = BS.GoldLight, size = 22, family = Poppins,
    )
}

@Composable
private fun CountPill(text: String, danger: Boolean, modifier: Modifier) {
    Box(modifier.background(Color(0xFF141B30), RoundedCornerShape(percent = 50))
        .border(r(2), if (danger) Color(0xFFFF5A5A) else BS.SurfaceStroke, RoundedCornerShape(percent = 50)),
        contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon("ic_cards", 26, if (danger) Color(0xFFFF8A8A) else Color.White)
            Spacer(Modifier.width(r(10)))
            BText(text, 30, if (danger) Color(0xFFFF8A8A) else Color.White)
        }
    }
}

@Composable
private fun CardFan(count: Int, vertical: Boolean, cx: Float, cy: Float) {
    val shown = minOf(count, 8)
    if (shown == 0) return
    val w = 92f; val h = 140f
    for (i in 0 until shown) {
        val t = i - (shown - 1) / 2f
        val angleStep = if (vertical) 0f else 5f
        val modifier = if (vertical) Modifier.atC(cx, cy + t * 30f, w, h) else Modifier.atC(cx + t * 26f, cy + kotlin.math.abs(t) * 3f, w, h)
        CardImage(null, modifier.graphicsLayer { rotationZ = if (vertical) 90f + (i % 2) * 2f else t * angleStep }, faceDown = true)
    }
}

// ----------------------------------------------------------- center piles

@Composable
private fun CenterPiles(ui: GameUi, vm: GameViewModel) {
    // Draw pile (tebal tumpukan)
    val canDraw = ui.humanTurn && !ui.humanDrawnPending
    Box(Modifier.at(0, 0, 2400, 1080)) {
        for (i in 3 downTo 1) CardImage(null, Modifier.atC(1055f - i * 3f, 466f + i * 3f, 140, 212), faceDown = true, dim = true)
        CardImage(null, Modifier.atC(1055, 462, 140, 212).clickable(enabled = canDraw) { vm.drawOrTake() }, faceDown = true)
        Pill("DRAW ${ui.drawCount}", Modifier.atC(1060, 620, 160, 44), fill = Color(0xFF0F1830), size = 24)

        // glow warna aktif
        Box(Modifier.atC(1345, 470, 360, 360).background(Brush.radialGradient(listOf(BS.cardColor(ui.activeColor).copy(alpha = 0.38f), Color.Transparent)), CircleShape))
        val under = ui.topCards.dropLast(1)
        under.forEachIndexed { i, c ->
            CardImage(c, Modifier.atC(1345f + (i - 1) * 26f, 478f, 140, 212).graphicsLayer {
                rotationZ = if (i == 0) -17f else 11f
                alpha = if (c.id in vm.holdIds) 0f else 1f
            })
        }
        val top = ui.topCards.last()
        val speed = Prefs.animSpeed
        // Bila kartu datang lewat animasi terbang, tidak perlu efek "pop" lagi.
        val anim = remember(top.id) { Animatable(if (top.id in vm.holdIds) 1f else 0f) }
        LaunchedEffect(top.id) { anim.animateTo(1f, tween((320 / speed).toInt())) }
        CardImage(top, Modifier.atC(1345, 468, 140, 212).graphicsLayer {
            val sc = 1.4f - 0.4f * anim.value
            scaleX = sc; scaleY = sc; rotationZ = -4f - 14f * (1f - anim.value)
            alpha = if (top.id in vm.holdIds) 0f else 0.35f + 0.65f * anim.value
        })
        Pill("DISCARD", Modifier.atC(1340, 620, 160, 44), fill = Color(0xFF0F1830), size = 24)
    }
}

// ----------------------------------------------------- banners & player

@Composable
private fun PreviewBanner(vm: GameViewModel) {
    val pv = vm.preview ?: return
    val w = maxOf(360, pv.text.length * 22 + 90)
    Pill(
        pv.text, Modifier.atC(1000, 738, w, 54),
        fill = if (pv.ok) BS.Gold else Color(0xFF9B1C1C), stroke = if (pv.ok) BS.GoldLight else Color(0xFFFF6B6B),
        color = if (pv.ok) Color(0xFF1A1206) else Color.White, size = 28, family = Poppins,
    )
}

@Composable
private fun ToastBanner(vm: GameViewModel) {
    val t = vm.toast ?: return
    var visible by remember(t.id) { mutableStateOf(true) }
    LaunchedEffect(t.id) { kotlinx.coroutines.delay(2300); visible = false }
    val a by animateFloatAsState(if (visible) 1f else 0f, tween(300), label = "toast")
    if (a <= 0.01f) return
    val w = maxOf(520, t.text.length * 19 + 100)
    Box(
        Modifier.atC(1200, 690, w, 58).alpha(a).background(Color(0xCC050A18), RoundedCornerShape(percent = 50))
            .border(r(2), Color(0x55FFFFFF), RoundedCornerShape(percent = 50)),
        contentAlignment = Alignment.Center,
    ) { BText(t.text, 27, Color.White, weight = FontWeight.SemiBold) }
}

@Composable
private fun PlayerPanel(ui: GameUi, vm: GameViewModel) {
    val me = ui.players[0]
    val respond = ui.phase == Phase.Respond(0)
    val myMove = ui.humanTurn || respond
    val ftz = ui.mode == Mode.FIRST_TO_ZERO

    // STAB! / BACKSTAB!
    if (ftz) {
        val pulse = rememberInfiniteTransition(label = "pulse")
        val sc by pulse.animateFloat(1f, 1.07f, infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "sc")
        val callable = ui.callKind != null
        val label = if (ui.callKind == CallKind.BACKSTAB) "BACKSTAB!" else "STAB!"
        BsButton(
            label, if (ui.callKind == CallKind.BACKSTAB) BtnKind.PRIMARY else BtnKind.GOLD,
            Modifier.at(60, 770, 300, 95).graphicsLayer { if (callable) { scaleX = sc; scaleY = sc } },
            enabled = callable, fontSize = 44,
        ) { vm.declare() }
        if (callable) {
            val prog = remember(vm.callStartedAt) { Animatable(1f) }
            LaunchedEffect(vm.callStartedAt) { prog.animateTo(0f, tween(GameViewModel.CALL_WINDOW_MS.toInt(), easing = LinearEasing)) }
            Box(Modifier.at(60, 750, 300, 12).background(Color(0x55FFFFFF), RoundedCornerShape(percent = 50))) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(prog.value).background(BS.Gold, RoundedCornerShape(percent = 50)))
            }
        }
    }

    // Panel pemain
    Avatar(0, "Kamu", 110, Modifier.atC(130, 960, 110, 110), glow = myMove)
    BText("KAMU", 38, modifier = Modifier.at(205, 912, 200, 48), align = TextAlign.Start, family = Poppins)
    Pill(if (myMove) "GILIRANMU" else "MENUNGGU", Modifier.at(205, 962, 190, 40),
        fill = if (myMove) BS.Gold else Color(0xFF3A4152), stroke = if (myMove) BS.GoldLight else Color(0xFF5A6275),
        color = if (myMove) Color(0xFF1A1206) else Color(0xFFD0D6E6), size = 22, family = Poppins)
    val countText = if (ui.mode == Mode.LAST_STANDING) "${me.count}/${ui.threshold}" else me.count.toString()
    CountPill(countText, ui.mode == Mode.LAST_STANDING && me.count >= ui.threshold - 3, Modifier.at(205, 1012, 150, 50))

    // Tombol MAINKAN / AMBIL
    val canPlay = myMove && vm.preview?.ok == true
    BsButton("MAINKAN", BtnKind.GOLD, Modifier.at(2020, 770, 300, 100), enabled = canPlay, fontSize = 40) { vm.playSelected() }
    val takeLabel = when {
        respond -> "AMBIL ${ui.chain?.total ?: ""}".trim()
        ui.humanDrawnPending -> "LEWATI"
        else -> "AMBIL"
    }
    BsButton(takeLabel, BtnKind.CONTROL, Modifier.at(2020, 900, 300, 100), enabled = myMove, fontSize = 40) { vm.drawOrTake() }
}

// ------------------------------------------------------------------ hand

@Composable
private fun HandFan(ui: GameUi, selected: List<String>, hold: Set<String>, onTap: (String) -> Unit) {
    val n = ui.hand.size
    if (n == 0) return
    val mid = (n - 1) / 2f
    val spacing = handSpacing(n)
    val ang = if (n <= 1) 0f else minOf(3.4f, 14f / mid)
    // Tangan besar: kartu sedikit mengecil, rentang melebar, tepi kipas hanya turun sedikit (tidak tenggelam).
    val cw = if (n > 10) 235f * maxOf(0.82f, 1f - (n - 10) * 0.03f) else 235f
    val ch = cw * 356f / 235f
    val interactive = ui.humanTurn || ui.phase == Phase.Respond(0)
    val speed = Prefs.animSpeed
    Box(Modifier.at(0, 0, 2400, 1080)) {
        ui.hand.forEachIndexed { i, card ->
            key(card.id) {
                val t = i - mid
                val sel = card.id in selected
                val dim = interactive && card.id !in ui.playable
                val lift by animateFloatAsState(if (sel) -112f else 0f, tween((170 / speed).toInt()), label = "lift")
                val drop = if (mid > 0f) minOf(4f * t * t, 45f * (t / mid) * (t / mid)) else 0f
                Box(
                    Modifier.atC(1200f + t * spacing, 890f + ch / 2f + drop + lift, cw, ch)
                        .zIndex(if (sel) 100f else i.toFloat())
                        .graphicsLayer {
                            rotationZ = t * ang
                            alpha = if (card.id in hold) 0f else 1f
                        }
                        .clickable { onTap(card.id) },
                ) {
                    CardImage(card, Modifier.fillMaxSize(), dim = dim)
                    if (sel) Box(Modifier.fillMaxSize().border(r(7), BS.GoldLight, RoundedCornerShape(r(24))))
                }
            }
        }
    }
}

// ------------------------------------------------------- flying cards

internal fun seatAnchor(id: Int, count: Int): Offset = when (id) {
    PILE_DRAW -> Offset(1055f, 462f)
    PILE_DISCARD -> Offset(1345f, 468f)
    0 -> Offset(1200f, 1010f)
    else -> when (seatsFor(count).getOrNull(id - 1)) {
        Seat.LEFT -> Offset(405f, 450f)
        Seat.RIGHT -> Offset(1995f, 450f)
        Seat.TOP_LEFT -> Offset(760f, 215f)
        Seat.TOP_RIGHT -> Offset(1640f, 215f)
        Seat.TOP_CENTER -> Offset(1330f, 225f)
        null -> Offset(1200f, 300f)
    }
}

private fun flightSize(id: Int): Pair<Float, Float> = when (id) {
    0 -> 235f to 356f
    PILE_DRAW, PILE_DISCARD -> 140f to 212f
    else -> 92f to 140f
}

@Composable
private fun FlightLayer(flights: List<Flight>, playerCount: Int) {
    if (flights.isEmpty()) return
    Box(Modifier.at(0, 0, 2400, 1080)) {
        for (f in flights) key(f.id) { FlightCard(f, playerCount) }
    }
}

@Composable
private fun FlightCard(f: Flight, playerCount: Int) {
    val u = LocalU.current
    val a = remember(f.id) { Animatable(0f) }
    var started by remember(f.id) { mutableStateOf(false) }
    LaunchedEffect(f.id) {
        kotlinx.coroutines.delay(f.delayMs)
        started = true
        a.animateTo(1f, tween(f.durMs.toInt(), easing = FastOutSlowInEasing))
    }
    if (!started) return
    val fa = seatAnchor(f.from, playerCount)
    val ta = seatAnchor(f.to, playerCount)
    val fx = if (f.fromX.isNaN()) fa.x else f.fromX
    val fy = if (f.fromY.isNaN()) fa.y else f.fromY
    val (fw, fh) = flightSize(f.from)
    val (tw, _) = flightSize(f.to)
    CardImage(
        f.card,
        Modifier.atC(fx, fy, fw, fh).graphicsLayer {
            val p = a.value
            val k = u * density
            translationX = (ta.x - fx) * k * p
            translationY = (ta.y - fy) * k * p - kotlin.math.sin(kotlin.math.PI.toFloat() * p) * 70f * k
            val sc = 1f + (tw / fw - 1f) * p
            scaleX = sc; scaleY = sc
            rotationZ = (if (f.to == PILE_DISCARD) -4f else 0f) * p
        },
        faceDown = f.card == null,
    )
}

// --------------------------------------------------------------- dialogs

@Composable
private fun Dialogs(ui: GameUi, vm: GameViewModel, onMenu: () -> Unit) {
    // Respons penalti / rantai (tidak menutupi tangan)
    val chain = ui.chain
    if (chain != null && vm.dialog == null) ChainDialog(chain, vm)

    when (val d = vm.dialog) {
        is GameDialog.PickColor -> ColorDialog(ui, d, vm)
        is GameDialog.PickTarget -> TargetDialog(ui, d, vm)
        GameDialog.GiveCard -> GiveDialog(ui, vm)
        GameDialog.Pause -> PauseDialog(vm, onMenu)
        GameDialog.Eliminated -> EliminatedDialog(ui, vm)
        null -> {}
    }
}

@Composable
private fun ColorDialog(ui: GameUi, d: GameDialog.PickColor, vm: GameViewModel) {
    var chosen by remember { mutableStateOf(ui.activeColor) }
    Scrim { }
    Panel(Modifier.at(630, 150, 1140, 700)) {
        BText(if (d.monopoly) "MONOPOLY" else "PILIH WARNA", 56, family = Poppins, modifier = Modifier.at(0, 40, 1140, 70))
        BText(if (d.monopoly) "kunci warna aktif selama satu putaran" else "WILD — tentukan warna aktif berikutnya", 28, BS.TextDim, weight = FontWeight.SemiBold, modifier = Modifier.at(0, 112, 1140, 40))
        CardColor.entries.forEachIndexed { i, c ->
            val cx = 190 + i * 253
            val on = chosen == c
            Box(Modifier.atC(cx, 340, 190, 190).clickable { chosen = c }, contentAlignment = Alignment.Center) {
                Box(Modifier.size(r(160)).background(Brush.verticalGradient(listOf(BS.cardColor(c), BS.cardColor(c).copy(alpha = 0.65f))), CircleShape)
                    .border(r(if (on) 9 else 4), if (on) BS.GoldLight else Color(0x66FFFFFF), CircleShape), contentAlignment = Alignment.Center) {
                    ColorGlyph(c, 74)
                }
            }
            BText(c.label, 26, if (on) BS.GoldLight else Color.White, modifier = Modifier.atC(cx, 470, 230, 40), spacing = 1)
        }
        BsButton("BATAL", BtnKind.GRAY, Modifier.at(250, 560, 300, 100), fontSize = 34) { vm.dismissDialog() }
        BsButton("KONFIRMASI", BtnKind.GOLD, Modifier.at(590, 560, 300, 100), fontSize = 34) { vm.confirmColor(chosen) }
    }
}

@Composable
private fun TargetDialog(ui: GameUi, d: GameDialog.PickTarget, vm: GameViewModel) {
    var chosen by remember { mutableStateOf<Int?>(null) }
    val targets = ui.players.filter { it.id != 0 && !it.eliminated }
    val desc = when (d.type) {
        CardType.HIJACK -> "HIJACK — ambil 2 kartu acak, beri 1 kartu"
        CardType.EXCHANGE -> "EXCHANGE — tukar seluruh kartu di tangan"
        else -> "DEBT — target ambil 2 kartu & kehilangan giliran"
    }
    Scrim { }
    Panel(Modifier.at(480, 130, 1440, 800)) {
        BText("PILIH TARGET", 56, family = Poppins, modifier = Modifier.at(0, 36, 1440, 70))
        BText(desc, 28, BS.TextDim, weight = FontWeight.SemiBold, modifier = Modifier.at(0, 108, 1440, 40))
        val card = ui.hand.firstOrNull { it.id in d.cardIds }
        CardImage(card, Modifier.atC(210, 420, 220, 334).graphicsLayer { rotationZ = -6f })
        targets.forEachIndexed { i, p ->
            val col = i % 2; val row = i / 2
            val on = chosen == p.id
            Row(
                Modifier.at(400 + col * 520, 200 + row * 140, 500, 120)
                    .background(Color(0xFF18213D), RoundedCornerShape(r(24)))
                    .border(r(if (on) 6 else 2), if (on) BS.GoldLight else BS.SurfaceStroke, RoundedCornerShape(r(24)))
                    .clickable { chosen = p.id }.padding(horizontal = r(22)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(p.id, p.name, 80)
                Spacer(Modifier.width(r(20)))
                BText(p.name, 38, align = TextAlign.Start, modifier = Modifier.width(r(190)))
                Spacer(Modifier.weight(1f))
                CountPill(p.count.toString(), false, Modifier.size(r(110), r(48)))
            }
        }
        BsButton("BATAL", BtnKind.GRAY, Modifier.at(420, 640, 300, 100), fontSize = 34) { vm.dismissDialog() }
        BsButton("KONFIRMASI", BtnKind.GOLD, Modifier.at(780, 640, 340, 100), enabled = chosen != null, fontSize = 34) { chosen?.let { vm.confirmTarget(it) } }
    }
}

@Composable
private fun GiveDialog(ui: GameUi, vm: GameViewModel) {
    var chosen by remember { mutableStateOf<String?>(null) }
    Scrim { }
    Panel(Modifier.at(250, 150, 1900, 760)) {
        BText("BERI KARTU", 56, family = Poppins, modifier = Modifier.at(0, 36, 1900, 70))
        BText("HIJACK — pilih 1 kartu dari tanganmu untuk diberikan kepada target", 28, BS.TextDim, weight = FontWeight.SemiBold, modifier = Modifier.at(0, 108, 1900, 40))
        Row(Modifier.at(60, 180, 1780, 400).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(r(18)), verticalAlignment = Alignment.CenterVertically) {
            ui.hand.forEach { c ->
                val on = chosen == c.id
                Box(Modifier.size(r(200), r(304)).offset(y = if (on) -r(18) else 0.dp).clickable { chosen = c.id }) {
                    CardImage(c, Modifier.fillMaxSize())
                    if (on) Box(Modifier.fillMaxSize().border(r(7), BS.GoldLight, RoundedCornerShape(r(22))))
                }
            }
        }
        BsButton("BERIKAN", BtnKind.GOLD, Modifier.at(780, 610, 340, 100), enabled = chosen != null, fontSize = 36) { chosen?.let { vm.give(it) } }
    }
}

@Composable
private fun PauseDialog(vm: GameViewModel, onMenu: () -> Unit) {
    Scrim { }
    Panel(Modifier.at(820, 200, 760, 680)) {
        BText("JEDA", 64, family = Poppins, modifier = Modifier.at(0, 50, 760, 80))
        BsButton("LANJUTKAN", BtnKind.PRIMARY, Modifier.at(130, 190, 500, 110), icon = "ic_play", fontSize = 38) { vm.setPaused(false) }
        BsButton("MENU UTAMA", BtnKind.GRAY, Modifier.at(130, 340, 500, 110), icon = "ic_exit", fontSize = 36) { vm.clear(); onMenu() }
        BText("Draw ${vm.ui?.drawCount ?: 0} • Arah ${if (vm.ui?.direction == Direction.CW) "searah" else "berlawanan"}", 26, BS.TextDim, weight = FontWeight.SemiBold, modifier = Modifier.at(0, 520, 760, 40))
    }
}

@Composable
private fun EliminatedDialog(ui: GameUi, vm: GameViewModel) {
    Scrim { }
    Panel(Modifier.at(700, 190, 1000, 700), stroke = Color(0xFFB91C1C)) {
        Icon("ic_skull", 130, Color.White, Modifier.atC(500, 130, 130, 130))
        BText("TERELIMINASI", 76, Color(0xFFE23B3B), family = Poppins, modifier = Modifier.at(0, 215, 1000, 100))
        Avatar(0, "Kamu", 90, Modifier.atC(250, 405, 90, 90))
        BText("Kamu", 40, modifier = Modifier.at(320, 370, 300, 50), align = TextAlign.Start)
        BText("mencapai batas ${ui.threshold} kartu", 26, BS.TextDim, weight = FontWeight.SemiBold, modifier = Modifier.at(320, 420, 400, 40), align = TextAlign.Start)
        Box(Modifier.at(640, 395, 260, 30).background(Color(0xFF2A1218), RoundedCornerShape(percent = 50)).border(r(2), Color(0xFFB91C1C), RoundedCornerShape(percent = 50))) {
            Box(Modifier.fillMaxSize().background(Color(0xFFE23B3B), RoundedCornerShape(percent = 50)))
        }
        BText("${ui.threshold} / ${ui.threshold}", 26, modifier = Modifier.at(640, 432, 260, 36))
        BText("Tersisa ${ui.alive} pemain", 30, BS.Gold, modifier = Modifier.at(0, 500, 1000, 40))
        BsButton("LANJUTKAN", BtnKind.GOLD, Modifier.at(340, 560, 320, 100), fontSize = 36) { vm.dismissDialog() }
    }
}

@Composable
private fun ChainDialog(c: ChainUi, vm: GameViewModel) {
    // Redam area meja saja; tangan & tombol tetap interaktif
    Box(Modifier.at(0, 0, 2400, 760).background(Color(0x99030610)).clickable(enabled = false) {})
    val title = if (c.chainable) "PLUS CHAIN" else c.sourceLabel
    Panel(Modifier.at(200, 120, 2000, 640), stroke = Color(0xFFB91C1C)) {
        BText(title, 52, family = Poppins, modifier = Modifier.at(0, 24, 2000, 64))
        BText(
            if (c.chainable) "Kamu jadi target — pilih respons atau putuskan rantai" else "Serangan ${c.sourceLabel} — bertahan atau ambil kartu",
            28, BS.TextDim, weight = FontWeight.SemiBold, modifier = Modifier.at(0, 92, 2000, 40),
        )
        // langkah rantai (maks 3 terakhir)
        val steps = c.steps.takeLast(3)
        steps.forEachIndexed { i, st ->
            val x = 60 + i * 340
            Row(
                Modifier.at(x, 160, 300, 160).background(Color(0xFF18213D), RoundedCornerShape(r(20))).border(r(2), BS.SurfaceStroke, RoundedCornerShape(r(20))).padding(r(12)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CardImage(st.cards.lastOrNull(), Modifier.size(r(84), r(128)))
                Spacer(Modifier.width(r(14)))
                Column {
                    BText(playerName(st.player), 28, align = TextAlign.Start)
                    BText(st.label, 22, BS.TextDim, weight = FontWeight.SemiBold, align = TextAlign.Start)
                    BText("+${st.amount}", 40, BS.GoldLight, family = Poppins, align = TextAlign.Start)
                }
            }
        }
        // pertahanan
        val defs = listOf(
            Triple(CardType.IMMUNITY, "IMMUNITY", "Batalkan rantai"),
            Triple(CardType.DEFLECT, "DEFLECT", "Alihkan ke berikutnya"),
            Triple(CardType.BOOMERANG, "BOOMERANG", "Kembali ke penyerang"),
        )
        defs.forEachIndexed { i, (type, name, desc) ->
            val owned = c.defenses[type] == true
            val usable = c.usable[type] == true
            Row(
                Modifier.at(60 + i * 460, 360, 430, 190).alpha(if (owned) 1f else 0.45f)
                    .background(Color(0xFF0C2A25), RoundedCornerShape(r(20))).border(r(2), Color(0xFF0E7A5A), RoundedCornerShape(r(20))).padding(r(14)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val sample = remember(type) { Deck.build().first { it.type == type } }
                CardImage(sample, Modifier.size(r(98), r(149)))
                Spacer(Modifier.width(r(16)))
                Column(Modifier.weight(1f)) {
                    BText(name, 28, align = TextAlign.Start)
                    BText(desc, 20, BS.TextDim, weight = FontWeight.SemiBold, align = TextAlign.Start)
                    Spacer(Modifier.height(r(10)))
                    BsButton(if (owned) "GUNAKAN" else "TIDAK ADA", BtnKind.DEFENSE, Modifier.size(r(220), r(64)), enabled = usable, fontSize = 22) { vm.defend(type) }
                }
            }
        }
        // total & aksi
        Box(
            Modifier.atC(1700, 250, 240, 240).background(Brush.verticalGradient(listOf(Color(0xFFE23B3B), Color(0xFF8E1717))), CircleShape)
                .border(r(6), Color(0x66FFFFFF), CircleShape),
            contentAlignment = Alignment.Center,
        ) { BText("+${c.total}", 92, family = Poppins) }
        BText("TARGET: ${c.targetName}", 26, BS.Gold, modifier = Modifier.atC(1700, 400, 400, 40))
        if (c.chainable) {
            BsButton("LANJUTKAN", BtnKind.GOLD, Modifier.at(1560, 440, 280, 84), enabled = vm.preview?.ok == true, fontSize = 30) { vm.playSelected() }
        }
        BsButton("AMBIL ${c.total}", BtnKind.PRIMARY, Modifier.at(1560, 540, 280, 84).let { if (c.chainable) it else it.offset(y = -r(100)) }, fontSize = 30) { vm.drawOrTake() }
        BText("Hanya satu respons pertahanan per rantai", 22, BS.TextDim, weight = FontWeight.SemiBold, modifier = Modifier.at(60, 580, 1300, 36), align = TextAlign.Start)
    }
}

private fun playerName(id: Int) = GameEngine.PLAYER_NAMES.getOrElse(id) { "?" }
