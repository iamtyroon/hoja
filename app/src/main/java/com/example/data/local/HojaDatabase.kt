package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_daily_motions")
data class CachedDailyMotionEntity(
    @PrimaryKey val cacheKey: String,
    val dateKey: String,
    val levelName: String,
    val motion: String,
    val shortContext: String,
    val keyTermsJson: String,
    val proposeHint: String,
    val opposeHint: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "debate_sessions")
data class DebateSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val dateKey: String,
    val levelName: String,
    val motion: String,
    val userSide: String,
    val argumentStrength: Int,
    val evidence: Int,
    val rebuttal: Int,
    val structure: Int,
    val writingQuality: Int,
    val overallScore: Double,
    val strengthsJson: String,
    val improvementsJson: String,
    val rewriteOriginal: String,
    val rewriteImproved: String,
    val rewriteReason: String,
    val coachMessage: String,
    val transcriptJson: String
)

@Entity(tableName = "daily_app_usage")
data class DailyUsageEntity(
    @PrimaryKey val dateKey: String,
    val firstOpenedTimestamp: Long = System.currentTimeMillis(),
    val debatesCompletedOnDay: Int = 0
)

@Dao
interface HojaDao {
    @Query("SELECT * FROM cached_daily_motions WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getCachedMotion(cacheKey: String): CachedDailyMotionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedMotion(entity: CachedDailyMotionEntity)

    @Query("DELETE FROM cached_daily_motions WHERE cacheKey = :cacheKey")
    suspend fun deleteCachedMotion(cacheKey: String)

    @Query("SELECT * FROM debate_sessions ORDER BY timestamp DESC")
    fun observeAllDebateSessions(): Flow<List<DebateSessionEntity>>

    @Query("SELECT * FROM debate_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getDebateSessionById(sessionId: Long): DebateSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebateSession(session: DebateSessionEntity): Long

    @Query("DELETE FROM debate_sessions WHERE id = :sessionId")
    suspend fun deleteDebateSessionById(sessionId: Long)

    @Query("DELETE FROM debate_sessions")
    suspend fun clearAllSessions()

    @Query("DELETE FROM cached_daily_motions")
    suspend fun clearAllCachedMotions()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDailyUsage(entity: DailyUsageEntity)

    @Query("SELECT dateKey FROM daily_app_usage ORDER BY dateKey DESC LIMIT 7")
    suspend fun getRecentUsageDateKeys(): List<String>

    @Query("DELETE FROM daily_app_usage")
    suspend fun clearAllDailyUsage()
}

@Database(
    entities = [
        CachedDailyMotionEntity::class,
        DebateSessionEntity::class,
        DailyUsageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HojaDatabase : RoomDatabase() {
    abstract fun hojaDao(): HojaDao

    companion object {
        @Volatile
        private var INSTANCE: HojaDatabase? = null

        fun getInstance(context: Context): HojaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HojaDatabase::class.java,
                    "hoja_debate.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
