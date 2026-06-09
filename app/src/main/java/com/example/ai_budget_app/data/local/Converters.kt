package com.example.ai_budget_app.data.local

import androidx.room.TypeConverter
import com.example.ai_budget_app.data.ReceiptItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromReceiptItemList(items: List<ReceiptItem>?): String? {
        if (items == null) return null
        val type = object : TypeToken<List<ReceiptItem>>() {}.type
        return gson.toJson(items, type)
    }

    @TypeConverter
    fun toReceiptItemList(itemsString: String?): List<ReceiptItem>? {
        if (itemsString == null) return null
        val type = object : TypeToken<List<ReceiptItem>>() {}.type
        return gson.fromJson(itemsString, type)
    }
}
