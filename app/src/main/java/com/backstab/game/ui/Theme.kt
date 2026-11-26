package com.backstab.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.backstab.game.R
import com.backstab.game.engine.CardColor

/** Palet PRD §12.7 + turunan UI dari referensi. */
object BS {
    val Crimson = Color(0xFFB91C1C)
    val Azure = Color(0xFF1D4ED8)
    val Emerald = Color(0xFF047857)
    val Amber = Color(0xFFB45309)
    val Black = Color(0xFF111827)
    val Gold = Color(0xFFF59E0B)
    val GoldLight = Color(0xFFFFC857)
    val Surface = Color(0xFF0E1424)
    val SurfaceStroke = Color(0xFF2D3858)
    val Felt = Color(0xFF16604F)
    val FeltDark = Color(0xFF082A24)
    val Text = Color(0xFFFFFFFF)
    val TextDim = Color(0xFFA9B2C9)
    val BgTop = Color(0xFF12192E)
    val BgBottom = Color(0xFF070A14)

    fun cardColor(c: CardColor) = when (c) {
        CardColor.CRIMSON -> Color(0xFFD42A2A)
        CardColor.AZURE -> Color(0xFF2F63F0)
        CardColor.EMERALD -> Color(0xFF0E9F74)
        CardColor.AMBER -> Color(0xFFD97A12)
    }
}

val Poppins = FontFamily(Font(R.font.poppins_bold, FontWeight.Bold), Font(R.font.poppins_bold, FontWeight.Normal))
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

/** Satuan layout: dp per piksel referensi (referensi 2400×1080, PRD/desain). */
val LocalU = compositionLocalOf { 1f }

@Composable @ReadOnlyComposable
fun r(v: Number): Dp = (v.toFloat() * LocalU.current).dp

@Composable @ReadOnlyComposable
fun rs(v: Number): TextUnit = (v.toFloat() * LocalU.current).sp

@Composable
fun Modifier.at(x: Number, y: Number, w: Number, h: Number): Modifier {
    val u = LocalU.current
    return this.offset((x.toFloat() * u).dp, (y.toFloat() * u).dp).size((w.toFloat() * u).dp, (h.toFloat() * u).dp)
}

/** Posisi berdasarkan titik tengah. */
@Composable
fun Modifier.atC(cx: Number, cy: Number, w: Number, h: Number): Modifier =
    at(cx.toFloat() - w.toFloat() / 2, cy.toFloat() - h.toFloat() / 2, w, h)

/** Kanvas 2400×1080 yang diskalakan seragam; latar mengisi seluruh layar. */
@Composable
fun Stage(modifier: Modifier = Modifier, background: Boolean = true, content: @Composable BoxScope.() -> Unit) {
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .let { if (background) it.background(Brush.verticalGradient(listOf(BS.BgTop, BS.BgBottom))) else it },
    ) {
        val u = minOf(maxWidth.value / 2400f, maxHeight.value / 1080f)
        CompositionLocalProvider(LocalU provides u) {
            if (background) BackgroundPattern()
            Box(Modifier.align(Alignment.Center).size((2400 * u).dp, (1080 * u).dp), content = content)
        }
    }
}

@Composable
private fun BackgroundPattern() {
    Canvas(Modifier.fillMaxSize()) {
        val step = 64.dp.toPx()
        val c = Color(0x0AFFFFFF)
        var x = -size.height
        while (x < size.width) {
            drawLine(c, Offset(x, 0f), Offset(x + size.height, size.height), strokeWidth = 1.dp.toPx())
            drawLine(c, Offset(x + size.height, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
            x += step
        }
    }
}
