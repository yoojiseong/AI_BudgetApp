package com.example.ai_budget_app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ai_budget_app.data.ReceiptItem

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val storeName: String,
    val date: String,
    val category: String,
    val paymentMethod: String,
    val items: List<ReceiptItem>,
    val totalAmount: Int
)
