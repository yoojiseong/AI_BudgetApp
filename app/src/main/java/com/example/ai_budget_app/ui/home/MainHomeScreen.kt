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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(
    onFabClick: () -> Unit
) {
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
                        Text("30,800원", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("3건의 지출", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            Text("5월", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        }
                    }
                }
            }

            // Warning Box
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
                            Text("시세보다 비싸게 구매", fontWeight = FontWeight.Bold, color = Color(0xFFF57C00))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        WarningItem("사과 (이마트)", "평균보다 19% 높음", "+1,400원")
                        WarningItem("우유 (이마트)", "평균보다 14% 높음", "+400원")
                        WarningItem("계란 (이마트)", "평균보다 8% 높음", "+500원")
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
                                data = listOf(
                                    PieChartData("식료품", 60f, Color(0xFF4285F4)),
                                    PieChartData("카페/간식", 31f, Color(0xFF0F9D58)),
                                    PieChartData("편의점", 9f, Color(0xFFF4B400))
                                )
                            )
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
                            Text("전체보기", color = Color(0xFF2979FF), fontSize = 12.sp, modifier = Modifier.clickable {  })
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        RecentItem("이마트", "식료품 · 2026. 5. 18.", "18,600원", true)
                        RecentItem("스타벅스", "카페/간식 · 2026. 5. 17.", "9,500원", false)
                        RecentItem("GS25", "편의점 · 2026. 5. 16.", "2,700원", false)
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
    var startAngle = -90f

    Canvas(modifier = Modifier.size(150.dp)) {
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
        
        // Inner circle to make it look like a donut if desired, 
        // but PDF shows a full pie chart. We keep it as pie chart.
    }
}
