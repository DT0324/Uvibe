package com.example.uvibe.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uvibe.network.RecommendationContentUi
import com.example.uvibe.ui.components.*
import com.example.uvibe.ui.model.AwarenessChartUiModel
import com.example.uvibe.ui.model.MythInfoUiModel
import com.example.uvibe.ui.model.StaticUvData
import com.google.android.gms.location.LocationServices

@Composable
fun MainMenuScreen(
    viewModel: MainMenuViewModel = viewModel()
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // 使用 mutableStateOf 包裹权限检查，确保权限改变时触发重绘
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showPermissionPrompt by remember { mutableStateOf(!hasLocationPermission) }

    // 当 hasLocationPermission 变为 true 时，自动拉取位置并开启轮询
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            @SuppressLint("MissingPermission")
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    // 调用新方法：启动实时轮询更新
                    viewModel.startRealtimeUpdates(it.latitude, it.longitude)
                } ?: viewModel.loadAllData()
            }
        }
    }

    if (showPermissionPrompt) {
        LocationPermissionPrompt(
            onPermissionHandled = {
                // 更新权限状态，这会自动触发上面的 LaunchedEffect
                hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                showPermissionPrompt = false

                if (!hasLocationPermission) {
                    viewModel.loadAllData() // 用户拒绝权限，加载默认数据
                }
            }
        )
    } else {
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val recommendationState by viewModel.recommendationState.collectAsState()
        val awarenessState by viewModel.awarenessState.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F6FA))
                .padding(16.dp),
        ) {
            // --- 顶部导航栏 (保持不变) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE9ECEF)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // UV Tracker Tab
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
                    Text("UV Tracker", color = if (selectedTabIndex == 0) Color.White else Color(0xFF6B7280), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                // Awareness Tab
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
                    Text("Awareness & Education", color = if (selectedTabIndex == 1) Color.White else Color(0xFF6B7280), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 底部内容区域 ---
            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedTabIndex == 0) {
                    UVTrackerPageBackground(
                        recommendationState = recommendationState,
                        // 修改点：调用带有当前位置刷新的逻辑
                        onRetry = { viewModel.refreshCurrentLocationData() },
                    )
                } else {
                    AwarenessPageBackground(
                        awarenessState = awarenessState,
                        myths = StaticUvData.myths,
                        onRetry = { viewModel.loadAllData() },
                    )
                }
            }
        }
    }
}

// LocationPermissionPrompt 保持不变
@Composable
fun LocationPermissionPrompt(onPermissionHandled: () -> Unit) {
    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { onPermissionHandled() }
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F6FA)).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFFE9ECEF)),
            contentAlignment = Alignment.Center
        ) { Text("📍", fontSize = 40.sp) }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Location Access", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Uvibe needs your location to provide real-time, accurate UV index and personalized sun protection advice for your area.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Enable Location", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onPermissionHandled) { Text("Skip for now", color = Color(0xFF6B7280)) }
    }
}

@Composable
private fun UVTrackerPageBackground(
    recommendationState: PageSectionState<RecommendationContentUi>,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (recommendationState) {
            PageSectionState.Loading -> LoadingStateView()
            is PageSectionState.Error -> ErrorStateView(message = recommendationState.message, onRetry = onRetry)
            is PageSectionState.Success -> {
                UVStatusCard(status = recommendationState.content.status, onRefresh = onRetry)
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
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (awarenessState) {
            PageSectionState.Loading -> LoadingStateView(message = "Loading awareness information...")
            is PageSectionState.Error -> ErrorStateView(message = awarenessState.message, onRetry = onRetry)
            is PageSectionState.Success -> {
                awarenessState.content.forEach { chart -> AwarenessChartCard(chart = chart) }
            }
        }
        myths.firstOrNull()?.let { myth -> MythInfoCard(mythInfo = myth) }
        Spacer(modifier = Modifier.height(24.dp))
    }
}