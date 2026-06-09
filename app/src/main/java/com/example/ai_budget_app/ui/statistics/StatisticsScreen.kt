package com.example.ai_budget_app.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai_budget_app.ui.home.CustomPieChart
import com.example.ai_budget_app.ui.home.PieChartData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen() {
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
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "이전 달")
                    }
                    Text("2026년 5월", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    IconButton(onClick = { /*TODO*/ }) {
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
                            Text("182,500원", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("전월 대비 12% 감소 ↓", color = Color(0xFF1976D2), fontSize = 10.sp)
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
                            Text("10,138원", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                                    data = listOf(
                                        PieChartData("식료품", 60f, Color(0xFF4285F4)),
                                        PieChartData("쇼핑", 20f, Color(0xFFAB47BC)),
                                        PieChartData("카페/간식", 15f, Color(0xFF0F9D58)),
                                        PieChartData("교통", 5f, Color(0xFFF4B400))
                                    )
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                LegendItem("식료품", "60%", Color(0xFF4285F4))
                                LegendItem("쇼핑", "20%", Color(0xFFAB47BC))
                                LegendItem("카페/간식", "15%", Color(0xFF0F9D58))
                                LegendItem("교통", "5%", Color(0xFFF4B400))
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
                                data = listOf(
                                    BarChartData("14", 5000f),
                                    BarChartData("15", 12000f),
                                    BarChartData("16", 2700f),
                                    BarChartData("17", 31500f),
                                    BarChartData("18", 18600f)
                                )
                            )
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
                        Text(
                            "지난 달보다 식료품 지출이 줄었지만, 카페/간식 지출이 조금씩 늘고 있어요. 커피값을 줄이면 더 많은 저축이 가능할 것 같아요!",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun LegendItem(title: String, percentage: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val barWidth = 30.dp.toPx()
        val spacing = (size.width - (barWidth * data.size)) / (data.size + 1)
        
        data.forEachIndexed { index, barData ->
            val barHeight = (barData.value / maxValue) * (size.height - 30.dp.toPx()) // Leave space for label
            val x = spacing + index * (barWidth + spacing)
            val y = size.height - 30.dp.toPx() - barHeight
            
            drawRoundRect(
                color = Color(0xFF2979FF),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            
            // X-Axis labels could be drawn using TextMeasurer in modern compose, 
            // but for simplicity in this canvas we skip native text drawing and let it be implied or 
            // you can add Compose text overlays
        }
    }
}
