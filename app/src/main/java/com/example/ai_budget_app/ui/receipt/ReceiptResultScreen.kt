package com.example.ai_budget_app.ui.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai_budget_app.data.GeminiRepository
import com.example.ai_budget_app.data.PriceComparison
import com.example.ai_budget_app.data.PublicDataRepository
import com.example.ai_budget_app.data.ReceiptData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptResultScreen(
    imageBytes: ByteArray,
    onBackClick: () -> Unit,
    onShowMapClick: (String) -> Unit
) {
    val geminiRepository = remember { GeminiRepository() }
    val publicDataRepository = remember { PublicDataRepository() }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var receiptData by remember { mutableStateOf<ReceiptData?>(null) }
    var comparisons by remember { mutableStateOf<List<PriceComparison>>(emptyList()) }

    LaunchedEffect(imageBytes) {
        isLoading = true
        val parsedData = geminiRepository.parseReceiptImage(imageBytes)
        receiptData = parsedData
        if (parsedData != null) {
            comparisons = publicDataRepository.getPriceComparisons(parsedData.items)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("스마트 가계부", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            if (receiptData != null && !isLoading) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            val repository = com.example.ai_budget_app.data.repository.ExpenseRepository(
                                com.example.ai_budget_app.data.local.AppDatabase.getDatabase(context).expenseDao()
                            )
                            val expense = com.example.ai_budget_app.data.local.ExpenseEntity(
                                storeName = receiptData!!.storeName,
                                date = receiptData!!.date,
                                category = receiptData!!.category,
                                paymentMethod = receiptData!!.paymentMethod,
                                items = receiptData!!.items,
                                totalAmount = receiptData!!.items.sumOf { it.price }
                            )
                            repository.insertExpense(expense)
                            // 저장 후 뒤로 가기 (또는 내역으로 이동)
                            onBackClick()
                        }
                    },
                    containerColor = Color(0xFF1976D2),
                    contentColor = Color.White
                ) {
                    Text("저장", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF1976D2))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI가 영수증을 분석하고 있습니다...", color = Color.Gray)
                }
            }
        } else if (receiptData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("영수증 분석에 실패했습니다.", color = Color.Red)
            }
        } else {
            val totalSpent = receiptData!!.items.sumOf { it.price }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Header (Total Spent)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("이번 분석된 지출", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${String.format("%,d", totalSpent)}원",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${receiptData!!.items.size}건의 지출", color = Color.White.copy(alpha = 0.8f))
                                Spacer(modifier = Modifier.weight(1f))
                                Text(receiptData!!.date, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                }

                // AI Price Comparison Section
                item {
                    val expensiveItems = comparisons.filter { (it.difference ?: 0) > 0 }
                    if (expensiveItems.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFF57C00))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("시세보다 비싸게 구매", fontWeight = FontWeight.Bold, color = Color(0xFFF57C00))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                expensiveItems.forEach { comp ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("${comp.item.itemName} (${receiptData!!.storeName})", fontWeight = FontWeight.Medium)
                                            Text("평균보다 ${String.format("%.0f", comp.percentage)}% 높음", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Text("+${String.format("%,d", comp.difference)}원", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                    }
                                }
                            }
                        }
                    }
                }

                // Parsed Items Section
                item {
                    Text("상세 내역", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(receiptData!!.storeName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("${receiptData!!.paymentMethod} · ${receiptData!!.date}", fontSize = 12.sp, color = Color.Gray)
                                }
                                IconButton(onClick = { onShowMapClick(receiptData!!.storeName) }) {
                                    Icon(Icons.Default.Map, contentDescription = "지도 보기", tint = Color(0xFF1976D2))
                                }
                            }
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            receiptData!!.items.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.itemName, color = Color.DarkGray)
                                    Text("${String.format("%,d", item.price)}원", fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
