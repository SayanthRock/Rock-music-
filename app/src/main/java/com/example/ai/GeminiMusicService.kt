package com.example.ai

import com.example.BuildConfig
import com.example.data.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// --- Gemini API Serialization Models ---

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: JsonObject? = null,
    val temperature: Float? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content
)

// --- Retrofit Endpoint Definitions ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

// --- Music Recognition & Smart Recommendation Service ---

object GeminiMusicService {
    private val apiKey = BuildConfig.GEMINI_API_KEY

    /**
     * Echo Find AI Assistant: Analyzes sung syllables, descriptive hums, or partial lyrics
     * and accurately matches them against the global Rock Music database with full trivia!
     */
    suspend fun recognizeTrackByLyrics(lyricsSyllables: String): RecognitionResult = withContext(Dispatchers.IO) {
        val prompt = """
            You are Echo Find, the world's most advanced rock music recognition neural network.
            Analyze the following hummed description, voice syllables, or partial lyrics of a rock song: "$lyricsSyllables".
            
            Identify the absolute most probable rock song matching this input, its artist, release year, album, and 1 interesting trivia detail.
            If the input is vague, make a top intelligent guess.
            
            Return the output STRICTLY in the following format:
            Track Name: <Track Name>
            Artist Name: <Artist Name/Band>
            Album Name: <Album Name>
            Release Year: <Year>
            Trivia: <Interesting trivia about this song/band>
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        try {
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext RecognitionResult(
                    isMatched = true,
                    title = "Born to Speed",
                    artist = "The Thunderbolts",
                    album = "High Voltage Grit",
                    year = "2026",
                    trivia = "Simulated Match: This high-voltage groove was born on a desert road under neon-crimson skies.",
                    details = "No Gemini API key detected in AI Studio secrets. To enable live AI recognition, add GEMINI_API_KEY in the Secrets panel."
                )
            }
            val response = GeminiClient.service.generateContent(apiKey, request)
            val output = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            parseRecognitionResult(output)
        } catch (e: Exception) {
            RecognitionResult(
                isMatched = false,
                title = "Unknown Rock Symphony",
                artist = "Mysterious Band",
                album = "Unknown",
                year = "n/a",
                trivia = "Failed to evaluate due to connectivity or config.",
                details = e.localizedMessage ?: "API Error"
            )
        }
    }

    private fun parseRecognitionResult(rawOutput: String): RecognitionResult {
        var title = "Unknown Track"
        var artist = "Unknown Artist"
        var album = "Unknown Album"
        var year = "N/A"
        var trivia = "A timeless rock classic."
        
        val lines = rawOutput.lines()
        for (line in lines) {
            when {
                line.startsWith("Track Name:") -> title = line.substringAfter("Track Name:").trim()
                line.startsWith("Artist Name:") -> artist = line.substringAfter("Artist Name:").trim()
                line.startsWith("Album Name:") -> album = line.substringAfter("Album Name:").trim()
                line.startsWith("Release Year:") -> year = line.substringAfter("Release Year:").trim()
                line.startsWith("Trivia:") -> trivia = line.substringAfter("Trivia:").trim()
            }
        }
        return RecognitionResult(
            isMatched = true,
            title = title,
            artist = artist,
            album = album,
            year = year,
            trivia = trivia,
            details = "Matched via Gemini AI Music Identification Network"
        )
    }

    /**
     * AI Rock Recommendation: Suggests tracks based on mood description.
     */
    suspend fun getAiRockSuggestions(moodPrompt: String): String = withContext(Dispatchers.IO) {
        val prompt = """
            As a premium Rock DJ and record curator, generate a short list of 3 rock song suggestions (including real bands)
            with a 1-sentence description of why they fit the mood/vibe described here: "$moodPrompt".
            Keep your feedback incredibly cool, concise, and punchy.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        try {
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext "🎵 1. Rock Anthem (The Rebels) - Energetic anthem perfect for starting a ride!\n🎵 2. Cosmic Distortion (Space Echoes) - Mind-bending space progressive sound.\n🎵 3. Grunge Tears (The Seattleites) - Melancholic 90s alternative drive.\n\n*Add GEMINI_API_KEY in the Secrets panel to activate live AI curated selections.*"
            }
            val response = GeminiClient.service.generateContent(apiKey, request)
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Could not build suggestions list at this time."
        } catch (e: Exception) {
            "Error loading AI suggestions: ${e.localizedMessage}"
        }
    }
}

@Serializable
data class RecognitionResult(
    val isMatched: Boolean,
    val title: String,
    val artist: String,
    val album: String,
    val year: String,
    val trivia: String,
    val details: String
)
