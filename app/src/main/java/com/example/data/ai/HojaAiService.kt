package com.example.data.ai

import android.util.Log
import com.example.data.model.DailyMotion
import com.example.data.model.DebateLevel
import com.example.data.model.DebateRound
import com.example.data.model.DebateScoreResult
import com.example.data.model.DebateSide
import com.example.data.model.DebateTurnMessage
import com.example.data.model.KeyTerm
import com.example.data.model.RewriteItFeedback
import com.example.data.model.TurnSpeaker
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "HojaAiService"
// Fast workhorse model for instant topic generation/rerolls and low-latency chat turns
private const val FAST_MODEL_NAME = "gemini-3.5-flash-lite"
// Frontier model for comprehensive end-of-debate adjudication and error analysis
private const val JUDGE_MODEL_NAME = "gemini-3.8-flash"

data class OpponentTurnResponse(
    val opponentReply: String,
    val exampleStrongAnswer: String
)

class HojaAiService {

    // A) Fast Topic & Reroll Generator Model
    private val topicModel by lazy {
        val keyTermSchema = Schema.obj(
            properties = mapOf(
                "term" to Schema.string(description = "Key debate term in the motion"),
                "definition" to Schema.string(description = "1-sentence clear definition")
            )
        )
        val schema = Schema.obj(
            properties = mapOf(
                "motion" to Schema.string(description = "Debate motion phrased as 'This House Believes That...' or 'This House Would...'"),
                "short_context" to Schema.string(description = "2 sentences of neutral background relevant to Kenya or East Africa"),
                "key_terms" to Schema.array(
                    items = keyTermSchema,
                    description = "3 key terms with short definitions"
                ),
                "propose_hint" to Schema.string(description = "Concise starter argument angle for Propose"),
                "oppose_hint" to Schema.string(description = "Concise starter argument angle for Oppose")
            )
        )
        Firebase.ai.generativeModel(
            modelName = FAST_MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
                temperature = 0.9f
            },
            systemInstruction = content {
                text(
                    "Generate one engaging, balanced debate motion for a debater in Kenya & East Africa " +
                        "(covering technology, youth, education, climate, devolution, economy, culture, or regional policy). " +
                        "Phrase as 'This House Believes That...' or 'This House Would...'. " +
                        "Match complexity to the requested rank: Beginner = everyday issues, Intermediate = national policy, " +
                        "Advanced = economic/social trade-offs, Champion = complex geopolitical/regional dilemmas. Return raw JSON."
                )
            }
        )
    }

    // B) Low-Latency Chat Opponent Model
    private val opponentModel by lazy {
        val schema = Schema.obj(
            properties = mapOf(
                "opponent_reply" to Schema.string(description = "The AI opponent's direct debate reply in the chat"),
                "example_strong_answer" to Schema.string(description = "1-2 sentences showing how the user can counter or strengthen their next turn")
            )
        )
        Firebase.ai.generativeModel(
            modelName = FAST_MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
                temperature = 0.7f
            }
        )
    }

    // C) End-of-Debate AI Adjudicator & Error Analyst Model
    private val scorerModel by lazy {
        val rewriteSchema = Schema.obj(
            properties = mapOf(
                "original" to Schema.string(description = "One weak or flawed sentence quoted directly from the user's turns"),
                "improved" to Schema.string(description = "A sharper, tournament-grade rewrite of that exact argument"),
                "reason" to Schema.string(description = "Why the original failed and why the rewrite wins the clash")
            )
        )
        val schema = Schema.obj(
            properties = mapOf(
                "argument_strength" to Schema.integer(description = "Score 0-10 for logic and claim mechanisms"),
                "evidence" to Schema.integer(description = "Score 0-10 for concrete examples"),
                "rebuttal" to Schema.integer(description = "Score 0-10 for directly engaging the opponent's points"),
                "structure" to Schema.integer(description = "Score 0-10 for clarity and organization"),
                "writing_quality" to Schema.integer(description = "Score 0-10 for persuasiveness and precision"),
                "overall" to Schema.double(description = "Overall score 0.0-10.0"),
                "strengths" to Schema.array(
                    items = Schema.string(),
                    description = "2 specific things the user did well"
                ),
                "improvements" to Schema.array(
                    items = Schema.string(),
                    description = "2-3 specific points explaining WHERE THE USER WENT WRONG (logical gaps, unbacked claims, or missed rebuttals)"
                ),
                "rewrite_it" to rewriteSchema,
                "coach_message" to Schema.string(description = "1-2 sentence verdict from Chief Adjudicator Mwalimu Kori")
            )
        )
        Firebase.ai.generativeModel(
            modelName = JUDGE_MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
                temperature = 0.35f
            },
            systemInstruction = content {
                text(
                    "You are Chief Adjudicator Mwalimu Kori analyzing a completed debate transcript. " +
                        "Pinpoint exactly where the user went wrong (logical fallacies, dropped opponent points, missing mechanisms, or vague claims) " +
                        "in 'improvements', quote their weakest line in 'rewrite_it.original' with a stronger version in 'rewrite_it.improved', " +
                        "and score fairly for their rank. Return raw JSON."
                )
            }
        )
    }

    suspend fun generateDailyMotion(
        dateKey: String,
        level: DebateLevel,
        avoidMotions: List<String> = emptyList()
    ): DailyMotion {
        val prompt = buildString {
            append("Date: $dateKey. Rank: ${level.displayName} (${level.description}). ")
            if (avoidMotions.isNotEmpty()) {
                append("REROLL: Pick a completely different topic domain and do NOT repeat any of these motions: ")
                append(avoidMotions.takeLast(5).joinToString(" | ") { "'$it'" })
            }
        }
        try {
            val response = withTimeout(45_000L) {
                topicModel.generateContent(prompt)
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty response from topic generator."))
            val motionText = json.optString("motion").takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Generated motion was blank.")

            val termsArray = json.optJSONArray("key_terms")
            val terms = buildList {
                if (termsArray != null) {
                    for (i in 0 until termsArray.length()) {
                        val item = termsArray.optJSONObject(i) ?: continue
                        add(
                            KeyTerm(
                                term = item.optString("term", "Key Term"),
                                definition = item.optString("definition", "")
                            )
                        )
                    }
                }
            }
            return DailyMotion(
                dateKey = dateKey,
                level = level,
                motion = motionText,
                shortContext = json.optString("short_context", ""),
                keyTerms = terms,
                proposeHint = json.optString("propose_hint", "Show why this policy solves an urgent challenge."),
                opposeHint = json.optString("oppose_hint", "Challenge the practical cost and unintended consequences.")
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Topic generation timed out", e)
            throw IllegalStateException("Topic generation timed out. Tap Reroll to try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Topic generation failed", e)
            throw IllegalStateException(friendlyAiErrorMessage(e))
        }
    }

    suspend fun generateOpponentTurn(
        motion: String,
        aiSide: DebateSide,
        level: DebateLevel,
        round: DebateRound,
        transcriptSoFar: List<DebateTurnMessage>
    ): OpponentTurnResponse {
        val transcriptFormatted = transcriptSoFar.joinToString("\n\n") { turn ->
            val who = if (turn.speaker == TurnSpeaker.USER) "User (${turn.side.label})" else "AI (${turn.side.label})"
            "[${turn.round.title} - $who]: ${turn.text}"
        }

        val prompt =
            "Motion: '$motion'\n" +
                "You are debating on the ${aiSide.label} bench against a ${level.displayName}-rank user in Round ${round.roundNumber} (${round.title}).\n" +
                "Directly counter the user's latest point and advance a sharp argument for ${aiSide.label} in 2-4 crisp sentences.\n\n" +
                "Transcript:\n$transcriptFormatted"

        try {
            val response = withTimeout(45_000L) {
                opponentModel.generateContent(prompt)
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty opponent response."))
            val reply = json.optString("opponent_reply").takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Opponent returned an empty reply.")
            return OpponentTurnResponse(
                opponentReply = reply,
                exampleStrongAnswer = json.optString("example_strong_answer", "")
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Opponent generation timed out", e)
            throw IllegalStateException("Opponent reply timed out. Tap Retry.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Opponent generation failed", e)
            throw IllegalStateException(friendlyAiErrorMessage(e))
        }
    }

    suspend fun scoreDebate(
        motion: String,
        userSide: DebateSide,
        level: DebateLevel,
        transcript: List<DebateTurnMessage>
    ): DebateScoreResult {
        val transcriptFormatted = transcript.joinToString("\n\n") { turn ->
            val who = if (turn.speaker == TurnSpeaker.USER) "USER (${turn.side.label})" else "OPPONENT (${turn.side.label})"
            "[${turn.round.title} - $who]:\n${turn.text}"
        }

        val prompt =
            "Motion: '$motion'\nUser Bench: ${userSide.label}\nUser Rank: ${level.displayName}\n\n" +
                "Transcript:\n$transcriptFormatted\n\n" +
                "Analyze the debate, explain specifically where the user went wrong in 'improvements', and provide the JSON verdict."

        try {
            val response = withTimeout(60_000L) {
                scorerModel.generateContent(prompt)
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty response from debate judge."))

            val argStrength = json.optInt("argument_strength", 7).coerceIn(0, 10)
            val evidence = json.optInt("evidence", 7).coerceIn(0, 10)
            val rebuttal = json.optInt("rebuttal", 7).coerceIn(0, 10)
            val structure = json.optInt("structure", 7).coerceIn(0, 10)
            val writingQuality = json.optInt("writing_quality", 7).coerceIn(0, 10)
            val computedAvg = (argStrength + evidence + rebuttal + structure + writingQuality) / 5.0
            val overall = json.optDouble("overall", computedAvg).let {
                if (it.isNaN()) computedAvg else it.coerceIn(0.0, 10.0)
            }

            val strengths = json.optJSONArray("strengths").toStringList()
                .ifEmpty { listOf("Clear stance on the ${userSide.label} bench.") }
            val improvements = json.optJSONArray("improvements").toStringList()
                .ifEmpty { listOf("Back your primary claim with a concrete East African mechanism and example.") }

            val rewriteObj = json.optJSONObject("rewrite_it")
            val rewriteIt = RewriteItFeedback(
                original = rewriteObj?.optString("original").orEmpty()
                    .ifBlank { "Your claim needed a clearer causal link." },
                improved = rewriteObj?.optString("improved").orEmpty()
                    .ifBlank { "This policy succeeds because it directly removes the bottleneck facing local communities." },
                reason = rewriteObj?.optString("reason").orEmpty()
                    .ifBlank { "Adds an explicit mechanism and sharper clash." }
            )

            return DebateScoreResult(
                argumentStrength = argStrength,
                evidence = evidence,
                rebuttal = rebuttal,
                structure = structure,
                writingQuality = writingQuality,
                overall = Math.round(overall * 10.0) / 10.0,
                strengths = strengths,
                improvements = improvements,
                rewriteIt = rewriteIt,
                coachMessage = json.optString("coach_message", "Debate analysis complete. Review the blind spots below.")
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Debate scoring timed out", e)
            throw IllegalStateException("Adjudicator analysis timed out. Tap Retry.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Debate scoring failed", e)
            throw IllegalStateException(friendlyAiErrorMessage(e))
        }
    }

    private fun org.json.JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { optString(it).takeIf { s -> s.isNotBlank() } }
    }

    private fun friendlyAiErrorMessage(e: Exception): String {
        val raw = e.localizedMessage ?: e.message ?: "Unknown AI error"
        return when {
            raw.contains("429") || raw.contains("quota", ignoreCase = true) || raw.contains("rate", ignoreCase = true) ->
                "Gemini rate limit reached. Wait a few seconds and tap Retry."
            raw.contains("network", ignoreCase = true) || raw.contains("Unable to resolve host", ignoreCase = true) ->
                "No internet connection detected. Check your network and retry."
            else -> "AI error: $raw"
        }
    }

    companion object {
        fun countWords(text: String): Int {
            val trimmed = text.trim()
            return if (trimmed.isEmpty()) 0 else trimmed.split(Regex("\\s+")).size
        }
    }
}
