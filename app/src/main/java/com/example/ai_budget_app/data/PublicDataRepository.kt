package com.example.ai_budget_app.data

import android.util.Log
import com.example.ai_budget_app.BuildConfig
import com.example.ai_budget_app.data.remote.PublicDataApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLDecoder

class PublicDataRepository {

    private val api: PublicDataApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.odcloud.kr/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PublicDataApi::class.java)
    }

    suspend fun getPriceComparison(item: ReceiptItem): PriceComparison = withContext(Dispatchers.IO) {
        var avgPrice = item.price // 기본값: 현재 가격과 동일하게 가정 (비교 결과 0)
        
        try {
            val rawKey = BuildConfig.PUBLIC_DATA_API_KEY
            if (rawKey.isNotBlank() && rawKey != "YOUR_API_KEY") {
                // 키가 인코딩되어 제공된다면 디코딩을 해줘야 할 수 있지만, Retrofit @Query는 기본적으로 URL Encoding을 합니다.
                // 만약 에러가 나면 @Query(encoded=true)를 고려.
                val decodedKey = URLDecoder.decode(rawKey, "UTF-8")

                // 전체 데이터를 가져와서 필터링 (간단화: 실제로는 page 처리를 해야하지만 데모용으로 1페이지만 조회)
                val response = api.getPriceInfo(page = 1, perPage = 1000, serviceKey = decodedKey)
                
                // 상품명에서 괄호와 그 안의 내용(예: 용량), 공백을 제거하여 비교의 정확도를 높임
                val cleanItemName = item.itemName.replace(Regex("\\(.*?\\)"), "").replace(" ", "")
                val matchedItems = response.data.filter {
                    val cleanProductName = it.productName?.replace(Regex("\\(.*?\\)"), "")?.replace(" ", "") ?: ""
                    cleanProductName.isNotEmpty() && cleanItemName.isNotEmpty() && 
                    (cleanProductName.contains(cleanItemName) || cleanItemName.contains(cleanProductName))
                }
                
                if (matchedItems.isNotEmpty()) {
                    val prices = matchedItems.mapNotNull { it.price?.replace(",", "")?.toIntOrNull() }
                    if (prices.isNotEmpty()) {
                        avgPrice = prices.sum() / prices.size
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PublicDataRepository", "API Error: ${e.message}", e)
        }

        val difference = item.price - avgPrice
        val percentage = if (avgPrice > 0) {
            (difference.toDouble() / avgPrice) * 100
        } else {
            0.0
        }

        PriceComparison(
            item = item,
            averagePrice = if (avgPrice == item.price) null else avgPrice,
            difference = difference,
            percentage = percentage
        )
    }

    suspend fun getPriceComparisons(items: List<ReceiptItem>): List<PriceComparison> {
        return items.map { getPriceComparison(it) }
    }
}
