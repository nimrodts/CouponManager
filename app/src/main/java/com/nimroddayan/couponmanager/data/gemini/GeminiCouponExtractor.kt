package com.nimroddayan.couponmanager.data.gemini

import com.google.ai.client.generativeai.GenerativeModel
import com.nimroddayan.couponmanager.data.gemini.InvalidApiKeyException
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.io.IOException

class GeminiCouponExtractor(private val geminiApiKeyRepository: GeminiApiKeyRepository) {

    suspend fun extractCoupon(text: String): ParsedCoupon {
        val apiKey = geminiApiKeyRepository.getApiKey.first()
        if (apiKey.isEmpty()) {
            throw InvalidApiKeyException("API Key is empty")
        }

        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )

        val prompt = """Analyze the following text and extract coupon details. Return ONLY a raw JSON object with keys: 'storeName' (string), 'code' (string, null if missing), 'initialValue' (double), 'expirationDate' (string format yyyy-MM-dd, null if missing), 'description' (string summary). Input text: $text"""

        val response = generativeModel.generateContent(prompt)

        val json = response.text?.trim()?.replace("```json", "")?.replace("```", "")

        if (json.isNullOrBlank()) {
            throw IOException("Failed to extract coupon details. Empty or blank response from Gemini model.")
        }

        val jsonObject = JSONObject(json)

        return ParsedCoupon(
            storeName = jsonObject.optString("storeName"),
            code = jsonObject.optString("code"),
            initialValue = jsonObject.optDouble("initialValue"),
            expirationDate = jsonObject.optString("expirationDate"),
            description = jsonObject.optString("description")
        )
    }
}
