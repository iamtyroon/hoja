package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DebateSessionEntity
import com.example.data.model.DebateLevel
import com.example.data.model.TurnSpeaker
import com.example.data.repository.HojaRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Hoja", appName)
    }

    @Test
    fun `Room DebateSessionEntity transcript and AI analysis deserialize cleanly for History review`() {
        val entity = DebateSessionEntity(
            id = 7L,
            timestamp = 2000L,
            dateKey = "2026-11-03",
            levelName = DebateLevel.BEGINNER.name,
            motion = "This House would ban smartphones in schools",
            userSide = "PROPOSE",
            argumentStrength = 8,
            evidence = 9,
            rebuttal = 8,
            structure = 8,
            writingQuality = 8,
            overallScore = 8.2,
            strengthsJson = """["Clear thesis","Good school example"]""",
            improvementsJson = """["Quantify test score impact"]""",
            rewriteOriginal = "Phones distract students.",
            rewriteImproved = "Classroom smartphone bans raise focus by removing notification loops.",
            rewriteReason = "Adds mechanism and academic impact.",
            coachMessage = "Strong opening and rebuttal.",
            transcriptJson = """[{"id":"t1","round":"OPENING","speaker":"USER","side":"PROPOSE","text":"Phones distract students.","wordCount":3,"exampleStrongAnswer":""}]"""
        )

        val turns = HojaRepository.parseSessionTranscript(entity)
        assertEquals(1, turns.size)
        assertEquals(TurnSpeaker.USER, turns.first().speaker)
        assertEquals("Phones distract students.", turns.first().text)

        val score = HojaRepository.parseSessionScoreResult(entity)
        assertEquals(8.2, score.overall, 0.01)
        assertEquals(2, score.strengths.size)
        assertEquals("Quantify test score impact", score.improvements.first())
        assertEquals("Adds mechanism and academic impact.", score.rewriteIt.reason)
    }
}
