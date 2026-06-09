package com.example.ai_budget_app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai_budget_app.data.local.ExpenseEntity
import com.example.ai_budget_app.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: ExpenseRepository) : ViewModel() {

    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun populateDummyDataIfNeeded() {
        viewModelScope.launch {
            val currentExpenses = repository.allExpenses.first()
            if (currentExpenses.isEmpty()) {
                val dummy1 = ExpenseEntity(storeName = "이마트", date = "2026-05-18", category = "식료품", paymentMethod = "신용카드", items = emptyList(), totalAmount = 18600)
                val dummy2 = ExpenseEntity(storeName = "스타벅스", date = "2026-05-17", category = "카페/간식", paymentMethod = "신용카드", items = emptyList(), totalAmount = 9500)
                val dummy3 = ExpenseEntity(storeName = "GS25", date = "2026-05-16", category = "편의점", paymentMethod = "신용카드", items = emptyList(), totalAmount = 2700)
                repository.insertExpense(dummy1)
                repository.insertExpense(dummy2)
                repository.insertExpense(dummy3)
            }
        }
    }
}

class HistoryViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
