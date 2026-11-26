package com.backstab.game.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.backstab.game.engine.Card
import com.backstab.game.engine.Deck
import java.util.concurrent.ConcurrentHashMap

/** Cache bitmap aset (kartu & ikon) dari folder assets/. Kartu didekode setengah resolusi (240×364). */
object Assets {
    private val cache = ConcurrentHashMap<String, ImageBitmap>()

    fun load(ctx: Context, path: String): ImageBitmap? {
        cache[path]?.let { return it }
        return try {
            val opts = BitmapFactory.Options().apply { inSampleSize = if (path.startsWith("cards/")) 2 else 1 }
            ctx.assets.open(path).use { BitmapFactory.decodeStream(it, null, opts) }
                ?.asImageBitmap()?.also { cache[path] = it }
        } catch (e: java.io.IOException) {
            null
        }
    }

    /** Daftar seluruh aset yang dipramuat saat splash. */
    fun preloadList(ctx: Context): List<String> {
        val cards = Deck.build().map { it.asset }.distinct() + Deck.BACK_ASSET
        val icons = (ctx.assets.list("icons") ?: emptyArray()).map { "icons/$it" }
        return cards + icons
    }
}

@Composable
fun rememberAsset(path: String): ImageBitmap? {
    val ctx = LocalContext.current
    return Assets.load(ctx, path)
}

@Composable
fun rememberCardImage(card: Card?): ImageBitmap? = rememberAsset(card?.asset ?: Deck.BACK_ASSET)
