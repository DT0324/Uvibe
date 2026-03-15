package com.example.uvibe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uvibe.network.RecommendationContentUi
import com.example.uvibe.ui.components.AwarenessChartCard
import com.example.uvibe.ui.components.ClothingRecommendationCard
import com.example.uvibe.ui.components.ErrorStateView
import com.example.uvibe.ui.components.LoadingStateView
import com.example.uvibe.ui.components.MythInfoCard
import com.example.uvibe.ui.components.ProtectionTipCard
import com.example.uvibe.ui.components.UVStatusCard
import com.example.uvibe.ui.model.AwarenessChartUiModel
import com.example.uvibe.ui.model.MockUvData
import com.example.uvibe.ui.model.MythInfoUiModel

@Composable
fun MainMenuScreen(
    viewModel: MainMenuViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val recommendationState by viewModel.recommendationState.collectAsState()
    val awarenessState by viewModel.awarenessState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .padding(16.dp),
    ) {
        // --- 顶部导航栏 ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE9ECEF)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTabIndex == 0) Color(0xFF1C64F2) else Color.Transparent)
                    .clickable { selectedTabIndex = 0 },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "UV Tracker",
                    color = if (selectedTabIndex == 0) Color.White else Color(0xFF6B7280),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTabIndex == 1) Color(0xFF1C64F2) else Color.Transparent)
                    .clickable { selectedTabIndex = 1 },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Awareness & Education",
                    color = if (selectedTabIndex == 1) Color.White else Color(0xFF6B7280),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 底部内容区域 ---
        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedTabIndex == 0) {
                UVTrackerPageBackground(
                    recommendationState = recommendationState,
                    onRetry = { viewModel.loadAllData() }, // 直接调用 ViewModel 的刷新方法
                )
            } else {
                AwarenessPageBackground(
                    awarenessState = awarenessState,
                    myths = MockUvData.myths,
                    onRetry = { viewModel.loadAllData() }, // 直接调用 ViewModel 的刷新方法
                )
            }
        }
    }
}

@Composable
private fun UVTrackerPageBackground(
    recommendationState: PageSectionState<RecommendationContentUi>,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (recommendationState) {
            PageSectionState.Loading -> LoadingStateView()
            is PageSectionState.Error -> ErrorStateView(
                message = recommendationState.message,
                onRetry = onRetry,
            )
            is PageSectionState.Success -> {
                UVStatusCard(status = recommendationState.content.status)
                ProtectionTipCard(tip = recommendationState.content.protectionTip)
                ClothingRecommendationCard(recommendation = recommendationState.content.clothingRecommendation)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AwarenessPageBackground(
    awarenessState: PageSectionState<List<AwarenessChartUiModel>>,
    myths: List<MythInfoUiModel>,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (awarenessState) {
            PageSectionState.Loading -> LoadingStateView(message = "Loading awareness information...")
            is PageSectionState.Error -> ErrorStateView(
                message = awarenessState.message,
                onRetry = onRetry,
            )
            is PageSectionState.Success -> {
                awarenessState.content.forEach { chart ->
                    AwarenessChartCard(chart = chart)
                }
            }
        }


        myths.firstOrNull()?.let { myth ->
            MythInfoCard(mythInfo = myth)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}