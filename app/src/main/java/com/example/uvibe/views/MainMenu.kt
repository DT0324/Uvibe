package com.example.uvibe

import android.os.Bundle
import androidx.activity.ComponentActivity // 注意：Compose 通常使用 ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier

// 导入我们在 views 包下写好的界面
import com.example.uvibe.views.MainMenuScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 开启边缘到边缘显示 (沉浸式状态栏)
        enableEdgeToEdge()

        // 舍弃原有的 setContentView(R.layout.activity_main)
        setContent {
            // 使用 Box 和 systemBarsPadding 来替代原来繁琐的 WindowInsetsCompat 代码
            // 它的作用是自动留出状态栏和底部导航条的安全距离，防止 UI 被遮挡
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // 直接挂载主界面
                MainMenuScreen()
            }
        }
    }
}