package com.example.ai_budget_app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai_budget_app.data.local.AppDatabase
import com.example.ai_budget_app.data.repository.ExpenseRepository
import com.example.ai_budget_app.ui.home.RecentItem
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val repository = remember { ExpenseRepository(AppDatabase.getDatabase(context).expenseDao()) }
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(repository))
    
    val expenses by viewModel.expenses.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("전체") }
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    val categories = listOf("전체", "식료품", "카페/간식", "편의점", "쇼핑", "기타")

    // Filter and group
    val filteredExpenses = expenses.filter {
        it.date.startsWith(selectedMonth.toString()) &&
        (selectedCategory == "전체" || it.category == selectedCategory) &&
        (searchQuery.isBlank() || it.storeName.contains(searchQuery, ignoreCase = true) || it.items.any { item -> item.itemName.contains(searchQuery, ignoreCase = true) })
    }
    
    val groupedExpenses = filteredExpenses.groupBy { it.date }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("지출 내역", fontWeight = FontWeight.Bold) },
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
            // Month Selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedMonth = selectedMonth.minusMonths(1) }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "이전 달")
                }
                Text(
                    text = "${selectedMonth.year}년 ${selectedMonth.monthValue}월",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(onClick = { selectedMonth = selectedMonth.plusMonths(1) }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "다음 달")
                }
            }

            // Search Bar
            Box(modifier = Modifier.padding(16.dp)) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("상호명 또는 품목 검색") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFEEEEEE),
                        unfocusedContainerColor = Color(0xFFEEEEEE),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Filter Chips
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFF2979FF) else Color(0xFFE0E0E0))
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else Color.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // History List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (groupedExpenses.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("지출 내역이 없습니다.", color = Color.Gray)
                        }
                    }
                } else {
                    groupedExpenses.forEach { (date, dailyExpenses) ->
                        item {
                            Text(
                                text = date,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    dailyExpenses.forEach { expense ->
                                        ExpandableExpenseItem(expense)
                                    }
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun ExpandableExpenseItem(expense: com.example.ai_budget_app.data.local.ExpenseEntity) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Box(modifier = Modifier.clickable { expanded = !expanded }) {
            RecentItem(
                store = expense.storeName,
                info = expense.category,
                price = "${NumberFormat.getNumberInstance(Locale.KOREA).format(expense.totalAmount)}원",
                isHigh = false // TODO: logic for warning
            )
        }
        
        if (expanded && expense.items.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .background(Color(0xFFF1F3F5), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                expense.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.itemName, fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
                        Text(
                            "${NumberFormat.getNumberInstance(Locale.KOREA).format(item.price)}원",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
