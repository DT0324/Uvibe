package com.example.uvibe.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showPermissionPrompt by remember { mutableStateOf(!hasLocationPermission) }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            @SuppressLint("MissingPermission")
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    viewModel.startRealtimeUpdates(it.latitude, it.longitude)
                } ?: viewModel.loadAllData()
            }
        }
    }

    if (showPermissionPrompt) {
        LocationPermissionPrompt(
            onPermissionHandled = {
                hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                showPermissionPrompt = false

                if (!hasLocationPermission) {
                    viewModel.loadAllData()
                }
            }
        )
    } else {
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val recommendationState by viewModel.recommendationState.collectAsState()
        val awarenessState by viewModel.awarenessState.collectAsState()

        Scaffold(
            containerColor = Color(0xFFFDFDFD), // Broken white
            bottomBar = {
                ElegantBottomBar(
                    selectedIndex = selectedTabIndex,
                    onItemSelected = { selectedTabIndex = it }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = if (selectedTabIndex == 0) "Daily UV Index" else "Skin Health Insight",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827),
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = if (selectedTabIndex == 0) "Stay protected, stay healthy" else "Knowledge is your best shield",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Normal
                    )
                }

                // Modern Tab Switcher
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 22.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF3F4F6)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TabItem(
                            title = "UV TRACKER",
                            isSelected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        TabItem(
                            title = "AWARENESS",
                            isSelected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "tabContent"
                ) { targetIndex ->
                    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
                        if (targetIndex == 0) {
                            UVTrackerPageBackground(
                                recommendationState = recommendationState,
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
    }
}

@Composable
private fun TabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color.White else Color.Transparent,
        shadowElevation = if (isSelected) 8.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
private fun ElegantBottomBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp)
            .height(72.dp)
            .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
        color = Color.White,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Default.WbSunny,
                label = "Tracker",
                isSelected = selectedIndex == 0,
                onClick = { onItemSelected(0) },
                modifier = Modifier.weight(1f)
            )
            NavBarItem(
                icon = Icons.Default.Info,
                label = "Learn",
                isSelected = selectedIndex == 1,
                onClick = { onItemSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) Color(0xFF4F46E5) else Color(0xFF9CA3AF)
    
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) Color(0xFFEEF2FF) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
fun LocationPermissionPrompt(onPermissionHandled: () -> Unit) {
    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { onPermissionHandled() }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .shadow(32.dp, CircleShape, spotColor = Color(0xFF4F46E5).copy(alpha = 0.2f))
                .background(Brush.linearGradient(listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF))), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("📍", fontSize = 80.sp)
        }

        Spacer(modifier = Modifier.height(56.dp))
        
        Text(
            "Precise UV Index",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            letterSpacing = (-1).sp
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            "To give you the most accurate UV protection advice, we need to know where you are. Your location data is used only for real-time UV updates.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color(0xFF4B5563),
            lineHeight = 28.sp,
            fontWeight = FontWeight.Normal
        )
        
        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = { 
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION, 
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF4F46E5).copy(alpha = 0.4f)),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
        ) {
            Text(
                "Enable Location Access", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        TextButton(
            onClick = onPermissionHandled,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(
                "Continue without location",
                color = Color(0xFF9CA3AF),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
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
        verticalArrangement = Arrangement.spacedBy(18.dp), // Reduced from 20dp
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        when (recommendationState) {
            PageSectionState.Loading -> LoadingStateView()
            is PageSectionState.Error -> ErrorStateView(message = recommendationState.message, onRetry = onRetry)
            is PageSectionState.Success -> {
                UVStatusCard(
                    status = recommendationState.content.status,
                    onRefresh = onRetry,
                    onLocationClick = onRetry
                )
                ProtectionTipCard(tip = recommendationState.content.protectionTip)
                ClothingRecommendationCard(recommendation = recommendationState.content.clothingRecommendation)
                CurrentLocationButton(onClick = onRetry) // 👈 Added with spacing provided by Column
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
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
        verticalArrangement = Arrangement.spacedBy(18.dp), // Reduced from 20dp
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        when (awarenessState) {
            PageSectionState.Loading -> LoadingStateView(message = "Fetching awareness insights...")
            is PageSectionState.Error -> ErrorStateView(message = awarenessState.message, onRetry = onRetry)
            is PageSectionState.Success -> {
                awarenessState.content.forEach { chart -> 
                    AwarenessChartCard(chart = chart) 
                }
            }
        }
        myths.firstOrNull()?.let { myth -> 
            MythInfoCard(mythInfo = myth) 
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}
