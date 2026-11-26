package com.backstab.game.engine

/** Empat warna angka (PRD §4.2). Kartu aksi tidak berwarna. */
enum class CardColor(val code: String, val label: String) {
    CRIMSON("CR", "CRIMSON"),
    AZURE("AZ", "AZURE"),
    EMERALD("EM", "EMERALD"),
    AMBER("AM", "AMBER"),
}

enum class CardType(val power: Int, val qty: Int) {
    NUMBER(1, 80),
    PLUS_TWO(2, 2), TAX(2, 2), DEBT(2, 2), REVERSE(2, 2), SKIP(2, 2),
    DEFLECT(3, 4), PICKPOCKET(3, 4), EXCHANGE(3, 3), HIJACK(3, 3),
    IMMUNITY(4, 3), BOOMERANG(4, 3),
    CHAOS(5, 4), MONOPOLY(5, 2), MUTINY(5, 2), WILD(5, 2);

    val isDefense get() = this == IMMUNITY || this == DEFLECT || this == BOOMERANG
}

/** Data kartu murni (PRD §14.2) — dipisahkan dari logika efek. */
data class Card(
    val id: String,
    val type: CardType,
    val color: CardColor?,
    val value: Int?,
    val asset: String,
) {
    val powerLevel get() = type.power
    val isNumber get() = type == CardType.NUMBER
}

enum class Mode { FIRST_TO_ZERO, LAST_STANDING }
enum class Direction { CW, CCW;
    fun flip() = if (this == CW) CCW else CW
}

class Player(val id: Int, val name: String, val isHuman: Boolean) {
    val hand = mutableListOf<Card>()
    var eliminated = false
    var skipTurns = 0
    var stabCalled = false
}

enum class CallKind { STAB, BACKSTAB }

sealed interface Phase {
    data class Turn(val player: Int) : Phase
    data class Respond(val player: Int) : Phase
    data class Give(val player: Int, val target: Int) : Phase
    data class Call(val player: Int, val kind: CallKind) : Phase
    data class Over(val winner: Int?) : Phase
}

data class ChainStep(val player: Int, val label: String, val amount: Int, val cards: List<Card>)

/** Penalti aktif (PRD §14.3: penalty_total, penalty_target, original_attacker, deflect_used). */
class Penalty(
    var total: Int,
    var target: Int,
    val originalAttacker: Int,
    var chainable: Boolean,
    val skipTarget: Boolean,
    val source: CardType?,
) {
    var deflectUsed = false
    var defenseUsed = false
    val steps = mutableListOf<ChainStep>()
}

class MonopolyLock(val owner: Int, val color: CardColor)

enum class EventType {
    PLAYED, DREW, PENALTY, DIRECTION, COLOR, SKIPPED, STOLE, SWAPPED, MUTINY, TAX, ELIMINATED,
    CALL_STAB, CALL_BACKSTAB, CALL_FAILED, CHAIN, DEFENSE, DEFLECT, BOOMERANG, IMMUNITY, MONOPOLY,
    RESHUFFLE, WIN, FORCED_DRAW,
}

data class GameEvent(
    val type: EventType,
    val player: Int = -1,
    val other: Int = -1,
    val n: Int = 0,
    val card: CardType? = null,
    val color: CardColor? = null,
)

class GameStats {
    var turns = 0
    var chains = 0
    var longestChain = 0
    var defenses = 0
    val effectCount = mutableMapOf<String, Int>()
}

class GameState(val mode: Mode, val players: List<Player>, val threshold: Int) {
    val drawPile = mutableListOf<Card>()      // elemen terakhir = teratas
    val discard = mutableListOf<Card>()       // elemen terakhir = teratas
    var direction = Direction.CW
    var activeColor: CardColor = CardColor.CRIMSON
    var lastValue: Int? = null
    var phase: Phase = Phase.Turn(0)
    var penalty: Penalty? = null
    var lock: MonopolyLock? = null
    var lastActor = 0
    var drawnCardId: String? = null
    var lsAnte = 2   // kartu tambahan per giliran di Last Standing (parameter balancing)
    var pendingGive: Pair<Int, Int>? = null
    val stats = GameStats()
    val events = mutableListOf<GameEvent>()
    val top: Card get() = discard.last()
    val active get() = players.filter { !it.eliminated }
}

sealed interface Action {
    data class Play(val player: Int, val cardIds: List<String>, val color: CardColor? = null, val target: Int? = null) : Action
    data class Draw(val player: Int) : Action
    data class Pass(val player: Int) : Action
    data class Take(val player: Int) : Action
    data class Chain(val player: Int, val cardIds: List<String>) : Action
    data class Defend(val player: Int, val cardId: String) : Action
    data class Give(val player: Int, val cardId: String) : Action
    data class Declare(val player: Int) : Action
    data class CallTimeout(val player: Int) : Action
}

sealed interface Result {
    data object Ok : Result
    data class Illegal(val reason: String) : Result
}
