package com.backstab.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.backstab.game.data.Feedback
import com.backstab.game.engine.Card
import com.backstab.game.engine.CardColor

enum class BtnKind(val top: Color, val bottom: Color, val lip: Color, val text: Color) {
    PRIMARY(Color(0xFFE23B3B), Color(0xFFB91C1C), Color(0xFF6E1010), Color.White),
    CONTROL(Color(0xFF3F74F5), Color(0xFF1D4ED8), Color(0xFF102C80), Color.White),
    GOLD(Color(0xFFFFC857), Color(0xFFE59A0A), Color(0xFF8A5806), Color(0xFF1A1206)),
    DEFENSE(Color(0xFF14B084), Color(0xFF047857), Color(0xFF024634), Color.White),
    GRAY(Color(0xFF5A6275), Color(0xFF3A4152), Color(0xFF1F2430), Color(0xFFE5E9F5)),
    DISABLED(Color(0xFF4B5262), Color(0xFF353B4A), Color(0xFF212633), Color(0xFF8A93A8)),
}

@Composable
fun BText(
    text: String,
    size: Number,
    color: Color = Color.White,
    modifier: Modifier = Modifier,
    family: FontFamily = Inter,
    weight: FontWeight = FontWeight.Bold,
    align: TextAlign = TextAlign.Center,
    spacing: Number = 0,
    maxLines: Int = 1,
) {
    Text(
        text, modifier, color = color, fontSize = rs(size), fontFamily = family, fontWeight = weight,
        textAlign = align, maxLines = maxLines, letterSpacing = rs(spacing),
        lineHeight = rs(size.toFloat() * 1.25f),
    )
}

@Composable
fun Icon(name: String, size: Number, tint: Color = Color.White, modifier: Modifier = Modifier) {
    val bmp = rememberAsset("icons/$name.png") ?: return
    Image(bmp, null, modifier.size(r(size)), colorFilter = ColorFilter.tint(tint))
}

@Composable
fun BsButton(
    text: String,
    kind: BtnKind,
    modifier: Modifier,
    enabled: Boolean = true,
    icon: String? = null,
    fontSize: Number = 38,
    onClick: () -> Unit,
) {
    val k = if (enabled) kind else BtnKind.DISABLED
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val lip = r(9)
    val shape = RoundedCornerShape(r(26))
    Box(
        modifier.clickable(enabled = enabled, interactionSource = src, indication = null) {
            Feedback.tap(); onClick()
        },
    ) {
        Box(Modifier.fillMaxSize().padding(top = lip).background(k.lip, shape))
        Box(
            Modifier
                .fillMaxSize()
                .padding(bottom = lip)
                .offset(y = if (pressed) lip / 2 else 0.dp)
                .background(Brush.verticalGradient(listOf(k.top, k.bottom)), shape)
                .border(r(2), Color.White.copy(alpha = 0.18f), shape),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) { Icon(icon, fontSize.toFloat() * 0.95f, k.text); Spacer(Modifier.width(r(14))) }
                BText(text, fontSize, k.text, family = Poppins, spacing = 1.5f)
            }
        }
    }
}

@Composable
fun Pill(
    text: String,
    modifier: Modifier,
    fill: Color = Color(0xFF141B30),
    stroke: Color = BS.SurfaceStroke,
    color: Color = Color.White,
    size: Number = 30,
    icon: String? = null,
    family: FontFamily = Inter,
) {
    val shape = RoundedCornerShape(percent = 50)
    Box(modifier.background(fill, shape).border(r(2), stroke, shape), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) { Icon(icon, size.toFloat() * 1.05f, color); Spacer(Modifier.width(r(10))) }
            BText(text, size, color, family = family, spacing = 1)
        }
    }
}

fun avatarColor(id: Int): Pair<Color, Color> = when (id) {
    0 -> Color(0xFFFFB92E) to Color(0xFFC77A00)
    1 -> Color(0xFFE23B3B) to Color(0xFF8E1717)
    2 -> Color(0xFF3F74F5) to Color(0xFF1B3FA8)
    3 -> Color(0xFFD97A12) to Color(0xFF8A4A06)
    4 -> Color(0xFF14B084) to Color(0xFF065F46)
    else -> Color(0xFF9B6BFF) to Color(0xFF4C2A9E)
}

@Composable
fun Avatar(id: Int, name: String, size: Number, modifier: Modifier = Modifier, glow: Boolean = false, dim: Boolean = false) {
    val (a, b) = avatarColor(id)
    val ring = if (glow) BS.GoldLight else Color.White.copy(alpha = 0.55f)
    Box(
        modifier
            .size(r(size))
            .alpha(if (dim) 0.4f else 1f)
            .drawBehind {
                if (glow) drawCircle(Brush.radialGradient(listOf(BS.Gold.copy(alpha = 0.55f), Color.Transparent)), radius = this.size.minDimension * 0.95f)
            }
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(a, b)))
            .border(r(5), ring, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        BText(name.take(1).uppercase(), size.toFloat() * 0.5f, Color.White, family = Poppins)
    }
}

/** Kartu (muka atau punggung) sebagai gambar 2D dengan rasio asli 480:728. */
@Composable
fun CardImage(card: Card?, modifier: Modifier, faceDown: Boolean = false, dim: Boolean = false) {
    val bmp = rememberCardImage(if (faceDown) null else card)
    if (bmp != null) {
        Image(
            bmp, card?.type?.name, modifier, contentScale = ContentScale.FillBounds,
            colorFilter = if (dim) ColorFilter.tint(Color(0x99000814), androidx.compose.ui.graphics.BlendMode.SrcAtop) else null,
        )
    } else {
        Box(modifier.background(Color(0xFF222A44), RoundedCornerShape(r(14))))
    }
}

const val CARD_ASPECT = 480f / 728f

/** Penanda warna dengan bentuk (aksesibel untuk buta warna): ◆ ● ▲ ■ */
@Composable
fun ColorGlyph(color: CardColor, size: Number, modifier: Modifier = Modifier, tint: Color = Color.White) {
    Canvas(modifier.size(r(size))) {
        val w = this.size.width
        val p = Path()
        when (color) {
            CardColor.CRIMSON -> { p.moveTo(w / 2, 0f); p.lineTo(w, w / 2); p.lineTo(w / 2, w); p.lineTo(0f, w / 2); p.close(); drawPath(p, tint) }
            CardColor.AZURE -> drawCircle(tint, w / 2.1f)
            CardColor.EMERALD -> { p.moveTo(w / 2, w * 0.05f); p.lineTo(w * 0.97f, w * 0.92f); p.lineTo(w * 0.03f, w * 0.92f); p.close(); drawPath(p, tint) }
            CardColor.AMBER -> drawRect(tint, Offset(w * 0.08f, w * 0.08f), Size(w * 0.84f, w * 0.84f))
        }
    }
}

@Composable
fun ColorDot(color: CardColor, size: Number) {
    Box(
        Modifier.size(r(size)).background(Brush.verticalGradient(listOf(BS.cardColor(color), BS.cardColor(color).copy(alpha = 0.7f))), CircleShape)
            .border(r(3), Color.White.copy(alpha = 0.35f), CircleShape),
    )
}

/** Panel gelap bergaris tepi (kartu info / dialog). */
@Composable
fun Panel(modifier: Modifier, stroke: Color = BS.SurfaceStroke, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(r(30))
    Box(
        modifier.background(Brush.verticalGradient(listOf(Color(0xFF141B33), Color(0xFF0B1022))), shape).border(r(3), stroke, shape),
    ) { content() }
}

@Composable
fun IconCircleButton(icon: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFF2A3560), Color(0xFF1A2244))))
            .border(r(3), BS.SurfaceStroke, CircleShape)
            .clickable { Feedback.tap(); onClick() },
        contentAlignment = Alignment.Center,
    ) { Icon(icon, 40) }
}

@Composable
fun Scrim(onTap: (() -> Unit)? = null, content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Color(0xB3030610))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onTap?.invoke() },
        content = content,
    )
}
