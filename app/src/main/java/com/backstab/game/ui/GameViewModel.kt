package com.backstab.game.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.backstab.game.data.Feedback
import com.backstab.game.data.Prefs
import com.backstab.game.engine.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class PlayerUi(
    val id: Int, val name: String, val count: Int, val eliminated: Boolean,
    val isHuman: Boolean, val stab: Boolean, val isTurn: Boolean,
)

data class Preview(val text: String, val ok: Boolean)

data class ChainUi(
    val total: Int,
    val steps: List<ChainStep>,
    val chainable: Boolean,
    val sourceLabel: String,
    val targetName: String,
    val defenses: Map<CardType, Boolean>,   // tipe -> dimiliki
    val usable: Map<CardType, Boolean>,     // tipe -> legal dipakai sekarang
)

data class Standing(val id: Int, val name: String, val count: Int, val eliminated: Boolean)

data class GameResult(
    val mode: Mode, val winnerId: Int, val winnerName: String, val humanWon: Boolean,
    val standings: List<Standing>, val durationSec: Int, val longestChain: Int,
)

sealed interface GameDialog {
    data class PickColor(val cardIds: List<String>, val monopoly: Boolean) : GameDialog
    data class PickTarget(val cardIds: List<String>, val type: CardType) : GameDialog
    data object GiveCard : GameDialog
    data object Pause : GameDialog
    data object Eliminated : GameDialog
}

data class Toast(val id: Int, val text: String)

const val PILE_DRAW = -1
const val PILE_DISCARD = -2

/** Satu kartu yang sedang terbang dari [from] ke [to] (id pemain atau PILE_*). card == null → tertutup. */
data class Flight(
    val id: Int, val from: Int, val to: Int, val card: Card?,
    val delayMs: Long, val durMs: Long,
    val fromX: Float = Float.NaN, val fromY: Float = Float.NaN,
)

internal val handOrder: Comparator<Card> = compareBy({ it.color?.ordinal ?: 9 }, { it.value ?: 99 }, { it.type.ordinal })

internal fun handSpan(n: Int): Float = if (n <= 10) 1250f else minOf(1500f, 1250f + (n - 10) * 30f)
internal fun handSpacing(n: Int): Float = if (n <= 1) 0f else minOf(158f, handSpan(n) / (n - 1))
internal fun handCardX(i: Int, n: Int): Float = 1200f + (i - (n - 1) / 2f) * handSpacing(n)

private fun Action.actorId(): Int = when (this) {
    is Action.Play -> player
    is Action.Draw -> player
    is Action.Pass -> player
    is Action.Take -> player
    is Action.Chain -> player
    is Action.Defend -> player
    is Action.Give -> player
    is Action.Declare -> player
    is Action.CallTimeout -> player
}

data class GameUi(
    val mode: Mode,
    val threshold: Int,
    val players: List<PlayerUi>,
    val hand: List<Card>,
    val playable: Set<String>,
    val topCards: List<Card>,
    val drawCount: Int,
    val activeColor: CardColor,
    val lockColor: CardColor?,
    val direction: Direction,
    val phase: Phase,
    val humanTurn: Boolean,
    val humanDrawnPending: Boolean,
    val chain: ChainUi?,
    val callKind: CallKind?,
    val humanEliminated: Boolean,
    val alive: Int,
)

class GameViewModel : ViewModel() {
    companion object {
        const val CALL_WINDOW_MS = 3600L
        const val HUMAN = 0
    }

    private var engine: GameEngine? = null
    private val bot = Bot(Random(System.nanoTime()))
    private var loop: Job? = null
    private var startMs = 0L
    private var toastId = 0

    var ui by mutableStateOf<GameUi?>(null); private set
    var selected by mutableStateOf<List<String>>(emptyList()); private set
    var dialog by mutableStateOf<GameDialog?>(null); private set
    var toast by mutableStateOf<Toast?>(null); private set
    var result by mutableStateOf<GameResult?>(null); private set
    var paused by mutableStateOf(false); private set
    var callStartedAt by mutableStateOf(0L); private set
    var preview by mutableStateOf<Preview?>(null); private set
    var flights by mutableStateOf<List<Flight>>(emptyList()); private set
    var holdIds by mutableStateOf<Set<String>>(emptySet()); private set   // kartu yang masih "di perjalanan" (disembunyikan di tujuan)
    var thinking by mutableStateOf<Int?>(null); private set

    private var flightSeq = 0
    private var flightEndMs = 0L
    private var settleMs = 0L
    private var snapHand: List<Card> = emptyList()
    private var snapDiscard: Set<String> = emptySet()
    private var animActor = -1
    private var animGiveTarget = -1
    private var animGiveCardId: String? = null

    // ------------------------------------------------------------ lifecycle

    fun start(mode: Mode, playerCount: Int, threshold: Int) {
        loop?.cancel()
        engine = GameEngine.newGame(mode, playerCount, threshold)
        startMs = System.currentTimeMillis()
        selected = emptyList(); dialog = null; result = null; paused = false; toast = null; preview = null
        flights = emptyList(); holdIds = emptySet(); thinking = null; settleMs = 0; animActor = -1
        engine?.let { g ->
            snapHand = g.player(HUMAN).hand.sortedWith(handOrder)
            snapDiscard = g.s.discard.map { it.id }.toSet()
        }
        refresh()
        runLoop()
    }

    fun clear() {
        loop?.cancel(); engine = null; ui = null; result = null; dialog = null; selected = emptyList(); preview = null
        flights = emptyList(); holdIds = emptySet(); thinking = null
    }

    val active get() = engine != null

    @JvmName("applyPaused") fun setPaused(p: Boolean) {
        paused = p
        dialog = if (p) GameDialog.Pause else if (dialog == GameDialog.Pause) null else dialog
    }

    fun dismissDialog() { if (dialog != GameDialog.Pause) dialog = null; selected = emptyList(); updatePreview() }

    // ----------------------------------------------------------- game loop

    private fun runLoop() {
        loop?.cancel()
        thinking = null
        val e = engine ?: return
        loop = viewModelScope.launch {
            if (settleMs > 0) { val d = settleMs; settleMs = 0; delay(d) }
            while (isActive) {
                while (paused) delay(100)
                val ph = e.s.phase
                if (ph is Phase.Over) { finish(e, ph); return@launch }
                val actor = e.actor() ?: return@launch
                val p = e.player(actor)
                if (p.isHuman) {
                    if (ph is Phase.Call) {
                        callStartedAt = System.currentTimeMillis()
                        delay(CALL_WINDOW_MS)
                        while (paused) delay(100)
                        if (e.s.phase == ph) { act(e, Action.CallTimeout(actor)); refresh() }
                        continue
                    }
                    return@launch
                }
                // Bot "berpikir" dulu: indikator tampil, lalu kartu diturunkan.
                val base = when (ph) {
                    is Phase.Call -> 450.0
                    is Phase.Give -> Random.nextDouble(1200.0, 2000.0)
                    is Phase.Respond -> Random.nextDouble(1300.0, 2200.0)
                    else -> Random.nextDouble(1600.0, 2800.0)
                }
                if (ph !is Phase.Call) thinking = actor
                delay((base / Prefs.animSpeed).toLong())
                while (paused) delay(100)
                thinking = null
                var action = bot.decide(e, actor)
                var res = act(e, action)
                if (res is Result.Illegal) {   // pengaman: tidak boleh terjadi
                    action = when (ph) {
                        is Phase.Respond -> Action.Take(actor)
                        is Phase.Call -> Action.CallTimeout(actor)
                        is Phase.Turn -> if (e.s.drawnCardId != null) Action.Pass(actor) else Action.Draw(actor)
                        else -> action
                    }
                    res = act(e, action)
                }
                refresh()
                if (flightEndMs > 0) delay(flightEndMs + 250)   // tunggu animasi kartu selesai
            }
        }
    }

    private suspend fun finish(e: GameEngine, over: Phase.Over) {
        refresh()
        delay(1500)
        val winner = over.winner ?: 0
        val standings = e.s.players
            .map { Standing(it.id, it.name, it.hand.size, it.eliminated) }
            .sortedWith(compareBy<Standing> { it.id != winner }.thenBy { it.eliminated }.thenBy { it.count })
        val human = winner == HUMAN
        if (human) Feedback.win()
        result = GameResult(
            e.s.mode, winner, e.player(winner).name, human, standings,
            ((System.currentTimeMillis() - startMs) / 1000).toInt(), e.s.stats.longestChain,
        )
    }

    // ------------------------------------------------------- human actions

    private fun human() = engine?.player(HUMAN)

    /** Terapkan aksi sambil mengambil snapshot untuk animasi kartu. */
    private fun act(e: GameEngine, a: Action): Result {
        snapHand = e.player(HUMAN).hand.sortedWith(handOrder)
        snapDiscard = e.s.discard.map { it.id }.toSet()
        animActor = a.actorId()
        val ph = e.s.phase
        animGiveTarget = if (a is Action.Give && ph is Phase.Give) ph.target else -1
        animGiveCardId = (a as? Action.Give)?.cardId
        return e.apply(a)
    }

    private fun send(a: Action): Boolean {
        val e = engine ?: return false
        val r = act(e, a)
        if (r is Result.Illegal) { showToast(r.reason); return false }
        selected = emptyList()
        dialog = if (dialog == GameDialog.Pause) dialog else null
        Feedback.play()
        refresh()
        settleMs = if (flightEndMs > 0) flightEndMs + 150 else 0
        runLoop()
        return true
    }

    fun toggle(cardId: String) {
        val e = engine ?: return
        val ph = e.s.phase
        if (!(ph == Phase.Turn(HUMAN) || ph == Phase.Respond(HUMAN))) return
        selected = if (cardId in selected) selected - cardId else selected + cardId
        Feedback.tap()
        updatePreview()
    }

    fun playSelected() {
        val e = engine ?: return
        val h = human() ?: return
        val ids = selected
        if (ids.isEmpty()) return
        when (e.s.phase) {
            Phase.Turn(HUMAN) -> {
                val cards = ids.mapNotNull { id -> h.hand.firstOrNull { it.id == id } }
                if (cards.size == 1 && !cards[0].isNumber) {
                    val c = cards[0]
                    when (c.type) {
                        CardType.WILD, CardType.MONOPOLY -> {
                            val probe = Action.Play(HUMAN, ids, color = e.s.activeColor)
                            val err = e.validatePlay(probe)
                            if (err != null) showToast(err) else dialog = GameDialog.PickColor(ids, c.type == CardType.MONOPOLY)
                        }
                        CardType.DEBT, CardType.EXCHANGE, CardType.HIJACK -> {
                            val targets = e.s.players.filter { it.id != HUMAN && !it.eliminated }
                            val valid = targets.filter { e.validatePlay(Action.Play(HUMAN, ids, target = it.id)) == null }
                            when {
                                valid.isEmpty() -> showToast(e.validatePlay(Action.Play(HUMAN, ids, target = targets.firstOrNull()?.id)) ?: "Target tidak valid")
                                valid.size == 1 -> send(Action.Play(HUMAN, ids, target = valid[0].id))
                                else -> dialog = GameDialog.PickTarget(ids, c.type)
                            }
                        }
                        else -> send(Action.Play(HUMAN, ids))
                    }
                } else send(Action.Play(HUMAN, ids))
            }
            Phase.Respond(HUMAN) -> {
                val cards = ids.mapNotNull { id -> h.hand.firstOrNull { it.id == id } }
                if (cards.size == 1 && cards[0].type.isDefense) send(Action.Defend(HUMAN, cards[0].id))
                else send(Action.Chain(HUMAN, ids))
            }
            else -> {}
        }
    }

    fun confirmColor(color: CardColor) {
        val d = dialog as? GameDialog.PickColor ?: return
        send(Action.Play(HUMAN, d.cardIds, color = color))
    }

    fun confirmTarget(target: Int) {
        val d = dialog as? GameDialog.PickTarget ?: return
        send(Action.Play(HUMAN, d.cardIds, target = target))
    }

    fun give(cardId: String) { send(Action.Give(HUMAN, cardId)) }

    /** Tombol AMBIL: tarik kartu / ambil penalti / lewati bila kartu tarikan tidak dimainkan. */
    fun drawOrTake() {
        val e = engine ?: return
        when (e.s.phase) {
            Phase.Turn(HUMAN) -> send(if (e.s.drawnCardId != null) Action.Pass(HUMAN) else Action.Draw(HUMAN))
            Phase.Respond(HUMAN) -> send(Action.Take(HUMAN))
            else -> {}
        }
    }

    fun defend(type: CardType) {
        val h = human() ?: return
        val c = h.hand.firstOrNull { it.type == type } ?: return
        send(Action.Defend(HUMAN, c.id))
    }

    fun declare() {
        val e = engine ?: return
        if (e.s.phase is Phase.Call && (e.s.phase as Phase.Call).player == HUMAN) send(Action.Declare(HUMAN))
    }

    // ----------------------------------------------------------- UI state

    private fun updatePreview() {
        val e = engine ?: return
        val h = human() ?: return
        val ids = selected
        if (ids.isEmpty()) { preview = null; return }
        val cards = ids.mapNotNull { id -> h.hand.firstOrNull { it.id == id } }
        val kind = e.classify(cards)
        val ph = e.s.phase
        if (ph == Phase.Respond(HUMAN)) {
            val pen = e.s.penalty
            val v = e.chainValue(cards)
            preview = when {
                cards.size == 1 && cards[0].type.isDefense ->
                    if (e.validateDefend(Action.Defend(HUMAN, cards[0].id)) == null) Preview("${cards[0].type.name} » RESPONS PERTAHANAN", true)
                    else Preview(e.validateDefend(Action.Defend(HUMAN, cards[0].id)) ?: "Tidak bisa", false)
                v > 0 && pen?.chainable == true -> Preview("${kindLabel(kind, cards)} » +$v → TOTAL +${pen.total + v}", true)
                else -> Preview("Bukan kartu penalti yang valid", false)
            }
            return
        }
        if (kind == GameEngine.SetKind.INVALID) { preview = Preview("Kombinasi tidak valid", false); return }
        val probe = Action.Play(HUMAN, ids, color = e.s.activeColor, target = e.s.players.firstOrNull { it.id != HUMAN && !it.eliminated }?.id)
        val err = e.validatePlay(probe)
        preview = if (err != null) Preview(err, false) else Preview(effectLabel(kind, cards), true)
    }

    private fun kindLabel(kind: GameEngine.SetKind, cards: List<Card>) = when (kind) {
        GameEngine.SetKind.SINGLE -> if (cards[0].type == CardType.PLUS_TWO) "PLUS TWO" else "${cards[0].color?.label ?: cards[0].type.name} ${cards[0].value ?: ""}".trim()
        GameEngine.SetKind.STRAIGHT -> "STRAIGHT ${cards.sortedBy { it.value }.joinToString("-") { it.value.toString() }}"
        else -> "${kind.name} ${cards[0].value}"
    }

    private fun effectLabel(kind: GameEngine.SetKind, cards: List<Card>): String = when (kind) {
        GameEngine.SetKind.PAIR -> "PAIR ${cards[0].value} » +1 PENALTI"
        GameEngine.SetKind.TRIPLE -> "TRIPLE ${cards[0].value} » +3 PENALTI"
        GameEngine.SetKind.QUAD -> "QUAD ${cards[0].value} » +4 PENALTI"
        GameEngine.SetKind.STRAIGHT -> "${kindLabel(kind, cards)} » BALIK ARAH"
        GameEngine.SetKind.SINGLE -> {
            val c = cards[0]
            when (c.type) {
                CardType.NUMBER -> "${c.color!!.label} ${c.value}"
                CardType.PLUS_TWO -> "PLUS TWO » +2 PENALTI"
                CardType.TAX -> "TAX » SEMUA LAWAN AMBIL 1"
                CardType.DEBT -> "DEBT » TARGET AMBIL 2 + LEWATI"
                CardType.REVERSE -> "REVERSE » BALIK ARAH"
                CardType.SKIP -> "SKIP » LEWATI PEMAIN BERIKUTNYA"
                CardType.WILD -> "WILD » PILIH WARNA"
                CardType.MONOPOLY -> "MONOPOLY » KUNCI WARNA"
                CardType.CHAOS -> "CHAOS » +3 & BALIK ARAH"
                CardType.MUTINY -> "MUTINY » KARTU BERPUTAR"
                CardType.PICKPOCKET -> "PICKPOCKET » CURI 1 KARTU"
                CardType.EXCHANGE -> "EXCHANGE » TUKAR TANGAN"
                CardType.HIJACK -> "HIJACK » CURI 2, BERI 1"
                else -> c.type.name
            }
        }
        else -> ""
    }

    // ------------------------------------------------------ card animation

    private fun buildFlights(e: GameEngine, evs: List<GameEvent>) {
        flightEndMs = 0
        val actor = animActor
        animActor = -1
        if (actor < 0 && evs.isEmpty()) return
        val s = e.s
        val spd = Prefs.animSpeed.coerceAtLeast(0.5f)
        val dur = (850 / spd).toLong()
        val gap = (150 / spd).toLong()
        val out = mutableListOf<Flight>()
        val hold = mutableSetOf<String>()
        var t = 0L
        fun fly(from: Int, to: Int, card: Card?, fx: Float = Float.NaN, fy: Float = Float.NaN) {
            out += Flight(++flightSeq, from, to, card, t, dur, fx, fy)
            t += gap
        }

        val before = snapHand
        val nowHand = e.player(HUMAN).hand.sortedWith(handOrder)
        val beforeIds = before.map { it.id }.toSet()
        val nowIds = nowHand.map { it.id }.toSet()
        val newDisc = if (actor >= 0) s.discard.filter { it.id !in snapDiscard } else emptyList()
        val discIds = newDisc.map { it.id }.toSet()
        val arrivals = ArrayDeque(nowHand.filter { it.id !in beforeIds })
        val departures = ArrayDeque(before.filter { it.id !in nowIds && it.id !in discIds && it.id != animGiveCardId })
        fun leave(c: Card, to: Int) {
            val idx = before.indexOfFirst { it.id == c.id }
            if (idx >= 0) fly(HUMAN, to, c, handCardX(idx, before.size), 1000f) else fly(HUMAN, to, c)
        }
        fun arrive(c: Card, from: Int) { hold += c.id; fly(from, HUMAN, c) }

        // 1) kartu yang dimainkan → tumpukan buang
        for ((i, c) in newDisc.withIndex()) {
            hold += c.id
            if (i >= 6) continue
            if (actor == HUMAN) leave(c, PILE_DISCARD) else fly(actor, PILE_DISCARD, c)
        }

        // 2) efek: ambil / curi / tukar / mutiny
        for (ev in evs) when (ev.type) {
            EventType.DREW, EventType.FORCED_DRAW, EventType.CALL_FAILED -> {
                if (ev.player == HUMAN) {
                    for (i in 0 until ev.n) {
                        val c = arrivals.removeFirstOrNull() ?: break
                        if (i < 6) arrive(c, PILE_DRAW) else hold += c.id
                    }
                } else repeat(minOf(ev.n, 5)) { fly(PILE_DRAW, ev.player, null) }
            }
            EventType.TAX -> for (o in s.players) if (o.id != ev.player && !o.eliminated) {
                if (o.id == HUMAN) arrivals.removeFirstOrNull()?.let { arrive(it, PILE_DRAW) } else fly(PILE_DRAW, o.id, null)
            }
            EventType.STOLE -> repeat(minOf(ev.n, 4)) {
                when {
                    ev.player == HUMAN -> arrivals.removeFirstOrNull()?.let { arrive(it, ev.other) }
                    ev.other == HUMAN -> departures.removeFirstOrNull()?.let { leave(it, ev.player) }
                    else -> fly(ev.other, ev.player, null)
                }
            }
            EventType.SWAPPED -> {
                if (ev.player == HUMAN || ev.other == HUMAN) {
                    val other = if (ev.player == HUMAN) ev.other else ev.player
                    var i = 0
                    while (arrivals.isNotEmpty()) { val c = arrivals.removeFirstOrNull() ?: break; if (i++ < 4) arrive(c, other) else hold += c.id }
                    var j = 0
                    while (departures.isNotEmpty()) { val c = departures.removeFirstOrNull() ?: break; if (j++ < 4) leave(c, other) }
                } else repeat(2) { fly(ev.player, ev.other, null); fly(ev.other, ev.player, null) }
            }
            EventType.MUTINY -> for (o in s.players.filter { !it.eliminated }) {
                val to = e.nextActive(o.id)
                when {
                    o.id == HUMAN -> departures.removeFirstOrNull()?.let { leave(it, to) }
                    to == HUMAN -> arrivals.removeFirstOrNull()?.let { arrive(it, o.id) }
                    else -> fly(o.id, to, null)
                }
            }
            else -> {}
        }

        // 3) Give (hijack): satu kartu berpindah ke target
        if (animGiveTarget >= 0) {
            val gid = animGiveCardId
            when {
                actor == HUMAN -> before.firstOrNull { it.id == gid }?.let { leave(it, animGiveTarget) }
                animGiveTarget == HUMAN -> arrivals.removeFirstOrNull()?.let { arrive(it, actor) }
                else -> fly(actor, animGiveTarget, null)
            }
        }
        animGiveTarget = -1; animGiveCardId = null

        if (out.isEmpty()) return
        val end = out.maxOf { it.delayMs + it.durMs }
        flightEndMs = end
        flights = flights + out
        holdIds = holdIds + hold
        val ids = out.map { it.id }.toSet()
        val held = hold.toSet()
        viewModelScope.launch {
            delay(end + 60)
            flights = flights.filter { it.id !in ids }
            holdIds = holdIds - held
        }
    }

    private fun showToast(text: String) { toast = Toast(++toastId, text) }

    private fun refresh() {
        val e = engine ?: return
        val s = e.s
        val h = e.player(HUMAN)
        val actor = e.actor()
        val ph = s.phase

        val playable = mutableSetOf<String>()
        if (ph == Phase.Turn(HUMAN)) e.candidatePlays(HUMAN).forEach { playable += it.cardIds }
        if (ph == Phase.Respond(HUMAN)) {
            e.chainOptions(HUMAN).forEach { playable += it }
            e.defenseOptions(HUMAN).forEach { d -> h.hand.filter { it.type == d.type }.forEach { playable += it.id } }
        }

        var chain: ChainUi? = null
        val pen = s.penalty
        if (pen != null && ph == Phase.Respond(HUMAN)) {
            val owned = listOf(CardType.IMMUNITY, CardType.DEFLECT, CardType.BOOMERANG).associateWith { t -> h.hand.any { it.type == t } }
            val usable = e.defenseOptions(HUMAN).map { it.type }.toSet()
            chain = ChainUi(
                pen.total, pen.steps.toList(), pen.chainable, pen.source?.name ?: "PLUS CHAIN",
                e.player(pen.target).name.uppercase(),
                owned, owned.keys.associateWith { it in usable },
            )
        }

        ui = GameUi(
            mode = s.mode, threshold = s.threshold,
            players = s.players.map { PlayerUi(it.id, it.name, it.hand.size, it.eliminated, it.isHuman, it.stabCalled, actor == it.id) },
            hand = h.hand.sortedWith(handOrder),
            playable = playable,
            topCards = s.discard.takeLast(3),
            drawCount = s.drawPile.size,
            activeColor = s.activeColor, lockColor = s.lock?.color, direction = s.direction,
            phase = ph,
            humanTurn = ph == Phase.Turn(HUMAN),
            humanDrawnPending = ph == Phase.Turn(HUMAN) && s.drawnCardId != null,
            chain = chain,
            callKind = (ph as? Phase.Call)?.takeIf { it.player == HUMAN }?.kind,
            humanEliminated = h.eliminated,
            alive = s.active.size,
        )

        if (ph is Phase.Give && ph.player == HUMAN && dialog != GameDialog.GiveCard) dialog = GameDialog.GiveCard
        selected = selected.filter { id -> h.hand.any { it.id == id } }
        updatePreview()

        val evs = e.drainEvents()
        buildFlights(e, evs)
        if (evs.isNotEmpty()) {
            val lines = evs.mapNotNull { describe(it, s) }
            if (lines.isNotEmpty()) showToast(lines.takeLast(2).joinToString("  •  "))
            for (ev in evs) {
                if (ev.type == EventType.ELIMINATED && ev.player == HUMAN) { dialog = GameDialog.Eliminated; Feedback.alert() }
                if (ev.type in listOf(EventType.PENALTY, EventType.CALL_FAILED) && (ev.other == HUMAN || ev.player == HUMAN)) Feedback.alert()
            }
        }
    }

    private fun nm(s: GameState, id: Int) = if (id in s.players.indices) s.players[id].name else "?"

    private fun describe(ev: GameEvent, s: GameState): String? = when (ev.type) {
        EventType.PLAYED -> when {
            ev.card == CardType.NUMBER && ev.n == 1 -> "${nm(s, ev.player)} memainkan kartu ${ev.color?.label ?: ""}"
            ev.card == CardType.NUMBER -> "${nm(s, ev.player)} memainkan kombinasi ${ev.n} kartu"
            else -> "${nm(s, ev.player)} memainkan ${ev.card?.name}"
        }
        EventType.DREW -> if (ev.n > 0) "${nm(s, ev.player)} mengambil ${ev.n} kartu" else null
        EventType.PENALTY -> "Penalti +${ev.n} untuk ${nm(s, ev.other)}"
        EventType.CHAIN -> "Rantai naik! Total +${ev.n} → ${nm(s, ev.other)}"
        EventType.DIRECTION -> "Arah permainan berbalik"
        EventType.COLOR -> "Warna aktif: ${ev.color?.label}"
        EventType.SKIPPED -> "${nm(s, if (ev.other >= 0) ev.other else ev.player)} dilewati"
        EventType.STOLE -> "${nm(s, ev.player)} mencuri ${ev.n} kartu dari ${nm(s, ev.other)}"
        EventType.SWAPPED -> "${nm(s, ev.player)} bertukar tangan dengan ${nm(s, ev.other)}"
        EventType.MUTINY -> "MUTINY! Kartu berpindah ke tetangga"
        EventType.TAX -> "TAX! Semua lawan mengambil 1 kartu"
        EventType.ELIMINATED -> "${nm(s, ev.player)} tereliminasi!"
        EventType.CALL_STAB -> "${nm(s, ev.player)}: STAB!"
        EventType.CALL_BACKSTAB -> "${nm(s, ev.player)}: BACKSTAB!"
        EventType.CALL_FAILED -> "${nm(s, ev.player)} lupa mengumumkan — ambil ${ev.n} kartu"
        EventType.IMMUNITY -> "IMMUNITY! Rantai +${ev.n} dibatalkan"
        EventType.DEFLECT -> "DEFLECT! +${ev.n} dialihkan ke ${nm(s, ev.other)}"
        EventType.BOOMERANG -> "BOOMERANG! +${ev.n} kembali ke ${nm(s, ev.other)}"
        EventType.MONOPOLY -> "MONOPOLY! Warna ${ev.color?.label} terkunci"
        EventType.RESHUFFLE -> "Draw Pile diisi ulang"
        EventType.FORCED_DRAW -> "${nm(s, ev.player)} tak bisa mencocokkan — ambil ${ev.n}"
        EventType.DEFENSE, EventType.WIN -> null
    }
}
