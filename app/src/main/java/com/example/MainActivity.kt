package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ThemeMode
import com.example.ui.screens.DebateScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PrepScreen
import com.example.ui.screens.ResultsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.HojaTheme
import com.example.ui.viewmodel.HojaScreen
import com.example.ui.viewmodel.HojaViewModel
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            HojaApp()
        }
    }
}

@Composable
fun HojaApp(viewModel: HojaViewModel = viewModel()) {
    val userPrefs by viewModel.userPrefs.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val mascotState by viewModel.mascotState.collectAsStateWithLifecycle()
    val mascotSpeech by viewModel.mascotSpeech.collectAsStateWithLifecycle()
    val dailyMotionState by viewModel.dailyMotionState.collectAsStateWithLifecycle()
    val debateState by viewModel.debateState.collectAsStateWithLifecycle()
    val debateHistory by viewModel.debateHistory.collectAsStateWithLifecycle()

    val systemDark = isSystemInDarkTheme()
    val isDark = when (userPrefs.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    HojaTheme(darkTheme = isDark) {
        val palette = HojaTheme.palette

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = palette.pageBg
        ) {
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                containerColor = palette.pageBg
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 640.dp)
                            .fillMaxSize()
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                val forward = targetState.ordinal >= initialState.ordinal
                                val slideDirection = if (forward) 1 else -1
                                (slideInHorizontally(
                                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                                    initialOffsetX = { fullWidth -> (fullWidth * 0.18f * slideDirection).toInt() }
                                ) + fadeIn(tween(260, easing = FastOutSlowInEasing))) togetherWith
                                    (slideOutHorizontally(
                                        animationSpec = tween(220, easing = FastOutSlowInEasing),
                                        targetOffsetX = { fullWidth -> (-fullWidth * 0.14f * slideDirection).toInt() }
                                    ) + fadeOut(tween(180)))
                            },
                            label = "StorybookScreenTransition"
                        ) { screen ->
                            when (screen) {
                                HojaScreen.ONBOARDING -> {
                                    OnboardingScreen(
                                        initialLevel = userPrefs.level,
                                        mascotState = mascotState,
                                        mascotSpeech = mascotSpeech,
                                        onSelectLevelAndContinue = { level ->
                                            viewModel.selectOnboardingLevelAndStart(level)
                                        },
                                        onMascotStatePreview = { previewState ->
                                            viewModel.updateMascot(previewState)
                                        },
                                        onToggleTheme = {
                                            viewModel.toggleLightDarkTheme(isDark)
                                        }
                                    )
                                }

                                HojaScreen.HOME -> {
                                    HomeScreen(
                                        userPrefs = userPrefs,
                                        mascotState = mascotState,
                                        mascotSpeech = mascotSpeech,
                                        dailyMotionState = dailyMotionState,
                                        debateHistory = debateHistory,
                                        onRefreshMotion = { viewModel.loadTodayMotion(forceRefresh = true) },
                                        onStartPrep = { viewModel.openPrepForCurrentMotion() },
                                        onOpenHistory = { viewModel.navigateTo(HojaScreen.HISTORY) },
                                        onOpenHistorySession = { session ->
                                            viewModel.openHistorySession(session)
                                        },
                                        onOpenSettings = { viewModel.navigateTo(HojaScreen.SETTINGS) },
                                        onSelectMascotMood = { mood -> viewModel.updateMascot(mood) },
                                        onToggleTheme = {
                                            viewModel.toggleLightDarkTheme(isDark)
                                        }
                                    )
                                }

                                HojaScreen.PREP -> {
                                    PrepScreen(
                                        level = userPrefs.level,
                                        mascotState = mascotState,
                                        mascotSpeech = mascotSpeech,
                                        debateState = debateState,
                                        onToggleHint = { viewModel.togglePrepHint() },
                                        onSelectSide = { viewModel.selectDebateSide(it) },
                                        onStartDebate = { viewModel.startWrittenDebate() },
                                        onBackToHome = { viewModel.navigateTo(HojaScreen.HOME) }
                                    )
                                }

                                HojaScreen.DEBATE -> {
                                    DebateScreen(
                                        level = userPrefs.level,
                                        debateState = debateState,
                                        onUpdateDraft = { viewModel.updateCurrentDraft(it) },
                                        onInsertSentenceStarter = { viewModel.insertSentenceStarter(it) },
                                        onSendTurn = { viewModel.sendTurnAndAwaitOpponent() },
                                        onFinishAndAnalyze = { viewModel.finishAndAnalyzeDebate() },
                                        onRetryError = { viewModel.retryAfterDebateError() },
                                        onBackToPrep = { viewModel.navigateTo(HojaScreen.PREP) }
                                    )
                                }

                                HojaScreen.RESULTS -> {
                                    ResultsScreen(
                                        level = userPrefs.level,
                                        mascotState = mascotState,
                                        mascotSpeech = mascotSpeech,
                                        debateState = debateState,
                                        onReturnHome = { viewModel.navigateTo(HojaScreen.HOME) },
                                        onOpenHistory = { viewModel.navigateTo(HojaScreen.HISTORY) },
                                        onDebateAgain = { viewModel.openPrepForCurrentMotion() }
                                    )
                                }

                                HojaScreen.HISTORY -> {
                                    HistoryScreen(
                                        userPrefs = userPrefs,
                                        debateHistory = debateHistory,
                                        onSelectSession = { session ->
                                            viewModel.openHistorySession(session)
                                        },
                                        onDeleteSession = { sessionId ->
                                            viewModel.deleteHistorySession(sessionId)
                                        },
                                        onBackToHome = { viewModel.navigateTo(HojaScreen.HOME) }
                                    )
                                }

                                HojaScreen.SETTINGS -> {
                                    SettingsScreen(
                                        userPrefs = userPrefs,
                                        onSelectThemeMode = { viewModel.setThemeMode(it) },
                                        onSaveApiKey = { viewModel.saveGeminiApiKey(it) },
                                        onResetProgress = { viewModel.resetAllProgress() },
                                        onBackToHome = { viewModel.navigateTo(HojaScreen.HOME) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
