package com.example.ai_budget_app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ai_budget_app.ui.history.HistoryScreen
import com.example.ai_budget_app.ui.home.MainHomeScreen
import com.example.ai_budget_app.ui.map.ConsumptionMapScreen
import com.example.ai_budget_app.ui.receipt.CameraPreviewScreen
import com.example.ai_budget_app.ui.receipt.ManualEntryScreen
import com.example.ai_budget_app.ui.receipt.ReceiptCaptureScreen
import com.example.ai_budget_app.ui.receipt.ReceiptResultScreen
import com.example.ai_budget_app.ui.statistics.StatisticsScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Home : Screen("home", "홈", Icons.Default.Home)
    object Capture : Screen("capture", "촬영", Icons.Default.CameraAlt)
    object History : Screen("history", "내역", Icons.Default.Receipt)
    object Statistics : Screen("statistics", "통계", Icons.Default.BarChart)
    
    // Hidden from bottom bar
    object ManualEntry : Screen("manual_entry", "수동입력", null)
    object CameraPreview : Screen("camera_preview", "카메라프리뷰", null)
    object ReceiptResult : Screen("receipt_result", "영수증결과", null)
    object Map : Screen("map/{storeName}", "지도", null) {
        fun createRoute(storeName: String) = "map/$storeName"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Capture,
    Screen.History,
    Screen.Statistics
)

@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            // Only show bottom bar on main destinations
            if (bottomNavItems.any { it.route == currentDestination?.route }) {
                NavigationBar(
                    containerColor = Color.White
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF2979FF),
                                selectedTextColor = Color(0xFF2979FF),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFFE3F2FD)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                MainHomeScreen(
                    onFabClick = { navController.navigate(Screen.ManualEntry.route) }
                )
            }
            composable(Screen.Capture.route) {
                ReceiptCaptureScreen(
                    onBackClick = { navController.navigateUp() },
                    onCameraClick = { navController.navigate(Screen.CameraPreview.route) }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(Screen.Statistics.route) {
                StatisticsScreen()
            }
            
            // Other screens
            composable(Screen.ManualEntry.route) {
                ManualEntryScreen(
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable(Screen.CameraPreview.route) {
                // In a real app we'd pass image data via shared ViewModel or savedStateHandle
                CameraPreviewScreen(
                    onBackClick = { navController.navigateUp() },
                    onImageCaptured = { bytes ->
                        // Navigate to result screen. Since we can't easily pass large ByteArray in Navigation route,
                        // we'd typically save it to a repository or ViewModel.
                        // For UI demonstration, we'll just navigate.
                        navController.navigate(Screen.ReceiptResult.route)
                    }
                )
            }
            composable(Screen.ReceiptResult.route) {
                // Mock bytes for UI display
                val mockBytes = ByteArray(0) 
                ReceiptResultScreen(
                    imageBytes = mockBytes,
                    onBackClick = { navController.navigateUp() },
                    onShowMapClick = { storeName ->
                        navController.navigate(Screen.Map.createRoute(storeName))
                    }
                )
            }
            composable(Screen.Map.route) { backStackEntry ->
                val storeName = backStackEntry.arguments?.getString("storeName") ?: "상호명"
                ConsumptionMapScreen(
                    storeName = storeName,
                    onBackClick = { navController.navigateUp() }
                )
            }
        }
    }
}
