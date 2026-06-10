package com.example.ai_budget_app.data.remote

import com.google.gson.annotations.SerializedName

data class PublicDataResponse(
    @SerializedName("currentCount") val currentCount: Int,
    @SerializedName("data") val data: List<PriceData>,
    @SerializedName("matchCount") val matchCount: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("perPage") val perPage: Int,
    @SerializedName("totalCount") val totalCount: Int
)

data class PriceData(
    @SerializedName("상품명") val productName: String?,
    @SerializedName("판매가격") val price: String?,
    @SerializedName("판매업소") val shopName: String?,
    @SerializedName("제조사") val manufacturer: String?,
    @SerializedName("조사일") val surveyDate: String?
)
