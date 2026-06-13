package com.example.ai_budget_app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai_budget_app.data.local.AppDatabase
import com.example.ai_budget_app.data.repository.ExpenseRepository
import com.example.ai_budget_app.ui.history.HistoryViewModel
import com.example.ai_budget_app.ui.history.HistoryViewModelFactory
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(
    onFabClick: () -> Unit,
    onViewAllClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { ExpenseRepository(AppDatabase.getDatabase(context).expenseDao()) }
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(repository))
    
    val allExpenses by viewModel.expenses.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.populateDummyDataIfNeeded()
    }
    
    val currentMonth = remember { YearMonth.now() }
    val expenses = allExpenses.filter { it.date.startsWith(currentMonth.toString()) }

    val totalAmount = expenses.sumOf { it.totalAmount }
    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
    
    // Calculate category percentages
    val categoryColors = listOf(Color(0xFF4285F4), Color(0xFF0F9D58), Color(0xFFF4B400), Color(0xFFDB4437), Color(0xFF9C27B0))
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.totalAmount } }
        .toList()
        .sortedByDescending { it.second }
    
    val pieChartDataList = if (totalAmount > 0) {
        categoryTotals.mapIndexed { index, pair ->
            val percentage = (pair.second.toFloat() / totalAmount) * 100f
            PieChartData(pair.first, percentage, categoryColors[index % categoryColors.size])
        }
    } else {
        listOf(PieChartData("데이터 없음", 100f, Color.LightGray))
    }

    val allItems = expenses.flatMap { exp -> exp.items.map { it to exp.storeName } }
    val itemAverages = allItems.groupBy { it.first.itemName }
        .mapValues { entry ->
            val sum = entry.value.sumOf { it.first.price }
            val count = entry.value.size
            if (count > 0) sum / count else 0
        }
    
    val comparisons by viewModel.publicDataComparisons.collectAsState()

    LaunchedEffect(expenses) {
        if (expenses.isNotEmpty()) {
            viewModel.fetchPublicDataComparisons(expenses)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("스마트 가계부", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "설정")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = Color(0xFF2979FF),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "수동 입력")
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Total Spending Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2979FF)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text("이번 달 총 지출", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${formatter.format(totalAmount)}원", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${expenses.size}건의 지출", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            Text("${currentMonth.monthValue}월", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        }
                    }
                }
            }

            // Warning Box (공공데이터 생필품 가격 비교 연동)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFF57C00))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("시세보다 비싸게 구매 (소비자원 연동)", fontWeight = FontWeight.Bold, color = Color(0xFFF57C00))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (comparisons == null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFFF57C00), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("공공데이터와 가격 비교 중...", fontSize = 12.sp, color = Color.Gray)
                            }
                        } else if (comparisons!!.isEmpty()) {
                            Text("시세(공공데이터 기준)보다 비싸게 구매한 생필품이 없습니다. 알뜰 소비 👍", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            comparisons!!.take(3).forEach { comp ->
                                WarningItem(comp.item.itemName, "평균 시세보다 높음", "+${formatter.format(comp.difference)}원")
                            }
                        }
                    }
                }
            }

            // Category Pie Chart
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("카테고리별 지출", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CustomPieChart(
                                data = pieChartDataList
                            )
                        }
                        
                        // Category Legend
                        Spacer(modifier = Modifier.height(16.dp))
                        pieChartDataList.forEach { pieData ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).background(pieData.color, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(pieData.name, fontSize = 14.sp)
                                }
                                Text("${String.format("%.1f", pieData.value)}%", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Recent List
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("최근 지출", fontWeight = FontWeight.Bold)
                            Text("전체보기", color = Color(0xFF2979FF), fontSize = 12.sp, modifier = Modifier.clickable { onViewAllClick() })
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val recentItems = expenses.take(5)
                        if (recentItems.isEmpty()) {
                            Text("지출 내역이 없습니다.", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                        } else {
                            recentItems.forEach { expense ->
                                RecentItem(
                                    store = expense.storeName, 
                                    info = "${expense.category} · ${expense.date}", 
                                    price = "${formatter.format(expense.totalAmount)}원", 
                                    isHigh = false // TODO logic
                                )
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) } // Bottom nav padding
        }
    }
}

@Composable
fun WarningItem(title: String, subtitle: String, priceDiff: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = Color(0xFFF57C00))
        }
        Text(priceDiff, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
    }
}

@Composable
fun RecentItem(store: String, info: String, price: String, isHigh: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(store, fontWeight = FontWeight.Medium)
            Text(info, fontSize = 12.sp, color = Color.Gray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(price, fontWeight = FontWeight.Bold)
            if (isHigh) {
                Text("📈 평균 ↑", fontSize = 10.sp, color = Color(0xFFF57C00))
            }
        }
    }
}

data class PieChartData(val name: String, val value: Float, val color: Color)

@Composable
fun CustomPieChart(data: List<PieChartData>) {
    val total = data.sumOf { it.value.toDouble() }.toFloat()

    Canvas(modifier = Modifier.size(150.dp)) {
        var startAngle = -90f
        if (total == 0f) {
            // Draw gray circle if no data
            drawArc(
                color = Color.LightGray,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            return@Canvas
        }

        data.forEach { pieData ->
            val sweepAngle = (pieData.value / total) * 360f
            drawArc(
                color = pieData.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            startAngle += sweepAngle
        }
    }
}
