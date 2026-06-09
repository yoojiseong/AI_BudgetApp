package com.example.ai_budget_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ai_budget_app.ui.MainAppScaffold
import com.example.ai_budget_app.ui.theme.AI_Budget_APPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AI_Budget_APPTheme {
                MainAppScaffold()
            }
        }
    }
}