package com.backstab.game.engine

import kotlin.random.Random

/**
 * Mesin aturan BACKSTAB! — Kotlin murni (tanpa Android) agar bisa diuji di JVM.
 * Keputusan aturan PRD §18 didokumentasikan di docs/RULES_DECISIONS.md.
 */
class GameEngine(val s: GameState, private val rng: Random) {

    companion object {
        val PLAYER_NAMES = listOf("Kamu", "Rexa", "Mira", "Dante", "Kilo", "Vega")

        fun newGame(mode: Mode, playerCount: Int, threshold: Int = 15, seed: Long = System.nanoTime(), humanSeat: Boolean = true): GameEngine {
            require(playerCount in 2..6)
            val rng = Random(seed)
            val players = (0 until playerCount).map { Player(it, PLAYER_NAMES[it], isHuman = humanSeat && it == 0) }
            val engine = GameEngine(GameState(mode, players, threshold), rng)
            engine.setup()
            return engine
        }
    }

    // ---------------------------------------------------------------- setup

    private fun setup() {
        s.drawPile += Deck.build().shuffled(rng)
        repeat(7) { for (p in s.players) p.hand += s.drawPile.removeLast() }
        // Kartu awal harus Number Card (PRD §9.1 #5-6)
        while (s.drawPile.last().type != CardType.NUMBER) s.drawPile.shuffle(rng)
        val first = s.drawPile.removeLast()
        s.discard += first
        s.activeColor = first.color!!
        s.lastValue = first.value
        s.phase = Phase.Turn(0)
        s.lastActor = 0
    }

    fun totalCards() = s.players.sumOf { it.hand.size } + s.drawPile.size + s.discard.size

    fun drainEvents(): List<GameEvent> = s.events.toList().also { s.events.clear() }

    private fun emit(e: GameEvent) { s.events += e }

    // ------------------------------------------------------------ queries

    fun actor(): Int? = when (val p = s.phase) {
        is Phase.Turn -> p.player
        is Phase.Respond -> p.player
        is Phase.Give -> p.player
        is Phase.Call -> p.player
        is Phase.Over -> null
    }

    fun player(id: Int) = s.players[id]

    fun nextActive(from: Int, dir: Direction = s.direction): Int {
        val n = s.players.size
        var p = from
        repeat(n) {
            p = if (dir == Direction.CW) (p + 1) % n else (p - 1 + n) % n
            if (!s.players[p].eliminated) return p
        }
        return from
    }

    fun handCard(player: Int, id: String): Card? = s.players[player].hand.firstOrNull { it.id == id }

    private fun cardsOf(player: Int, ids: List<String>): List<Card>? {
        if (ids.toSet().size != ids.size) return null
        val out = ids.map { handCard(player, it) ?: return null }
        return out
    }

    enum class SetKind { SINGLE, PAIR, TRIPLE, QUAD, STRAIGHT, INVALID }

    fun classify(cards: List<Card>): SetKind {
        if (cards.isEmpty()) return SetKind.INVALID
        if (cards.size == 1) return SetKind.SINGLE
        if (cards.any { !it.isNumber }) return SetKind.INVALID
        val vals = cards.map { it.value!! }
        if (vals.toSet().size == 1) return when (cards.size) {
            2 -> SetKind.PAIR; 3 -> SetKind.TRIPLE; 4 -> SetKind.QUAD; else -> SetKind.INVALID
        }
        if (cards.size >= 3) {
            val sorted = vals.sorted()
            // 9 dan 0 tidak dianggap berurutan (tanpa wrap)
            if (sorted.zipWithNext().all { (a, b) -> b == a + 1 }) return SetKind.STRAIGHT
        }
        return SetKind.INVALID
    }

    private fun penaltyOf(kind: SetKind) = when (kind) {
        SetKind.PAIR -> 1; SetKind.TRIPLE -> 3; SetKind.QUAD -> 4; else -> 0
    }

    /** Urutan kartu dalam set: STRAIGHT otomatis naik; kartu terakhir = kartu teratas di Discard. */
    private fun ordered(cards: List<Card>, kind: SetKind) =
        if (kind == SetKind.STRAIGHT) cards.sortedBy { it.value } else cards

    private fun lockColor(): CardColor? = s.lock?.color

    /** Validasi pilihan kartu pada giliran normal. Null = legal. */
    fun validatePlay(a: Action.Play): String? {
        val ph = s.phase
        if (ph !is Phase.Turn || ph.player != a.player) return "Bukan giliranmu"
        val cards = cardsOf(a.player, a.cardIds) ?: return "Kartu tidak ada di tangan"
        s.drawnCardId?.let { if (a.cardIds != listOf(it)) return "Hanya kartu yang baru diambil yang boleh dimainkan" }
        val kind = classify(cards)
        if (kind == SetKind.INVALID) return "Kombinasi tidak valid"
        val c = cards.first()
        if (kind == SetKind.SINGLE && !c.isNumber) return validateAction(c, a)
        // Set angka: single / PAIR / TRIPLE / QUAD / STRAIGHT
        val seq = ordered(cards, kind)
        val topCard = seq.last()
        val lc = lockColor()
        if (lc != null) {
            if (topCard.color != lc) return "MONOPOLY aktif: hanya warna ${lc.label}"
            return null
        }
        val matches = seq.any { it.color == s.activeColor || (s.lastValue != null && it.value == s.lastValue) }
        return if (matches) null else "Warna atau angka tidak cocok"
    }

    private fun validateAction(c: Card, a: Action.Play): String? {
        if (s.lock != null) return "MONOPOLY aktif: hanya kartu angka warna terkunci"
        if (c.type.isDefense) return "Kartu pertahanan hanya dipakai sebagai respons"
        when (c.type) {
            CardType.WILD, CardType.MONOPOLY -> if (a.color == null) return "Pilih warna"
            CardType.DEBT, CardType.EXCHANGE, CardType.HIJACK -> {
                val t = a.target ?: return "Pilih target"
                if (t == a.player || t !in s.players.indices || s.players[t].eliminated) return "Target tidak valid"
                if (c.type == CardType.HIJACK && s.players[t].hand.isEmpty()) return "Target tidak punya kartu"
            }
            else -> {}
        }
        return null
    }

    // ------------------------------------------------------------ apply

    fun apply(a: Action): Result {
        val err = when (a) {
            is Action.Play -> validatePlay(a)
            is Action.Draw -> validateDraw(a)
            is Action.Pass -> if (s.phase == Phase.Turn(a.player) && s.drawnCardId != null) null else "Tidak bisa pass"
            is Action.Take -> if (s.phase == Phase.Respond(a.player)) null else "Tidak ada penalti"
            is Action.Chain -> validateChain(a)
            is Action.Defend -> validateDefend(a)
            is Action.Give -> validateGive(a)
            is Action.Declare -> if (s.phase is Phase.Call && (s.phase as Phase.Call).player == a.player) null else "Tidak ada pengumuman"
            is Action.CallTimeout -> if (s.phase is Phase.Call && (s.phase as Phase.Call).player == a.player) null else "Tidak ada pengumuman"
        }
        if (err != null) return Result.Illegal(err)
        when (a) {
            is Action.Play -> doPlay(a)
            is Action.Draw -> doDraw(a)
            is Action.Pass -> { s.drawnCardId = null; endTurn(a.player) }
            is Action.Take -> doTake(a.player)
            is Action.Chain -> doChain(a)
            is Action.Defend -> doDefend(a)
            is Action.Give -> doGive(a)
            is Action.Declare -> doDeclare(a.player)
            is Action.CallTimeout -> doTimeout(a.player)
        }
        invariant()
        return Result.Ok
    }

    private fun invariant() = check(totalCards() == Deck.SIZE) { "Card conservation dilanggar: ${totalCards()}" }

    private fun validateDraw(a: Action.Draw): String? {
        val ph = s.phase
        if (ph !is Phase.Turn || ph.player != a.player) return "Bukan giliranmu"
        if (s.drawnCardId != null) return "Sudah mengambil kartu"
        return null
    }

    // ------------------------------------------------------------- draw

    fun draw(p: Int, n: Int): List<Card> {
        val got = mutableListOf<Card>()
        repeat(n) {
            if (s.drawPile.isEmpty()) reshuffle()
            if (s.drawPile.isEmpty()) return got
            val c = s.drawPile.removeLast()
            s.players[p].hand += c
            got += c
        }
        return got
    }

    /** §18.7: discard (kecuali kartu teratas) dikocok menjadi Draw Pile. */
    private fun reshuffle() {
        if (s.discard.size <= 1) return
        val topCard = s.discard.removeLast()
        s.drawPile += s.discard.shuffled(rng)
        s.discard.clear()
        s.discard += topCard
        emit(GameEvent(EventType.RESHUFFLE, n = s.drawPile.size))
    }

    private fun doDraw(a: Action.Draw) {
        val p = a.player
        s.stats.turns++
        if (hasLegalPlay(p)) {
            val got = draw(p, 1)
            emit(GameEvent(EventType.DREW, p, n = got.size))
            val c = got.firstOrNull()
            if (c != null) {
                s.drawnCardId = c.id
                if (!candidatePlays(p).any { it.cardIds == listOf(c.id) }) s.drawnCardId = null
            }
            if (s.drawnCardId == null) { afterDraw(p); endTurn(p) }
            // else: pemain boleh langsung memainkannya atau Pass
        } else {
            // Tidak ada yang bisa dimainkan
            val n = if (s.lock != null) 2 else 1
            val got = draw(p, n)
            emit(GameEvent(if (n == 2) EventType.FORCED_DRAW else EventType.DREW, p, n = got.size))
            val c = got.lastOrNull()
            s.drawnCardId = null
            if (n == 1 && c != null && candidatePlays(p).any { it.cardIds == listOf(c.id) }) {
                s.drawnCardId = c.id
                return
            }
            afterDraw(p); endTurn(p)
        }
    }

    private fun afterDraw(p: Int) {
        s.players[p].stabCalled = false
        checkElimination()
    }

    // ------------------------------------------------------------- play

    private fun bump(key: String) { s.stats.effectCount[key] = (s.stats.effectCount[key] ?: 0) + 1 }

    private fun doPlay(a: Action.Play) {
        val p = a.player
        val cards = cardsOf(p, a.cardIds)!!
        val kind = classify(cards)
        val seq = ordered(cards, kind)
        s.stats.turns++
        s.drawnCardId = null
        s.lastActor = p
        moveToDiscard(p, seq)
        s.players[p].stabCalled = false
        emit(GameEvent(EventType.PLAYED, p, n = seq.size, card = seq.last().type, color = seq.last().color))

        if (kind == SetKind.SINGLE && !cards.first().isNumber) {
            resolveAction(p, cards.first(), a)
        } else {
            val top = seq.last()
            s.activeColor = top.color!!
            s.lastValue = top.value
            when (kind) {
                SetKind.PAIR, SetKind.TRIPLE, SetKind.QUAD -> {
                    bump(kind.name)
                    startPenalty(p, penaltyOf(kind), chainable = true, skip = false, source = null, label = kind.name, cards = seq)
                }
                SetKind.STRAIGHT -> {
                    bump("STRAIGHT")
                    s.direction = s.direction.flip()
                    emit(GameEvent(EventType.DIRECTION, p, color = s.activeColor))
                }
                else -> {}
            }
        }
        if (s.mode == Mode.LAST_STANDING && s.lsAnte > 0) {
            // Tekanan Last Standing: setiap kartu yang dimainkan membuat pemain menarik kartu (lihat docs/RULES_DECISIONS.md)
            val got = draw(p, s.lsAnte)
            emit(GameEvent(EventType.DREW, p, n = got.size))
        }
        finishPlay(p)
    }

    private fun moveToDiscard(p: Int, seq: List<Card>) {
        val hand = s.players[p].hand
        for (c in seq) { hand.remove(c); s.discard += c }
    }

    private fun startPenalty(attacker: Int, total: Int, chainable: Boolean, skip: Boolean, source: CardType?, label: String, cards: List<Card>, target: Int? = null) {
        val t = target ?: nextActive(attacker)
        val pen = Penalty(total, t, attacker, chainable, skip, source)
        pen.steps += ChainStep(attacker, label, total, cards)
        s.penalty = pen
        if (chainable) { s.stats.chains++; s.stats.longestChain = maxOf(s.stats.longestChain, total) }
        emit(GameEvent(EventType.PENALTY, attacker, t, total))
    }

    private fun resolveAction(p: Int, c: Card, a: Action.Play) {
        bump(c.type.name)
        when (c.type) {
            CardType.WILD -> { s.activeColor = a.color!!; emit(GameEvent(EventType.COLOR, p, color = a.color)) }
            CardType.MONOPOLY -> {
                s.activeColor = a.color!!
                s.lock = MonopolyLock(p, a.color)
                emit(GameEvent(EventType.MONOPOLY, p, color = a.color))
            }
            CardType.REVERSE -> { s.direction = s.direction.flip(); emit(GameEvent(EventType.DIRECTION, p)) }
            CardType.SKIP -> {
                val n = nextActive(p)
                s.players[n].skipTurns++
                emit(GameEvent(EventType.SKIPPED, p, n))
            }
            CardType.PLUS_TWO ->
                startPenalty(p, 2, chainable = true, skip = false, source = CardType.PLUS_TWO, label = "PLUS TWO", cards = listOf(c))
            CardType.DEBT ->
                startPenalty(p, 2, chainable = false, skip = true, source = CardType.DEBT, label = "DEBT", cards = listOf(c), target = a.target)
            CardType.CHAOS -> {
                s.direction = s.direction.flip()
                emit(GameEvent(EventType.DIRECTION, p))
                startPenalty(p, 3, chainable = false, skip = false, source = CardType.CHAOS, label = "CHAOS", cards = listOf(c))
            }
            CardType.TAX -> {
                for (o in s.players) if (o.id != p && !o.eliminated) draw(o.id, 1).also { o.stabCalled = false }
                emit(GameEvent(EventType.TAX, p))
            }
            CardType.PICKPOCKET -> {
                val v = nextActive(p)
                val vh = s.players[v].hand
                if (vh.isNotEmpty()) {
                    val stolen = vh.removeAt(rng.nextInt(vh.size))
                    s.players[p].hand += stolen
                    emit(GameEvent(EventType.STOLE, p, v, 1))
                }
            }
            CardType.EXCHANGE -> {
                val t = a.target!!
                val mine = s.players[p].hand.toList()
                val theirs = s.players[t].hand.toList()
                s.players[p].hand.clear(); s.players[p].hand += theirs
                s.players[t].hand.clear(); s.players[t].hand += mine
                s.players[p].stabCalled = false; s.players[t].stabCalled = false
                emit(GameEvent(EventType.SWAPPED, p, t))
            }
            CardType.HIJACK -> {
                val t = a.target!!
                val th = s.players[t].hand
                var taken = 0
                repeat(2) {
                    if (th.isNotEmpty()) { s.players[p].hand += th.removeAt(rng.nextInt(th.size)); taken++ }
                }
                emit(GameEvent(EventType.STOLE, p, t, taken))
                if (s.players[p].hand.isNotEmpty() && taken > 0) s.pendingGive = p to t
            }
            CardType.MUTINY -> {
                val order = s.players.filter { !it.eliminated }
                val picks = order.associate { it.id to (if (it.hand.isEmpty()) null else it.hand.removeAt(rng.nextInt(it.hand.size))) }
                for (o in order) {
                    val card = picks[o.id] ?: continue
                    s.players[nextActive(o.id)].hand += card
                }
                for (o in order) o.stabCalled = false
                emit(GameEvent(EventType.MUTINY, p))
            }
            else -> error("Kartu tidak bisa dimainkan: ${c.type}")
        }
    }

    // ------------------------------------------------- post-play pipeline

    /** §15: selesaikan efek → eliminasi → STAB!/BACKSTAB! → penalti / giliran berikutnya. */
    private fun finishPlay(p: Int) {
        s.pendingGive?.let { (from, to) ->
            s.phase = Phase.Give(from, to)
            return
        }
        checkElimination()
        if (s.phase is Phase.Over) return
        if (s.mode == Mode.FIRST_TO_ZERO && !s.players[p].eliminated) {
            val h = s.players[p].hand.size
            if (h == 0) { s.phase = Phase.Call(p, CallKind.BACKSTAB); return }
            if (h == 1 && !s.players[p].stabCalled) { s.phase = Phase.Call(p, CallKind.STAB); return }
        }
        proceed()
    }

    private fun proceed() {
        val pen = s.penalty
        if (pen != null) {
            if (s.players[pen.target].eliminated) s.penalty = null
            else { s.phase = Phase.Respond(pen.target); return }
        }
        endTurn(s.lastActor)
    }

    private fun endTurn(from: Int) {
        s.lastActor = from
        checkElimination()
        if (s.phase is Phase.Over) return
        s.drawnCardId = null
        val n = advance(from)
        s.phase = Phase.Turn(n)
        s.lastActor = n
        if (s.lock?.owner == n) s.lock = null
    }

    private fun advance(from: Int): Int {
        var p = from
        repeat(s.players.size * 6) {
            p = nextActive(p)
            val pl = s.players[p]
            if (pl.skipTurns > 0) { pl.skipTurns--; emit(GameEvent(EventType.SKIPPED, other = p)); return@repeat }
            return p
        }
        return nextActive(from)
    }

    private fun checkElimination() {
        if (s.mode == Mode.LAST_STANDING) {
            for (pl in s.players) {
                if (!pl.eliminated && pl.hand.size >= s.threshold) {
                    pl.eliminated = true
                    emit(GameEvent(EventType.ELIMINATED, pl.id, n = pl.hand.size))
                    s.discard.addAll(0, pl.hand) // kartu kembali ke zona discard (conservation)
                    pl.hand.clear()
                    if (s.lock?.owner == pl.id) s.lock = null
                }
            }
            val alive = s.active
            if (alive.size <= 1) {
                s.phase = Phase.Over(alive.firstOrNull()?.id)
                emit(GameEvent(EventType.WIN, alive.firstOrNull()?.id ?: -1))
            }
        }
    }

    // --------------------------------------------------- penalty response

    fun validateChain(a: Action.Chain): String? {
        val ph = s.phase
        val pen = s.penalty
        if (ph !is Phase.Respond || ph.player != a.player || pen == null) return "Tidak ada penalti"
        if (!pen.chainable) return "Penalti ini tidak bisa dilanjutkan"
        val cards = cardsOf(a.player, a.cardIds) ?: return "Kartu tidak ada di tangan"
        return if (chainValue(cards) > 0) null else "Bukan kartu penalti yang valid"
    }

    /** Nilai penalti dari set yang boleh melanjutkan rantai (PAIR+1, PLUS_TWO+2, TRIPLE+3, QUAD+4). */
    fun chainValue(cards: List<Card>): Int {
        if (cards.size == 1 && cards[0].type == CardType.PLUS_TWO) return 2
        if (cards.all { it.isNumber }) return penaltyOf(classify(cards))
        return 0
    }

    private fun doChain(a: Action.Chain) {
        val p = a.player
        val pen = s.penalty!!
        val cards = cardsOf(p, a.cardIds)!!
        val v = chainValue(cards)
        s.lastActor = p
        moveToDiscard(p, cards)
        s.players[p].stabCalled = false
        val c = cards.last()
        if (c.isNumber) { s.activeColor = c.color!!; s.lastValue = c.value }
        pen.total += v
        pen.target = nextActive(p)
        pen.steps += ChainStep(p, if (c.isNumber) classify(cards).name else "PLUS TWO", v, cards)
        s.stats.longestChain = maxOf(s.stats.longestChain, pen.total)
        emit(GameEvent(EventType.CHAIN, p, pen.target, pen.total))
        finishPlay(p)
    }

    private fun doTake(p: Int) {
        val pen = s.penalty!!
        val got = draw(p, pen.total)
        emit(GameEvent(EventType.DREW, p, n = got.size))
        if (pen.skipTarget) s.players[p].skipTurns++
        s.players[p].stabCalled = false
        s.penalty = null
        s.lastActor = p
        checkElimination()
        if (s.phase is Phase.Over) return
        endTurn(p)
    }

    fun validateDefend(a: Action.Defend): String? {
        val ph = s.phase
        val pen = s.penalty
        if (ph !is Phase.Respond || ph.player != a.player || pen == null) return "Tidak ada penalti"
        val c = handCard(a.player, a.cardId) ?: return "Kartu tidak ada di tangan"
        if (!c.type.isDefense) return "Bukan kartu pertahanan"
        if (pen.defenseUsed) return "Hanya satu respons pertahanan per rantai"
        if (c.type == CardType.BOOMERANG) {
            if (pen.originalAttacker == a.player || s.players[pen.originalAttacker].eliminated) return "Tidak ada penyerang untuk dikembalikan"
        }
        if (c.type == CardType.DEFLECT) {
            if (pen.deflectUsed) return "DEFLECT sudah dipakai"
            if (nextActive(a.player) == a.player) return "Tidak ada target pengalihan"
        }
        return null
    }

    private fun doDefend(a: Action.Defend) {
        val p = a.player
        val pen = s.penalty!!
        val c = handCard(p, a.cardId)!!
        s.lastActor = p
        moveToDiscard(p, listOf(c))
        s.players[p].stabCalled = false
        pen.defenseUsed = true
        s.stats.defenses++
        bump(c.type.name)
        when (c.type) {
            CardType.IMMUNITY -> {
                s.penalty = null
                emit(GameEvent(EventType.IMMUNITY, p, n = pen.total))
            }
            CardType.BOOMERANG -> {
                val atk = pen.originalAttacker
                emit(GameEvent(EventType.BOOMERANG, p, atk, pen.total))
                val got = draw(atk, pen.total)
                emit(GameEvent(EventType.DREW, atk, n = got.size))
                if (pen.skipTarget) s.players[atk].skipTurns++
                s.players[atk].stabCalled = false
                s.penalty = null
            }
            CardType.DEFLECT -> {
                pen.deflectUsed = true
                pen.target = nextActive(p)
                emit(GameEvent(EventType.DEFLECT, p, pen.target, pen.total))
            }
            else -> {}
        }
        finishPlay(p)
    }

    // ---------------------------------------------------------- hijack give

    private fun validateGive(a: Action.Give): String? {
        val ph = s.phase
        if (ph !is Phase.Give || ph.player != a.player) return "Bukan giliranmu memberi kartu"
        return if (handCard(a.player, a.cardId) == null) "Kartu tidak ada" else null
    }

    private fun doGive(a: Action.Give) {
        val ph = s.phase as Phase.Give
        val c = handCard(a.player, a.cardId)!!
        s.players[a.player].hand.remove(c)
        s.players[ph.target].hand += c
        s.players[ph.target].stabCalled = false
        s.pendingGive = null
        finishPlay(a.player)
    }

    // ------------------------------------------------------------- calls

    private fun doDeclare(p: Int) {
        val ph = s.phase as Phase.Call
        if (ph.kind == CallKind.BACKSTAB) {
            emit(GameEvent(EventType.CALL_BACKSTAB, p))
            s.phase = Phase.Over(p)   // §18.6: kemenangan berlaku segera setelah tervalidasi
            emit(GameEvent(EventType.WIN, p))
        } else {
            s.players[p].stabCalled = true
            emit(GameEvent(EventType.CALL_STAB, p))
            proceed()
        }
    }

    private fun doTimeout(p: Int) {
        val got = draw(p, 2)
        s.players[p].stabCalled = false
        emit(GameEvent(EventType.CALL_FAILED, p, n = got.size))
        checkElimination()
        if (s.phase is Phase.Over) return
        proceed()
    }

    // --------------------------------------------------- move generation

    fun hasLegalPlay(p: Int) = candidatePlays(p).isNotEmpty()

    /** Semua aksi Play legal untuk pemain (dipakai UI untuk menandai kartu & oleh bot). */
    fun candidatePlays(p: Int): List<Action.Play> {
        if (s.phase != Phase.Turn(p)) return emptyList()
        val hand = s.players[p].hand
        val out = mutableListOf<Action.Play>()
        fun tryAdd(a: Action.Play) { if (validatePlay(a) == null) out += a }
        val others = s.players.filter { it.id != p && !it.eliminated }.map { it.id }
        val nums = hand.filter { it.isNumber }
        // single + PAIR/TRIPLE/QUAD
        for ((_, group) in nums.groupBy { it.value }) {
            for (k in 1..minOf(4, group.size)) {
                for (combo in group.combinations(k)) tryAdd(Action.Play(p, combo.map { it.id }))
            }
        }
        // STRAIGHT: semua run ≥3 dari nilai berurutan; pilih satu kartu per nilai (variasi warna terbatas)
        val byVal = nums.groupBy { it.value!! }
        for (start in 0..7) for (end in start + 2..9) {
            if ((start..end).all { it in byVal }) {
                val variants = mutableListOf<List<Card>>()
                variants += (start..end).map { byVal[it]!!.first() }
                variants += (start..end).map { byVal[it]!!.last() }
                for (colorPick in CardColor.entries) {
                    val v = (start..end).map { x -> byVal[x]!!.firstOrNull { it.color == colorPick } ?: byVal[x]!!.first() }
                    variants += v
                }
                for (v in variants.distinctBy { it.map { c -> c.id } }) tryAdd(Action.Play(p, v.map { it.id }))
            }
        }
        // Aksi tunggal
        for (c in hand.filter { !it.isNumber }.distinctBy { it.type }) {
            when (c.type) {
                CardType.WILD, CardType.MONOPOLY -> for (col in CardColor.entries) tryAdd(Action.Play(p, listOf(c.id), color = col))
                CardType.DEBT, CardType.EXCHANGE, CardType.HIJACK -> for (t in others) tryAdd(Action.Play(p, listOf(c.id), target = t))
                else -> tryAdd(Action.Play(p, listOf(c.id)))
            }
        }
        s.drawnCardId?.let { id -> return out.filter { it.cardIds == listOf(id) } }
        return out
    }

    /** Opsi melanjutkan rantai untuk pemain yang sedang merespons. */
    fun chainOptions(p: Int): List<List<String>> {
        val pen = s.penalty ?: return emptyList()
        if (s.phase != Phase.Respond(p) || !pen.chainable) return emptyList()
        val hand = s.players[p].hand
        val out = mutableListOf<List<String>>()
        hand.firstOrNull { it.type == CardType.PLUS_TWO }?.let { out.add(listOf(it.id)) }
        for ((_, g) in hand.filter { it.isNumber }.groupBy { it.value }) {
            for (k in 2..minOf(4, g.size)) out += g.take(k).map { it.id }
        }
        return out
    }

    fun defenseOptions(p: Int): List<Card> =
        s.players[p].hand.filter { it.type.isDefense && validateDefend(Action.Defend(p, it.id)) == null }
            .distinctBy { it.type }

    private fun <T> List<T>.combinations(k: Int): List<List<T>> {
        if (k == 0) return listOf(emptyList())
        if (size < k) return emptyList()
        if (k == 1) return map { listOf(it) }
        val res = mutableListOf<List<T>>()
        for (i in 0..size - k) for (rest in drop(i + 1).combinations(k - 1)) res += listOf(this[i]) + rest
        return res
    }
}
