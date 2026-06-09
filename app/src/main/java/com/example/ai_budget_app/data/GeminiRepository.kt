package com.example.ai_budget_app.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.ai_budget_app.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class GeminiRepository {

    // BuildConfig.GEMINI_API_KEY should be set in local.properties
    // For now we check if it exists or fallback to mock
    private val apiKey = try { BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: "" } catch(e: Exception) { "" }

    suspend fun parseReceiptImage(imageBytes: ByteArray): ReceiptData? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY") {
            Log.w("GeminiRepository", "API Key not found, returning mock data")
            return@withContext getMockData()
        }

        try {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )

            val prompt = """
                이 영수증 이미지에서 다음 정보를 추출하여 JSON 형식으로만 반환해줘. 다른 텍스트는 포함하지 마.
                - storeName: 상호명 (문자열)
                - date: 결제일시 (YYYY-MM-DD 형식)
                - paymentMethod: 결제 수단 (예: 신용카드, 현금 등)
                - items: 구매한 품목 리스트 (각 항목은 itemName(품목명), price(단가/금액)를 포함)
            """.trimIndent()

            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            val response = generativeModel.generateContent(inputContent)
            val jsonText = response.text?.replace("```json", "")?.replace("```", "")?.trim()
            
            if (jsonText != null) {
                Gson().fromJson(jsonText, ReceiptData::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Error parsing receipt", e)
            null
        }
    }

    private suspend fun getMockData(): ReceiptData {
        delay(2000) // Simulate network delay
        return ReceiptData(
            storeName = "이마트",
            date = "2026-05-18",
            paymentMethod = "신용카드",
            items = listOf(
                ReceiptItem("사과", 8500),
                ReceiptItem("우유", 3200),
                ReceiptItem("계란", 6900)
            )
        )
    }
}
