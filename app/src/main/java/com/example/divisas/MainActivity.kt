package com.example.divisas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.divisas.navigation.AppNavigation
import com.example.divisas.ui.theme.DivisasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DivisasTheme {
                AppNavigation()
            }
        }
    }
}
