package com.example.ai_budget_app.ui.receipt

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai_budget_app.data.ReceiptItem
import com.example.ai_budget_app.data.local.ExpenseEntity
import com.example.ai_budget_app.data.repository.ExpenseRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class ManualEntryViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val _storeName = mutableStateOf("")
    val storeName: State<String> = _storeName

    private val _date = mutableStateOf(LocalDate.now().toString()) // Default or current date
    val date: State<String> = _date

    private val _category = mutableStateOf("식료품")
    val category: State<String> = _category

    private val _paymentMethod = mutableStateOf("신용카드")
    val paymentMethod: State<String> = _paymentMethod

    private val _items = mutableStateOf<List<ReceiptItem>>(emptyList())
    val items: State<List<ReceiptItem>> = _items

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
