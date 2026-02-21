package com.example.reportitindia.ai

import android.graphics.Bitmap
import com.example.reportitindia.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

data class AIAnalysisResult(
    val category: String,
    val description: String,
    val severity: String
)

class GeminiRepository {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun analyzeComplaintImage(bitmap: Bitmap): Result<AIAnalysisResult> {
        return try {
            val prompt = """
                You are analyzing a civic complaint photo from India.
                Look at this image and respond ONLY in this exact format with no extra text:
                CATEGORY: [one of: Roads, Sanitation, Water, Electricity, Parks, Street Lights, Drainage, Other]
                DESCRIPTION: [one sentence describing the issue]
                SEVERITY: [Low, Medium, or High]
            """.trimIndent()

            val response = model.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )

            val text = response.text ?: return Result.failure(Exception("No response"))
            val result = parseAIResponse(text)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseAIResponse(text: String): AIAnalysisResult {
        val lines = text.trim().lines()
        var category = "Other"
        var description = ""
        var severity = "Medium"

        lines.forEach { line ->
            when {
                line.startsWith("CATEGORY:") -> category = line.removePrefix("CATEGORY:").trim()
                line.startsWith("DESCRIPTION:") -> description = line.removePrefix("DESCRIPTION:").trim()
                line.startsWith("SEVERITY:") -> severity = line.removePrefix("SEVERITY:").trim()
            }
        }

        return AIAnalysisResult(category, description, severity)
    }
}