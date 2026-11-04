package com.example.data.ai

import android.content.Context
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
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "HojaAiService"
private const val GEMINI_MODEL = "gemini-2.5-flash"

data class OpponentTurnResponse(
    val opponentReply: String,
    val exampleStrongAnswer: String
)

class HojaAiService(private val context: Context? = null) {

    private fun getEffectiveApiKey(): String {
        return context?.getSharedPreferences("hoja_local_prefs", Context.MODE_PRIVATE)
            ?.getString("gemini_api_key", "")
            ?.trim()
            .orEmpty()
    }

    suspend fun generateDailyMotion(
        dateKey: String,
        level: DebateLevel,
        avoidMotions: List<String> = emptyList()
    ): DailyMotion {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isNotBlank()) {
            try {
                val prompt = buildString {
                    append("Date: $dateKey. Rank: ${level.displayName} (${level.description}). ")
                    if (avoidMotions.isNotEmpty()) {
                        append("REROLL: Pick a completely different topic domain and do NOT repeat any of these motions: ")
                        append(avoidMotions.takeLast(5).joinToString(" | ") { "'$it'" })
                    }
                }
                val systemInstruction =
                    "Generate one engaging, balanced debate motion for a debater in Kenya & East Africa " +
                        "(covering technology, youth, education, climate, devolution, economy, culture, or regional policy). " +
                        "Phrase as 'This House Believes That...' or 'This House Would...'. " +
                        "Match complexity to the requested rank: Beginner = everyday issues, Intermediate = national policy, " +
                        "Advanced = economic/social trade-offs, Champion = complex geopolitical/regional dilemmas. " +
                        "Return JSON with keys: motion (string), short_context (string), key_terms (array of objects with term and definition), " +
                        "propose_hint (string), oppose_hint (string)."

                val responseText = callGeminiRest(
                    apiKey = apiKey,
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    temperature = 0.85f
                )

                val json = JSONObject(responseText)
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
            } catch (e: Exception) {
                Log.w(TAG, "Live Gemini motion generation failed; falling back to curated engine", e)
            }
        }

        // Seamless, high-quality fallback engine
        return CuratedDebateEngine.getDailyMotion(dateKey, level, avoidMotions)
    }

    suspend fun generateOpponentTurn(
        motion: String,
        aiSide: DebateSide,
        level: DebateLevel,
        round: DebateRound,
        transcriptSoFar: List<DebateTurnMessage>
    ): OpponentTurnResponse {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isNotBlank()) {
            try {
                val transcriptFormatted = transcriptSoFar.joinToString("\n\n") { turn ->
                    val who = if (turn.speaker == TurnSpeaker.USER) "User (${turn.side.label})" else "AI (${turn.side.label})"
                    "[${turn.round.title} - $who]: ${turn.text}"
                }

                val prompt =
                    "Motion: '$motion'\n" +
                        "You are debating on the ${aiSide.label} bench against a ${level.displayName}-rank user in Round ${round.roundNumber} (${round.title}).\n" +
                        "Directly counter the user's latest point and advance a sharp argument for ${aiSide.label} in 2-4 crisp sentences.\n\n" +
                        "Transcript:\n$transcriptFormatted"

                val systemInstruction =
                    "You are a sparring debate opponent in East Africa. Return JSON with keys: " +
                        "opponent_reply (string with your direct debate speech) and " +
                        "example_strong_answer (1-2 sentences showing how the user can counter your point)."

                val responseText = callGeminiRest(
                    apiKey = apiKey,
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    temperature = 0.7f
                )

                val json = JSONObject(responseText)
                val reply = json.optString("opponent_reply").takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException("Opponent returned an empty reply.")

                return OpponentTurnResponse(
                    opponentReply = reply,
                    exampleStrongAnswer = json.optString("example_strong_answer", "")
                )
            } catch (e: Exception) {
                Log.w(TAG, "Live Gemini opponent generation failed; falling back to curated engine", e)
            }
        }

        // Seamless fallback
        return CuratedDebateEngine.generateOpponentTurn(motion, aiSide, level, round, transcriptSoFar)
    }

    suspend fun scoreDebate(
        motion: String,
        userSide: DebateSide,
        level: DebateLevel,
        transcript: List<DebateTurnMessage>
    ): DebateScoreResult {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isNotBlank()) {
            try {
                val transcriptFormatted = transcript.joinToString("\n\n") { turn ->
                    val who = if (turn.speaker == TurnSpeaker.USER) "USER (${turn.side.label})" else "OPPONENT (${turn.side.label})"
                    "[${turn.round.title} - $who]:\n${turn.text}"
                }

                val prompt =
                    "Motion: '$motion'\nUser Bench: ${userSide.label}\nUser Rank: ${level.displayName}\n\n" +
                        "Transcript:\n$transcriptFormatted\n\n" +
                        "Analyze the debate, explain specifically where the user went wrong in 'improvements', quote their weakest sentence in 'rewrite_it.original' with a sharper tournament-grade rewrite in 'rewrite_it.improved' and 'rewrite_it.reason', and provide the JSON verdict."

                val systemInstruction =
                    "You are Chief Adjudicator Mwalimu Kori. Return JSON with keys: " +
                        "argument_strength (integer 0-10), evidence (integer 0-10), rebuttal (integer 0-10), " +
                        "structure (integer 0-10), writing_quality (integer 0-10), overall (double 0.0-10.0), " +
                        "strengths (array of 2 strings), improvements (array of 2-3 strings), " +
                        "rewrite_it (object with original, improved, reason), coach_message (string)."

                val responseText = callGeminiRest(
                    apiKey = apiKey,
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    temperature = 0.35f
                )

                val json = JSONObject(responseText)
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
            } catch (e: Exception) {
                Log.w(TAG, "Live Gemini debate scoring failed; falling back to curated engine", e)
            }
        }

        // Seamless fallback
        return CuratedDebateEngine.scoreDebate(motion, userSide, level, transcript)
    }

    private suspend fun callGeminiRest(
        apiKey: String,
        prompt: String,
        systemInstruction: String? = null,
        temperature: Float = 0.7f
    ): String = withContext(Dispatchers.IO) {
        val urlStr = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent?key=$apiKey"
        val url = URL(urlStr)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            doOutput = true
        }

        val requestObj = JSONObject().apply {
            val contentsArr = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            }
            put("contents", contentsArr)
            if (!systemInstruction.isNullOrBlank()) {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    })
                })
            }
            put("generationConfig", JSONObject().apply {
                put("temperature", temperature)
                put("responseMimeType", "application/json")
            })
        }

        conn.outputStream.use { os ->
            os.write(requestObj.toString().toByteArray(Charsets.UTF_8))
        }

        val responseCode = conn.responseCode
        val responseStream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
        val responseText = responseStream.bufferedReader().use { it.readText() }

        if (responseCode !in 200..299) {
            throw IllegalStateException("Gemini API error ($responseCode): $responseText")
        }

        val respJson = JSONObject(responseText)
        val candidates = respJson.optJSONArray("candidates")
            ?: throw IllegalStateException("No candidates returned from Gemini.")
        val firstCandidate = candidates.optJSONObject(0)
            ?: throw IllegalStateException("Empty candidate list.")
        val content = firstCandidate.optJSONObject("content")
            ?: throw IllegalStateException("No content in candidate.")
        val parts = content.optJSONArray("parts")
            ?: throw IllegalStateException("No parts in candidate content.")
        val firstPart = parts.optJSONObject(0)
            ?: throw IllegalStateException("No part object.")

        firstPart.optString("text")
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { optString(it).takeIf { s -> s.isNotBlank() } }
    }

    companion object {
        fun countWords(text: String): Int {
            val trimmed = text.trim()
            return if (trimmed.isEmpty()) 0 else trimmed.split(Regex("\\s+")).count { it.isNotBlank() }
        }
    }
}
