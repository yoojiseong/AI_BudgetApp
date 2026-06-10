package com.example.ai_budget_app.ui.map

import android.location.Geocoder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionMapScreen(
    storeName: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Default location (e.g., somewhere in Seoul)
    var location by remember { mutableStateOf(LatLng(37.5665, 126.9780)) }
    var snippetText by remember { mutableStateOf("위치를 검색하는 중입니다...") }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 15f)
    }

    LaunchedEffect(storeName) {
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context)
                // Search for the store name. Usually appending 'Seoul' or 'Korea' helps, but we just use storeName.
                val results = geocoder.getFromLocationName(storeName, 1)
                
                if (!results.isNullOrEmpty()) {
                    val address = results[0]
                    val newLatLng = LatLng(address.latitude, address.longitude)
                    withContext(Dispatchers.Main) {
                        location = newLatLng
                        snippetText = address.getAddressLine(0) ?: "최근 지출 내역을 확인해보세요."
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(newLatLng, 15f))
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        snippetText = "위치를 찾을 수 없습니다. 기본 위치를 표시합니다."
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    snippetText = "위치 검색 중 오류가 발생했습니다."
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("소비 지도", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = location),
                    title = storeName,
                    snippet = snippetText
                )
            }
        }
    }
}
