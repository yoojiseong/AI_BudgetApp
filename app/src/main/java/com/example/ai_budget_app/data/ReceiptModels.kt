package com.example.ai_budget_app.data

import com.google.gson.annotations.SerializedName

data class ReceiptData(
    @SerializedName("storeName") val storeName: String,
    @SerializedName("date") val date: String,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("category") val category: String,
    @SerializedName("items") val items: List<ReceiptItem>
)

data class ReceiptItem(
    @SerializedName("itemName") val itemName: String,
    @SerializedName("price") val price: Int
)

data class PriceComparison(
    val item: ReceiptItem,
    val averagePrice: Int?, // null if not found
    val difference: Int?,
    val percentage: Double?
)
