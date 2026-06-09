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

    suspend fun generateMonthlyFeedback(categoryTotals: Map<String, Int>, overspentItems: List<String>): String? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY") {
            delay(1500)
            return@withContext "이번 달은 전반적으로 예산 관리가 잘 되고 있습니다. 다만, 일부 과소비 항목이 발견되었으니 다음 달에는 조금 더 신경 써보는 건 어떨까요?"
        }

        try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )

            val prompt = """
                다음은 사용자의 이번 달 지출 내역 요약입니다:
                - 카테고리별 총 지출: $categoryTotals
                - 평균 시세보다 비싸게 구매한 품목(과소비): $overspentItems
                
                이 데이터를 바탕으로 사용자에게 예산 관리를 돕는 2~3문장의 따뜻하고 유용한 재무 조언을 한국어로 해줘.
                마크다운 없이, 인사말 없이 바로 조언 본문만 출력해.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            response.text?.trim() ?: "AI가 안전성 문제로 응답을 거부했거나 빈 텍스트를 반환했습니다."
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Error generating feedback", e)
            "API 통신 오류: ${e.localizedMessage}\n네트워크 상태나 API 키 설정을 확인해주세요."
        }
    }
}
