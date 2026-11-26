package com.backstab.game

import com.backstab.game.engine.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class EngineTest {
    private fun num(c: CardColor, v: Int, copy: String = "a") =
        Card("${c.code}_${v}_$copy", CardType.NUMBER, c, v, "x")
    private fun act(t: CardType, i: Int = 1) = Card("${t.name}_$i", t, null, null, "x")

    /** Engine 3 pemain dengan tangan rakitan; sisa deck dipindah ke draw pile (conservation tetap 120). */
    private fun rigged(mode: Mode = Mode.FIRST_TO_ZERO, n: Int = 3, vararg hands: List<Card>): GameEngine {
        val e = GameEngine.newGame(mode, n, seed = 7)
        val s = e.s
        val all = (s.drawPile + s.discard + s.players.flatMap { it.hand }).toMutableList()
        s.drawPile.clear(); s.discard.clear(); s.players.forEach { it.hand.clear() }
        hands.forEachIndexed { i, h -> h.forEach { c -> all.removeAll { it.id == c.id }; s.players[i].hand += c } }
        val start = num(CardColor.CRIMSON, 5, "z")
        all.removeAll { it.id == start.id }
        // kartu 'z' tambahan tidak ada di deck asli: ganti kartu CR_5_b sebagai kartu awal
        val startReal = all.first { it.id == "CR_5_b" || it.id == "CR_5_a" }
        all.remove(startReal)
        s.discard += startReal
        s.drawPile += all
        s.activeColor = CardColor.CRIMSON; s.lastValue = 5
        s.phase = Phase.Turn(0); s.lastActor = 0
        assertEquals(120, e.totalCards())
        return e
    }

    @Test fun deckIs120() {
        val d = Deck.build()
        assertEquals(120, d.size)
        assertEquals(80, d.count { it.isNumber })
        assertEquals(40, d.count { !it.isNumber })
        assertEquals(120, d.map { it.id }.toSet().size)
    }

    @Test fun setupIsValid() {
        val e = GameEngine.newGame(Mode.FIRST_TO_ZERO, 5, seed = 1)
        assertTrue(e.s.players.all { it.hand.size == 7 })
        assertEquals(CardType.NUMBER, e.s.top.type)
        assertEquals(Direction.CW, e.s.direction)
        assertEquals(120, e.totalCards())
    }

    @Test fun pairStartsPlusOneChain() {
        val e = rigged(hands = arrayOf(listOf(num(CardColor.CRIMSON, 7), num(CardColor.AZURE, 7), num(CardColor.AMBER, 1), num(CardColor.AMBER, 2)), listOf(num(CardColor.EMERALD, 2)), listOf(num(CardColor.EMERALD, 3))))
        assertEquals(Result.Ok, e.apply(Action.Play(0, listOf("CR_7_a", "AZ_7_a"))))
        assertEquals(Phase.Respond(1), e.s.phase)
        assertEquals(1, e.s.penalty!!.total)
        assertEquals(CardColor.AZURE, e.s.activeColor)
    }

    @Test fun immunityCancelsWholeChain() {
        val e = rigged(hands = arrayOf(
            listOf(num(CardColor.CRIMSON, 7), num(CardColor.AZURE, 7), num(CardColor.AMBER, 1), num(CardColor.AMBER, 2)),
            listOf(act(CardType.PLUS_TWO), num(CardColor.EMERALD, 2), num(CardColor.EMERALD, 4)),
            listOf(act(CardType.IMMUNITY), num(CardColor.EMERALD, 3), num(CardColor.EMERALD, 6))))
        e.apply(Action.Play(0, listOf("CR_7_a", "AZ_7_a")))
        assertEquals(Result.Ok, e.apply(Action.Chain(1, listOf("PLUS_TWO_1"))))
        assertEquals(3, e.s.penalty!!.total)
        val before = e.player(2).hand.size
        assertEquals(Result.Ok, e.apply(Action.Defend(2, "IMMUNITY_1")))
        assertNull(e.s.penalty)
        assertEquals(before - 1, e.player(2).hand.size)
        assertEquals(Phase.Turn(0), e.s.phase)
    }

    @Test fun boomerangReturnsToOriginalAttackerAndDeflectBlocked() {
        val e = rigged(hands = arrayOf(
            listOf(num(CardColor.CRIMSON, 7), num(CardColor.AZURE, 7), num(CardColor.AMBER, 1), num(CardColor.AMBER, 2)),
            listOf(act(CardType.BOOMERANG), num(CardColor.EMERALD, 2)),
            listOf(num(CardColor.EMERALD, 3))))
        e.apply(Action.Play(0, listOf("CR_7_a", "AZ_7_a")))
        val atk = e.player(0).hand.size
        assertEquals(Result.Ok, e.apply(Action.Defend(1, "BOOMERANG_1")))
        assertEquals(atk + 1, e.player(0).hand.size)
        assertNull(e.s.penalty)
    }

    @Test fun deflectOnlyOncePerChain() {
        val e = rigged(hands = arrayOf(
            listOf(num(CardColor.CRIMSON, 7), num(CardColor.AZURE, 7), num(CardColor.AMBER, 1), num(CardColor.AMBER, 2)),
            listOf(act(CardType.DEFLECT, 1), num(CardColor.EMERALD, 2), num(CardColor.EMERALD, 4)),
            listOf(act(CardType.DEFLECT, 2), num(CardColor.EMERALD, 3), num(CardColor.EMERALD, 6))))
        e.apply(Action.Play(0, listOf("CR_7_a", "AZ_7_a")))
        assertEquals(Result.Ok, e.apply(Action.Defend(1, "DEFLECT_1")))
        assertEquals(Phase.Respond(2), e.s.phase)
        assertTrue(e.apply(Action.Defend(2, "DEFLECT_2")) is Result.Illegal)
    }

    @Test fun defenseCardCannotBePlayedOnTurn() {
        val e = rigged(hands = arrayOf(listOf(act(CardType.IMMUNITY), num(CardColor.CRIMSON, 1)), listOf(num(CardColor.AZURE, 1)), listOf(num(CardColor.AZURE, 2))))
        assertTrue(e.apply(Action.Play(0, listOf("IMMUNITY_1"))) is Result.Illegal)
    }

    @Test fun illegalActionDoesNotChangeState() {
        val e = rigged(hands = arrayOf(listOf(num(CardColor.AZURE, 1), num(CardColor.AZURE, 2)), listOf(num(CardColor.AZURE, 1, "b")), listOf(num(CardColor.AZURE, 3))))
        val snap = e.s.players.map { it.hand.size }
        assertTrue(e.apply(Action.Play(0, listOf("AZ_1_a"))) is Result.Illegal)
        assertEquals(snap, e.s.players.map { it.hand.size })
        assertEquals(Phase.Turn(0), e.s.phase)
    }

    @Test fun stabAndBackstabFlow() {
        val e = rigged(hands = arrayOf(listOf(num(CardColor.CRIMSON, 1), num(CardColor.CRIMSON, 2)), listOf(num(CardColor.AZURE, 1)), listOf(num(CardColor.AZURE, 3))))
        e.apply(Action.Play(0, listOf("CR_1_a")))
        assertEquals(Phase.Call(0, CallKind.STAB), e.s.phase)
        e.apply(Action.Declare(0))
        assertEquals(Phase.Turn(1), e.s.phase)
        // Gagal menyebut STAB! -> ambil 2 kartu
        val e2 = rigged(hands = arrayOf(listOf(num(CardColor.CRIMSON, 1), num(CardColor.CRIMSON, 2)), listOf(num(CardColor.AZURE, 1)), listOf(num(CardColor.AZURE, 3))))
        e2.apply(Action.Play(0, listOf("CR_1_a")))
        e2.apply(Action.CallTimeout(0))
        assertEquals(3, e2.player(0).hand.size)
        // BACKSTAB!
        val e3 = rigged(hands = arrayOf(listOf(num(CardColor.CRIMSON, 1)), listOf(num(CardColor.AZURE, 1)), listOf(num(CardColor.AZURE, 3))))
        e3.apply(Action.Play(0, listOf("CR_1_a")))
        assertEquals(Phase.Call(0, CallKind.BACKSTAB), e3.s.phase)
        e3.apply(Action.Declare(0))
        assertEquals(Phase.Over(0), e3.s.phase)
    }

    @Test fun lastStandingEliminatesAtThreshold() {
        val big = (0..9).map { num(CardColor.AZURE, it) } + (0..3).map { num(CardColor.EMERALD, it) } // 14 kartu
        val e = rigged(Mode.LAST_STANDING, 2, big, listOf(act(CardType.TAX), num(CardColor.AMBER, 1)))
        e.s.lsAnte = 0
        e.s.phase = Phase.Turn(1); e.s.lastActor = 1
        e.apply(Action.Play(1, listOf("TAX_1")))
        assertTrue(e.player(0).eliminated)
        assertEquals(Phase.Over(1), e.s.phase)
        assertEquals(120, e.totalCards())
    }

    @Test fun botSimulationAlwaysFinishesAndConservesCards() {
        for (mode in Mode.entries) for (n in 2..6) for (seed in 1L..25L) {
            val e = GameEngine.newGame(mode, n, seed = seed * 13 + n, humanSeat = false)
            val bot = Bot(Random(seed))
            var steps = 0
            while (e.s.phase !is Phase.Over) {
                val r = e.apply(bot.decide(e, e.actor()!!))
                assertTrue("aksi bot ilegal: $r", r is Result.Ok)
                assertEquals(120, e.totalCards())
                assertTrue("game tidak selesai ($mode,$n,$seed)", ++steps < 4000)
            }
        }
    }
}
