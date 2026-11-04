package com.example.data.model

enum class ThemeMode(val displayName: String, val swahiliTag: String) {
    LIGHT("Light", "Mchana"),
    DARK("Dark", "Usiku"),
    SYSTEM("System", "Mfumo")
}

enum class DebateLevel(
    val displayName: String,
    val swahiliTag: String,
    val wordLimit: Int,
    val winsRequiredToAdvance: Int,
    val description: String,
    val ruleSummary: String
) {
    BEGINNER(
        displayName = "Beginner",
        swahiliTag = "Mwanzo",
        wordLimit = 200,
        winsRequiredToAdvance = 2,
        description = "Everyday motions, guided coaching, and approachable opponent replies.",
        ruleSummary = "Score 7.0+ in 2 debates to advance to Intermediate"
    ),
    INTERMEDIATE(
        displayName = "Intermediate",
        swahiliTag = "Wastani",
        wordLimit = 180,
        winsRequiredToAdvance = 2,
        description = "National issues with a balanced opponent who tests your evidence.",
        ruleSummary = "Score 7.5+ in 2 debates to advance to Advanced"
    ),
    ADVANCED(
        displayName = "Advanced",
        swahiliTag = "Hodari",
        wordLimit = 150,
        winsRequiredToAdvance = 3,
        description = "Policy trade-offs and sharp East African rebuttals.",
        ruleSummary = "Score 8.0+ in 3 debates to advance to Champion"
    ),
    CHAMPION(
        displayName = "Champion",
        swahiliTag = "Bingwa",
        wordLimit = 120,
        winsRequiredToAdvance = 0,
        description = "Tournament standard with complex regional and global motions.",
        ruleSummary = "Highest rank achieved"
    );

    fun nextLevelOrNull(): DebateLevel? = when (this) {
        BEGINNER -> INTERMEDIATE
        INTERMEDIATE -> ADVANCED
        ADVANCED -> CHAMPION
        CHAMPION -> null
    }

    fun qualifyingScoreThreshold(): Double = when (this) {
        BEGINNER -> 7.0
        INTERMEDIATE -> 7.5
        ADVANCED -> 8.0
        CHAMPION -> 8.5
    }
}

enum class DebateSide(val label: String, val roleDescription: String) {
    PROPOSE("Propose", "Support the motion"),
    OPPOSE("Oppose", "Argue against the motion");

    fun opposite(): DebateSide = if (this == PROPOSE) OPPOSE else PROPOSE
}

enum class DebateRound(
    val roundNumber: Int,
    val title: String,
    val subtitle: String,
    val coachGuidance: String
) {
    OPENING(
        roundNumber = 1,
        title = "Opening",
        subtitle = "Round 1 of 3",
        coachGuidance = "State your main claim, explain why it holds true, and ground it in an example."
    ),
    REBUTTAL(
        roundNumber = 2,
        title = "Rebuttal",
        subtitle = "Round 2 of 3",
        coachGuidance = "Directly challenge your opponent's argument and reinforce your case."
    ),
    CLOSING(
        roundNumber = 3,
        title = "Closing",
        subtitle = "Round 3 of 3",
        coachGuidance = "Weigh the central clash of the debate and deliver a decisive closing line."
    );

    fun nextOrNull(): DebateRound? = when (this) {
        OPENING -> REBUTTAL
        REBUTTAL -> CLOSING
        CLOSING -> null
    }
}

enum class MascotState(
    val title: String,
    val swahiliMood: String,
    val defaultSpeech: String
) {
    IDLE(
        title = "Ready",
        swahiliMood = "Tayari",
        defaultSpeech = "Jambo. I am Mwalimu Kori. Let us sharpen your next argument."
    ),
    ANNOUNCING(
        title = "Motion",
        swahiliMood = "Tangazo",
        defaultSpeech = "Today's East African debate motion is ready."
    ),
    THINKING(
        title = "Pondering",
        swahiliMood = "Anawaza",
        defaultSpeech = "Weighing each point carefully..."
    ),
    IMPRESSED(
        title = "Impressed",
        swahiliMood = "Safi Sana",
        defaultSpeech = "Clean, structured, and precise. That point strikes like a secretary bird."
    ),
    CONCERNED(
        title = "Review",
        swahiliMood = "Angalia",
        defaultSpeech = "Let us examine the gaps in that argument."
    ),
    CHEERING(
        title = "Bravo",
        swahiliMood = "Hongera",
        defaultSpeech = "Hongera! A commanding performance across the rounds."
    ),
    ENCOURAGING(
        title = "Keep Going",
        swahiliMood = "Jikaze",
        defaultSpeech = "Every debate sharpens your reasoning. Keep building your streak!"
    )
}

data class DailyMotion(
    val dateKey: String,
    val level: DebateLevel,
    val motion: String,
    val shortContext: String,
    val keyTerms: List<KeyTerm>,
    val proposeHint: String,
    val opposeHint: String
)

data class KeyTerm(
    val term: String,
    val definition: String
)

data class DebateTurnMessage(
    val id: String,
    val round: DebateRound,
    val speaker: TurnSpeaker,
    val side: DebateSide,
    val text: String,
    val wordCount: Int,
    val exampleStrongAnswer: String? = null
)

enum class TurnSpeaker {
    USER,
    AI_OPPONENT
}

data class RewriteItFeedback(
    val original: String,
    val improved: String,
    val reason: String
)

data class DebateScoreResult(
    val argumentStrength: Int,
    val evidence: Int,
    val rebuttal: Int,
    val structure: Int,
    val writingQuality: Int,
    val overall: Double,
    val strengths: List<String>,
    val improvements: List<String>,
    val rewriteIt: RewriteItFeedback,
    val coachMessage: String
)
