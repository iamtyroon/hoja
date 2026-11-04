package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.data.model.DebateLevel
import com.example.data.model.DebateRound
import com.example.data.model.DebateSide
import com.example.data.model.ThemeMode
import com.example.data.repository.HojaRepository
import com.example.ui.components.koriGearDescription
import com.example.ui.components.koriPersonaTitle
import com.example.ui.theme.DarkStorybookPalette
import com.example.ui.theme.LightStorybookPalette
import com.example.ui.theme.StorybookPalette
import com.example.ui.viewmodel.ActiveDebateUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min

class ExampleUnitTest {

    private fun contrastRatio(fg: Color, bg: Color): Double {
        val l1 = fg.luminance().toDouble()
        val l2 = bg.luminance().toDouble()
        val brightest = max(l1, l2)
        val darkest = min(l1, l2)
        return (brightest + 0.05) / (darkest + 0.05)
    }

    private fun assertPaletteAccessible(palette: StorybookPalette, modeName: String) {
        // Primary & Secondary text on Page, Stage, Card, and Subtle surfaces
        assertTrue(
            "$modeName: inkPrimary on pageBg must exceed 7:1 AAA contrast",
            contrastRatio(palette.inkPrimary, palette.pageBg) >= 7.0
        )
        assertTrue(
            "$modeName: inkPrimary on stageBg must exceed 7:1 AAA contrast",
            contrastRatio(palette.inkPrimary, palette.stageBg) >= 7.0
        )
        assertTrue(
            "$modeName: inkPrimary on cardSurface must exceed 7:1 AAA contrast",
            contrastRatio(palette.inkPrimary, palette.cardSurface) >= 7.0
        )
        assertTrue(
            "$modeName: inkPrimary on subtleSurface must exceed 7:1 AAA contrast",
            contrastRatio(palette.inkPrimary, palette.subtleSurface) >= 7.0
        )
        assertTrue(
            "$modeName: inkSecondary on cardSurface must exceed 4.5:1 AA contrast",
            contrastRatio(palette.inkSecondary, palette.cardSurface) >= 4.5
        )
        assertTrue(
            "$modeName: inkSecondary on pageBg must exceed 4.5:1 AA contrast",
            contrastRatio(palette.inkSecondary, palette.pageBg) >= 4.5
        )

        // All 5 Pastel Gouache Washes (Sage, Coral, Sky, Ochre, Lavender)
        assertTrue(
            "$modeName: onSageWash on sageWash must exceed 4.5:1 AA contrast",
            contrastRatio(palette.onSageWash, palette.sageWash) >= 4.5
        )
        assertTrue(
            "$modeName: onCoralWash on coralWash must exceed 4.5:1 AA contrast",
            contrastRatio(palette.onCoralWash, palette.coralWash) >= 4.5
        )
        assertTrue(
            "$modeName: onSkyWash on skyWash must exceed 4.5:1 AA contrast",
            contrastRatio(palette.onSkyWash, palette.skyWash) >= 4.5
        )
        assertTrue(
            "$modeName: onOchreWash on ochreWash must exceed 4.5:1 AA contrast",
            contrastRatio(palette.onOchreWash, palette.ochreWash) >= 4.5
        )
        assertTrue(
            "$modeName: onLavenderWash on lavenderWash must exceed 4.5:1 AA contrast",
            contrastRatio(palette.onLavenderWash, palette.lavenderWash) >= 4.5
        )
    }

    @Test
    fun `light and dark storybook palettes meet WCAG AA and AAA contrast across all screens`() {
        assertPaletteAccessible(LightStorybookPalette, "LightMode")
        assertPaletteAccessible(DarkStorybookPalette, "DarkMode")
    }

    @Test
    fun `theme modes and debate level progression thresholds are accurate`() {
        assertEquals(listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM), ThemeMode.entries)
        assertEquals(DebateLevel.INTERMEDIATE, DebateLevel.BEGINNER.nextLevelOrNull())
        assertEquals(DebateLevel.ADVANCED, DebateLevel.INTERMEDIATE.nextLevelOrNull())
        assertEquals(DebateLevel.CHAMPION, DebateLevel.ADVANCED.nextLevelOrNull())
        assertEquals(null, DebateLevel.CHAMPION.nextLevelOrNull())

        assertEquals(2, DebateLevel.BEGINNER.winsRequiredToAdvance)
        assertEquals(7.0, DebateLevel.BEGINNER.qualifyingScoreThreshold(), 0.01)
        assertEquals(7.5, DebateLevel.INTERMEDIATE.qualifyingScoreThreshold(), 0.01)
        assertEquals(8.0, DebateLevel.ADVANCED.qualifyingScoreThreshold(), 0.01)
    }

    @Test
    fun `consecutive days of app usage increment streak and missed days reset to 1`() {
        val today = "2026-11-03"
        val yesterday = "2026-11-02"
        val twoDaysAgo = "2026-11-01"

        // First-ever launch
        assertEquals(
            1,
            HojaRepository.calculateUpdatedStreak(
                previousStreak = 0,
                lastActiveDateKey = "",
                todayKey = today,
                yesterdayKey = yesterday
            )
        )

        // Same-day reopen preserves current streak
        assertEquals(
            4,
            HojaRepository.calculateUpdatedStreak(
                previousStreak = 4,
                lastActiveDateKey = today,
                todayKey = today,
                yesterdayKey = yesterday
            )
        )

        // Consecutive day increments streak by 1
        assertEquals(
            5,
            HojaRepository.calculateUpdatedStreak(
                previousStreak = 4,
                lastActiveDateKey = yesterday,
                todayKey = today,
                yesterdayKey = yesterday
            )
        )

        // Missed day resets streak to 1 for the new active day
        assertEquals(
            1,
            HojaRepository.calculateUpdatedStreak(
                previousStreak = 9,
                lastActiveDateKey = twoDaysAgo,
                todayKey = today,
                yesterdayKey = yesterday
            )
        )
    }

    @Test
    fun `each debate level has a unique Kori persona title and gear description`() {
        val titles = DebateLevel.entries.map { it.koriPersonaTitle() }.toSet()
        val gears = DebateLevel.entries.map { it.koriGearDescription() }.toSet()
        assertEquals(4, titles.size)
        assertEquals(4, gears.size)
        assertEquals("Fledgling Kori", DebateLevel.BEGINNER.koriPersonaTitle())
        assertEquals("Scholar Kori", DebateLevel.INTERMEDIATE.koriPersonaTitle())
        assertEquals("Master Kori", DebateLevel.ADVANCED.koriPersonaTitle())
        assertEquals("Grand Sensei Kori", DebateLevel.CHAMPION.koriPersonaTitle())
    }

    @Test
    fun `debate side opposite and round progression work`() {
        assertEquals(DebateSide.OPPOSE, DebateSide.PROPOSE.opposite())
        assertEquals(DebateRound.REBUTTAL, DebateRound.OPENING.nextOrNull())
        assertEquals(DebateRound.CLOSING, DebateRound.REBUTTAL.nextOrNull())
        assertEquals(null, DebateRound.CLOSING.nextOrNull())
    }

    @Test
    fun `canSendTurn allows direct chat sending when draft is non-blank and opponent is ready`() {
        val blankState = ActiveDebateUiState(currentDraft = "   ")
        assertFalse(blankState.canSendTurn(DebateLevel.BEGINNER))

        val readyState = ActiveDebateUiState(currentDraft = "My first point is that solar power helps schools.")
        assertTrue(readyState.canSendTurn(DebateLevel.BEGINNER))
        assertTrue(readyState.canSendTurn(DebateLevel.CHAMPION))

        val busyState = readyState.copy(isOpponentThinking = true)
        assertFalse(busyState.canSendTurn(DebateLevel.BEGINNER))
    }
}
