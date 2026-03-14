package com.example.uvibe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.uvibe.ui.screens.ComponentPlaygroundScreen
import com.example.uvibe.ui.theme.UvibeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UvibeTheme {
                ComponentPlaygroundScreen()
            }
        }
    }
}
