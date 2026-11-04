package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DebateLevel
import com.example.data.model.MascotState
import com.example.ui.theme.BambooGreen
import com.example.ui.theme.BambooSurface
import com.example.ui.theme.CoralPersimmon
import com.example.ui.theme.EspressoStroke
import com.example.ui.theme.FrauncesFamily
import com.example.ui.theme.HojaTheme
import com.example.ui.theme.OchreGold
import com.example.ui.theme.OchreSurface
import com.example.ui.theme.VermilionRed
import com.example.ui.theme.VermilionSurface
import com.example.ui.theme.WashiCardPure
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Accessible pill toggle for switching between Daylight Washi (Light) and Sumi Night (Dark).
 */
@Composable
fun ThemeModeQuickToggle(
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String = "theme_toggle_button"
) {
    val palette = HojaTheme.palette
    Surface(
        modifier = modifier
            .height(38.dp)
            .clickable(onClick = onToggle)
            .semantics {
                contentDescription = if (isDark) {
                    "Switch to Light Mode"
                } else {
                    "Switch to Dark Mode"
                }
            }
            .testTag(tag),
        shape = RoundedCornerShape(50),
        color = palette.subtleSurface,
        border = BorderStroke(1.dp, palette.subtleBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                contentDescription = null,
                tint = palette.inkPrimary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isDark) "Light" else "Dark",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = palette.inkPrimary
            )
        }
    }
}

/**
 * Custom Typographic & Hand-Drawn Brand Logo for Hoja.
 * High-contrast across both Light and Dark modes.
 */
@Composable
fun HojaWordmark(
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val palette = HojaTheme.palette
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (large) 8.dp else 6.dp)
    ) {
        // Hand-drawn Vermilion Hanko Seal Stamp with Quill Nib
        Canvas(
            modifier = Modifier.size(if (large) 30.dp else 24.dp)
        ) {
            val w = size.width
            val h = size.height
            drawRoundRect(
                color = VermilionRed,
                topLeft = Offset(w * 0.06f, h * 0.06f),
                size = Size(w * 0.88f, h * 0.88f),
                cornerRadius = CornerRadius(w * 0.24f, h * 0.24f)
            )
            drawRoundRect(
                color = WashiCardPure.copy(alpha = 0.88f),
                topLeft = Offset(w * 0.15f, h * 0.15f),
                size = Size(w * 0.70f, h * 0.70f),
                cornerRadius = CornerRadius(w * 0.16f, h * 0.16f),
                style = Stroke(width = w * 0.055f)
            )
            val quill = Path().apply {
                moveTo(w * 0.32f, h * 0.72f)
                cubicTo(w * 0.30f, h * 0.46f, w * 0.48f, h * 0.26f, w * 0.70f, h * 0.26f)
                cubicTo(w * 0.68f, h * 0.50f, w * 0.52f, h * 0.68f, w * 0.32f, h * 0.72f)
                close()
            }
            drawPath(quill, color = WashiCardPure)
            drawLine(
                color = VermilionRed,
                start = Offset(w * 0.34f, h * 0.70f),
                end = Offset(w * 0.62f, h * 0.36f),
                strokeWidth = w * 0.05f,
                cap = StrokeCap.Round
            )
        }

        // Custom Wordmark with PlayMo-style extended pill-loop 'o'
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "H",
                fontFamily = FrauncesFamily,
                fontWeight = FontWeight.Bold,
                fontSize = if (large) 28.sp else 22.sp,
                color = palette.inkPrimary
            )
            Canvas(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(if (large) 24.dp else 19.dp)
                    .height(if (large) 16.dp else 13.dp)
            ) {
                val strokeW = size.height * 0.26f
                drawRoundRect(
                    color = CoralPersimmon,
                    topLeft = Offset(strokeW / 2f, strokeW / 2f),
                    size = Size(size.width - strokeW, size.height - strokeW),
                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }
            Text(
                text = "ja",
                fontFamily = FrauncesFamily,
                fontWeight = FontWeight.Bold,
                fontSize = if (large) 28.sp else 22.sp,
                color = palette.inkPrimary
            )
        }
    }
}

/**
 * Centered section title flanked by delicate hand-drawn ink lines.
 */
@Composable
fun FlankedSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    val palette = HojaTheme.palette
    val lineColor = palette.subtleBorder
    val textColor = palette.inkSecondary
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
        ) {
            val cy = size.height / 2f
            drawLine(
                color = lineColor,
                start = Offset(0f, cy),
                end = Offset(size.width - 4.dp.toPx(), cy),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = lineColor,
                radius = 2.dp.toPx(),
                center = Offset(size.width - 2.dp.toPx(), cy)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
        ) {
            val cy = size.height / 2f
            drawCircle(
                color = lineColor,
                radius = 2.dp.toPx(),
                center = Offset(2.dp.toPx(), cy)
            )
            drawLine(
                color = lineColor,
                start = Offset(4.dp.toPx(), cy),
                end = Offset(size.width, cy),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Duolingo-inspired Living Storybook Washi Background modifier:
 * Renders animated wind-current streamlines, drifting savanna-washi hills, Seigaiha wave motifs,
 * and floating ink/sakura particles that flow directly into Kori's animations and cards.
 */
@Composable
fun Modifier.storybookWashiBackground(): Modifier {
    val palette = HojaTheme.palette
    val infiniteTransition = rememberInfiniteTransition(label = "LivingWashiBackground")

    val windPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "LivingWindPhase"
    )

    val waveColor = palette.inkPrimary.copy(alpha = if (palette.isDark) 0.055f else 0.038f)
    val ribbonColor = CoralPersimmon.copy(alpha = if (palette.isDark) 0.14f else 0.10f)
    val sageStreamColor = palette.sageAccent.copy(alpha = if (palette.isDark) 0.12f else 0.08f)
    val hillColorBack = if (palette.isDark) {
        Color(0xFF1F2A22).copy(alpha = 0.45f)
    } else {
        Color(0xFFEAE1CE).copy(alpha = 0.55f)
    }
    val hillColorFront = if (palette.isDark) {
        Color(0xFF28221D).copy(alpha = 0.50f)
    } else {
        Color(0xFFE2D6BE).copy(alpha = 0.45f)
    }
    val goldEmber = OchreGold.copy(alpha = if (palette.isDark) 0.25f else 0.18f)

    return this.drawBehind {
        val w = size.width
        val h = size.height

        // 1. Subtle Top Atmospheric Glow
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    ribbonColor.copy(alpha = if (palette.isDark) 0.07f else 0.05f),
                    Color.Transparent,
                    sageStreamColor.copy(alpha = if (palette.isDark) 0.06f else 0.04f)
                )
            ),
            size = size
        )

        // 2. Animated Flowing Wind Streamlines (connects screen background directly to character stages)
        val topStream = Path().apply {
            val yOffset = sin(windPhase) * 14.dp.toPx()
            moveTo(-w * 0.05f, h * 0.14f + yOffset)
            cubicTo(
                w * 0.30f, h * 0.06f - yOffset,
                w * 0.68f, h * 0.22f + yOffset,
                w * 1.05f, h * 0.12f - yOffset * 0.5f
            )
        }
        drawPath(
            path = topStream,
            color = ribbonColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(28.dp.toPx(), 12.dp.toPx()),
                    phase = -windPhase * 22f
                )
            )
        )

        val midStream = Path().apply {
            val yOffset = cos(windPhase) * 16.dp.toPx()
            moveTo(-w * 0.05f, h * 0.42f - yOffset * 0.6f)
            cubicTo(
                w * 0.28f, h * 0.49f + yOffset,
                w * 0.65f, h * 0.35f - yOffset,
                w * 1.05f, h * 0.44f + yOffset * 0.5f
            )
        }
        drawPath(
            path = midStream,
            color = sageStreamColor,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        )

        // 3. Japanese Seigaiha (Concentric Wave Arcs) in Upper Corner
        val cellRadius = 36.dp.toPx()
        val stroke = Stroke(width = 1.dp.toPx())
        for (row in 0..3) {
            for (col in 0..4) {
                val cx = w - col * cellRadius * 1.4f + (if (row % 2 == 1) cellRadius * 0.7f else 0f)
                val cy = row * cellRadius * 0.65f + 24.dp.toPx() + sin(windPhase + col * 0.6f) * 2.dp.toPx()
                for (ring in 1..2) {
                    val r = cellRadius * (ring / 2.2f)
                    drawArc(
                        color = waveColor,
                        startAngle = 195f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(cx - r, cy - r),
                        size = Size(r * 2f, r * 2f),
                        style = stroke
                    )
                }
            }
        }

        // 4. Floating Storybook Wind Motes / Leaves
        for (i in 0..5) {
            val px = ((w * (0.12f + i * 0.16f) + cos(windPhase + i * 1.1f) * 18.dp.toPx()) % w)
            val py = h * (0.10f + (i % 3) * 0.28f) + sin(windPhase * 1.2f + i) * 10.dp.toPx()
            drawCircle(
                color = if (i % 2 == 0) goldEmber else ribbonColor,
                radius = (2.5f + (i % 2) * 1.2f).dp.toPx(),
                center = Offset(px, py)
            )
        }

        // 5. Rolling Savanna-Washi Horizon Hills at Bottom
        val hillSway = sin(windPhase) * 8.dp.toPx()
        val backHill = Path().apply {
            moveTo(0f, h * 0.88f)
            quadraticTo(w * 0.35f, h * 0.82f + hillSway, w * 0.75f, h * 0.87f)
            quadraticTo(w * 0.90f, h * 0.89f - hillSway * 0.5f, w, h * 0.85f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(backHill, color = hillColorBack)

        val frontHill = Path().apply {
            moveTo(0f, h * 0.93f)
            quadraticTo(w * 0.42f, h * 0.88f - hillSway, w, h * 0.92f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(frontHill, color = hillColorFront)
    }
}

/**
 * Modifier that draws a hand-stitched dashed border.
 */
fun Modifier.dashedStorybookBorder(
    color: Color,
    cornerRadius: Dp = 14.dp,
    strokeWidth: Dp = 1.5.dp,
    dashLength: Dp = 7.dp,
    gapLength: Dp = 5.dp
): Modifier = this.drawBehind {
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left = strokeWidth.toPx() / 2f,
                top = strokeWidth.toPx() / 2f,
                right = size.width - strokeWidth.toPx() / 2f,
                bottom = size.height - strokeWidth.toPx() / 2f,
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            )
        )
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth.toPx(),
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashLength.toPx(), gapLength.toPx()),
                0f
            )
        )
    )
}

/**
 * Hand-drawn Circular Ring Gauge with optional subtitle label underneath.
 */
@Composable
fun StorybookProgressRing(
    current: Int,
    total: Int,
    accentColor: Color,
    label: String = "",
    modifier: Modifier = Modifier,
    ringSize: Dp = 66.dp
) {
    val palette = HojaTheme.palette
    val fraction = if (total > 0) (current.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "RingProgress"
    )
    val trackColor = palette.subtleBorder

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier.size(ringSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 5.5.dp.toPx()
                val inset = strokeW / 2f + 2.dp.toPx()
                val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)

                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeW * 0.7f, cap = StrokeCap.Round)
                )

                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = (animatedFraction * 360f).coerceAtLeast(18f),
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }

            Text(
                text = "$current/$total",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = palette.inkPrimary
            )
        }

        if (label.isNotBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = palette.inkSecondary
            )
        }
    }
}

/**
 * Duolingo-style Animated Panorama Illustration Scene for Onboarding.
 * The background landscape (hills, clouds, wind streams, sun/moon) flows continuously
 * right into Kori's live level-specific character animation and props!
 */
@Composable
fun AnimatedOnboardingIllustration(
    pageIndex: Int,
    modifier: Modifier = Modifier,
    selectedLevel: DebateLevel = DebateLevel.BEGINNER,
    mascotState: MascotState = MascotState.ENCOURAGING
) {
    val palette = HojaTheme.palette
    val infiniteTransition = rememberInfiniteTransition(label = "OnboardingSceneMotion")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatMotion"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Continuous Duolingo-style living panorama background that flows into the foreground scene
        FlowingPanoramaBackdrop(
            level = selectedLevel,
            isDark = palette.isDark,
            modifier = Modifier.fillMaxSize()
        )

        if (pageIndex == 0) {
            // Scene 0: Live evolving Mwalimu Kori for the selected DebateLevel!
            SecretaryBirdCanvas(
                state = mascotState,
                level = selectedLevel,
                impulseTrigger = selectedLevel.ordinal,
                drawContainedBackdrop = false,
                modifier = Modifier
                    .size(142.dp)
                    .padding(top = 6.dp)
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f

                // Warm Ivory Enso Halo behind the prop scene
                drawCircle(
                    color = WashiCardPure.copy(alpha = 0.90f),
                    radius = h * 0.36f,
                    center = Offset(cx, cy)
                )

                when (pageIndex) {
                    1 -> drawBalanceScalesScene(cx, cy, h, h, floatOffset)
                    else -> drawQuillAndHankoScene(cx, cy, h, h, floatOffset)
                }
            }
        }
    }
}

private fun DrawScope.drawBalanceScalesScene(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    sway: Float
) {
    val strokeW = w * 0.018f
    val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

    drawRoundRect(
        color = OchreSurface,
        topLeft = Offset(cx - w * 0.16f, cy + h * 0.22f),
        size = Size(w * 0.32f, h * 0.055f),
        cornerRadius = CornerRadius(w * 0.025f, w * 0.025f)
    )
    drawRoundRect(
        color = EspressoStroke,
        topLeft = Offset(cx - w * 0.16f, cy + h * 0.22f),
        size = Size(w * 0.32f, h * 0.055f),
        cornerRadius = CornerRadius(w * 0.025f, w * 0.025f),
        style = stroke
    )
    drawLine(
        color = EspressoStroke,
        start = Offset(cx, cy - h * 0.18f),
        end = Offset(cx, cy + h * 0.22f),
        strokeWidth = strokeW * 1.4f,
        cap = StrokeCap.Round
    )

    rotate(degrees = sway * 6.5f, pivot = Offset(cx, cy - h * 0.14f)) {
        val leftX = cx - w * 0.24f
        val rightX = cx + w * 0.24f
        val beamY = cy - h * 0.14f

        drawLine(
            color = EspressoStroke,
            start = Offset(leftX, beamY),
            end = Offset(rightX, beamY),
            strokeWidth = strokeW * 1.4f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = CoralPersimmon,
            radius = w * 0.036f,
            center = Offset(cx, beamY)
        )
        drawCircle(
            color = EspressoStroke,
            radius = w * 0.036f,
            center = Offset(cx, beamY),
            style = stroke
        )

        drawLine(
            color = EspressoStroke,
            start = Offset(leftX, beamY),
            end = Offset(leftX, beamY + h * 0.14f),
            strokeWidth = strokeW * 0.85f
        )
        drawRoundRect(
            color = BambooSurface,
            topLeft = Offset(leftX - w * 0.12f, beamY + h * 0.14f),
            size = Size(w * 0.24f, h * 0.17f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )
        drawRoundRect(
            color = EspressoStroke,
            topLeft = Offset(leftX - w * 0.12f, beamY + h * 0.14f),
            size = Size(w * 0.24f, h * 0.17f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f),
            style = stroke
        )
        drawLine(
            color = BambooGreen,
            start = Offset(leftX - w * 0.06f, beamY + h * 0.20f),
            end = Offset(leftX + w * 0.06f, beamY + h * 0.20f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = BambooGreen,
            start = Offset(leftX - w * 0.06f, beamY + h * 0.24f),
            end = Offset(leftX + w * 0.03f, beamY + h * 0.24f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        drawLine(
            color = EspressoStroke,
            start = Offset(rightX, beamY),
            end = Offset(rightX, beamY + h * 0.14f),
            strokeWidth = strokeW * 0.85f
        )
        drawRoundRect(
            color = VermilionSurface,
            topLeft = Offset(rightX - w * 0.12f, beamY + h * 0.14f),
            size = Size(w * 0.24f, h * 0.17f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )
        drawRoundRect(
            color = EspressoStroke,
            topLeft = Offset(rightX - w * 0.12f, beamY + h * 0.14f),
            size = Size(w * 0.24f, h * 0.17f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f),
            style = stroke
        )
        drawLine(
            color = VermilionRed,
            start = Offset(rightX - w * 0.06f, beamY + h * 0.20f),
            end = Offset(rightX + w * 0.06f, beamY + h * 0.20f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = VermilionRed,
            start = Offset(rightX - w * 0.06f, beamY + h * 0.24f),
            end = Offset(rightX + w * 0.03f, beamY + h * 0.24f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawQuillAndHankoScene(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    sway: Float
) {
    val strokeW = w * 0.018f
    val stroke = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

    val scrollLeft = cx - w * 0.22f
    val scrollTop = cy - h * 0.20f
    val scrollW = w * 0.44f
    val scrollH = h * 0.40f

    drawRoundRect(
        color = OchreSurface,
        topLeft = Offset(scrollLeft, scrollTop),
        size = Size(scrollW, scrollH),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
    )
    drawRoundRect(
        color = EspressoStroke,
        topLeft = Offset(scrollLeft, scrollTop),
        size = Size(scrollW, scrollH),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
        style = stroke
    )

    drawRoundRect(
        color = OchreGold,
        topLeft = Offset(scrollLeft - w * 0.04f, scrollTop - h * 0.025f),
        size = Size(scrollW + w * 0.08f, h * 0.045f),
        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
    )
    drawRoundRect(
        color = EspressoStroke,
        topLeft = Offset(scrollLeft - w * 0.04f, scrollTop - h * 0.025f),
        size = Size(scrollW + w * 0.08f, h * 0.045f),
        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f),
        style = stroke
    )
    drawRoundRect(
        color = OchreGold,
        topLeft = Offset(scrollLeft - w * 0.04f, scrollTop + scrollH - h * 0.02f),
        size = Size(scrollW + w * 0.08f, h * 0.045f),
        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
    )
    drawRoundRect(
        color = EspressoStroke,
        topLeft = Offset(scrollLeft - w * 0.04f, scrollTop + scrollH - h * 0.02f),
        size = Size(scrollW + w * 0.08f, h * 0.045f),
        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f),
        style = stroke
    )

    for (i in 0..2) {
        val ly = scrollTop + h * (0.09f + i * 0.065f)
        drawLine(
            color = EspressoStroke.copy(alpha = 0.7f),
            start = Offset(scrollLeft + w * 0.06f, ly),
            end = Offset(scrollLeft + scrollW - w * (0.08f + (i % 2) * 0.06f), ly),
            strokeWidth = strokeW * 0.85f,
            cap = StrokeCap.Round
        )
    }

    val stampCenter = Offset(scrollLeft + scrollW * 0.74f, scrollTop + scrollH * 0.74f)
    drawCircle(
        color = VermilionRed,
        radius = w * 0.065f,
        center = stampCenter
    )
    drawCircle(
        color = WashiCardPure,
        radius = w * 0.045f,
        center = stampCenter,
        style = Stroke(width = strokeW * 0.7f)
    )

    rotate(degrees = -18f + sway * 6f, pivot = stampCenter) {
        val quillPath = Path().apply {
            moveTo(stampCenter.x, stampCenter.y - h * 0.02f)
            cubicTo(
                stampCenter.x - w * 0.02f, stampCenter.y - h * 0.18f,
                stampCenter.x + w * 0.08f, stampCenter.y - h * 0.30f,
                stampCenter.x + w * 0.18f, stampCenter.y - h * 0.32f
            )
            cubicTo(
                stampCenter.x + w * 0.16f, stampCenter.y - h * 0.16f,
                stampCenter.x + w * 0.08f, stampCenter.y - h * 0.06f,
                stampCenter.x, stampCenter.y - h * 0.02f
            )
            close()
        }
        drawPath(quillPath, color = CoralPersimmon)
        drawPath(quillPath, color = EspressoStroke, style = stroke)
    }
}

enum class SpotArtType {
    BOOK_ACADEMY,
    QUILL_MOTION,
    BAMBOO_PROPOSE,
    SHIELD_OPPOSE
}

@Composable
fun HandDrawnSpotIcon(
    type: SpotArtType,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(44.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.045f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (type) {
            SpotArtType.BOOK_ACADEMY -> {
                drawRoundRect(
                    color = WashiCardPure,
                    topLeft = Offset(w * 0.18f, h * 0.12f),
                    size = Size(w * 0.64f, h * 0.76f),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
                )
                drawRoundRect(
                    color = EspressoStroke,
                    topLeft = Offset(w * 0.18f, h * 0.12f),
                    size = Size(w * 0.64f, h * 0.76f),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                    style = stroke
                )
                drawLine(
                    color = EspressoStroke,
                    start = Offset(w * 0.30f, h * 0.12f),
                    end = Offset(w * 0.30f, h * 0.88f),
                    strokeWidth = w * 0.04f
                )
                drawRoundRect(
                    color = BambooSurface,
                    topLeft = Offset(w * 0.38f, h * 0.26f),
                    size = Size(w * 0.32f, h * 0.20f),
                    cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
                )
                drawRoundRect(
                    color = EspressoStroke,
                    topLeft = Offset(w * 0.38f, h * 0.26f),
                    size = Size(w * 0.32f, h * 0.20f),
                    cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
                    style = stroke
                )
            }

            SpotArtType.QUILL_MOTION -> {
                drawLine(
                    color = EspressoStroke,
                    start = Offset(w * 0.26f, h * 0.84f),
                    end = Offset(w * 0.42f, h * 0.18f),
                    strokeWidth = w * 0.045f,
                    cap = StrokeCap.Round
                )
                val flag = Path().apply {
                    moveTo(w * 0.40f, h * 0.22f)
                    quadraticTo(w * 0.64f, h * 0.18f, w * 0.82f, h * 0.32f)
                    quadraticTo(w * 0.62f, h * 0.44f, w * 0.34f, h * 0.50f)
                    close()
                }
                drawPath(flag, color = WashiCardPure)
                drawPath(flag, color = EspressoStroke, style = stroke)
                drawCircle(
                    color = CoralPersimmon,
                    radius = w * 0.09f,
                    center = Offset(w * 0.68f, h * 0.66f)
                )
            }

            SpotArtType.BAMBOO_PROPOSE -> {
                drawCircle(
                    color = OchreGold.copy(alpha = 0.40f),
                    radius = w * 0.32f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
                drawLine(
                    color = EspressoStroke,
                    start = Offset(w * 0.5f, h * 0.82f),
                    end = Offset(w * 0.5f, h * 0.22f),
                    strokeWidth = w * 0.055f,
                    cap = StrokeCap.Round
                )
                val leaf = Path().apply {
                    moveTo(w * 0.5f, h * 0.48f)
                    quadraticTo(w * 0.78f, h * 0.30f, w * 0.80f, h * 0.46f)
                    quadraticTo(w * 0.65f, h * 0.56f, w * 0.5f, h * 0.48f)
                    close()
                }
                drawPath(leaf, color = BambooGreen)
                drawPath(leaf, color = EspressoStroke, style = stroke)
            }

            SpotArtType.SHIELD_OPPOSE -> {
                drawCircle(
                    color = VermilionRed.copy(alpha = 0.30f),
                    radius = w * 0.32f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
                val shield = Path().apply {
                    moveTo(w * 0.26f, h * 0.24f)
                    lineTo(w * 0.74f, h * 0.24f)
                    quadraticTo(w * 0.74f, h * 0.64f, w * 0.50f, h * 0.82f)
                    quadraticTo(w * 0.26f, h * 0.64f, w * 0.26f, h * 0.24f)
                    close()
                }
                drawPath(shield, color = WashiCardPure)
                drawPath(shield, color = EspressoStroke, style = stroke)
            }
        }
    }
}
