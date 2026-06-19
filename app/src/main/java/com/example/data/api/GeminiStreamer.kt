package com.example.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object GeminiStreamer {
    private val responseAdapter by lazy {
        RetrofitClient.moshiInstance.adapter(GenerateContentResponse::class.java)
    }

    suspend fun streamContent(
        model: String,
        apiKey: String,
        requestBody: GenerateContentRequest,
        onRetry: () -> Unit = {},
        onChunk: (thought: String?, text: String?) -> Unit
    ) = withContext(Dispatchers.IO) {
        val maxAttempts = 3
        var lastException: Exception? = null
        var currentRequestBody = requestBody

        for (attempt in 1..maxAttempts) {
            try {
                if (attempt > 1) {
                    onRetry()
                    // Adaptive backoff: 1000ms, 2000ms
                    kotlinx.coroutines.delay((attempt - 1) * 1000L)
                }

                executeStream(model, apiKey, currentRequestBody, onChunk)
                return@withContext // Success! Exit function.
            } catch (e: Exception) {
                lastException = e
                
                val errorMsg = e.message ?: ""
                val hasThinkingConfig = currentRequestBody.generationConfig?.thinkingConfig != null
                val isThinkingOr400Error = errorMsg.contains("thinking", ignoreCase = true) || 
                                          errorMsg.contains("budget", ignoreCase = true) ||
                                          errorMsg.contains("INVALID_ARGUMENT", ignoreCase = true) ||
                                          errorMsg.contains("400")

                if (hasThinkingConfig && isThinkingOr400Error) {
                    // Seamlessly strip thinkingConfig to fallback to regular generation config
                    val strippedGenConfig = currentRequestBody.generationConfig?.copy(thinkingConfig = null)
                    currentRequestBody = currentRequestBody.copy(generationConfig = strippedGenConfig)
                }

                // If it is our last attempt, we let the exception bubble up to the caller
                if (attempt == maxAttempts) {
                    throw e
                }
            }
        }
        throw lastException ?: Exception("Streaming failed")
    }

    private suspend fun executeStream(
        model: String,
        apiKey: String,
        requestBody: GenerateContentRequest,
        onChunk: (thought: String?, text: String?) -> Unit
    ) {
        val response = RetrofitClient.service.streamGenerateContent(
            model = model,
            apiKey = apiKey,
            request = requestBody
        )

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            throw Exception("API Error (${response.code()}): $errorBody")
        }

        val body = response.body() ?: throw Exception("Response body is null")
        
        val buffer = StringBuilder()
        var braceCount = 0
        var inString = false
        var escaping = false

        body.charStream().use { reader ->
            val charBuffer = CharArray(4096)
            var charsRead: Int
            while (reader.read(charBuffer).also { charsRead = it } != -1) {
                for (i in 0 until charsRead) {
                    val c = charBuffer[i]
                    buffer.append(c)

                    if (escaping) {
                        escaping = false
                        continue
                    }

                    if (c == '\\') {
                        escaping = true
                        continue
                    }

                    if (c == '"') {
                        inString = !inString
                        continue
                    }

                    if (!inString) {
                        if (c == '{') {
                            braceCount++
                        } else if (c == '}') {
                            braceCount--
                            if (braceCount == 0 && buffer.isNotEmpty()) {
                                val firstBraceIndex = buffer.indexOf('{')
                                if (firstBraceIndex != -1) {
                                    val jsonStr = buffer.substring(firstBraceIndex)
                                    try {
                                        val chunkJson = responseAdapter.fromJson(jsonStr)
                                        val firstCandidate = chunkJson?.candidates?.firstOrNull()
                                        val parts = firstCandidate?.content?.parts ?: emptyList()
                                        
                                        for (part in parts) {
                                            if (part.thought == true) {
                                                onChunk(part.text, null)
                                            } else {
                                                onChunk(null, part.text)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Ignore parse errors of incomplete brackets or commas outside content objects
                                    }
                                }
                                buffer.setLength(0) // Reset chunk buffer
                            }
                        }
                    }
                }
            }
        }
    }
}
