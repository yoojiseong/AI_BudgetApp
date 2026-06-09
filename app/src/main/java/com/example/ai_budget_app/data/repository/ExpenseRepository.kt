package com.example.ai_budget_app.data.repository

import com.example.ai_budget_app.data.local.ExpenseDao
import com.example.ai_budget_app.data.local.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByCategory(category)
    }

    suspend fun insertExpense(expense: ExpenseEntity) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            expenseDao.insertExpense(expense)
        }
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            expenseDao.deleteExpense(expense)
        }
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            expenseDao.updateExpense(expense)
        }
    }
}
