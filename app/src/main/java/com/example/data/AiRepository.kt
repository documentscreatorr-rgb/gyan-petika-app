package com.example.data

import android.util.Log
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "TranslationRepository"

data class TranslationLanguage(
  val code: String,
  val displayName: String,
  val nativeName: String,
)

object TranslationLanguages {
  val supported: List<TranslationLanguage> =
    listOf(
      TranslationLanguage("hi", "Hindi", "हिन्दी"),
      TranslationLanguage("en", "English", "English"),
      TranslationLanguage("bn", "Bengali", "বাংলা"),
      TranslationLanguage("mr", "Marathi", "मराठी"),
      TranslationLanguage("ta", "Tamil", "தமிழ்"),
      TranslationLanguage("te", "Telugu", "తెలుగు"),
      TranslationLanguage("gu", "Gujarati", "ગુજરાતી"),
      TranslationLanguage("pa", "Punjabi", "ਪੰਜਾਬੀ"),
      TranslationLanguage("ur", "Urdu", "اردو"),
      TranslationLanguage("kn", "Kannada", "ಕನ್ನಡ"),
      TranslationLanguage("ml", "Malayalam", "മലയാളം"),
    )
}

data class TranslationResult(
  val targetLanguage: TranslationLanguage,
  val translatedTitle: String,
  val translatedSegments: List<String>,
)

/**
 * Standard, non-AI translation boundary for study posts.
 */
interface AiRepository {
  suspend fun translatePostSegments(
    targetLanguage: TranslationLanguage,
    title: String,
    segments: List<String>,
  ): TranslationResult
}

/**
 * Fast, parallel standard translation engine backed by direct HTTP translation endpoints.
 * Completely independent of generative AI quotas/high demand and uses zero native C++ libraries.
 */
class StandardTranslationRepository : AiRepository {

  private val httpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(12, TimeUnit.SECONDS)
      .readTimeout(20, TimeUnit.SECONDS)
      .writeTimeout(12, TimeUnit.SECONDS)
      .build()
  }

  override suspend fun translatePostSegments(
    targetLanguage: TranslationLanguage,
    title: String,
    segments: List<String>,
  ): TranslationResult = withContext(Dispatchers.IO) {
    val combinedInput = listOf(title) + segments
    val batches = buildBatches(combinedInput, MAX_SEGMENTS_PER_CALL, MAX_CHARS_PER_CALL)

    val translatedCombined =
      coroutineScope {
        batches.chunked(MAX_CONCURRENT_BATCHES).flatMap { group ->
          group.map { batch ->
            async { translateBatch(targetLanguage.code, batch) }
          }.awaitAll().flatten()
        }
      }

    val translatedTitle =
      translatedCombined.firstOrNull()?.takeIf { it.isNotBlank() } ?: title
    val translatedBody =
      if (translatedCombined.size > 1) {
        translatedCombined.drop(1)
      } else {
        emptyList()
      }

    // Guarantee exact 1:1 count alignment so DOM text nodes never shift
    val alignedBody =
      segments.indices.map { idx ->
        translatedBody.getOrNull(idx)?.takeIf { it.isNotBlank() } ?: segments[idx]
      }

    TranslationResult(
      targetLanguage = targetLanguage,
      translatedTitle = translatedTitle,
      translatedSegments = alignedBody,
    )
  }

  private suspend fun translateBatch(
    targetCode: String,
    batch: List<String>,
  ): List<String> {
    if (batch.isEmpty()) return emptyList()

    try {
      return translateBatchPrimary(targetCode, batch)
    } catch (e: CancellationException) {
      throw e
    } catch (primaryErr: Exception) {
      Log.w(TAG, "Primary standard translator failed; trying secondary endpoint", primaryErr)
    }

    try {
      return translateBatchSecondary(targetCode, batch)
    } catch (e: CancellationException) {
      throw e
    } catch (secondaryErr: Exception) {
      Log.w(TAG, "Secondary standard translator failed; trying MyMemory fallback", secondaryErr)
    }

    return translateBatchMyMemory(targetCode, batch)
  }

  private suspend fun translateBatchPrimary(
    targetCode: String,
    batch: List<String>,
  ): List<String> {
    val sanitized = batch.map { it.replace(INLINE_NEWLINE_REGEX, " ").trim().ifEmpty { it } }
    val joined = sanitized.joinToString("\n")
    val translatedJoined = requestGtxSingle(targetCode, joined)
    val lines = translatedJoined.trimEnd('\n', '\r').split('\n')

    if (lines.size == sanitized.size) {
      return lines.mapIndexed { idx, line ->
        line.trim().ifBlank { batch[idx] }
      }
    }

    // Fallback to concurrent single-item translation if line splitting differed
    return coroutineScope {
      sanitized.chunked(6).flatMap { chunk ->
        chunk.map { item ->
          async {
            if (item.isBlank()) {
              item
            } else {
              runCatching { requestGtxSingle(targetCode, item).trim() }
                .getOrNull()
                ?.takeIf { it.isNotBlank() } ?: item
            }
          }
        }.awaitAll()
      }
    }
  }

  private suspend fun translateBatchSecondary(
    targetCode: String,
    batch: List<String>,
  ): List<String> {
    val sanitized = batch.map { it.replace(INLINE_NEWLINE_REGEX, " ").trim().ifEmpty { it } }
    val joined = sanitized.joinToString("\n")
    val translatedJoined = requestClients5Single(targetCode, joined)
    val lines = translatedJoined.trimEnd('\n', '\r').split('\n')

    if (lines.size == sanitized.size) {
      return lines.mapIndexed { idx, line ->
        line.trim().ifBlank { batch[idx] }
      }
    }

    return coroutineScope {
      sanitized.chunked(6).flatMap { chunk ->
        chunk.map { item ->
          async {
            if (item.isBlank()) {
              item
            } else {
              runCatching { requestClients5Single(targetCode, item).trim() }
                .getOrNull()
                ?.takeIf { it.isNotBlank() } ?: item
            }
          }
        }.awaitAll()
      }
    }
  }

  private fun requestGtxSingle(
    targetCode: String,
    text: String,
  ): String {
    val encodedTl = URLEncoder.encode(targetCode, "UTF-8")
    val url =
      "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=$encodedTl&dt=t"
    val formBody = FormBody.Builder().add("q", text).build()
    val request =
      Request.Builder()
        .url(url)
        .post(formBody)
        .header("User-Agent", "Mozilla/5.0 (Android)")
        .build()

    httpClient.newCall(request).execute().use { response ->
      val body = response.body?.string().orEmpty()
      if (!response.isSuccessful || body.isBlank()) {
        throw IllegalStateException("Translator HTTP ${response.code}")
      }
      val outer = JSONArray(body)
      val sentences =
        outer.optJSONArray(0)
          ?: throw IllegalStateException("Unexpected translator response format")
      val sb = StringBuilder()
      for (i in 0 until sentences.length()) {
        val segmentArr = sentences.optJSONArray(i) ?: continue
        val piece = segmentArr.optString(0, "")
        if (piece.isNotEmpty() && piece != "null") {
          sb.append(piece)
        }
      }
      return sb.toString().ifBlank {
        throw IllegalStateException("Translator returned empty text")
      }
    }
  }

  private fun requestClients5Single(
    targetCode: String,
    text: String,
  ): String {
    val encodedTl = URLEncoder.encode(targetCode, "UTF-8")
    val url =
      "https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=auto&tl=$encodedTl"
    val formBody = FormBody.Builder().add("q", text).build()
    val request =
      Request.Builder()
        .url(url)
        .post(formBody)
        .header("User-Agent", "Mozilla/5.0 (Android)")
        .build()

    httpClient.newCall(request).execute().use { response ->
      val body = response.body?.string().orEmpty()
      if (!response.isSuccessful || body.isBlank()) {
        throw IllegalStateException("Secondary translator HTTP ${response.code}")
      }
      val arr = JSONArray(body)
      val first = arr.opt(0)
      val translated =
        when (first) {
          is JSONArray -> first.optString(0, "")
          is String -> first
          else -> ""
        }
      return translated.ifBlank {
        throw IllegalStateException("Secondary translator returned empty text")
      }
    }
  }

  private suspend fun translateBatchMyMemory(
    targetCode: String,
    batch: List<String>,
  ): List<String> = coroutineScope {
    val sampleText = batch.joinToString(" ").take(400)
    val dominantSource = inferSourceLanguageCode(sampleText, targetCode)

    batch.chunked(4).flatMap { chunk ->
      chunk.map { item ->
        async {
          val trimmed = item.trim()
          if (trimmed.length < 2) return@async item
          val itemSource = inferSourceLanguageCode(trimmed, targetCode)
          val sourceCode = if (itemSource == targetCode) dominantSource else itemSource
          if (sourceCode == targetCode) return@async item

          val encodedQ = URLEncoder.encode(trimmed.take(480), "UTF-8")
          val langPair = URLEncoder.encode("$sourceCode|$targetCode", "UTF-8")
          val url = "https://api.mymemory.translated.net/get?q=$encodedQ&langpair=$langPair"
          val request = Request.Builder().url(url).get().build()

          httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful || body.isBlank()) {
              throw IllegalStateException("Backup translator HTTP ${response.code}")
            }
            val json = JSONObject(body)
            val status = json.optInt("responseStatus", 0)
            if (status != 200) {
              throw IllegalStateException("Backup translator status $status")
            }
            val translated =
              json.optJSONObject("responseData")?.optString("translatedText", "").orEmpty().trim()
            translated.ifBlank { item }
          }
        }
      }.awaitAll()
    }
  }

  private fun inferSourceLanguageCode(
    text: String,
    targetCode: String,
  ): String {
    var devanagari = 0
    var bengali = 0
    var tamil = 0
    var telugu = 0
    var gujarati = 0
    var gurmukhi = 0
    var malayalam = 0
    var kannada = 0
    var arabic = 0
    var latin = 0

    for (ch in text) {
      when (ch.code) {
        in 0x0900..0x097F -> devanagari++
        in 0x0980..0x09FF -> bengali++
        in 0x0B80..0x0BFF -> tamil++
        in 0x0C00..0x0C7F -> telugu++
        in 0x0A80..0x0AFF -> gujarati++
        in 0x0A00..0x0A7F -> gurmukhi++
        in 0x0D00..0x0D7F -> malayalam++
        in 0x0C80..0x0CFF -> kannada++
        in 0x0600..0x06FF -> arabic++
        in 'A'.code..'Z'.code, in 'a'.code..'z'.code -> latin++
      }
    }

    val best =
      listOf(
        "hi" to devanagari,
        "bn" to bengali,
        "ta" to tamil,
        "te" to telugu,
        "gu" to gujarati,
        "pa" to gurmukhi,
        "ml" to malayalam,
        "kn" to kannada,
        "ur" to arabic,
        "en" to latin,
      ).maxByOrNull { it.second }

    if (best != null && best.second > 0) return best.first
    return if (targetCode == "en") "hi" else "en"
  }

  private fun buildBatches(
    items: List<String>,
    maxSegments: Int,
    maxChars: Int,
  ): List<List<String>> {
    if (items.isEmpty()) return emptyList()
    val batches = mutableListOf<List<String>>()
    var current = mutableListOf<String>()
    var currentChars = 0

    for (item in items) {
      val itemLen = item.length
      if (
        current.isNotEmpty() &&
          (current.size >= maxSegments || currentChars + itemLen > maxChars)
      ) {
        batches.add(current)
        current = mutableListOf()
        currentChars = 0
      }
      current.add(item)
      currentChars += itemLen
    }
    if (current.isNotEmpty()) {
      batches.add(current)
    }
    return batches
  }

  private companion object {
    const val MAX_SEGMENTS_PER_CALL = 20
    const val MAX_CHARS_PER_CALL = 1_600
    const val MAX_CONCURRENT_BATCHES = 4
    val INLINE_NEWLINE_REGEX = Regex("\\s*\\r?\\n+\\s*")
  }
}
