package com.example.ai_budget_app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class PublicDataRepository {

    suspend fun getPriceComparison(item: ReceiptItem): PriceComparison = withContext(Dispatchers.IO) {
        // Here we would use Retrofit to call the Public Data API
        // For demonstration without an API key, we return mock data based on the item name
        delay(500)
        
        val avgPrice = when (item.itemName) {
            "사과" -> 7100
            "우유" -> 2800
            "계란" -> 6400
            else -> item.price // If not found, assume average is the same
        }

        val difference = item.price - avgPrice
        val percentage = if (avgPrice > 0) {
            (difference.toDouble() / avgPrice) * 100
        } else {
            0.0
        }

        PriceComparison(
            item = item,
            averagePrice = avgPrice,
            difference = difference,
            percentage = percentage
        )
    }

    suspend fun getPriceComparisons(items: List<ReceiptItem>): List<PriceComparison> {
        return items.map { getPriceComparison(it) }
    }
}
