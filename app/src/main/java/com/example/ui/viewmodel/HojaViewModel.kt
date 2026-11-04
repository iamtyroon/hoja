package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.HojaAiService
import com.example.data.local.DebateSessionEntity
import com.example.data.local.HojaDatabase
import com.example.data.model.DailyMotion
import com.example.data.model.DebateLevel
import com.example.data.model.DebateRound
import com.example.data.model.DebateScoreResult
import com.example.data.model.DebateSide
import com.example.data.model.DebateTurnMessage
import com.example.data.model.MascotState
import com.example.data.model.ThemeMode
import com.example.data.model.TurnSpeaker
import com.example.data.repository.HojaRepository
import com.example.data.repository.UserProgressPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class HojaScreen {
    ONBOARDING,
    HOME,
    PREP,
    DEBATE,
    RESULTS,
    HISTORY,
    SETTINGS
}

sealed interface DailyMotionUiState {
    data object Loading : DailyMotionUiState
    data class Success(val motion: DailyMotion) : DailyMotionUiState
    data class Error(val message: String) : DailyMotionUiState
}

data class ActiveDebateUiState(
    val motion: DailyMotion? = null,
    val userSide: DebateSide = DebateSide.PROPOSE,
    val isHintRevealed: Boolean = false,
    val currentRound: DebateRound = DebateRound.OPENING,
    val transcript: List<DebateTurnMessage> = emptyList(),
    val currentDraft: String = "",
    val isOpponentThinking: Boolean = false,
    val isScoringDebate: Boolean = false,
    val scoreResult: DebateScoreResult? = null,
    val promotedToLevel: DebateLevel? = null,
    val isReviewingPastSession: Boolean = false,
    val reviewedSessionDateKey: String? = null,
    val actionError: String? = null
) {
    fun canSendTurn(level: DebateLevel = DebateLevel.BEGINNER): Boolean {
        return currentDraft.isNotBlank() && !isOpponentThinking && !isScoringDebate
    }
}

class HojaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HojaRepository(
        context = application,
        dao = HojaDatabase.getInstance(application).hojaDao()
    )
    private val aiService = HojaAiService(application)
    private val seenMotions = mutableListOf<String>()

    val userPrefs: StateFlow<UserProgressPreferences> = repository.userPrefs
    val debateHistory: StateFlow<List<DebateSessionEntity>> = repository.debateHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    private val _currentScreen = MutableStateFlow(
        if (repository.userPrefs.value.hasCompletedOnboarding) HojaScreen.HOME else HojaScreen.ONBOARDING
    )
    val currentScreen: StateFlow<HojaScreen> = _currentScreen.asStateFlow()

    private val _mascotState = MutableStateFlow(MascotState.IDLE)
    val mascotState: StateFlow<MascotState> = _mascotState.asStateFlow()

    private val _mascotSpeech = MutableStateFlow(MascotState.IDLE.defaultSpeech)
    val mascotSpeech: StateFlow<String> = _mascotSpeech.asStateFlow()

    private val _dailyMotionState = MutableStateFlow<DailyMotionUiState>(DailyMotionUiState.Loading)
    val dailyMotionState: StateFlow<DailyMotionUiState> = _dailyMotionState.asStateFlow()

    private val _debateState = MutableStateFlow(ActiveDebateUiState())
    val debateState: StateFlow<ActiveDebateUiState> = _debateState.asStateFlow()

    private val _showQuotaDialog = MutableStateFlow(false)
    val showQuotaDialog: StateFlow<Boolean> = _showQuotaDialog.asStateFlow()

    init {
        aiService.onQuotaExceeded = { _showQuotaDialog.value = true }
        viewModelScope.launch {
            repository.recordDailyAppUsage()
        }
        if (repository.userPrefs.value.hasCompletedOnboarding) {
            loadTodayMotion(forceRefresh = false)
        }
    }

    fun selectOnboardingLevelAndStart(level: DebateLevel = DebateLevel.BEGINNER) {
        repository.completeOnboarding(level)
        viewModelScope.launch {
            repository.recordDailyAppUsage()
        }
        _currentScreen.value = HojaScreen.HOME
        updateMascot(
            MascotState.ANNOUNCING,
            "Karibu! You begin at ${level.displayName} rank. Win debates to evolve Kori and maintain your daily streak!"
        )
        loadTodayMotion(forceRefresh = false)
    }

    fun saveGeminiApiKey(key: String) { repository.setGeminiApiKey(key) }

    fun dismissQuotaDialog() { _showQuotaDialog.value = false }

    fun getDailyUsageInfo(): Pair<Int, Int> = aiService.getDailyUsageInfo()

    fun isSharedQuotaLocked(): Boolean = aiService.isSharedQuotaLocked()

    fun setThemeMode(mode: ThemeMode) {
        repository.setThemeMode(mode)
    }

    fun toggleLightDarkTheme(currentlyDark: Boolean) {
        repository.setThemeMode(if (currentlyDark) ThemeMode.LIGHT else ThemeMode.DARK)
    }

    fun navigateTo(screen: HojaScreen) {
        _currentScreen.value = screen
        when (screen) {
            HojaScreen.HOME -> {
                if (_dailyMotionState.value !is DailyMotionUiState.Success) {
                    loadTodayMotion(forceRefresh = false)
                } else {
                    updateMascot(
                        MascotState.IDLE,
                        "Engage with today's motion to extend your streak, or tap Reroll for a fresh topic."
                    )
                }
            }
            HojaScreen.HISTORY -> {
                updateMascot(
                    MascotState.IDLE,
                    "Review your past debate transcripts and AI Adjudicator feedback."
                )
            }
            HojaScreen.SETTINGS -> {
                updateMascot(
                    MascotState.IDLE,
                    "Switch between Daylight Washi and Sumi Night themes or manage your progress."
                )
            }
            else -> Unit
        }
    }

    fun updateMascot(state: MascotState, customSpeech: String? = null) {
        _mascotState.value = state
        _mascotSpeech.value = customSpeech ?: state.defaultSpeech
    }

    /**
     * Loads today's motion from Room cache or generates/rerolls via Gemini API.
     */
    fun loadTodayMotion(forceRefresh: Boolean = false) {
        val level = userPrefs.value.level
        val dateKey = repository.todayDateKey()

        viewModelScope.launch {
            (_dailyMotionState.value as? DailyMotionUiState.Success)?.motion?.motion?.let { current ->
                if (!seenMotions.contains(current)) seenMotions.add(current)
            }
            debateHistory.value.take(4).forEach { session ->
                if (!seenMotions.contains(session.motion)) seenMotions.add(session.motion)
            }

            _dailyMotionState.value = DailyMotionUiState.Loading
            updateMascot(
                MascotState.THINKING,
                if (forceRefresh) "Rerolling a brand-new motion with Gemini..." else "Unrolling today's debate motion..."
            )

            try {
                if (!forceRefresh) {
                    val cached = repository.getCachedDailyMotion(dateKey, level)
                    if (cached != null) {
                        _dailyMotionState.value = DailyMotionUiState.Success(cached)
                        updateMascot(
                            MascotState.ANNOUNCING,
                            "Today's motion is ready. Tap 'Engage with Motion' or 'Reroll' for another topic."
                        )
                        return@launch
                    }
                } else {
                    repository.invalidateDailyMotionCache(dateKey, level)
                }

                val generated = aiService.generateDailyMotion(
                    dateKey = dateKey,
                    level = level,
                    avoidMotions = if (forceRefresh) seenMotions.toList() else emptyList()
                )
                seenMotions.add(generated.motion)
                repository.saveDailyMotionToCache(generated)
                _dailyMotionState.value = DailyMotionUiState.Success(generated)
                updateMascot(
                    MascotState.ANNOUNCING,
                    if (forceRefresh) "New motion rolled! Choose your side to begin." else "Fresh motion unrolled! Engage below to pick your side."
                )
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Could not generate today's motion."
                _dailyMotionState.value = DailyMotionUiState.Error(msg)
                updateMascot(MascotState.CONCERNED, msg)
            }
        }
    }

    fun openPrepForCurrentMotion() {
        val currentMotion = (_dailyMotionState.value as? DailyMotionUiState.Success)?.motion ?: return
        _debateState.value = ActiveDebateUiState(
            motion = currentMotion,
            userSide = DebateSide.PROPOSE,
            isHintRevealed = false,
            currentRound = DebateRound.OPENING
        )
        _currentScreen.value = HojaScreen.PREP
        updateMascot(
            MascotState.IDLE,
            "Choose Propose or Oppose. I will take the opposite bench in our live debate chat."
        )
    }

    fun togglePrepHint() {
        _debateState.update { state ->
            val next = !state.isHintRevealed
            if (next) {
                updateMascot(
                    MascotState.ENCOURAGING,
                    "Use this angle to spark your opening point in the chat."
                )
            }
            state.copy(isHintRevealed = next)
        }
    }

    fun selectDebateSide(side: DebateSide) {
        _debateState.update { it.copy(userSide = side) }
        updateMascot(
            MascotState.ANNOUNCING,
            "You chose ${side.label}. I will argue for ${side.opposite().label}."
        )
    }

    fun startWrittenDebate() {
        val state = _debateState.value
        state.motion ?: return
        _debateState.update {
            it.copy(
                currentRound = DebateRound.OPENING,
                transcript = emptyList(),
                currentDraft = "",
                actionError = null,
                scoreResult = null,
                promotedToLevel = null,
                isReviewingPastSession = false
            )
        }
        _currentScreen.value = HojaScreen.DEBATE
        updateMascot(
            MascotState.ANNOUNCING,
            "Table your opening point for ${state.userSide.label}. I'm ready to counter as ${state.userSide.opposite().label}."
        )
    }

    fun updateCurrentDraft(newText: String) {
        _debateState.update { it.copy(currentDraft = newText, actionError = null) }
    }

    fun insertSentenceStarter(starter: String) {
        _debateState.update { current ->
            val prefix = if (
                current.currentDraft.isBlank() ||
                current.currentDraft.endsWith(" ") ||
                current.currentDraft.endsWith("\n")
            ) {
                ""
            } else {
                " "
            }
            current.copy(currentDraft = current.currentDraft + prefix + starter, actionError = null)
        }
    }

    fun sendTurnAndAwaitOpponent() {
        val current = _debateState.value
        val motion = current.motion ?: return
        val level = userPrefs.value.level
        if (!current.canSendTurn(level)) return

        val userText = current.currentDraft.trim()
        val userTurn = DebateTurnMessage(
            id = UUID.randomUUID().toString(),
            round = current.currentRound,
            speaker = TurnSpeaker.USER,
            side = current.userSide,
            text = userText,
            wordCount = HojaAiService.countWords(userText)
        )
        val updatedTranscript = current.transcript + userTurn

        viewModelScope.launch {
            _debateState.update {
                it.copy(
                    transcript = updatedTranscript,
                    currentDraft = "",
                    isOpponentThinking = true,
                    actionError = null
                )
            }
            updateMascot(
                MascotState.THINKING,
                "Countering from the ${current.userSide.opposite().label} bench..."
            )

            try {
                val aiResponse = aiService.generateOpponentTurn(
                    motion = motion.motion,
                    aiSide = current.userSide.opposite(),
                    level = level,
                    round = current.currentRound,
                    transcriptSoFar = updatedTranscript
                )

                val aiTurn = DebateTurnMessage(
                    id = UUID.randomUUID().toString(),
                    round = current.currentRound,
                    speaker = TurnSpeaker.AI_OPPONENT,
                    side = current.userSide.opposite(),
                    text = aiResponse.opponentReply,
                    wordCount = HojaAiService.countWords(aiResponse.opponentReply),
                    exampleStrongAnswer = aiResponse.exampleStrongAnswer.takeIf { it.isNotBlank() }
                )

                val fullRoundTranscript = updatedTranscript + aiTurn
                val nextRound = current.currentRound.nextOrNull()

                if (nextRound != null) {
                    _debateState.update {
                        it.copy(
                            transcript = fullRoundTranscript,
                            currentRound = nextRound,
                            isOpponentThinking = false
                        )
                    }
                    updateMascot(
                        MascotState.ANNOUNCING,
                        "Your turn for ${nextRound.title}: rebut my point or advance your next argument."
                    )
                } else {
                    _debateState.update {
                        it.copy(
                            transcript = fullRoundTranscript,
                            isOpponentThinking = false,
                            isScoringDebate = true
                        )
                    }
                    scoreCompletedDebate(motion, current.userSide, level, fullRoundTranscript)
                }
            } catch (e: Exception) {
                val err = e.localizedMessage ?: "Opponent reply failed."
                _debateState.update { it.copy(isOpponentThinking = false, actionError = err) }
                updateMascot(MascotState.CONCERNED, err)
            }
        }
    }

    fun finishAndAnalyzeDebate() {
        val current = _debateState.value
        val motion = current.motion ?: return
        if (current.transcript.isEmpty() || current.isOpponentThinking || current.isScoringDebate) return
        val level = userPrefs.value.level

        viewModelScope.launch {
            _debateState.update { it.copy(isScoringDebate = true, actionError = null) }
            scoreCompletedDebate(motion, current.userSide, level, current.transcript)
        }
    }

    fun retryAfterDebateError() {
        val current = _debateState.value
        val motion = current.motion ?: return
        val level = userPrefs.value.level

        val lastTurn = current.transcript.lastOrNull()
        if (lastTurn != null && lastTurn.speaker == TurnSpeaker.USER) {
            viewModelScope.launch {
                _debateState.update { it.copy(isOpponentThinking = true, actionError = null) }
                updateMascot(MascotState.THINKING, "Retrying my ${current.userSide.opposite().label} reply...")
                try {
                    val aiResponse = aiService.generateOpponentTurn(
                        motion = motion.motion,
                        aiSide = current.userSide.opposite(),
                        level = level,
                        round = current.currentRound,
                        transcriptSoFar = current.transcript
                    )
                    val aiTurn = DebateTurnMessage(
                        id = UUID.randomUUID().toString(),
                        round = current.currentRound,
                        speaker = TurnSpeaker.AI_OPPONENT,
                        side = current.userSide.opposite(),
                        text = aiResponse.opponentReply,
                        wordCount = HojaAiService.countWords(aiResponse.opponentReply),
                        exampleStrongAnswer = aiResponse.exampleStrongAnswer.takeIf { it.isNotBlank() }
                    )
                    val fullTranscript = current.transcript + aiTurn
                    val nextRound = current.currentRound.nextOrNull()
                    if (nextRound != null) {
                        _debateState.update {
                            it.copy(
                                transcript = fullTranscript,
                                currentRound = nextRound,
                                isOpponentThinking = false
                            )
                        }
                        updateMascot(MascotState.ANNOUNCING, "On to ${nextRound.title}!")
                    } else {
                        _debateState.update {
                            it.copy(
                                transcript = fullTranscript,
                                isOpponentThinking = false,
                                isScoringDebate = true
                            )
                        }
                        scoreCompletedDebate(motion, current.userSide, level, fullTranscript)
                    }
                } catch (e: Exception) {
                    val err = e.localizedMessage ?: "Retry failed."
                    _debateState.update { it.copy(isOpponentThinking = false, actionError = err) }
                    updateMascot(MascotState.CONCERNED, err)
                }
            }
        } else if (current.transcript.isNotEmpty() && current.scoreResult == null) {
            viewModelScope.launch {
                _debateState.update { it.copy(isScoringDebate = true, actionError = null) }
                scoreCompletedDebate(motion, current.userSide, level, current.transcript)
            }
        }
    }

    private suspend fun scoreCompletedDebate(
        motion: DailyMotion,
        userSide: DebateSide,
        level: DebateLevel,
        transcript: List<DebateTurnMessage>
    ) {
        updateMascot(
            MascotState.THINKING,
            "AI Adjudicator is analyzing the full debate and saving your session..."
        )
        try {
            val score = aiService.scoreDebate(
                motion = motion.motion,
                userSide = userSide,
                level = level,
                transcript = transcript
            )
            val outcome = repository.recordCompletedDebate(
                motion = motion,
                userSide = userSide,
                scoreResult = score,
                transcript = transcript
            )
            _debateState.update {
                it.copy(
                    isScoringDebate = false,
                    scoreResult = score,
                    promotedToLevel = outcome.promotedToLevel,
                    isReviewingPastSession = false,
                    actionError = null
                )
            }
            _currentScreen.value = HojaScreen.RESULTS

            if (outcome.promotedToLevel != null) {
                updateMascot(
                    MascotState.CHEERING,
                    "Rank Up! You advanced to ${outcome.promotedToLevel.displayName}! ${score.coachMessage}"
                )
            } else if (score.overall >= 7.0) {
                updateMascot(MascotState.CHEERING, score.coachMessage)
            } else {
                updateMascot(MascotState.ENCOURAGING, score.coachMessage)
            }
        } catch (e: Exception) {
            val err = e.localizedMessage ?: "Could not analyze the debate."
            _debateState.update { it.copy(isScoringDebate = false, actionError = err) }
            updateMascot(MascotState.CONCERNED, err)
        }
    }

    /**
     * Loads a saved Room DebateSessionEntity into ResultsScreen so the user can review
     * both the full AI Adjudicator analysis and the saved debate transcript.
     */
    fun openHistorySession(session: DebateSessionEntity) {
        val parsedScore = HojaRepository.parseSessionScoreResult(session)
        val parsedTranscript = HojaRepository.parseSessionTranscript(session)
        val parsedLevel = runCatching { DebateLevel.valueOf(session.levelName) }
            .getOrDefault(userPrefs.value.level)
        val parsedSide = runCatching { DebateSide.valueOf(session.userSide) }
            .getOrDefault(DebateSide.PROPOSE)

        _debateState.value = ActiveDebateUiState(
            motion = DailyMotion(
                dateKey = session.dateKey,
                level = parsedLevel,
                motion = session.motion,
                shortContext = "",
                keyTerms = emptyList(),
                proposeHint = "",
                opposeHint = ""
            ),
            userSide = parsedSide,
            transcript = parsedTranscript,
            scoreResult = parsedScore,
            isReviewingPastSession = true,
            reviewedSessionDateKey = session.dateKey
        )
        _currentScreen.value = HojaScreen.RESULTS
        updateMascot(MascotState.IDLE, parsedScore.coachMessage)
    }

    fun deleteHistorySession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteDebateSession(sessionId)
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
            seenMotions.clear()
            _debateState.value = ActiveDebateUiState()
            _currentScreen.value = HojaScreen.ONBOARDING
            updateMascot(
                MascotState.IDLE,
                "Progress reset. Begin your journey from Beginner rank!"
            )
        }
    }
}
