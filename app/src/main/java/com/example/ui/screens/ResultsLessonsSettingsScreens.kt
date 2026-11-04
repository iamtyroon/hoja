package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DebateSessionEntity
import com.example.data.model.DebateLevel
import com.example.data.model.MascotState
import com.example.data.model.ThemeMode
import com.example.data.repository.HojaRepository
import com.example.data.repository.UserProgressPreferences
import com.example.ui.components.CompactMascotCoachBanner
import com.example.ui.components.FlankedSectionHeader
import com.example.ui.components.SecretaryBirdCanvas
import com.example.ui.components.dashedStorybookBorder
import com.example.ui.components.koriGearDescription
import com.example.ui.components.koriPersonaTitle
import com.example.ui.components.storybookWashiBackground
import com.example.ui.theme.HojaTheme
import com.example.ui.viewmodel.ActiveDebateUiState

/**
 * Post-Debate AI Adjudicator Analysis & Saved Session Viewer:
 * Reused for both live debate endings and Room History reviews (Ponytail re-use principle).
 * Lets users inspect the AI Adjudicator analysis AND toggle to the full saved chat transcript.
 */
@Composable
fun ResultsScreen(
    level: DebateLevel,
    mascotState: MascotState,
    mascotSpeech: String,
    debateState: ActiveDebateUiState,
    onReturnHome: () -> Unit,
    onOpenHistory: () -> Unit,
    onDebateAgain: () -> Unit
) {
    val handleBack = if (debateState.isReviewingPastSession) onOpenHistory else onReturnHome
    BackHandler(onBack = handleBack)

    val score = debateState.scoreResult ?: return
    val palette = HojaTheme.palette
    var selectedTab by remember(debateState.reviewedSessionDateKey, debateState.motion?.motion) {
        mutableIntStateOf(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .storybookWashiBackground()
            .testTag("results_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Top Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = handleBack,
                        modifier = Modifier.testTag("results_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.inkPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (debateState.isReviewingPastSession) {
                                "Saved Debate Review"
                            } else {
                                "AI Adjudicator Analysis"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = palette.inkPrimary
                        )
                        val motionText = debateState.motion?.motion.orEmpty()
                        if (motionText.isNotBlank()) {
                            Text(
                                text = motionText,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.inkSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = palette.ochreWash
                    ) {
                        Text(
                            text = debateState.userSide.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.onOchreWash,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 2. Celebratory Rank Up Banner (when promoted after a qualifying debate)
            debateState.promotedToLevel?.let { promoted ->
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rank_up_banner"),
                        shape = RoundedCornerShape(22.dp),
                        color = palette.lavenderWash,
                        border = BorderStroke(2.dp, palette.sketchBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SecretaryBirdCanvas(
                                state = MascotState.CHEERING,
                                level = promoted,
                                drawContainedBackdrop = true,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "RANK UP UNLOCKED!",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.onLavenderWash
                                )
                                Text(
                                    text = "${promoted.koriPersonaTitle()} • ${promoted.displayName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.onLavenderWash
                                )
                                Text(
                                    text = promoted.koriGearDescription(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.onLavenderWash.copy(alpha = 0.88f)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Chief Adjudicator Verdict Summary
            item {
                CompactMascotCoachBanner(
                    state = mascotState,
                    message = mascotSpeech,
                    level = level
                )
            }

            // 4. Pill Toggle: AI Analysis vs Saved Transcript (from Room)
            if (debateState.transcript.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(palette.subtleSurface, RoundedCornerShape(50))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tabs = listOf(
                            "AI Analysis" to "results_tab_analysis",
                            "Saved Transcript (${debateState.transcript.size})" to "results_tab_transcript"
                        )
                        tabs.forEachIndexed { idx, (title, tag) ->
                            val active = selectedTab == idx
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTab = idx }
                                    .testTag(tag),
                                shape = RoundedCornerShape(50),
                                color = if (active) palette.cardSurface else palette.subtleSurface,
                                border = if (active) BorderStroke(1.2.dp, palette.sketchBorder) else null
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                    color = if (active) palette.inkPrimary else palette.inkSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                // TAB 0: AI Adjudicator Analysis
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = palette.coralWash,
                        border = BorderStroke(1.5.dp, palette.coralAccent.copy(alpha = 0.65f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Where You Went Wrong",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.onCoralWash
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            score.improvements.forEach { item ->
                                Text(
                                    text = "• $item",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.onCoralWash,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rewrite_it_panel"),
                        shape = RoundedCornerShape(20.dp),
                        color = palette.cardSurface,
                        border = BorderStroke(1.2.dp, palette.subtleBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "How to Fix Your Weakest Line",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = palette.inkPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .fillMaxHeight()
                                        .background(palette.coralAccent)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "What you wrote",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.inkSecondary
                                    )
                                    Text(
                                        text = "“${score.rewriteIt.original}”",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontStyle = FontStyle.Italic,
                                        color = palette.inkPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .fillMaxHeight()
                                        .background(palette.sageAccent)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Stronger rebuttal framing",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.sageAccent
                                    )
                                    Text(
                                        text = "“${score.rewriteIt.improved}”",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = palette.inkPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Why: ${score.rewriteIt.reason}",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.inkSecondary
                            )
                        }
                    }
                }

                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("score_breakdown_card"),
                        shape = RoundedCornerShape(22.dp),
                        color = palette.cardSurface,
                        border = BorderStroke(1.2.dp, palette.subtleBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Adjudicator Score",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.coralAccent
                                    )
                                    Text(
                                        text = when {
                                            score.overall >= 8.5 -> "Tournament Distinction"
                                            score.overall >= 7.0 -> "Persuasive Case"
                                            else -> "Developing Case"
                                        },
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.inkPrimary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = palette.sageWash,
                                    border = BorderStroke(1.5.dp, palette.sageAccent)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = "${score.overall}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = palette.onSageWash
                                        )
                                        Text(
                                            text = " /10",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.onSageWash.copy(alpha = 0.85f),
                                            modifier = Modifier.padding(bottom = 3.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            score.strengths.forEach { item ->
                                Text(
                                    text = "✓ $item",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.inkPrimary,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            RubricScoreRow("Argument & Logic", score.argumentStrength)
                            Spacer(modifier = Modifier.height(8.dp))
                            RubricScoreRow("Direct Rebuttal", score.rebuttal)
                            Spacer(modifier = Modifier.height(8.dp))
                            RubricScoreRow("Evidence & Examples", score.evidence)
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.fact_check_reminder),
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.inkSecondary
                    )
                }
            } else {
                // TAB 1: Full Saved Debate Transcript (reusing DebateChatBubble)
                items(
                    items = debateState.transcript,
                    key = { it.id }
                ) { turn ->
                    DebateChatBubble(turn = turn, level = level)
                }
            }
        }

        // Pinned Bottom Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = palette.cardSurface,
            border = BorderStroke(1.dp, palette.subtleBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (debateState.isReviewingPastSession) {
                    OutlinedButton(
                        onClick = onOpenHistory,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("back_to_history_button"),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.5.dp, palette.sketchBorder.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "All History",
                            fontWeight = FontWeight.Bold,
                            color = palette.inkPrimary
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onDebateAgain,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("debate_again_button"),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.5.dp, palette.sketchBorder.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "Debate Again",
                            fontWeight = FontWeight.Bold,
                            color = palette.inkPrimary
                        )
                    }
                }

                Button(
                    onClick = onReturnHome,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("results_home_button"),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.ctaBg,
                        contentColor = palette.onCtaBg
                    )
                ) {
                    Text(
                        text = "Home",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RubricScoreRow(label: String, score: Int) {
    val palette = HojaTheme.palette
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = palette.inkPrimary
            )
            Text(
                text = "$score / 10",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = palette.inkPrimary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (score / 10f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = when {
                score >= 8 -> palette.sageAccent
                score >= 6 -> palette.ochreAccent
                else -> palette.coralAccent
            },
            trackColor = palette.subtleSurface
        )
    }
}

/**
 * Room Database Debate History Screen:
 * Displays past completed debate sessions with their saved AI analysis and full chat transcripts.
 */
@Composable
fun HistoryScreen(
    userPrefs: UserProgressPreferences,
    debateHistory: List<DebateSessionEntity>,
    onSelectSession: (DebateSessionEntity) -> Unit,
    onDeleteSession: (Long) -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler(onBack = onBackToHome)
    val palette = HojaTheme.palette

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .storybookWashiBackground()
            .testTag("history_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier.testTag("history_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = palette.inkPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Debate History",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.inkPrimary
                    )
                    Text(
                        text = "${debateHistory.size} saved Room sessions • Tap any debate to review transcript & AI analysis",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.inkSecondary
                    )
                }
            }
        }

        // Streak & Rank Summary Banner
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = palette.cardSurface,
                border = BorderStroke(1.2.dp, palette.subtleBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HistoryStatColumn(
                        label = "Active Streak",
                        value = "${userPrefs.streakDays}d 🔥",
                        accent = palette.coralAccent
                    )
                    HistoryStatColumn(
                        label = "Best Streak",
                        value = "${userPrefs.bestStreakDays}d",
                        accent = palette.ochreAccent
                    )
                    HistoryStatColumn(
                        label = "Current Rank",
                        value = userPrefs.level.displayName,
                        accent = palette.sageAccent
                    )
                }
            }
        }

        if (debateHistory.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .testTag("history_empty_state"),
                    shape = RoundedCornerShape(24.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.5.dp, palette.subtleBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SecretaryBirdCanvas(
                            state = MascotState.THINKING,
                            level = userPrefs.level,
                            drawContainedBackdrop = true,
                            modifier = Modifier.size(110.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Debates Archived Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.inkPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Complete your first debate on today's motion to save its full transcript and AI Adjudicator feedback in Room.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.inkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(debateHistory, key = { it.id }) { session ->
                val parsedScore = remember(session.id, session.strengthsJson) {
                    HojaRepository.parseSessionScoreResult(session)
                }
                val parsedTurns = remember(session.id, session.transcriptJson) {
                    HojaRepository.parseSessionTranscript(session)
                }
                val topBlindspot = parsedScore.improvements.firstOrNull()

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSession(session) }
                        .testTag("history_session_card_${session.id}"),
                    shape = RoundedCornerShape(20.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.2.dp, palette.subtleBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = palette.ochreWash
                                ) {
                                    Text(
                                        text = session.userSide,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.onOchreWash,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = "${session.dateKey} • ${parsedTurns.size} turns",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.inkSecondary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = palette.sageWash,
                                    border = BorderStroke(1.dp, palette.sageAccent.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "${session.overallScore}/10",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.onSageWash,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteSession(session.id) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("delete_history_session_${session.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "Delete Session",
                                        tint = palette.inkSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = session.motion,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.inkPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!topBlindspot.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Blindspot: $topBlindspot",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.inkSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryStatColumn(
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color
) {
    val palette = HojaTheme.palette
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = palette.inkSecondary
        )
    }
}

@Composable
fun SettingsScreen(
    userPrefs: UserProgressPreferences,
    onSelectThemeMode: (ThemeMode) -> Unit = {},
    onResetProgress: () -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler(onBack = onBackToHome)

    val palette = HojaTheme.palette
    var showResetDialog by remember { mutableStateOf(false) }
    val levels = DebateLevel.entries

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Debate Progress?") },
            text = {
                Text("This clears your Room debate history, streaks, and rank progress, returning you to Beginner.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetProgress()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .storybookWashiBackground()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = palette.inkPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.inkPrimary
                )
            }
        }

        // 1. Appearance Selector
        item {
            FlankedSectionHeader(title = "Appearance")
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThemeMode.entries.forEach { mode ->
                    val selected = userPrefs.themeMode == mode
                    val icon = when (mode) {
                        ThemeMode.LIGHT -> Icons.Outlined.LightMode
                        ThemeMode.DARK -> Icons.Outlined.DarkMode
                        ThemeMode.SYSTEM -> Icons.Outlined.BrightnessAuto
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (!selected) {
                                    Modifier.dashedStorybookBorder(
                                        color = palette.sketchBorder.copy(alpha = 0.40f),
                                        cornerRadius = 16.dp
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { onSelectThemeMode(mode) }
                            .testTag("theme_mode_${mode.name.lowercase()}"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) palette.ochreWash else palette.cardSurface,
                        border = if (selected) {
                            BorderStroke(2.dp, palette.sketchBorder)
                        } else {
                            null
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = mode.displayName,
                                tint = if (selected) palette.onOchreWash else palette.inkPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = mode.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) palette.onOchreWash else palette.inkPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // 2. Kori Evolution & Streak Status
        item {
            FlankedSectionHeader(title = "Progression & Streaks")
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = palette.cardSurface,
                border = BorderStroke(1.2.dp, palette.subtleBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = palette.coralAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "${userPrefs.streakDays} Day Streak (Best: ${userPrefs.bestStreakDays}d)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = palette.inkPrimary
                            )
                        }
                        Text(
                            text = "${userPrefs.totalDebatesCompleted} Debates",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.inkSecondary
                        )
                    }

                    levels.forEach { rank ->
                        val isUnlocked = rank.ordinal <= userPrefs.level.ordinal
                        val isCurrent = rank == userPrefs.level
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isCurrent) palette.sageWash else palette.subtleSurface,
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SecretaryBirdCanvas(
                                    state = if (isCurrent) MascotState.CHEERING else MascotState.IDLE,
                                    level = rank,
                                    drawContainedBackdrop = true,
                                    modifier = Modifier.size(36.dp)
                                )
                                Column {
                                    Text(
                                        text = "${rank.koriPersonaTitle()} • ${rank.displayName}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) palette.onSageWash else palette.inkPrimary
                                    )
                                    Text(
                                        text = rank.koriGearDescription(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isCurrent) {
                                            palette.onSageWash.copy(alpha = 0.85f)
                                        } else {
                                            palette.inkSecondary
                                        }
                                    )
                                }
                            }
                            Text(
                                text = when {
                                    isCurrent -> "Current"
                                    isUnlocked -> "Unlocked"
                                    else -> "Score ≥${rank.qualifyingScoreThreshold()}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) palette.onSageWash else palette.inkSecondary
                            )
                        }
                    }
                }
            }
        }

        // 3. Reset Progress
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = palette.cardSurface,
                border = BorderStroke(1.2.dp, palette.subtleBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reset Progress",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.inkPrimary
                        )
                        Text(
                            text = "Clear Room history, streaks, and return to Beginner",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.inkSecondary
                        )
                    }
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag("reset_progress_button"),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Reset",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
