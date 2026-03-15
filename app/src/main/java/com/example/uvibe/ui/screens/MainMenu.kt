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
import com.example.uvibe.ui.model.MythInfoUiModel
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.example.uvibe.ui.model.StaticUvData
import com.google.android.gms.location.LocationServices


@Composable
fun MainMenuScreen(
    viewModel: MainMenuViewModel = viewModel()
) {
    val context = LocalContext.current

    // 1. 初始化 FusedLocationClient (使用 remember 确保屏幕重绘时对象不丢失)
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // 2. 检查应用启动时是否已经拥有定位权限
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // 3. 状态开关：如果没有权限，显示请求页；如果有权限，直接进主菜单
    var showPermissionPrompt by remember { mutableStateOf(!hasLocationPermission) }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    viewModel.loadDataWithLocation(it.latitude, it.longitude)
                } ?: viewModel.loadAllData() // 无缓存位置则用默认
            }
        }
    }

    // 4. 核心路由逻辑
    if (showPermissionPrompt) {
        // === 显示挡在前面的位置请求窗口 ===
        LocationPermissionPrompt(
            onPermissionHandled = {
                // 弹窗结束（无论同意/拒绝），立刻关闭提示页
                showPermissionPrompt = false

                // 再次检查权限，因为用户刚刚在弹窗里可能点击了“允许”
                val isGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (isGranted) {
                    // 🎉 用户同意了权限，抓取底层的坐标！
                    @SuppressLint("MissingPermission")
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            // 完美拿到经纬度，传给 ViewModel 去请求 OpenWeatherMap 和 AWS
                            viewModel.loadDataWithLocation(location.latitude, location.longitude)
                        } else {
                            // 极端情况：手机刚开机没有缓存位置数据，回退到默认数据
                            viewModel.loadAllData()
                        }
                    }
                } else {
                    // 💔 用户残忍拒绝，直接加载默认数据
                    viewModel.loadAllData()
                }
            }
        )
    } else {
        // === 正常的主菜单 UI 代码 ===
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
                        onRetry = { viewModel.loadAllData() },
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

@Composable
fun LocationPermissionPrompt(onPermissionHandled: () -> Unit) {
    // 注册系统权限请求弹窗
    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            // 当系统权限弹窗关闭时（无论同意还是拒绝），触发回调进入主菜单
            onPermissionHandled()
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 这里后续可以放一个精美的地球或者定位图标
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE9ECEF)),
            contentAlignment = Alignment.Center
        ) {
            Text("📍", fontSize = 40.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Location Access",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Uvibe needs your location to provide real-time, accurate UV index and personalized sun protection advice for your area.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 授权按钮
        Button(
            onClick = {
                // 唤起 Android 系统的授权弹窗
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Enable Location", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 也可以选择暂时跳过
        TextButton(onClick = onPermissionHandled) {
            Text("Skip for now", color = Color(0xFF6B7280))
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
                UVStatusCard(status = recommendationState.content.status,onRefresh = onRetry)
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