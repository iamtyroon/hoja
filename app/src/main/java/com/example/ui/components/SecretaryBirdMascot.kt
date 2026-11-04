package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.DebateLevel
import com.example.data.model.MascotState
import com.example.ui.theme.BambooGreen
import com.example.ui.theme.BambooSurface
import com.example.ui.theme.CoralPersimmon
import com.example.ui.theme.EspressoStroke
import com.example.ui.theme.HojaTheme
import com.example.ui.theme.OchreGold
import com.example.ui.theme.OchreSurface
import com.example.ui.theme.SoraBlue
import com.example.ui.theme.SoraBlueSurface
import com.example.ui.theme.VermilionRed
import com.example.ui.theme.VermilionSurface
import com.example.ui.theme.WashiCardPure
import com.example.ui.theme.LavenderMistInk
import com.example.ui.theme.LavenderMistSurface
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun DebateLevel.koriPersonaTitle(): String = when (this) {
    DebateLevel.BEGINNER -> "Fledgling Kori"
    DebateLevel.INTERMEDIATE -> "Scholar Kori"
    DebateLevel.ADVANCED -> "Master Kori"
    DebateLevel.CHAMPION -> "Grand Sensei Kori"
}

fun DebateLevel.koriGearDescription(): String = when (this) {
    DebateLevel.BEGINNER -> "Matcha scarf & sprout crest"
    DebateLevel.INTERMEDIATE -> "Round spectacles & scholar vest"
    DebateLevel.ADVANCED -> "Vermilion headband & orator coat"
    DebateLevel.CHAMPION -> "Golden crown & sensei mantle"
}

/**
 * Compact, high-craft Storybook Mascot Hero Banner that integrates a flowing savanna-washi
 * landscape directly into Mwalimu Kori's character animation without taking up excessive vertical space.
 */
@Composable
fun SecretaryBirdMascotStage(
    state: MascotState,
    speechText: String,
    modifier: Modifier = Modifier,
    level: DebateLevel = DebateLevel.BEGINNER,
    avatarSize: Dp = 92.dp,
    onStateSelected: ((MascotState) -> Unit)? = null
) {
    val palette = HojaTheme.palette
    var tapCount by remember { mutableIntStateOf(0) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("mascot_stage_card"),
        shape = RoundedCornerShape(24.dp),
        color = palette.cardSurface,
        border = BorderStroke(1.5.dp, palette.subtleBorder)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Living Savanna-Washi Panorama Backdrop flowing directly into Kori
            FlowingPanoramaBackdrop(
                level = level,
                isDark = palette.isDark,
                modifier = Modifier.matchParentSize()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            tapCount++
                            if (onStateSelected != null) {
                                val allStates = MascotState.entries
                                val nextIndex = (allStates.indexOf(state) + 1) % allStates.size
                                onStateSelected(allStates[nextIndex])
                            }
                        }
                        .semantics {
                            contentDescription = "Mwalimu Kori mascot in ${state.title} state"
                        }
                        .testTag("mascot_canvas_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    SecretaryBirdCanvas(
                        state = state,
                        level = level,
                        impulseTrigger = tapCount + level.ordinal * 10 + state.ordinal,
                        drawContainedBackdrop = false,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Mwalimu Kori",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.inkPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = palette.cardSurface.copy(alpha = 0.90f),
                            border = BorderStroke(1.dp, palette.subtleBorder)
                        ) {
                            Text(
                                text = state.swahiliMood,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = palette.coralAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 16.dp,
                            bottomEnd = 16.dp,
                            bottomStart = 16.dp
                        ),
                        color = palette.cardSurface.copy(alpha = if (palette.isDark) 0.92f else 0.95f),
                        border = BorderStroke(1.dp, palette.subtleBorder)
                    ) {
                        AnimatedContent(
                            targetState = speechText,
                            transitionSpec = {
                                (fadeIn(tween(240)) + slideInVertically(tween(240)) { it / 4 }) togetherWith
                                    (fadeOut(tween(150)) + slideOutVertically(tween(150)) { -it / 4 })
                            },
                            label = "MascotSpeechTransition"
                        ) { targetSpeech ->
                            Text(
                                text = targetSpeech,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.inkPrimary,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 9.dp)
                                    .testTag("mascot_speech_bubble")
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Mascot Coach Banner for secondary headers.
 */
@Composable
fun CompactMascotCoachBanner(
    state: MascotState,
    message: String,
    modifier: Modifier = Modifier,
    level: DebateLevel = DebateLevel.INTERMEDIATE
) {
    val palette = HojaTheme.palette
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("compact_mascot_banner"),
        shape = RoundedCornerShape(18.dp),
        color = palette.cardSurface,
        border = BorderStroke(1.2.dp, palette.subtleBorder)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            FlowingPanoramaBackdrop(
                level = level,
                isDark = palette.isDark,
                modifier = Modifier.matchParentSize()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SecretaryBirdCanvas(
                    state = state,
                    level = level,
                    drawContainedBackdrop = false,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mwalimu Kori · ${state.swahiliMood}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.coralAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.inkPrimary
                    )
                }
            }
        }
    }
}

/**
 * Flowing Panorama Backdrop that connects directly with Kori's feathers and posture.
 */
@Composable
fun FlowingPanoramaBackdrop(
    level: DebateLevel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PanoramaFlow")
    val flowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PanoramaWavePhase"
    )

    val (skyTint, hillBack, hillFront, streamAccent) = when (level) {
        DebateLevel.BEGINNER -> if (isDark) {
            listOf(Color(0xFF1A2932), Color(0xFF21362A), Color(0xFF2A4434), SoraBlue.copy(alpha = 0.25f))
        } else {
            listOf(Color(0xFFEAF4F8), Color(0xFFDCECD9), Color(0xFFC8E2C4), SoraBlue.copy(alpha = 0.22f))
        }
        DebateLevel.INTERMEDIATE -> if (isDark) {
            listOf(Color(0xFF1D2B22), Color(0xFF26382B), Color(0xFF324636), BambooGreen.copy(alpha = 0.28f))
        } else {
            listOf(Color(0xFFEEF5EC), Color(0xFFD9E8D3), Color(0xFFC4DCBC), BambooGreen.copy(alpha = 0.22f))
        }
        DebateLevel.ADVANCED -> if (isDark) {
            listOf(Color(0xFF2E2318), Color(0xFF3B2B1E), Color(0xFF4A3525), CoralPersimmon.copy(alpha = 0.28f))
        } else {
            listOf(Color(0xFFFBF1DF), Color(0xFFF3DEC0), Color(0xFFE8CBA5), CoralPersimmon.copy(alpha = 0.22f))
        }
        DebateLevel.CHAMPION -> if (isDark) {
            listOf(Color(0xFF291F33), Color(0xFF362742), Color(0xFF463254), OchreGold.copy(alpha = 0.32f))
        } else {
            listOf(Color(0xFFF4ECF8), Color(0xFFE6D6F0), Color(0xFFD7C2E6), OchreGold.copy(alpha = 0.28f))
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(skyTint.copy(alpha = 0.65f), skyTint.copy(alpha = 0.20f))
            ),
            size = size
        )

        val sunX = w * 0.16f + cos(flowPhase) * w * 0.01f
        val sunY = h * 0.26f + sin(flowPhase) * h * 0.03f
        drawCircle(
            color = if (isDark) OchreGold.copy(alpha = 0.20f) else CoralPersimmon.copy(alpha = 0.16f),
            radius = h * 0.22f,
            center = Offset(sunX, sunY)
        )

        // Flowing wind current ribbons
        for (i in 0..1) {
            val yBase = h * (0.30f + i * 0.26f)
            val waveOffset = sin(flowPhase + i * 1.6f) * h * 0.07f
            val windPath = Path().apply {
                moveTo(-w * 0.05f, yBase + waveOffset)
                cubicTo(
                    w * 0.28f, yBase - waveOffset * 1.2f,
                    w * 0.64f, yBase + waveOffset * 1.2f,
                    w * 1.05f, yBase - waveOffset * 0.7f
                )
            }
            drawPath(
                path = windPath,
                color = streamAccent,
                style = Stroke(width = (2.2f - i * 0.5f).dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Rolling savanna-washi hills
        val backHill = Path().apply {
            moveTo(0f, h * 0.76f)
            quadraticTo(w * 0.30f, h * (0.62f + sin(flowPhase) * 0.025f), w * 0.66f, h * 0.74f)
            quadraticTo(w * 0.85f, h * 0.80f, w, h * 0.70f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(backHill, color = hillBack.copy(alpha = 0.70f))

        val frontHill = Path().apply {
            moveTo(0f, h * 0.85f)
            quadraticTo(w * 0.24f, h * (0.72f + cos(flowPhase) * 0.025f), w * 0.56f, h * 0.83f)
            quadraticTo(w * 0.82f, h * 0.90f, w, h * 0.80f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(frontHill, color = hillFront.copy(alpha = 0.80f))
    }
}

/**
 * Studio-quality Chibi / Storybook Secretary Bird Character Illustration ("Mwalimu Kori").
 * Features smooth spring-interpolated transitions between moods and levels, layered plumage,
 * signature coral-orange orbital eye patches, black-tipped crest plumes, and expressive anime eyes.
 */
@Composable
fun SecretaryBirdCanvas(
    state: MascotState,
    modifier: Modifier = Modifier,
    level: DebateLevel = DebateLevel.BEGINNER,
    impulseTrigger: Int = 0,
    drawContainedBackdrop: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "KoriCharacterLoop")

    // Continuous 0..2PI breathing & feather wave loop
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "KoriPhase"
    )

    // Periodic natural blink
    val blinkWave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "KoriBlink"
    )

    // Smooth spring-interpolated pose parameters for clear transitions between animations!
    val targetHeadTilt = when (state) {
        MascotState.IDLE -> 0f
        MascotState.ANNOUNCING -> -4f
        MascotState.THINKING -> 8f
        MascotState.IMPRESSED -> -5f
        MascotState.CONCERNED -> 6f
        MascotState.CHEERING -> 0f
        MascotState.ENCOURAGING -> -3f
    }
    val headTilt by animateFloatAsState(
        targetValue = targetHeadTilt,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "HeadTiltTransition"
    )

    val targetWingLift = when (state) {
        MascotState.CHEERING -> 1f
        MascotState.ANNOUNCING -> 0.75f
        MascotState.IMPRESSED -> 0.55f
        MascotState.ENCOURAGING -> 0.45f
        MascotState.THINKING -> 0.35f
        MascotState.CONCERNED -> 0.15f
        MascotState.IDLE -> 0f
    }
    val wingLift by animateFloatAsState(
        targetValue = targetWingLift,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = Spring.StiffnessMediumLow),
        label = "WingLiftTransition"
    )

    val targetBeakOpen = when (state) {
        MascotState.CHEERING -> 1f
        MascotState.ANNOUNCING -> 0.7f
        MascotState.ENCOURAGING -> 0.4f
        else -> 0f
    }
    val beakOpen by animateFloatAsState(
        targetValue = targetBeakOpen,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        label = "BeakOpenTransition"
    )

    val targetBrowAngle = when (state) {
        MascotState.CONCERNED -> -14f
        MascotState.THINKING -> 8f
        MascotState.IMPRESSED -> -6f
        MascotState.CHEERING -> -10f
        else -> 0f
    }
    val browAngle by animateFloatAsState(
        targetValue = targetBrowAngle,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "BrowAngleTransition"
    )

    // Spring pop on tap or state change
    val popScale = remember { androidx.compose.animation.core.Animatable(1f) }
    LaunchedEffect(impulseTrigger, state, level) {
        popScale.snapTo(1.10f)
        popScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val strokeW = (w * 0.026f).coerceAtLeast(1.8f)
        val outline = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

        val breath = sin(phase)
        val delayedWave = sin(phase - 0.65f)
        val isBlinking = blinkWave > 0.93f

        // Optional soft circular badge backdrop for standalone avatars
        if (drawContainedBackdrop) {
            val badgeBg = when (level) {
                DebateLevel.BEGINNER -> SoraBlueSurface
                DebateLevel.INTERMEDIATE -> BambooSurface
                DebateLevel.ADVANCED -> OchreSurface
                DebateLevel.CHAMPION -> LavenderMistSurface
            }
            val badgeRing = when (level) {
                DebateLevel.BEGINNER -> SoraBlue
                DebateLevel.INTERMEDIATE -> BambooGreen
                DebateLevel.ADVANCED -> CoralPersimmon
                DebateLevel.CHAMPION -> OchreGold
            }
            drawCircle(
                color = WashiCardPure,
                radius = w * 0.46f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = badgeBg.copy(alpha = 0.75f),
                radius = w * 0.43f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = badgeRing.copy(alpha = 0.45f),
                radius = w * 0.46f,
                center = Offset(cx, cy),
                style = Stroke(width = strokeW * 0.8f)
            )
        }

        // Ground shadow
        drawOval(
            color = EspressoStroke.copy(alpha = 0.14f),
            topLeft = Offset(cx - w * 0.26f, cy + h * 0.36f),
            size = Size(w * 0.52f, h * 0.07f)
        )

        val bounceY = -breath * h * 0.018f - (popScale.value - 1f) * h * 0.12f

        translate(top = bounceY) {
            scale(
                scaleX = popScale.value * (1f - breath * 0.015f),
                scaleY = popScale.value * (1f + breath * 0.020f),
                pivot = Offset(cx, cy + h * 0.34f)
            ) {
                // 1. Champion Golden Halo Aura (behind character)
                if (level == DebateLevel.CHAMPION) {
                    drawCircle(
                        color = OchreGold.copy(alpha = 0.22f),
                        radius = w * 0.38f,
                        center = Offset(cx, cy - h * 0.02f)
                    )
                    drawArc(
                        color = OchreGold,
                        startAngle = -160f + breath * 5f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(cx - w * 0.38f, cy - h * 0.40f),
                        size = Size(w * 0.76f, h * 0.76f),
                        style = Stroke(width = strokeW * 1.1f, cap = StrokeCap.Round)
                    )
                }

                // 2. Tailored Torso & Japanese Haori Jacket
                drawKoriTorsoAndAttire(
                    cx = cx,
                    cy = cy,
                    w = w,
                    h = h,
                    level = level,
                    wingLift = wingLift,
                    breath = breath,
                    strokeW = strokeW,
                    outline = outline
                )

                // 3. Articulated Head, Iconic Crest Plumes, Orbital Patches, Eyes & Raptor Beak
                val headPivot = Offset(cx, cy + h * 0.02f)
                rotate(degrees = headTilt + delayedWave * 2.2f, pivot = headPivot) {
                    // 3A. Signature Secretary Bird Black-Tipped Crest Plumes (fanned behind head)
                    drawSecretaryCrestPlumes(
                        cx = cx,
                        cy = cy,
                        w = w,
                        h = h,
                        level = level,
                        phase = phase,
                        wingLift = wingLift,
                        outline = outline
                    )

                    // 3B. Sculpted Avian Head & Fluffy Cheek Tufts
                    val headCenterY = cy - h * 0.08f
                    val headPath = Path().apply {
                        // Top dome
                        moveTo(cx - w * 0.21f, headCenterY + h * 0.02f)
                        cubicTo(
                            cx - w * 0.22f, headCenterY - h * 0.18f,
                            cx - w * 0.12f, headCenterY - h * 0.23f,
                            cx, headCenterY - h * 0.23f
                        )
                        cubicTo(
                            cx + w * 0.12f, headCenterY - h * 0.23f,
                            cx + w * 0.22f, headCenterY - h * 0.18f,
                            cx + w * 0.21f, headCenterY + h * 0.02f
                        )
                        // Right fluffy cheek tuft
                        lineTo(cx + w * 0.25f, headCenterY + h * 0.05f)
                        quadraticTo(
                            cx + w * 0.21f, headCenterY + h * 0.13f,
                            cx + w * 0.14f, headCenterY + h * 0.14f
                        )
                        // Soft chin curve
                        quadraticTo(
                            cx, headCenterY + h * 0.17f,
                            cx - w * 0.14f, headCenterY + h * 0.14f
                        )
                        // Left fluffy cheek tuft
                        quadraticTo(
                            cx - w * 0.21f, headCenterY + h * 0.13f,
                            cx - w * 0.25f, headCenterY + h * 0.05f
                        )
                        close()
                    }
                    // Warm cream-white plumage fill + subtle lower cheek shadow
                    drawPath(headPath, color = Color(0xFFFFFBF4))
                    drawArc(
                        color = Color(0xFFEDE2CE),
                        startAngle = 20f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(cx - w * 0.17f, headCenterY - h * 0.04f),
                        size = Size(w * 0.34f, h * 0.18f),
                        style = Stroke(width = strokeW * 1.4f, cap = StrokeCap.Round)
                    )
                    drawPath(headPath, color = EspressoStroke, style = outline)

                    // Forehead feather tuft
                    val foreTuft = Path().apply {
                        moveTo(cx - w * 0.04f, headCenterY - h * 0.22f)
                        quadraticTo(cx, headCenterY - h * 0.29f, cx + w * 0.03f, headCenterY - h * 0.22f)
                    }
                    drawPath(foreTuft, color = EspressoStroke, style = outline)

                    // 3C. Iconic Secretary Bird Coral-Orange Orbital Eye Patches (winged masks)
                    val eyeY = headCenterY - h * 0.02f
                    val leftEyeX = cx - w * 0.095f
                    val rightEyeX = cx + w * 0.095f

                    val leftPatch = Path().apply {
                        moveTo(leftEyeX + w * 0.055f, eyeY)
                        cubicTo(
                            leftEyeX + w * 0.05f, eyeY - h * 0.075f,
                            leftEyeX - w * 0.06f, eyeY - h * 0.075f,
                            leftEyeX - w * 0.095f, eyeY - h * 0.035f
                        )
                        quadraticTo(
                            leftEyeX - w * 0.075f, eyeY + h * 0.065f,
                            leftEyeX + w * 0.045f, eyeY + h * 0.045f
                        )
                        close()
                    }
                    drawPath(leftPatch, color = CoralPersimmon.copy(alpha = 0.85f))
                    drawPath(leftPatch, color = EspressoStroke, style = Stroke(width = strokeW * 0.7f))

                    val rightPatch = Path().apply {
                        moveTo(rightEyeX - w * 0.055f, eyeY)
                        cubicTo(
                            rightEyeX - w * 0.05f, eyeY - h * 0.075f,
                            rightEyeX + w * 0.06f, eyeY - h * 0.075f,
                            rightEyeX + w * 0.095f, eyeY - h * 0.035f
                        )
                        quadraticTo(
                            rightEyeX + w * 0.075f, eyeY + h * 0.065f,
                            rightEyeX - w * 0.045f, eyeY + h * 0.045f
                        )
                        close()
                    }
                    drawPath(rightPatch, color = CoralPersimmon.copy(alpha = 0.85f))
                    drawPath(rightPatch, color = EspressoStroke, style = Stroke(width = strokeW * 0.7f))

                    // 3D. Large Expressive Glossy Storybook Eyes
                    drawExpressiveBirdEyes(
                        leftEye = Offset(leftEyeX, eyeY),
                        rightEye = Offset(rightEyeX, eyeY),
                        w = w,
                        h = h,
                        state = state,
                        isBlinking = isBlinking,
                        browAngle = browAngle,
                        strokeW = strokeW
                    )

                    // 3E. Level-Specific Eyewear / Headgear (Spectacles, Hachimaki, Crown)
                    drawLevelHeadgear(
                        cx = cx,
                        headCenterY = headCenterY,
                        leftEye = Offset(leftEyeX, eyeY),
                        rightEye = Offset(rightEyeX, eyeY),
                        w = w,
                        h = h,
                        level = level,
                        phase = phase,
                        strokeW = strokeW,
                        outline = outline
                    )

                    // 3F. Sculpted Hooked Raptor Beak (Golden Ochre + Dark Slate Tip)
                    val beakTopY = eyeY + h * 0.015f
                    if (beakOpen > 0.1f) {
                        // Open happy/speaking lower beak interior
                        val mouthInterior = Path().apply {
                            moveTo(cx - w * 0.038f, beakTopY + h * 0.035f)
                            quadraticTo(
                                cx,
                                beakTopY + h * (0.09f + beakOpen * 0.035f),
                                cx + w * 0.038f,
                                beakTopY + h * 0.035f
                            )
                            close()
                        }
                        drawPath(mouthInterior, color = VermilionRed)
                        drawPath(mouthInterior, color = EspressoStroke, style = outline)
                    }

                    val upperBeak = Path().apply {
                        moveTo(cx - w * 0.048f, beakTopY + h * 0.025f)
                        quadraticTo(cx, beakTopY - h * 0.025f, cx + w * 0.048f, beakTopY + h * 0.025f)
                        quadraticTo(cx + w * 0.035f, beakTopY + h * 0.075f, cx, beakTopY + h * 0.095f)
                        quadraticTo(cx - w * 0.035f, beakTopY + h * 0.075f, cx - w * 0.048f, beakTopY + h * 0.025f)
                        close()
                    }
                    drawPath(upperBeak, color = OchreGold)

                    // Dark raptor beak tip highlight
                    val beakTip = Path().apply {
                        moveTo(cx - w * 0.022f, beakTopY + h * 0.062f)
                        quadraticTo(cx, beakTopY + h * 0.055f, cx + w * 0.022f, beakTopY + h * 0.062f)
                        lineTo(cx, beakTopY + h * 0.095f)
                        close()
                    }
                    drawPath(beakTip, color = Color(0xFF4A433C))
                    drawPath(upperBeak, color = EspressoStroke, style = outline)
                }

                // 4. Subtle Emote Sparkles around Kori
                drawMoodSparkles(
                    cx = cx,
                    cy = cy,
                    w = w,
                    h = h,
                    state = state,
                    phase = phase,
                    strokeW = strokeW
                )
            }
        }
    }
}

private fun DrawScope.drawSecretaryCrestPlumes(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    level: DebateLevel,
    phase: Float,
    wingLift: Float,
    outline: Stroke
) {
    val angles = when (level) {
        DebateLevel.BEGINNER -> listOf(-32f, 0f, 32f)
        DebateLevel.INTERMEDIATE -> listOf(-46f, -23f, 0f, 23f, 46f)
        DebateLevel.ADVANCED -> listOf(-56f, -34f, -12f, 12f, 34f, 56f)
        DebateLevel.CHAMPION -> listOf(-64f, -42f, -21f, 0f, 21f, 42f, 64f)
    }

    val pivot = Offset(cx, cy - h * 0.22f)
    angles.forEachIndexed { idx, baseAngle ->
        val wave = sin(phase - idx * 0.45f) * (3.5f + wingLift * 2.5f)
        val spreadBoost = baseAngle * (1f + wingLift * 0.08f)
        rotate(degrees = spreadBoost + wave, pivot = pivot) {
            val plumeLen = h * (0.19f + (if (idx == angles.size / 2) 0.03f else 0f))
            val plumeTop = pivot.y - plumeLen

            val plumePath = Path().apply {
                moveTo(cx, pivot.y + h * 0.02f)
                cubicTo(
                    cx - w * 0.038f, pivot.y - plumeLen * 0.55f,
                    cx - w * 0.045f, plumeTop + h * 0.02f,
                    cx, plumeTop
                )
                cubicTo(
                    cx + w * 0.045f, plumeTop + h * 0.02f,
                    cx + w * 0.038f, pivot.y - plumeLen * 0.55f,
                    cx, pivot.y + h * 0.02f
                )
                close()
            }

            val baseColor = when (level) {
                DebateLevel.BEGINNER -> Color(0xFFF6EFE2)
                DebateLevel.INTERMEDIATE -> Color(0xFFF3E9D6)
                DebateLevel.ADVANCED -> if (idx % 2 == 0) CoralPersimmon else OchreGold
                DebateLevel.CHAMPION -> if (idx % 2 == 0) OchreGold else LavenderMistInk
            }
            drawPath(plumePath, color = baseColor)

            // Signature Secretary Bird Dark Sumi-Ink Plume Tip
            drawCircle(
                color = EspressoStroke,
                radius = w * 0.024f,
                center = Offset(cx, plumeTop + h * 0.028f)
            )
            drawPath(plumePath, color = EspressoStroke, style = outline)
        }
    }
}

private fun DrawScope.drawExpressiveBirdEyes(
    leftEye: Offset,
    rightEye: Offset,
    w: Float,
    h: Float,
    state: MascotState,
    isBlinking: Boolean,
    browAngle: Float,
    strokeW: Float
) {
    val eyeR = w * 0.046f
    val stroke = Stroke(width = strokeW * 1.05f, cap = StrokeCap.Round)

    listOf(leftEye to -1f, rightEye to 1f).forEach { (center, sideSign) ->
        if (isBlinking || state == MascotState.CHEERING) {
            // Happy curved arc eyes
            drawArc(
                color = EspressoStroke,
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(center.x - eyeR, center.y - eyeR * 0.6f),
                size = Size(eyeR * 2f, eyeR * 1.4f),
                style = Stroke(width = strokeW * 1.35f, cap = StrokeCap.Round)
            )
        } else {
            // Crisp white sclera + dark espresso iris + warm amber lower glow + dual catchlights
            drawCircle(color = WashiCardPure, radius = eyeR, center = center)
            drawCircle(color = EspressoStroke, radius = eyeR * 0.78f, center = center)
            drawArc(
                color = OchreGold,
                startAngle = 25f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(center.x - eyeR * 0.52f, center.y - eyeR * 0.52f),
                size = Size(eyeR * 1.04f, eyeR * 1.04f),
                style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round)
            )
            // Primary & secondary specular catchlights
            drawCircle(
                color = WashiCardPure,
                radius = eyeR * 0.28f,
                center = Offset(center.x - eyeR * 0.24f, center.y - eyeR * 0.26f)
            )
            drawCircle(
                color = WashiCardPure,
                radius = eyeR * 0.13f,
                center = Offset(center.x + eyeR * 0.24f, center.y + eyeR * 0.22f)
            )
            drawCircle(color = EspressoStroke, radius = eyeR, center = center, style = stroke)
        }

        // Expressive eyebrow above orbital patch
        rotate(degrees = browAngle * sideSign, pivot = Offset(center.x, center.y - eyeR * 1.45f)) {
            drawLine(
                color = EspressoStroke,
                start = Offset(center.x - eyeR * 0.85f, center.y - eyeR * 1.45f),
                end = Offset(center.x + eyeR * 0.85f, center.y - eyeR * 1.55f),
                strokeWidth = strokeW * 1.2f,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawLevelHeadgear(
    cx: Float,
    headCenterY: Float,
    leftEye: Offset,
    rightEye: Offset,
    w: Float,
    h: Float,
    level: DebateLevel,
    phase: Float,
    strokeW: Float,
    outline: Stroke
) {
    when (level) {
        DebateLevel.BEGINNER -> {
            // Fresh green bamboo sprout leaf on top of head
            val leafPivot = Offset(cx + w * 0.08f, headCenterY - h * 0.21f)
            rotate(degrees = sin(phase) * 8f, pivot = leafPivot) {
                val leaf = Path().apply {
                    moveTo(leafPivot.x, leafPivot.y)
                    quadraticTo(
                        leafPivot.x + w * 0.12f, leafPivot.y - h * 0.10f,
                        leafPivot.x + w * 0.14f, leafPivot.y - h * 0.01f
                    )
                    quadraticTo(
                        leafPivot.x + w * 0.06f, leafPivot.y + h * 0.04f,
                        leafPivot.x, leafPivot.y
                    )
                    close()
                }
                drawPath(leaf, color = BambooGreen)
                drawPath(leaf, color = EspressoStroke, style = outline)
            }
        }

        DebateLevel.INTERMEDIATE -> {
            // Classic Round Scholar Spectacles
            val glassR = w * 0.064f
            drawCircle(color = WashiCardPure.copy(alpha = 0.22f), radius = glassR, center = leftEye)
            drawCircle(color = WashiCardPure.copy(alpha = 0.22f), radius = glassR, center = rightEye)
            drawCircle(color = EspressoStroke, radius = glassR, center = leftEye, style = outline)
            drawCircle(color = EspressoStroke, radius = glassR, center = rightEye, style = outline)
            drawLine(
                color = EspressoStroke,
                start = Offset(leftEye.x + glassR, leftEye.y),
                end = Offset(rightEye.x - glassR, rightEye.y),
                strokeWidth = strokeW * 1.1f
            )
        }

        DebateLevel.ADVANCED -> {
            // Crisp Vermilion Hachimaki Headband with fluttering ribbon tail
            val bandY = headCenterY - h * 0.15f
            val tailWave = sin(phase * 1.5f) * h * 0.03f
            val ribbonTail = Path().apply {
                moveTo(cx + w * 0.18f, bandY)
                quadraticTo(
                    cx + w * 0.30f, bandY - h * 0.04f + tailWave,
                    cx + w * 0.35f, bandY + h * 0.03f + tailWave
                )
            }
            drawPath(
                ribbonTail,
                color = VermilionRed,
                style = Stroke(width = strokeW * 2.2f, cap = StrokeCap.Round)
            )
            drawRoundRect(
                color = VermilionRed,
                topLeft = Offset(cx - w * 0.20f, bandY - h * 0.025f),
                size = Size(w * 0.40f, h * 0.048f),
                cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
            )
            drawRoundRect(
                color = EspressoStroke,
                topLeft = Offset(cx - w * 0.20f, bandY - h * 0.025f),
                size = Size(w * 0.40f, h * 0.048f),
                cornerRadius = CornerRadius(w * 0.02f, w * 0.02f),
                style = outline
            )
            drawCircle(color = OchreGold, radius = w * 0.022f, center = Offset(cx, bandY))
        }

        DebateLevel.CHAMPION -> {
            // Golden Sensei Sun-Crest Circlet
            val crownBaseY = headCenterY - h * 0.19f
            val crown = Path().apply {
                moveTo(cx - w * 0.11f, crownBaseY)
                lineTo(cx - w * 0.08f, crownBaseY - h * 0.09f)
                lineTo(cx - w * 0.03f, crownBaseY - h * 0.04f)
                lineTo(cx, crownBaseY - h * 0.12f)
                lineTo(cx + w * 0.03f, crownBaseY - h * 0.04f)
                lineTo(cx + w * 0.08f, crownBaseY - h * 0.09f)
                lineTo(cx + w * 0.11f, crownBaseY)
                close()
            }
            drawPath(crown, color = OchreGold)
            drawPath(crown, color = EspressoStroke, style = outline)
            drawCircle(
                color = VermilionRed,
                radius = w * 0.018f,
                center = Offset(cx, crownBaseY - h * 0.035f)
            )
        }
    }
}

private fun DrawScope.drawKoriTorsoAndAttire(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    level: DebateLevel,
    wingLift: Float,
    breath: Float,
    strokeW: Float,
    outline: Stroke
) {
    val torsoTop = cy + h * 0.04f
    val torsoBottom = cy + h * 0.36f

    val coatColor = when (level) {
        DebateLevel.BEGINNER -> SoraBlue
        DebateLevel.INTERMEDIATE -> BambooGreen
        DebateLevel.ADVANCED -> EspressoStroke
        DebateLevel.CHAMPION -> LavenderMistInk
    }
    val lapelColor = when (level) {
        DebateLevel.BEGINNER -> BambooGreen
        DebateLevel.INTERMEDIATE -> OchreGold
        DebateLevel.ADVANCED -> VermilionRed
        DebateLevel.CHAMPION -> OchreGold
    }

    // Left & Right Articulated Feathered Wings (behind/beside torso, lifting smoothly with state)
    val leftWingAngle = 12f + wingLift * 36f + breath * 3f
    val rightWingAngle = -12f - wingLift * 36f - breath * 3f

    rotate(degrees = leftWingAngle, pivot = Offset(cx - w * 0.16f, torsoTop + h * 0.04f)) {
        val leftWing = Path().apply {
            moveTo(cx - w * 0.15f, torsoTop + h * 0.02f)
            cubicTo(
                cx - w * 0.34f, torsoTop + h * 0.06f,
                cx - w * 0.36f, torsoBottom - h * 0.04f,
                cx - w * 0.20f, torsoBottom - h * 0.02f
            )
            close()
        }
        drawPath(leftWing, color = Color(0xFFF3EAD8))
        drawPath(leftWing, color = EspressoStroke, style = outline)
    }

    rotate(degrees = rightWingAngle, pivot = Offset(cx + w * 0.16f, torsoTop + h * 0.04f)) {
        val rightWing = Path().apply {
            moveTo(cx + w * 0.15f, torsoTop + h * 0.02f)
            cubicTo(
                cx + w * 0.34f, torsoTop + h * 0.06f,
                cx + w * 0.36f, torsoBottom - h * 0.04f,
                cx + w * 0.20f, torsoBottom - h * 0.02f
            )
            close()
        }
        drawPath(rightWing, color = Color(0xFFF3EAD8))
        drawPath(rightWing, color = EspressoStroke, style = outline)
    }

    // Main Torso / Haori Coat Silhouette
    val torsoPath = Path().apply {
        moveTo(cx - w * 0.18f, torsoTop)
        cubicTo(
            cx - w * 0.23f, torsoTop + h * 0.12f,
            cx - w * 0.21f, torsoBottom,
            cx - w * 0.14f, torsoBottom
        )
        lineTo(cx + w * 0.14f, torsoBottom)
        cubicTo(
            cx + w * 0.21f, torsoBottom,
            cx + w * 0.23f, torsoTop + h * 0.12f,
            cx + w * 0.18f, torsoTop
        )
        close()
    }
    drawPath(torsoPath, color = coatColor)

    // Cream Feathered Chest V-Opening
    val chestV = Path().apply {
        moveTo(cx - w * 0.09f, torsoTop)
        lineTo(cx, torsoBottom - h * 0.06f)
        lineTo(cx + w * 0.09f, torsoTop)
        close()
    }
    drawPath(chestV, color = Color(0xFFFFFBF4))

    // Haori Collar Lapels
    drawLine(
        color = lapelColor,
        start = Offset(cx - w * 0.10f, torsoTop + h * 0.01f),
        end = Offset(cx, torsoBottom - h * 0.05f),
        strokeWidth = strokeW * 1.8f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = lapelColor,
        start = Offset(cx + w * 0.10f, torsoTop + h * 0.01f),
        end = Offset(cx, torsoBottom - h * 0.05f),
        strokeWidth = strokeW * 1.8f,
        cap = StrokeCap.Round
    )

    drawPath(torsoPath, color = EspressoStroke, style = outline)

    // Cute Avian Talons at Base
    drawRoundRect(
        color = OchreGold,
        topLeft = Offset(cx - w * 0.11f, torsoBottom - h * 0.02f),
        size = Size(w * 0.07f, h * 0.045f),
        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
    )
    drawRoundRect(
        color = EspressoStroke,
        topLeft = Offset(cx - w * 0.11f, torsoBottom - h * 0.02f),
        size = Size(w * 0.07f, h * 0.045f),
        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f),
        style = outline
    )
    drawRoundRect(
        color = OchreGold,
        topLeft = Offset(cx + w * 0.04f, torsoBottom - h * 0.02f),
        size = Size(w * 0.07f, h * 0.045f),
        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
    )
    drawRoundRect(
        color = EspressoStroke,
        topLeft = Offset(cx + w * 0.04f, torsoBottom - h * 0.02f),
        size = Size(w * 0.07f, h * 0.045f),
        cornerRadius = CornerRadius(w * 0.02f, w * 0.02f),
        style = outline
    )
}

private fun DrawScope.drawMoodSparkles(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    state: MascotState,
    phase: Float,
    strokeW: Float
) {
    when (state) {
        MascotState.CHEERING, MascotState.IMPRESSED, MascotState.ANNOUNCING -> {
            val sx = cx + w * 0.34f
            val sy = cy - h * 0.24f + sin(phase * 2f) * h * 0.02f
            val r = w * 0.045f
            drawLine(
                color = OchreGold,
                start = Offset(sx - r, sy),
                end = Offset(sx + r, sy),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = OchreGold,
                start = Offset(sx, sy - r),
                end = Offset(sx, sy + r),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }
        MascotState.THINKING -> {
            val bx = cx + w * 0.32f
            val by = cy - h * 0.22f + sin(phase) * h * 0.02f
            drawCircle(color = OchreGold, radius = w * 0.018f, center = Offset(bx - w * 0.04f, by + h * 0.05f))
            drawCircle(color = CoralPersimmon, radius = w * 0.028f, center = Offset(bx, by))
        }
        else -> Unit
    }
}
