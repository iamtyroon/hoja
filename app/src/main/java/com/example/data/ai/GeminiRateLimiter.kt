package com.example.data.ai

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface RateLimitCheckResult {
    data object Allowed : RateLimitCheckResult
    data class DailyLimitReached(val count: Int, val max: Int) : RateLimitCheckResult
    data class RapidRequestCooldown(val waitSeconds: Int) : RateLimitCheckResult
    data object QuotaExhaustedToday : RateLimitCheckResult
}

/**
 * Enforces rate limiting and free-tier cost protection for Hoja.
 * Guarantees the shared developer API key never exceeds Google AI Studio free-tier quotas.
 */
class GeminiRateLimiter(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("hoja_rate_limit_prefs", Context.MODE_PRIVATE)

    companion object {
        // Safe daily cap per installation on the shared developer key
        const val SHARED_DAILY_CAP = 10
        // Minimum cooldown in seconds between requests to prevent burst exhaustion
        const val MIN_SECONDS_BETWEEN_REQUESTS = 4L
    }

    private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun checkAndResetNewDay(today: String) {
        val lastDate = prefs.getString("last_usage_date", "") ?: ""
        if (lastDate != today) {
            prefs.edit()
                .putString("last_usage_date", today)
                .putInt("daily_request_count", 0)
                .putBoolean("quota_exhausted_today", false)
                .apply()
        }
    }

    @Synchronized
    fun checkCanRequest(isCustomUserKey: Boolean): RateLimitCheckResult {
        // Debaters using their own personal API key bypass the shared free tier cap
        if (isCustomUserKey) {
            return RateLimitCheckResult.Allowed
        }

        val today = todayKey()
        checkAndResetNewDay(today)

        val isExhausted = prefs.getBoolean("quota_exhausted_today", false)
        if (isExhausted) {
            return RateLimitCheckResult.QuotaExhaustedToday
        }

        val count = prefs.getInt("daily_request_count", 0)
        if (count >= SHARED_DAILY_CAP) {
            return RateLimitCheckResult.DailyLimitReached(count, SHARED_DAILY_CAP)
        }

        val lastTimestamp = prefs.getLong("last_request_timestamp", 0L)
        val elapsedSec = (System.currentTimeMillis() - lastTimestamp) / 1000L
        if (lastTimestamp > 0 && elapsedSec < MIN_SECONDS_BETWEEN_REQUESTS) {
            val waitSec = (MIN_SECONDS_BETWEEN_REQUESTS - elapsedSec).toInt()
            return RateLimitCheckResult.RapidRequestCooldown(waitSec)
        }

        return RateLimitCheckResult.Allowed
    }

    @Synchronized
    fun recordSuccess(isCustomUserKey: Boolean) {
        if (isCustomUserKey) return
        val today = todayKey()
        checkAndResetNewDay(today)
        val count = prefs.getInt("daily_request_count", 0)
        prefs.edit()
            .putInt("daily_request_count", count + 1)
            .putLong("last_request_timestamp", System.currentTimeMillis())
            .apply()
    }

    @Synchronized
    fun recordQuotaExhausted(isCustomUserKey: Boolean) {
        if (isCustomUserKey) return
        val today = todayKey()
        checkAndResetNewDay(today)
        prefs.edit()
            .putBoolean("quota_exhausted_today", true)
            .apply()
    }

    fun getDailyUsageInfo(): Pair<Int, Int> {
        val today = todayKey()
        checkAndResetNewDay(today)
        val count = prefs.getInt("daily_request_count", 0)
        return Pair(count, SHARED_DAILY_CAP)
    }

    fun isSharedQuotaLocked(): Boolean {
        val today = todayKey()
        checkAndResetNewDay(today)
        return prefs.getBoolean("quota_exhausted_today", false) ||
            prefs.getInt("daily_request_count", 0) >= SHARED_DAILY_CAP
    }
}
