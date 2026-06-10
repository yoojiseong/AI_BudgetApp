package com.example.ai_budget_app.ui.receipt

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai_budget_app.utils.ImageCompressor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptCaptureScreen(
    onBackClick: () -> Unit,
    onCameraClick: () -> Unit,
    onImageReady: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var compressedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val bytes = ImageCompressor.compressImage(context, uri)
                compressedImageBytes = bytes
                if (bytes != null) {
                    Toast.makeText(context, "이미지가 선택되었고 압축되었습니다 (${bytes.size / 1024}KB)", Toast.LENGTH_SHORT).show()
                    com.example.ai_budget_app.ui.ImageHolder.imageBytes = bytes
                    onImageReady()
                } else {
                    Toast.makeText(context, "이미지 압축 실패", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // User canceled
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("스마트 가계부", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* Settings action */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "설정")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                }
                Text(
                    text = "영수증 촬영",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Guide Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("촬영 가이드", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• 영수증 전체가 화면에 들어오도록 촬영하세요\n• 밝은 곳에서 촬영하면 인식률이 높아집니다\n• 토스 결제 내역 캡처도 가능합니다", fontSize = 14.sp, color = Color.DarkGray)
                }
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SelectionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CameraAlt,
                    iconTint = Color(0xFF2196F3),
                    title = "카메라 촬영",
                    subtitle = "실시간 촬영",
                    onClick = onCameraClick
                )

                SelectionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Image,
                    iconTint = Color(0xFF4CAF50),
                    title = "갤러리 선택",
                    subtitle = "저장된 이미지",
                    onClick = {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("지원 가능한 형식", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FormatItem("종이\n영수증")
                FormatItem("토스\n결제내역")
                FormatItem("카드사\n알림")
            }
        }
    }
}

@Composable
fun SelectionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun FormatItem(text: String) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 12.sp, color = Color.DarkGray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
