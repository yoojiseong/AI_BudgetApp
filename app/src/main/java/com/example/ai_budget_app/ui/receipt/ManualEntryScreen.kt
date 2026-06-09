package com.example.ai_budget_app.ui.receipt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    onBackClick: () -> Unit
) {
    var storeName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-05-18") }
    
    // For demo purposes, simplified category and payment state
    var category by remember { mutableStateOf("식료품") }
    var paymentMethod by remember { mutableStateOf("신용카드") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("수동 입력", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(onClick = { onBackClick() }) {
                        Text("저장", color = Color(0xFF2979FF), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text("기본 정보", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text("상호명") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("결제 일시") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            trailingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = "달력")
                            },
                            readOnly = true
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("카테고리") },
                                modifier = Modifier.weight(1f).padding(end = 4.dp),
                                readOnly = true
                            )
                            OutlinedTextField(
                                value = paymentMethod,
                                onValueChange = { paymentMethod = it },
                                label = { Text("결제 수단") },
                                modifier = Modifier.weight(1f).padding(start = 4.dp),
                                readOnly = true
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("구매 품목", fontWeight = FontWeight.Bold)
                    TextButton(onClick = { /* TODO Add item */ }, contentPadding = PaddingValues(0.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "추가", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("항목 추가")
                    }
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Dummy items
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            OutlinedTextField(
                                value = "사과",
                                onValueChange = {},
                                label = { Text("품목명") },
                                modifier = Modifier.weight(1.5f).padding(end = 8.dp)
                            )
                            OutlinedTextField(
                                value = "8,500",
                                onValueChange = {},
                                label = { Text("금액") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            OutlinedTextField(
                                value = "우유",
                                onValueChange = {},
                                label = { Text("품목명") },
                                modifier = Modifier.weight(1.5f).padding(end = 8.dp)
                            )
                            OutlinedTextField(
                                value = "3,200",
                                onValueChange = {},
                                label = { Text("금액") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("총 금액", fontWeight = FontWeight.Bold)
                            Text("18,600원", fontWeight = FontWeight.Bold, color = Color(0xFF2979FF), fontSize = 18.sp)
                        }
                    }
                }
            }
            
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Tip. 영수증을 촬영하면 자동으로 입력됩니다!",
                        color = Color(0xFF1976D2),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
