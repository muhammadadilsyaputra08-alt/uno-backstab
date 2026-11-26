package com.backstab.game.engine

import kotlin.random.Random

/** Bot heuristik sederhana. Selalu mengembalikan aksi legal untuk fase saat ini. */
class Bot(private val rng: Random) {

    fun decide(e: GameEngine, p: Int): Action {
        val s = e.s
        return when (val ph = s.phase) {
            is Phase.Call -> Action.Declare(p)
            is Phase.Give -> Action.Give(p, giveAway(e, p))
            is Phase.Respond -> respond(e, p)
            is Phase.Turn -> turn(e, p)
            is Phase.Over -> error("Game selesai")
        }
    }

    private fun giveAway(e: GameEngine, p: Int): String {
        // Beri kartu aksi paling lemah / angka tertinggi yang tidak cocok warna terbanyak
        val hand = e.player(p).hand
        val colorCount = hand.filter { it.isNumber }.groupingBy { it.color }.eachCount()
        return hand.minByOrNull { c ->
            when {
                c.isNumber -> (colorCount[c.color] ?: 0) * 2 + (c.value ?: 0) * 0.1
                else -> 20.0 + c.powerLevel
            }
        }!!.id
    }

    private fun respond(e: GameEngine, p: Int): Action {
        val pen = e.s.penalty!!
        val defs = e.defenseOptions(p)
        val big = pen.total >= 3
        defs.firstOrNull { it.type == CardType.IMMUNITY }?.takeIf { big || rng.nextInt(4) == 0 }?.let { return Action.Defend(p, it.id) }
        defs.firstOrNull { it.type == CardType.BOOMERANG }?.takeIf { big || rng.nextInt(3) == 0 }?.let { return Action.Defend(p, it.id) }
        val chains = e.chainOptions(p)
        if (chains.isNotEmpty() && pen.total < 12) return Action.Chain(p, chains.minByOrNull { it.size }!!)
        defs.firstOrNull { it.type == CardType.DEFLECT }?.let { return Action.Defend(p, it.id) }
        defs.firstOrNull()?.takeIf { pen.total >= 6 }?.let { return Action.Defend(p, it.id) }
        return Action.Take(p)
    }

    private fun turn(e: GameEngine, p: Int): Action {
        val plays = e.candidatePlays(p)
        if (plays.isEmpty()) return if (e.s.drawnCardId != null) Action.Pass(p) else Action.Draw(p)
        val hand = e.player(p).hand
        val best = plays.maxByOrNull { score(e, p, hand, it) + rng.nextDouble() * 2 }!!
        // Jangan selalu menghabiskan kartu: kadang mengambil kartu bila hanya ada aksi mubazir
        return if (score(e, p, hand, best) < -5 && e.s.drawnCardId == null) Action.Draw(p) else best
    }

    private fun score(e: GameEngine, p: Int, hand: List<Card>, a: Action.Play): Double {
        val cards = a.cardIds.map { id -> hand.first { it.id == id } }
        var sc = cards.size * 10.0
        val c = cards.first()
        val pl = e.s.players
        val foes = pl.filter { it.id != p && !it.eliminated }
        val minFoe = foes.minOf { it.hand.size }
        val ftz = e.s.mode == Mode.FIRST_TO_ZERO
        val nextHand = pl[e.nextActive(p)].hand.size
        if (hand.size - cards.size == 0 && ftz) sc += 100
        if (c.isNumber) {
            val kind = e.classify(cards)
            if (kind == GameEngine.SetKind.STRAIGHT) sc += 6
            if (kind in listOf(GameEngine.SetKind.PAIR, GameEngine.SetKind.TRIPLE, GameEngine.SetKind.QUAD)) sc += 8 + cards.size
            // pertahankan warna mayoritas
            val topColor = cards.last().color
            val majority = hand.filter { it.isNumber && it !in cards }.groupingBy { it.color }.eachCount()
            sc += (majority[topColor] ?: 0) * 1.5
            return sc
        }
        sc += when (c.type) {
            CardType.WILD -> if (hand.size <= 3) 4.0 else -8.0
            CardType.MONOPOLY -> 6.0 + (if (foes.size >= 3) 3 else 0)
            CardType.TAX -> 4.0 + foes.size * 1.5
            CardType.DEBT -> 6.0 + (if (pl[a.target!!].hand.size <= 2) 8 else 0)
            CardType.CHAOS -> 7.0 + (if (nextHand <= 2) 6 else 0)
            CardType.PLUS_TWO -> 5.0 + (if (nextHand <= 2) 5 else 0)
            CardType.SKIP -> 3.0 + (if (nextHand <= 2) 8 else 0)
            CardType.REVERSE -> 2.0 + (if (pl[e.nextActive(p, e.s.direction.flip())].hand.size > nextHand) 3 else 0)
            CardType.PICKPOCKET -> 3.0 + (if (nextHand <= 2) -2 else 3)
            CardType.EXCHANGE -> {
                val t = pl[a.target!!].hand.size
                (hand.size - 1 - t).toDouble() * 3 - 2
            }
            CardType.HIJACK -> if (hand.size >= 6) -3.0 else 1.0
            CardType.MUTINY -> 1.0
            else -> 0.0
        }
        if (c.type == CardType.EXCHANGE && hand.size - 1 <= pl[a.target!!].hand.size) sc -= 30
        if (ftz && c.type in listOf(CardType.DEBT, CardType.EXCHANGE) && pl[a.target!!].hand.size == minFoe) sc += 2
        return sc
    }
}
