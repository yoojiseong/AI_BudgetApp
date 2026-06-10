package com.example.ai_budget_app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface PublicDataApi {
    @GET("15083256/v1/uddi:2aac4945-acb4-4e13-b4ce-bc6b80f0d476")
    suspend fun getPriceInfo(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 100,
        @Query("returnType") returnType: String = "JSON",
        @Query("serviceKey") serviceKey: String
    ): PublicDataResponse
}
