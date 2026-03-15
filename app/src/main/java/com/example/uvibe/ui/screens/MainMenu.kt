package com.example.uvibe.ui.screens // 注意检查这里的包名是否与你的项目一致

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.uvibe.ui.components.*
import com.example.uvibe.ui.model.MockUvData

@Composable
fun MainMenuScreen() {
    // 记录当前选中的页面索引：0 为 UV Tracker, 1 为 Awareness & Education
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // 主背景
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .padding(16.dp)
    ) {
        // --- 顶部导航栏 (Segmented Control) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE9ECEF)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧按钮：UV Tracker
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTabIndex == 0) Color(0xFF1C64F2) else Color.Transparent)
                    .clickable { selectedTabIndex = 0 },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "UV Tracker",
                    color = if (selectedTabIndex == 0) Color.White else Color(0xFF6B7280),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 右侧按钮：Awareness & Education
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTabIndex == 1) Color(0xFF1C64F2) else Color.Transparent)
                    .clickable { selectedTabIndex = 1 },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Awareness & Education",
                    color = if (selectedTabIndex == 1) Color.White else Color(0xFF6B7280),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 下方页面内容容器 ---
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (selectedTabIndex == 0) {
                UVTrackerPageBackground()
            } else {
                AwarenessPageBackground()
            }
        }
    }
}

@Composable
fun UVTrackerPageBackground() {
    // 1. verticalScroll: 允许内容超出屏幕时上下滑动
    // 2. Arrangement.spacedBy(16.dp): 自动在每个卡片之间留出 16dp 的空隙，无需手动写 Spacer
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        UVStatusCard(status = MockUvData.currentUvStatus)

        ProtectionTipCard(tip = MockUvData.protectionTip)

        ClothingRecommendationCard(recommendation = MockUvData.clothingRecommendation)


        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AwarenessPageBackground() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        AwarenessChartCard(chart = MockUvData.awarenessCharts.first())

        MythInfoCard(mythInfo = MockUvData.myths.first())


        Spacer(modifier = Modifier.height(24.dp))
    }
}