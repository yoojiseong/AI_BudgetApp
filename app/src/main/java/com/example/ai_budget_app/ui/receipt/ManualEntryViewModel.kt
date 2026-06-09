package com.example.ai_budget_app.ui.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai_budget_app.data.ReceiptItem
import com.example.ai_budget_app.data.local.ExpenseEntity
import com.example.ai_budget_app.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ManualEntryViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val _storeName = MutableStateFlow("")
    val storeName: StateFlow<String> = _storeName.asStateFlow()

    private val _date = MutableStateFlow("2026-05-18") // Default or current date
    val date: StateFlow<String> = _date.asStateFlow()

    private val _category = MutableStateFlow("식료품")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _paymentMethod = MutableStateFlow("신용카드")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _items = MutableStateFlow<List<ReceiptItem>>(emptyList())
    val items: StateFlow<List<ReceiptItem>> = _items.asStateFlow()

    fun updateStoreName(name: String) { _storeName.value = name }
    fun updateDate(newDate: String) { _date.value = newDate }
    fun updateCategory(newCategory: String) { _category.value = newCategory }
    fun updatePaymentMethod(method: String) { _paymentMethod.value = method }

    fun addItem() {
        val currentItems = _items.value.toMutableList()
        currentItems.add(ReceiptItem(itemName = "", price = 0))
        _items.value = currentItems
    }

    fun updateItemName(index: Int, name: String) {
        val currentItems = _items.value.toMutableList()
        if (index in currentItems.indices) {
            currentItems[index] = currentItems[index].copy(itemName = name)
            _items.value = currentItems
        }
    }

    fun updateItemPrice(index: Int, priceStr: String) {
        val currentItems = _items.value.toMutableList()
        if (index in currentItems.indices) {
            val price = priceStr.filter { it.isDigit() }.toIntOrNull() ?: 0
            currentItems[index] = currentItems[index].copy(price = price)
            _items.value = currentItems
        }
    }

    fun removeItem(index: Int) {
        val currentItems = _items.value.toMutableList()
        if (index in currentItems.indices) {
            currentItems.removeAt(index)
            _items.value = currentItems
        }
    }

    fun saveExpense(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val totalAmount = _items.value.sumOf { it.price }
            val expense = ExpenseEntity(
                storeName = _storeName.value,
                date = _date.value,
                category = _category.value,
                paymentMethod = _paymentMethod.value,
                items = _items.value,
                totalAmount = totalAmount
            )
            repository.insertExpense(expense)
            onSuccess()
        }
    }
}

class ManualEntryViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManualEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManualEntryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
