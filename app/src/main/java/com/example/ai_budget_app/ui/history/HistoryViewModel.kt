package com.example.ai_budget_app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai_budget_app.data.local.ExpenseEntity
import com.example.ai_budget_app.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.ai_budget_app.data.GeminiRepository

class HistoryViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val geminiRepository = GeminiRepository()
    private val publicDataRepository = com.example.ai_budget_app.data.PublicDataRepository()

    private val _insightFeedback = MutableStateFlow<String?>(null)
    val insightFeedback: StateFlow<String?> = _insightFeedback.asStateFlow()

    private val _publicDataComparisons = MutableStateFlow<List<com.example.ai_budget_app.data.PriceComparison>?>(null)
    val publicDataComparisons: StateFlow<List<com.example.ai_budget_app.data.PriceComparison>?> = _publicDataComparisons.asStateFlow()

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

    fun generateInsightFeedback(currentMonthExpenses: List<ExpenseEntity>) {
        viewModelScope.launch {
            _insightFeedback.value = "Loading..."
            
            val categoryTotals = currentMonthExpenses.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.totalAmount } }
            
            val allItems = currentMonthExpenses.flatMap { exp -> exp.items }
            val itemAverages = allItems.groupBy { it.itemName }
                .mapValues { entry -> 
                    val sum = entry.value.sumOf { it.price }
                    val count = entry.value.size
                    if (count > 0) sum / count else 0
                }
            
            val overspentItems = allItems.filter { item ->
                val avg = itemAverages[item.itemName] ?: 0
                avg > 0 && item.price > avg * 1.1
            }.map { it.itemName }.distinct()

            val feedback = geminiRepository.generateMonthlyFeedback(categoryTotals, overspentItems)
            _insightFeedback.value = feedback ?: "AI 분석에 실패했습니다. 나중에 다시 시도해주세요."
        }
    }

    fun resetInsightFeedback() {
        _insightFeedback.value = null
        _publicDataComparisons.value = null
    }

    fun fetchPublicDataComparisons(currentMonthExpenses: List<ExpenseEntity>) {
        viewModelScope.launch {
            val allItems = currentMonthExpenses.flatMap { it.items }
            // 중복 상품의 경우 가장 높은 가격 기준으로 한 번만 검사하도록 필터링
            val distinctItems = allItems.groupBy { it.itemName }
                .map { entry -> entry.value.maxByOrNull { it.price }!! }
            
            if (distinctItems.isNotEmpty()) {
                val comparisons = publicDataRepository.getPriceComparisons(distinctItems)
                // 평균가보다 비싸게 산 품목들만 필터링
                val overspent = comparisons.filter { it.difference != null && it.difference > 0 }
                _publicDataComparisons.value = overspent
            } else {
                _publicDataComparisons.value = emptyList()
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
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
