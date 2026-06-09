package com.example.ai_budget_app.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai_budget_app.data.local.AppDatabase
import com.example.ai_budget_app.data.repository.ExpenseRepository
import com.example.ai_budget_app.ui.history.HistoryViewModel
import com.example.ai_budget_app.ui.history.HistoryViewModelFactory
import com.example.ai_budget_app.ui.home.CustomPieChart
import com.example.ai_budget_app.ui.home.PieChartData
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onCategoryClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { ExpenseRepository(AppDatabase.getDatabase(context).expenseDao()) }
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(repository))
    
    val allExpenses by viewModel.expenses.collectAsState()
    val insightFeedback by viewModel.insightFeedback.collectAsState()
    
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    
    LaunchedEffect(currentMonth) {
        viewModel.resetInsightFeedback()
    }
    
    val expenses = allExpenses.filter {
        it.date.startsWith(currentMonth.toString())
    }
    
    val totalAmount = expenses.sumOf { it.totalAmount }
    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
    
    val uniqueDays = expenses.map { it.date }.distinct().size
    val dailyAvg = if (uniqueDays > 0) totalAmount / uniqueDays else 0
    
    // Calculate category percentages
    val categoryColors = listOf(Color(0xFF4285F4), Color(0xFFAB47BC), Color(0xFF0F9D58), Color(0xFFF4B400), Color(0xFF9C27B0))
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

    // Daily Trend Bar Chart Data
    val dailyTotals = expenses.groupBy { it.date }
        .mapValues { entry -> entry.value.sumOf { it.totalAmount }.toFloat() }
        .toList()
        .sortedBy { it.first } // sort by date ascending

    // take last 5 days
    val recentDays = dailyTotals.takeLast(5)
    val barChartData = if (recentDays.isNotEmpty()) {
        recentDays.map { (date, amount) ->
            val dayPart = date.split("-").lastOrNull() ?: date
            BarChartData(dayPart, amount)
        }
    } else {
        listOf(BarChartData("없음", 0f))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("통계 / 분석", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Date Selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "이전 달")
                    }
                    Text("${currentMonth.year}년 ${currentMonth.monthValue}월", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "다음 달")
                    }
                }
            }

            // Summary Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("총 지출", color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${formatter.format(totalAmount)}원", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("전월 대비 -% 감소 ↓", color = Color(0xFF1976D2), fontSize = 10.sp)
                        }
                    }
                    
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("일평균 지출", color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${formatter.format(dailyAvg)}원", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("목표 예산 순항 중", color = Color(0xFF0F9D58), fontSize = 10.sp)
                        }
                    }
                }
            }

            // Category Pie Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("카테고리별 지출", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                CustomPieChart(
                                    data = pieChartDataList
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                pieChartDataList.forEach { pieData ->
                                    if (pieData.name != "데이터 없음") {
                                        LegendItem(
                                            title = pieData.name, 
                                            percentage = "${String.format("%.1f", pieData.value)}%", 
                                            color = pieData.color,
                                            onClick = { onCategoryClick(pieData.name) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Daily Trend Bar Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("일별 지출 추이", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                            CustomBarChart(
                                data = barChartData
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        // Draw labels roughly under the bars
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            barChartData.forEach { data ->
                                Text(data.label, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Insight Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = "인사이트", tint = Color(0xFF673AB7))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("이번 달 소비 인사이트", fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        when (insightFeedback) {
                            null -> {
                                Text(
                                    "AI에게 맞춤형 소비 진단을 받아보세요!",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.generateInsightFeedback(expenses) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("AI 소비 진단 받기")
                                }
                            }
                            "Loading..." -> {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color(0xFF673AB7))
                                }
                            }
                            else -> {
                                Text(
                                    insightFeedback ?: "",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { viewModel.generateInsightFeedback(expenses) },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("다시 진단받기", color = Color(0xFF673AB7))
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

@Composable
fun LegendItem(title: String, percentage: String, color: Color, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontSize = 12.sp, color = Color.DarkGray)
        }
        Text(percentage, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

data class BarChartData(val label: String, val value: Float)

@Composable
fun CustomBarChart(data: List<BarChartData>) {
    val maxValue = data.maxOfOrNull { it.value }?.takeIf { it > 0 } ?: 1f
    val barColors = listOf(Color(0xFF4285F4), Color(0xFFAB47BC), Color(0xFF0F9D58), Color(0xFFF4B400), Color(0xFF9C27B0), Color(0xFF00BCD4))
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val barWidth = 30.dp.toPx()
        val spacing = (size.width - (barWidth * data.size)) / (data.size + 1)
        
        data.forEachIndexed { index, barData ->
            val barHeight = (barData.value / maxValue) * size.height
            val x = spacing + index * (barWidth + spacing)
            val y = size.height - barHeight
            
            if (barHeight > 0) {
                drawRoundRect(
                    color = barColors[index % barColors.size],
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }
    }
}
