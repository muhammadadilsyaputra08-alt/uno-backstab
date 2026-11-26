package com.backstab.game.engine

/** Deck tetap 120 kartu: 80 angka + 40 aksi (PRD §4). ID & aset sama dengan card_manifest.json. */
object Deck {
    const val SIZE = 120

    fun build(): List<Card> {
        val cards = mutableListOf<Card>()
        for (color in CardColor.entries) for (v in 0..9) for (copy in listOf("a", "b")) {
            cards += Card("${color.code}_${v}_$copy", CardType.NUMBER, color, v, "cards/png/num_${color.code}_$v.png")
        }
        for (type in CardType.entries) {
            if (type == CardType.NUMBER) continue
            for (i in 1..type.qty) cards += Card("${type.name}_$i", type, null, null, "cards/png/act_${type.name}.png")
        }
        check(cards.size == SIZE) { "Deck harus 120 kartu, ditemukan ${cards.size}" }
        return cards
    }

    const val BACK_ASSET = "cards/png/back.png"
}
