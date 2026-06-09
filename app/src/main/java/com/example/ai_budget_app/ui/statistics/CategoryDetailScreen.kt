package com.example.ai_budget_app.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai_budget_app.data.local.AppDatabase
import com.example.ai_budget_app.data.repository.ExpenseRepository
import com.example.ai_budget_app.ui.home.RecentItem
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryName: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ExpenseRepository(AppDatabase.getDatabase(context).expenseDao()) }
    
    val categoryExpenses by repository.getExpensesByCategory(categoryName).collectAsState(initial = emptyList())
    val totalAmount = categoryExpenses.sumOf { it.totalAmount }
    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$categoryName 지출 내역", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2979FF))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("$categoryName 총 지출", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${formatter.format(totalAmount)}원", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${categoryExpenses.size}건의 지출", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
            }

            // List of Expenses
            if (categoryExpenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("해당 카테고리의 지출 내역이 없습니다.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categoryExpenses) { expense ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                RecentItem(
                                    store = expense.storeName,
                                    info = "${expense.date} · ${expense.paymentMethod}",
                                    price = "${formatter.format(expense.totalAmount)}원",
                                    isHigh = false
                                )
                                // If items exist, show them
                                if (expense.items.isNotEmpty()) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))
                                    expense.items.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("- ${item.itemName}", fontSize = 12.sp, color = Color.DarkGray)
                                            Text("${formatter.format(item.price)}원", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}
