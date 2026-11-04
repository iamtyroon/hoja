package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.DebateLevel
import com.example.data.model.DebateRound
import com.example.data.model.DebateSide
import com.example.data.model.DebateTurnMessage
import com.example.data.model.MascotState
import com.example.data.model.TurnSpeaker
import com.example.ui.components.CompactMascotCoachBanner
import com.example.ui.components.HandDrawnSpotIcon
import com.example.ui.components.SecretaryBirdCanvas
import com.example.ui.components.SpotArtType
import com.example.ui.components.dashedStorybookBorder
import com.example.ui.components.storybookWashiBackground
import com.example.ui.theme.HojaTheme
import com.example.ui.viewmodel.ActiveDebateUiState

@Composable
fun PrepScreen(
    level: DebateLevel,
    mascotState: MascotState,
    mascotSpeech: String,
    debateState: ActiveDebateUiState,
    onToggleHint: () -> Unit,
    onSelectSide: (DebateSide) -> Unit,
    onStartDebate: () -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler(onBack = onBackToHome)

    val motion = debateState.motion ?: return
    val palette = HojaTheme.palette

    Column(
        modifier = Modifier
            .fillMaxSize()
            .storybookWashiBackground()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("prep_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier.testTag("prep_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = palette.inkPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Choose Your Side",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.inkPrimary
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = palette.cardSurface,
                border = BorderStroke(1.2.dp, palette.subtleBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = motion.motion,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.inkPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = motion.shortContext,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.inkSecondary
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pick Your Bench",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.inkPrimary
                    )
                    Text(
                        text = "AI counters as ${debateState.userSide.opposite().label}",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.coralAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DebateSide.entries.forEach { side ->
                        val selected = debateState.userSide == side
                        val (activeFill, activeFg) = if (side == DebateSide.PROPOSE) {
                            palette.sageWash to palette.onSageWash
                        } else {
                            palette.coralWash to palette.onCoralWash
                        }
                        val spotType = if (side == DebateSide.PROPOSE) {
                            SpotArtType.BAMBOO_PROPOSE
                        } else {
                            SpotArtType.SHIELD_OPPOSE
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (!selected) {
                                        Modifier.dashedStorybookBorder(
                                            color = palette.sketchBorder.copy(alpha = 0.40f),
                                            cornerRadius = 20.dp
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { onSelectSide(side) }
                                .testTag("pick_side_${side.name.lowercase()}"),
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) activeFill else palette.cardSurface,
                            border = if (selected) {
                                BorderStroke(2.2.dp, palette.sketchBorder)
                            } else {
                                null
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                HandDrawnSpotIcon(type = spotType)
                                Text(
                                    text = side.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) activeFg else palette.inkPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = side.roleDescription,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) activeFg.copy(alpha = 0.85f) else palette.inkSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Need an opening angle?",
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.inkSecondary
                    )
                    TextButton(
                        onClick = onToggleHint,
                        modifier = Modifier.testTag("toggle_hint_button")
                    ) {
                        Text(
                            text = if (debateState.isHintRevealed) "Hide spark" else "Show spark",
                            fontWeight = FontWeight.Bold,
                            color = palette.coralAccent
                        )
                    }
                }

                AnimatedVisibility(visible = debateState.isHintRevealed) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hint_content_card"),
                        shape = RoundedCornerShape(16.dp),
                        color = palette.ochreWash,
                        border = BorderStroke(1.dp, palette.subtleBorder)
                    ) {
                        Text(
                            text = if (debateState.userSide == DebateSide.PROPOSE) {
                                motion.proposeHint
                            } else {
                                motion.opposeHint
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.onOchreWash,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CompactMascotCoachBanner(
                state = mascotState,
                message = mascotSpeech,
                level = level
            )

            Button(
                onClick = onStartDebate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("begin_written_debate_button"),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.ctaBg,
                    contentColor = palette.onCtaBg
                )
            ) {
                Text(
                    text = "Enter Debate Chat as ${debateState.userSide.label}",
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

@Composable
fun DebateScreen(
    level: DebateLevel,
    debateState: ActiveDebateUiState,
    onUpdateDraft: (String) -> Unit,
    onInsertSentenceStarter: (String) -> Unit,
    onSendTurn: () -> Unit,
    onFinishAndAnalyze: () -> Unit = {},
    onRetryError: () -> Unit,
    onBackToPrep: () -> Unit
) {
    BackHandler(onBack = onBackToPrep)

    val motion = debateState.motion ?: return
    val palette = HojaTheme.palette
    val listState = rememberLazyListState()

    val sentenceStarters = remember(debateState.currentRound) {
        when (debateState.currentRound) {
            DebateRound.OPENING -> listOf(
                "My first point is that ",
                "This holds true because ",
                "For example, in Kenya, ",
                "Furthermore, "
            )
            DebateRound.REBUTTAL -> listOf(
                "My opponent claimed that ",
                "However, this overlooks ",
                "Our case still stands because ",
                "Across East Africa, "
            )
            DebateRound.CLOSING -> listOf(
                "The central clash is ",
                "Even if we accept their view, ",
                "Our side wins because ",
                "Therefore, this House must "
            )
        }
    }

    LaunchedEffect(debateState.transcript.size, debateState.isOpponentThinking, debateState.isScoringDebate) {
        val totalItems = debateState.transcript.size + 2
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .storybookWashiBackground()
            .testTag("debate_screen")
    ) {
        // 1. Pinned Top Debate Chat Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = palette.cardSurface,
            border = BorderStroke(1.dp, palette.subtleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBackToPrep,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("debate_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = palette.inkPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Round ${debateState.currentRound.roundNumber}/3 · ${debateState.currentRound.title}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.inkPrimary
                            )
                            Text(
                                text = "You (${debateState.userSide.label}) vs AI (${debateState.userSide.opposite().label})",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.inkSecondary
                            )
                        }
                    }

                    if (debateState.transcript.size >= 2 && !debateState.isOpponentThinking && !debateState.isScoringDebate) {
                        Surface(
                            modifier = Modifier
                                .clickable(onClick = onFinishAndAnalyze)
                                .testTag("finish_analyze_button"),
                            shape = RoundedCornerShape(50),
                            color = palette.coralWash,
                            border = BorderStroke(1.dp, palette.coralAccent)
                        ) {
                            Text(
                                text = "End & Analyze",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.onCoralWash,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        val isPropose = debateState.userSide == DebateSide.PROPOSE
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isPropose) palette.sageWash else palette.coralWash
                        ) {
                            Text(
                                text = debateState.userSide.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isPropose) palette.onSageWash else palette.onCoralWash,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DebateRound.entries.forEach { r ->
                        val isCurrent = r == debateState.currentRound
                        val isDone = r.roundNumber < debateState.currentRound.roundNumber
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCurrent -> palette.ctaBg
                                        isDone -> palette.sageAccent
                                        else -> palette.subtleBorder
                                    }
                                )
                        )
                    }
                }
            }
        }

        // 2. Center Auto-Scrolling Chat Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = palette.subtleSurface,
                    border = BorderStroke(1.dp, palette.subtleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "MOTION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.coralAccent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = motion.motion,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.inkPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = debateState.currentRound.coachGuidance,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.inkSecondary
                        )
                    }
                }
            }

            items(debateState.transcript, key = { it.id }) { turn ->
                DebateChatBubble(
                    turn = turn,
                    level = level
                )
            }

            if (debateState.isOpponentThinking || debateState.isScoringDebate) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SecretaryBirdCanvas(
                            state = MascotState.THINKING,
                            level = level,
                            drawContainedBackdrop = true,
                            modifier = Modifier.size(40.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 6.dp,
                                topEnd = 18.dp,
                                bottomEnd = 18.dp,
                                bottomStart = 18.dp
                            ),
                            color = palette.skyWash,
                            border = BorderStroke(1.dp, palette.subtleBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = palette.onSkyWash
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (debateState.isScoringDebate) {
                                        "AI Adjudicator is analyzing the debate & saving session..."
                                    } else {
                                        "Mwalimu Kori (${debateState.userSide.opposite().label}) is countering..."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.onSkyWash
                                )
                            }
                        }
                    }
                }
            }

            if (debateState.actionError != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = palette.coralWash
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = debateState.actionError,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.onCoralWash,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            OutlinedButton(
                                onClick = onRetryError,
                                modifier = Modifier.testTag("retry_debate_action_button"),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }

        // 3. Pinned Bottom Chat Composer Bar
        if (!debateState.isScoringDebate && debateState.scoreResult == null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = palette.cardSurface,
                border = BorderStroke(1.dp, palette.subtleBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sentenceStarters.forEachIndexed { idx, starter ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = palette.subtleSurface,
                                border = BorderStroke(1.dp, palette.subtleBorder),
                                modifier = Modifier
                                    .clickable { onInsertSentenceStarter(starter) }
                                    .testTag("sentence_starter_$idx")
                            ) {
                                Text(
                                    text = starter.trim(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.inkPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = debateState.currentDraft,
                            onValueChange = onUpdateDraft,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp, max = 116.dp)
                                .testTag("debate_turn_input"),
                            placeholder = {
                                Text(
                                    text = "Table your ${debateState.currentRound.title.lowercase()} point for ${debateState.userSide.label}...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.inkSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            enabled = !debateState.isOpponentThinking,
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = palette.subtleSurface,
                                unfocusedContainerColor = palette.subtleSurface,
                                disabledContainerColor = palette.subtleSurface,
                                focusedTextColor = palette.inkPrimary,
                                unfocusedTextColor = palette.inkPrimary,
                                focusedBorderColor = palette.sketchBorder,
                                unfocusedBorderColor = palette.subtleBorder
                            )
                        )

                        Button(
                            onClick = onSendTurn,
                            enabled = debateState.canSendTurn(level),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("send_turn_button"),
                            shape = RoundedCornerShape(26.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.ctaBg,
                                contentColor = palette.onCtaBg
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send point",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DebateChatBubble(
    turn: DebateTurnMessage,
    level: DebateLevel
) {
    val palette = HojaTheme.palette
    val isUser = turn.speaker == TurnSpeaker.USER
    var showModelAnswer by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            SecretaryBirdCanvas(
                state = MascotState.ANNOUNCING,
                level = level,
                drawContainedBackdrop = true,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            modifier = Modifier.widthIn(max = 310.dp),
            shape = RoundedCornerShape(
                topStart = if (isUser) 18.dp else 6.dp,
                topEnd = if (isUser) 6.dp else 18.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp
            ),
            color = if (isUser) palette.sageWash else palette.cardSurface,
            border = BorderStroke(
                width = 1.2.dp,
                color = if (isUser) palette.sageAccent.copy(alpha = 0.55f) else palette.subtleBorder
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isUser) {
                        "You · ${turn.side.label} (${turn.round.title})"
                    } else {
                        "Mwalimu Kori · ${turn.side.label} (${turn.round.title})"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) palette.onSageWash else palette.coralAccent
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = turn.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) palette.onSageWash else palette.inkPrimary
                )

                if (!isUser && !turn.exampleStrongAnswer.isNullOrBlank()) {
                    TextButton(
                        onClick = { showModelAnswer = !showModelAnswer },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (showModelAnswer) "Hide counter-tip" else "How to counter this",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = palette.coralAccent
                        )
                    }
                    AnimatedVisibility(visible = showModelAnswer) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 2.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = palette.ochreWash
                        ) {
                            Text(
                                text = turn.exampleStrongAnswer,
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = palette.onOchreWash,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
