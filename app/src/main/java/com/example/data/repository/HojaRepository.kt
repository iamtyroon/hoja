package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.CachedDailyMotionEntity
import com.example.data.local.DailyUsageEntity
import com.example.data.local.DebateSessionEntity
import com.example.data.local.HojaDao
import com.example.data.model.DailyMotion
import com.example.data.model.DebateLevel
import com.example.data.model.DebateRound
import com.example.data.model.DebateScoreResult
import com.example.data.model.DebateSide
import com.example.data.model.DebateTurnMessage
import com.example.data.model.KeyTerm
import com.example.data.model.RewriteItFeedback
import com.example.data.model.ThemeMode
import com.example.data.model.TurnSpeaker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class UserProgressPreferences(
    val hasCompletedOnboarding: Boolean = false,
    val level: DebateLevel = DebateLevel.BEGINNER,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val streakDays: Int = 1,
    val bestStreakDays: Int = 1,
    val lastDebateDateKey: String = "",
    val totalDebatesCompleted: Int = 0,
    val rankProgressWins: Int = 0
) {
    val isStreakActiveToday: Boolean
        get() = lastDebateDateKey == SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}

data class RecordedDebateOutcome(
    val sessionId: Long,
    val newStreakDays: Int,
    val promotedToLevel: DebateLevel?
)

class HojaRepository(
    context: Context,
    private val dao: HojaDao
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("hoja_local_prefs", Context.MODE_PRIVATE)

    private val _userPrefs = MutableStateFlow(loadPreferences())
    val userPrefs: StateFlow<UserProgressPreferences> = _userPrefs.asStateFlow()

    val debateHistory: Flow<List<DebateSessionEntity>> = dao.observeAllDebateSessions()

    fun todayDateKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun yesterdayDateKey(): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    private fun loadPreferences(): UserProgressPreferences {
        val hasOnboarded = prefs.getBoolean(KEY_ONBOARDED, false)
        val levelName = prefs.getString(KEY_LEVEL, DebateLevel.BEGINNER.name) ?: DebateLevel.BEGINNER.name
        val level = runCatching { DebateLevel.valueOf(levelName) }.getOrDefault(DebateLevel.BEGINNER)
        val themeName = prefs.getString(KEY_THEME_MODE, ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name
        val themeMode = runCatching { ThemeMode.valueOf(themeName) }.getOrDefault(ThemeMode.LIGHT)
        val lastDate = prefs.getString(KEY_LAST_DATE, "") ?: ""
        val rawStreak = prefs.getInt(KEY_STREAK, 0)
        val today = todayDateKey()
        val yesterday = yesterdayDateKey()

        val activeStreak = if (lastDate == today || lastDate == yesterday) {
            maxOf(1, rawStreak)
        } else if (lastDate.isEmpty()) {
            1
        } else {
            0
        }
        val bestStreak = maxOf(prefs.getInt(KEY_BEST_STREAK, 1), activeStreak)

        return UserProgressPreferences(
            hasCompletedOnboarding = hasOnboarded,
            level = level,
            themeMode = themeMode,
            streakDays = activeStreak,
            bestStreakDays = bestStreak,
            lastDebateDateKey = lastDate.ifEmpty { today },
            totalDebatesCompleted = prefs.getInt(KEY_TOTAL_DEBATES, 0),
            rankProgressWins = prefs.getInt(KEY_RANK_WINS, 0)
        )
    }

    /**
     * Tracks consecutive days of app usage in both SharedPreferences and Room (`daily_app_usage`).
     * Automatically increments the user's streak when they open the app on consecutive days,
     * preserves it on the same day, and starts a new 1-day streak if a full day was missed.
     */
    suspend fun recordDailyAppUsage(): Int {
        val today = todayDateKey()
        val yesterday = yesterdayDateKey()
        val lastDate = prefs.getString(KEY_LAST_DATE, "") ?: ""
        val previousStreak = prefs.getInt(KEY_STREAK, 0)

        val updatedStreak = calculateUpdatedStreak(
            previousStreak = previousStreak,
            lastActiveDateKey = lastDate,
            todayKey = today,
            yesterdayKey = yesterday
        )
        val updatedBest = maxOf(prefs.getInt(KEY_BEST_STREAK, 0), updatedStreak)

        if (lastDate != today || previousStreak != updatedStreak) {
            prefs.edit()
                .putInt(KEY_STREAK, updatedStreak)
                .putInt(KEY_BEST_STREAK, updatedBest)
                .putString(KEY_LAST_DATE, today)
                .apply()
            _userPrefs.value = loadPreferences()
        }

        dao.insertDailyUsage(DailyUsageEntity(dateKey = today))
        return updatedStreak
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _userPrefs.value = loadPreferences()
    }

    fun completeOnboarding(startingLevel: DebateLevel = DebateLevel.BEGINNER) {
        val today = todayDateKey()
        val yesterday = yesterdayDateKey()
        val lastDate = prefs.getString(KEY_LAST_DATE, "") ?: ""
        val previousStreak = prefs.getInt(KEY_STREAK, 0)
        val updatedStreak = calculateUpdatedStreak(previousStreak, lastDate, today, yesterday)
        val updatedBest = maxOf(prefs.getInt(KEY_BEST_STREAK, 0), updatedStreak)

        prefs.edit()
            .putBoolean(KEY_ONBOARDED, true)
            .putString(KEY_LEVEL, startingLevel.name)
            .putInt(KEY_RANK_WINS, 0)
            .putInt(KEY_STREAK, updatedStreak)
            .putInt(KEY_BEST_STREAK, updatedBest)
            .putString(KEY_LAST_DATE, today)
            .apply()
        _userPrefs.value = loadPreferences()
    }

    fun setDebateLevel(level: DebateLevel) {
        prefs.edit()
            .putString(KEY_LEVEL, level.name)
            .putInt(KEY_RANK_WINS, 0)
            .apply()
        _userPrefs.value = loadPreferences()
    }

    suspend fun getCachedDailyMotion(dateKey: String, level: DebateLevel): DailyMotion? {
        val entity = dao.getCachedMotion("${dateKey}_${level.name}") ?: return null
        return DailyMotion(
            dateKey = entity.dateKey,
            level = level,
            motion = entity.motion,
            shortContext = entity.shortContext,
            keyTerms = parseKeyTermsJson(entity.keyTermsJson),
            proposeHint = entity.proposeHint,
            opposeHint = entity.opposeHint
        )
    }

    suspend fun saveDailyMotionToCache(motion: DailyMotion) {
        val termsArray = JSONArray().apply {
            motion.keyTerms.forEach { term ->
                put(JSONObject().put("term", term.term).put("definition", term.definition))
            }
        }
        dao.insertCachedMotion(
            CachedDailyMotionEntity(
                cacheKey = "${motion.dateKey}_${motion.level.name}",
                dateKey = motion.dateKey,
                levelName = motion.level.name,
                motion = motion.motion,
                shortContext = motion.shortContext,
                keyTermsJson = termsArray.toString(),
                proposeHint = motion.proposeHint,
                opposeHint = motion.opposeHint
            )
        )
    }

    suspend fun invalidateDailyMotionCache(dateKey: String, level: DebateLevel) {
        dao.deleteCachedMotion("${dateKey}_${level.name}")
    }

    suspend fun recordCompletedDebate(
        motion: DailyMotion,
        userSide: DebateSide,
        scoreResult: DebateScoreResult,
        transcript: List<DebateTurnMessage>
    ): RecordedDebateOutcome {
        val today = todayDateKey()
        val yesterday = yesterdayDateKey()
        val lastDate = prefs.getString(KEY_LAST_DATE, "") ?: ""
        val previousStreak = prefs.getInt(KEY_STREAK, 0)
        val currentPrefs = loadPreferences()

        val newStreak = calculateUpdatedStreak(
            previousStreak = previousStreak,
            lastActiveDateKey = lastDate,
            todayKey = today,
            yesterdayKey = yesterday
        )
        val newBestStreak = maxOf(currentPrefs.bestStreakDays, newStreak)

        // Progression System: Start at Beginner and advance when earning qualifying scores
        val currentLevel = currentPrefs.level
        val nextLevel = currentLevel.nextLevelOrNull()
        val qualifiedWin = scoreResult.overall >= currentLevel.qualifyingScoreThreshold()
        val updatedWins = if (qualifiedWin) currentPrefs.rankProgressWins + 1 else currentPrefs.rankProgressWins

        val promotedTo: DebateLevel? = if (
            nextLevel != null &&
            currentLevel.winsRequiredToAdvance > 0 &&
            updatedWins >= currentLevel.winsRequiredToAdvance
        ) {
            nextLevel
        } else {
            null
        }

        val finalLevel = promotedTo ?: currentLevel
        val finalRankWins = if (promotedTo != null) 0 else updatedWins

        prefs.edit()
            .putInt(KEY_STREAK, newStreak)
            .putInt(KEY_BEST_STREAK, newBestStreak)
            .putString(KEY_LAST_DATE, today)
            .putInt(KEY_TOTAL_DEBATES, currentPrefs.totalDebatesCompleted + 1)
            .putString(KEY_LEVEL, finalLevel.name)
            .putInt(KEY_RANK_WINS, finalRankWins)
            .apply()
        _userPrefs.value = loadPreferences()

        dao.insertDailyUsage(
            DailyUsageEntity(
                dateKey = today,
                debatesCompletedOnDay = currentPrefs.totalDebatesCompleted + 1
            )
        )

        val strengthsArr = JSONArray().apply { scoreResult.strengths.forEach { put(it) } }
        val improvementsArr = JSONArray().apply { scoreResult.improvements.forEach { put(it) } }
        val transcriptArr = JSONArray().apply {
            transcript.forEach { turn ->
                put(
                    JSONObject().apply {
                        put("id", turn.id)
                        put("round", turn.round.name)
                        put("speaker", turn.speaker.name)
                        put("side", turn.side.name)
                        put("text", turn.text)
                        put("wordCount", turn.wordCount)
                        if (!turn.exampleStrongAnswer.isNullOrBlank()) {
                            put("exampleStrongAnswer", turn.exampleStrongAnswer)
                        }
                    }
                )
            }
        }

        val sessionId = dao.insertDebateSession(
            DebateSessionEntity(
                dateKey = today,
                levelName = currentLevel.name,
                motion = motion.motion,
                userSide = userSide.name,
                argumentStrength = scoreResult.argumentStrength,
                evidence = scoreResult.evidence,
                rebuttal = scoreResult.rebuttal,
                structure = scoreResult.structure,
                writingQuality = scoreResult.writingQuality,
                overallScore = scoreResult.overall,
                strengthsJson = strengthsArr.toString(),
                improvementsJson = improvementsArr.toString(),
                rewriteOriginal = scoreResult.rewriteIt.original,
                rewriteImproved = scoreResult.rewriteIt.improved,
                rewriteReason = scoreResult.rewriteIt.reason,
                coachMessage = scoreResult.coachMessage,
                transcriptJson = transcriptArr.toString()
            )
        )

        return RecordedDebateOutcome(
            sessionId = sessionId,
            newStreakDays = newStreak,
            promotedToLevel = promotedTo
        )
    }

    suspend fun deleteDebateSession(sessionId: Long) {
        dao.deleteDebateSessionById(sessionId)
    }

    suspend fun resetAllProgress() {
        val savedTheme = prefs.getString(KEY_THEME_MODE, ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name
        prefs.edit().clear().putString(KEY_THEME_MODE, savedTheme).apply()
        dao.clearAllSessions()
        dao.clearAllCachedMotions()
        dao.clearAllDailyUsage()
        _userPrefs.value = loadPreferences()
    }

    private fun parseKeyTermsJson(jsonStr: String): List<KeyTerm> = runCatching {
        val arr = JSONArray(jsonStr)
        (0 until arr.length()).mapNotNull { idx ->
            val item = arr.optJSONObject(idx) ?: return@mapNotNull null
            KeyTerm(
                term = item.optString("term", "Term"),
                definition = item.optString("definition", "")
            )
        }
    }.getOrDefault(emptyList())

    companion object {
        private const val KEY_ONBOARDED = "has_completed_onboarding"
        private const val KEY_LEVEL = "selected_debate_level"
        private const val KEY_THEME_MODE = "selected_theme_mode"
        private const val KEY_STREAK = "streak_days"
        private const val KEY_BEST_STREAK = "best_streak_days"
        private const val KEY_LAST_DATE = "last_debate_date_key"
        private const val KEY_TOTAL_DEBATES = "total_debates_completed"
        private const val KEY_RANK_WINS = "rank_progress_wins"

        /**
         * Pure function that calculates the updated consecutive-day streak for app usage.
         */
        fun calculateUpdatedStreak(
            previousStreak: Int,
            lastActiveDateKey: String,
            todayKey: String,
            yesterdayKey: String
        ): Int = when (lastActiveDateKey) {
            todayKey -> maxOf(1, previousStreak)
            yesterdayKey -> maxOf(1, previousStreak) + 1
            else -> 1
        }

        fun parseSessionScoreResult(entity: DebateSessionEntity): DebateScoreResult {
            val strengths = parseStringList(entity.strengthsJson)
            val improvements = parseStringList(entity.improvementsJson)
            return DebateScoreResult(
                argumentStrength = entity.argumentStrength,
                evidence = entity.evidence,
                rebuttal = entity.rebuttal,
                structure = entity.structure,
                writingQuality = entity.writingQuality,
                overall = entity.overallScore,
                strengths = strengths,
                improvements = improvements,
                rewriteIt = RewriteItFeedback(
                    original = entity.rewriteOriginal,
                    improved = entity.rewriteImproved,
                    reason = entity.rewriteReason
                ),
                coachMessage = entity.coachMessage
            )
        }

        fun parseSessionTranscript(entity: DebateSessionEntity): List<DebateTurnMessage> = runCatching {
            val arr = JSONArray(entity.transcriptJson)
            (0 until arr.length()).mapNotNull { idx ->
                val obj = arr.optJSONObject(idx) ?: return@mapNotNull null
                val round = runCatching { DebateRound.valueOf(obj.optString("round")) }
                    .getOrDefault(DebateRound.OPENING)
                val speaker = runCatching { TurnSpeaker.valueOf(obj.optString("speaker")) }
                    .getOrDefault(TurnSpeaker.USER)
                val side = runCatching { DebateSide.valueOf(obj.optString("side")) }
                    .getOrDefault(DebateSide.PROPOSE)
                val text = obj.optString("text", "")
                DebateTurnMessage(
                    id = obj.optString("id", "turn_$idx"),
                    round = round,
                    speaker = speaker,
                    side = side,
                    text = text,
                    wordCount = obj.optInt("wordCount", text.split(Regex("\\s+")).count { it.isNotBlank() }),
                    exampleStrongAnswer = obj.optString("exampleStrongAnswer").takeIf { it.isNotBlank() }
                )
            }
        }.getOrDefault(emptyList())

        private fun parseStringList(jsonStr: String): List<String> = runCatching {
            val arr = JSONArray(jsonStr)
            (0 until arr.length()).mapNotNull { idx ->
                arr.optString(idx)?.takeIf { it.isNotBlank() }
            }
        }.getOrDefault(emptyList())
    }
}
