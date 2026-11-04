package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.DebateSessionEntity
import com.example.data.model.DailyMotion
import com.example.data.model.DebateLevel
import com.example.data.model.KeyTerm
import com.example.data.model.MascotState
import com.example.data.repository.UserProgressPreferences
import com.example.ui.components.AnimatedOnboardingIllustration
import com.example.ui.components.FlankedSectionHeader
import com.example.ui.components.HojaWordmark
import com.example.ui.components.SecretaryBirdCanvas
import com.example.ui.components.SecretaryBirdMascotStage
import com.example.ui.components.ThemeModeQuickToggle
import com.example.ui.components.dashedStorybookBorder
import com.example.ui.components.koriPersonaTitle
import com.example.ui.components.storybookWashiBackground
import com.example.ui.theme.HojaTheme
import com.example.ui.viewmodel.DailyMotionUiState
import kotlinx.coroutines.launch

private data class OnboardingSlide(
    val title: String,
    val caption: String
)

@Composable
fun OnboardingScreen(
    initialLevel: DebateLevel,
    mascotState: MascotState,
    mascotSpeech: String,
    onSelectLevelAndContinue: (DebateLevel) -> Unit,
    onMascotStatePreview: (MascotState) -> Unit,
    onToggleTheme: () -> Unit = {}
) {
    val palette = HojaTheme.palette
    var previewLevel by remember { mutableStateOf(DebateLevel.BEGINNER) }
    var cardImpulse by remember { mutableIntStateOf(0) }

    val slides = remember {
        listOf(
            OnboardingSlide(
                title = "Start at Beginner & Evolve",
                caption = "Begin with Fledgling Kori and unlock higher ranks as you win debates."
            ),
            OnboardingSlide(
                title = "Live Debate Chat & Rerolls",
                caption = "Roll a fresh motion anytime, pick Propose or Oppose, and spar in real time."
            ),
            OnboardingSlide(
                title = "AI Adjudicator & History",
                caption = "See where you went wrong after every match and review saved transcripts."
            )
        )
    }
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val coroutineScope = rememberCoroutineScope()
    val levels = DebateLevel.entries

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.stageBg)
            .storybookWashiBackground()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("onboarding_screen"),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Clean Top Bar (Wordmark + Light/Dark Toggle)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HojaWordmark(large = true)
            ThemeModeQuickToggle(
                isDark = palette.isDark,
                onToggle = onToggleTheme,
                tag = "onboarding_theme_toggle"
            )
        }

        // 2. Animated Panorama Hero Carousel
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(198.dp)
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            coroutineScope.launch {
                                val next = (page + 1) % slides.size
                                pagerState.animateScrollToPage(next)
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedOnboardingIllustration(
                        pageIndex = page,
                        selectedLevel = previewLevel,
                        mascotState = mascotState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = slides[page].title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = palette.inkPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = slides[page].caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.inkSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                slides.indices.forEach { idx ->
                    val active = pagerState.currentPage == idx
                    val pillWidth by animateDpAsState(
                        targetValue = if (active) 24.dp else 8.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
                        label = "PagePillWidth"
                    )
                    val pillColor by animateColorAsState(
                        targetValue = if (active) palette.ctaBg else palette.sketchBorder.copy(alpha = 0.30f),
                        label = "PagePillColor"
                    )
                    Box(
                        modifier = Modifier
                            .height(7.dp)
                            .width(pillWidth)
                            .clip(CircleShape)
                            .background(pillColor)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(idx)
                                }
                            }
                    )
                }
            }
        }

        // 3. Progression Path Showcase (Tap to preview Kori's 4 evolutions; journey begins at Beginner)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FlankedSectionHeader(title = "Your Progression Path")

            for (rowIndex in 0..1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (colIndex in 0..1) {
                        val level = levels[rowIndex * 2 + colIndex]
                        val isPreviewed = previewLevel == level
                        val isStarter = level == DebateLevel.BEGINNER
                        val (washBg, washFg) = when (level) {
                            DebateLevel.BEGINNER -> palette.skyWash to palette.onSkyWash
                            DebateLevel.INTERMEDIATE -> palette.sageWash to palette.onSageWash
                            DebateLevel.ADVANCED -> palette.ochreWash to palette.onOchreWash
                            DebateLevel.CHAMPION -> palette.lavenderWash to palette.onLavenderWash
                        }
                        val titleColor = if (isPreviewed) washFg else palette.inkPrimary
                        val subColor = if (isPreviewed) washFg.copy(alpha = 0.85f) else palette.inkSecondary
                        val levelMood = when (level) {
                            DebateLevel.BEGINNER -> MascotState.ENCOURAGING
                            DebateLevel.INTERMEDIATE -> MascotState.IDLE
                            DebateLevel.ADVANCED -> MascotState.IMPRESSED
                            DebateLevel.CHAMPION -> MascotState.CHEERING
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (!isPreviewed) {
                                        Modifier.dashedStorybookBorder(
                                            color = palette.sketchBorder.copy(alpha = 0.38f),
                                            cornerRadius = 18.dp
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable {
                                    previewLevel = level
                                    cardImpulse++
                                    onMascotStatePreview(levelMood)
                                }
                                .testTag("level_card_${level.name.lowercase()}"),
                            shape = RoundedCornerShape(18.dp),
                            color = if (isPreviewed) washBg else palette.cardSurface,
                            border = if (isPreviewed) {
                                BorderStroke(2.dp, palette.sketchBorder)
                            } else {
                                null
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SecretaryBirdCanvas(
                                    state = if (isPreviewed) mascotState else levelMood,
                                    level = level,
                                    impulseTrigger = if (isPreviewed) cardImpulse else 0,
                                    drawContainedBackdrop = true,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isStarter) "1. ${level.displayName} (Start)" else "${level.ordinal + 1}. ${level.displayName}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = titleColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = level.koriPersonaTitle(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = subColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Primary Start Journey Button (Always starts at Beginner and advances by winning)
        Button(
            onClick = { onSelectLevelAndContinue(DebateLevel.BEGINNER) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("start_onboarding_button"),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.ctaBg,
                contentColor = palette.onCtaBg
            )
        ) {
            Text(
                text = "Begin Journey at Beginner",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun HomeScreen(
    userPrefs: UserProgressPreferences,
    mascotState: MascotState,
    mascotSpeech: String,
    dailyMotionState: DailyMotionUiState,
    debateHistory: List<DebateSessionEntity>,
    onRefreshMotion: () -> Unit,
    onStartPrep: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenHistorySession: (DebateSessionEntity) -> Unit,
    onOpenSettings: () -> Unit,
    onSelectMascotMood: (MascotState) -> Unit,
    onToggleTheme: () -> Unit = {}
) {
    val palette = HojaTheme.palette

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .storybookWashiBackground()
            .testTag("home_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Clean Masthead with HojaWordmark, Streak Flame Pill, History Icon, Theme Toggle & Settings
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HojaWordmark(large = false)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Streak Counter Pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (userPrefs.streakDays > 0) palette.coralWash else palette.subtleSurface,
                        border = BorderStroke(1.dp, palette.subtleBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("streak_counter_badge"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = if (userPrefs.streakDays > 0) palette.coralAccent else palette.inkSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${userPrefs.streakDays}d",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (userPrefs.streakDays > 0) palette.onCoralWash else palette.inkSecondary
                            )
                        }
                    }

                    ThemeModeQuickToggle(
                        isDark = palette.isDark,
                        onToggle = onToggleTheme,
                        tag = "home_theme_toggle"
                    )

                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("open_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Debate History",
                            tint = palette.inkPrimary,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = palette.inkPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 2. Compact Storybook Mascot Stage + Visually Appealing 'Current Streak' & Evolution Badge
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SecretaryBirdMascotStage(
                    state = mascotState,
                    speechText = mascotSpeech,
                    level = userPrefs.level,
                    avatarSize = 84.dp,
                    onStateSelected = onSelectMascotMood
                )

                CurrentStreakDashboardBadge(
                    userPrefs = userPrefs,
                    onCelebrateStreak = { onSelectMascotMood(MascotState.CHEERING) }
                )
            }
        }

        // 3. Today's Motion Card with Fast Gemini Reroll Button
        item {
            DailyMotionScrollCard(
                dailyMotionState = dailyMotionState,
                onRerollMotion = onRefreshMotion,
                onEngageMotion = onStartPrep
            )
        }

        // 4. Compact Past Debates Preview + "View Full History" Button
        if (debateHistory.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Sessions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.inkPrimary
                    )
                    TextButton(
                        onClick = onOpenHistory,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "View All (${debateHistory.size}) →",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.coralAccent
                        )
                    }
                }
            }
            items(debateHistory.take(2), key = { it.id }) { session ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenHistorySession(session) }
                        .testTag("home_history_item_${session.id}"),
                    shape = RoundedCornerShape(16.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.dp, palette.subtleBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (session.overallScore >= 7.5) palette.sageWash else palette.skyWash
                        ) {
                            Text(
                                text = "${session.overallScore}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (session.overallScore >= 7.5) palette.onSageWash else palette.onSkyWash,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.motion,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.inkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${session.userSide} · ${session.dateKey} · Tap to review",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.inkSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Review session",
                            tint = palette.inkSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyMotionScrollCard(
    dailyMotionState: DailyMotionUiState,
    onRerollMotion: () -> Unit,
    onEngageMotion: () -> Unit
) {
    val palette = HojaTheme.palette
    var selectedTerm by remember { mutableStateOf<KeyTerm?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_motion_card"),
        shape = RoundedCornerShape(24.dp),
        color = palette.cardSurface,
        border = BorderStroke(1.5.dp, palette.subtleBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = palette.ochreWash
                ) {
                    Text(
                        text = "Today's Motion",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.onOchreWash,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                // Explicit Reroll Motion Pill Button
                Surface(
                    modifier = Modifier
                        .clickable(
                            enabled = dailyMotionState !is DailyMotionUiState.Loading,
                            onClick = onRerollMotion
                        )
                        .testTag("refresh_motion_button"),
                    shape = RoundedCornerShape(50),
                    color = palette.subtleSurface,
                    border = BorderStroke(1.dp, palette.subtleBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reroll motion",
                            tint = palette.inkPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Reroll",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.inkPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (dailyMotionState) {
                is DailyMotionUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = palette.ctaBg
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Rolling fresh motion with Gemini...",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.inkSecondary
                        )
                    }
                }

                is DailyMotionUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = dailyMotionState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onRerollMotion,
                            modifier = Modifier.testTag("retry_motion_button"),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Retry Reroll")
                        }
                    }
                }

                is DailyMotionUiState.Success -> {
                    val motion: DailyMotion = dailyMotionState.motion
                    Text(
                        text = motion.motion,
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.inkPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = motion.shortContext,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.inkSecondary
                    )

                    if (motion.keyTerms.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            motion.keyTerms.forEach { kt ->
                                val isTermSelected = selectedTerm == kt
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isTermSelected) {
                                        palette.skyWash
                                    } else {
                                        palette.subtleSurface
                                    },
                                    border = if (isTermSelected) {
                                        BorderStroke(1.2.dp, palette.sketchBorder)
                                    } else {
                                        null
                                    },
                                    modifier = Modifier
                                        .then(
                                            if (!isTermSelected) {
                                                Modifier.dashedStorybookBorder(
                                                    color = palette.sketchBorder.copy(alpha = 0.40f),
                                                    cornerRadius = 10.dp
                                                )
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .clickable {
                                            selectedTerm = if (isTermSelected) null else kt
                                        }
                                ) {
                                    Text(
                                        text = kt.term,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isTermSelected) palette.onSkyWash else palette.inkPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = selectedTerm != null) {
                            selectedTerm?.let { term ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = palette.skyWash
                                ) {
                                    Text(
                                        text = "${term.term}: ${term.definition}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.onSkyWash,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onEngageMotion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_prep_button"),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.ctaBg,
                            contentColor = palette.onCtaBg
                        )
                    ) {
                        Text(
                            text = "Engage with Motion",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visually appealing Japanese hand-drawn 'Current Streak' badge for the main dashboard:
 * Features an animated Sumi & Vermilion gouache flame emblem, explicit 'Current Streak' counter,
 * 7-day flame momentum dots, and integrated Next Evolution progress bar.
 */
@Composable
fun CurrentStreakDashboardBadge(
    userPrefs: UserProgressPreferences,
    onCelebrateStreak: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = HojaTheme.palette
    val infiniteTransition = rememberInfiniteTransition(label = "StreakFlamePulse")
    val flamePulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlameScale"
    )

    val streakCount = maxOf(1, userPrefs.streakDays)
    val bestStreak = maxOf(streakCount, userPrefs.bestStreakDays)
    val litDots = (streakCount % 7).let { if (it == 0 && streakCount > 0) 7 else it }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCelebrateStreak() }
            .testTag("current_streak_badge"),
        shape = RoundedCornerShape(20.dp),
        color = palette.cardSurface,
        border = BorderStroke(1.5.dp, palette.sketchBorder.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Hand-drawn Sumi & Vermilion Flame Medallion
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val baseRadius = size.minDimension * 0.44f

                            // Warm Gouache Aura
                            drawCircle(
                                color = palette.coralWash,
                                radius = baseRadius * flamePulse,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = palette.sketchBorder,
                                radius = baseRadius,
                                center = Offset(cx, cy),
                                style = Stroke(width = 1.8.dp.toPx())
                            )

                            // Outer Vermilion Flame Path
                            val outerFlame = Path().apply {
                                moveTo(cx, cy - baseRadius * 0.62f * flamePulse)
                                cubicTo(
                                    cx + baseRadius * 0.55f,
                                    cy - baseRadius * 0.15f,
                                    cx + baseRadius * 0.52f,
                                    cy + baseRadius * 0.48f,
                                    cx,
                                    cy + baseRadius * 0.56f
                                )
                                cubicTo(
                                    cx - baseRadius * 0.52f,
                                    cy + baseRadius * 0.48f,
                                    cx - baseRadius * 0.55f,
                                    cy - baseRadius * 0.15f,
                                    cx,
                                    cy - baseRadius * 0.62f * flamePulse
                                )
                                close()
                            }
                            drawPath(path = outerFlame, color = palette.coralAccent)
                            drawPath(
                                path = outerFlame,
                                color = palette.sketchBorder,
                                style = Stroke(
                                    width = 1.6.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Inner Golden Ochre Flame Core
                            val innerFlame = Path().apply {
                                moveTo(cx, cy - baseRadius * 0.22f * flamePulse)
                                cubicTo(
                                    cx + baseRadius * 0.26f,
                                    cy + baseRadius * 0.05f,
                                    cx + baseRadius * 0.24f,
                                    cy + baseRadius * 0.42f,
                                    cx,
                                    cy + baseRadius * 0.48f
                                )
                                cubicTo(
                                    cx - baseRadius * 0.24f,
                                    cy + baseRadius * 0.42f,
                                    cx - baseRadius * 0.26f,
                                    cy + baseRadius * 0.05f,
                                    cx,
                                    cy - baseRadius * 0.22f * flamePulse
                                )
                                close()
                            }
                            drawPath(path = innerFlame, color = palette.ochreAccent)
                        }
                    }

                    Column {
                        Text(
                            text = "CURRENT STREAK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.coralAccent
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "$streakCount ${if (streakCount == 1) "Day" else "Days"}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = palette.inkPrimary,
                                modifier = Modifier.testTag("current_streak_count_text")
                            )
                            Text(
                                text = "Best $bestStreak",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.inkSecondary,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }
                }

                // 7-Day Consecutive Momentum Stamp
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = palette.ochreWash,
                        border = BorderStroke(1.dp, palette.ochreAccent.copy(alpha = 0.55f))
                    ) {
                        Text(
                            text = "Active Today",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.onOchreWash,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (dayIdx in 1..7) {
                            val isLit = dayIdx <= litDots
                            Box(
                                modifier = Modifier
                                    .size(if (isLit) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isLit) palette.coralAccent else palette.subtleBorder
                                    )
                            )
                        }
                    }
                }
            }

            // Integrated Next Rank Evolution Progress Bar
            val nextLevel = userPrefs.level.nextLevelOrNull()
            if (nextLevel != null && userPrefs.level.winsRequiredToAdvance > 0) {
                val progressTarget = (userPrefs.rankProgressWins.toFloat() / userPrefs.level.winsRequiredToAdvance.toFloat())
                    .coerceIn(0f, 1f)
                val animatedProgress by animateFloatAsState(
                    targetValue = progressTarget,
                    animationSpec = tween(500, easing = FastOutSlowInEasing),
                    label = "RankProgress"
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Evolution · ${nextLevel.koriPersonaTitle()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.inkPrimary
                        )
                        Text(
                            text = "${userPrefs.rankProgressWins}/${userPrefs.level.winsRequiredToAdvance} strong debates",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.inkSecondary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = palette.sageAccent,
                        trackColor = palette.subtleSurface
                    )
                }
            }
        }
    }
}

