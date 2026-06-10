package com.example.ai_budget_app.ui.map

import android.location.Geocoder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.ai_budget_app.data.local.AppDatabase
import com.example.ai_budget_app.data.repository.ExpenseRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class MapMarkerData(
    val title: String,
    val position: LatLng,
    val snippet: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyLocationMapScreen() {
    val context = LocalContext.current
    val repository = remember { ExpenseRepository(AppDatabase.getDatabase(context).expenseDao()) }
    
    // Get all expenses as state
    val expenses by repository.allExpenses.collectAsState(initial = emptyList())
    
    // Default location (e.g., somewhere in Seoul)
    val defaultLocation = LatLng(37.5665, 126.9780)
    var markerList by remember { mutableStateOf<List<MapMarkerData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 11f)
    }

    LaunchedEffect(expenses) {
        if (expenses.isEmpty()) return@LaunchedEffect
        
        isLoading = true
        withContext(Dispatchers.IO) {
            val now = LocalDate.now()
            val currentMonthPrefix = String.format("%04d-%02d", now.year, now.monthValue)
            
            // Filter expenses by current month and get unique store names
            val uniqueStoreNames = expenses
                .filter { it.date.startsWith(currentMonthPrefix) }
                .map { it.storeName }
                .distinct()
                
            val geocoder = Geocoder(context)
            val newMarkers = mutableListOf<MapMarkerData>()
            
            for (storeName in uniqueStoreNames) {
                try {
                    val results = geocoder.getFromLocationName(storeName, 1)
                    if (!results.isNullOrEmpty()) {
                        val address = results[0]
                        val latLng = LatLng(address.latitude, address.longitude)
                        val snippet = address.getAddressLine(0) ?: "방문 기록"
                        newMarkers.add(MapMarkerData(storeName, latLng, snippet))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            withContext(Dispatchers.Main) {
                markerList = newMarkers
                isLoading = false
                
                // Adjust camera if we have markers
                if (newMarkers.isNotEmpty()) {
                    if (newMarkers.size == 1) {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(newMarkers[0].position, 15f))
                    } else {
                        val boundsBuilder = LatLngBounds.Builder()
                        newMarkers.forEach { boundsBuilder.include(it.position) }
                        val bounds = boundsBuilder.build()
                        // 100 pixels padding
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("이번 달 위치", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                markerList.forEach { markerData ->
                    Marker(
                        state = MarkerState(position = markerData.position),
                        title = markerData.title,
                        snippet = markerData.snippet
                    )
                }
            }
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                )
            }
        }
    }
}
