package com.example.uvibe.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uvibe.util.SunscreenReminderScheduler
import com.example.uvibe.network.RecommendationContentUi
import com.example.uvibe.ui.components.*
import com.example.uvibe.ui.model.AwarenessChartUiModel
import com.example.uvibe.ui.model.MythInfoUiModel
import com.example.uvibe.ui.model.StaticUvData
import com.example.uvibe.ui.model.SunscreenReminderUiModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

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
        val sunscreenReminderState by viewModel.sunscreenReminderState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        val notificationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { }

        LaunchedEffect(sunscreenReminderState) {
            if (sunscreenReminderState.isEnabled) {
                SunscreenReminderScheduler.schedule(context, sunscreenReminderState)
            } else {
                SunscreenReminderScheduler.cancel(context)
            }
        }

        LaunchedEffect(sunscreenReminderState.isEnabled, sunscreenReminderState.nextReminderAtMillis) {
            val nextReminderAt = sunscreenReminderState.nextReminderAtMillis ?: return@LaunchedEffect
            if (!sunscreenReminderState.isEnabled) return@LaunchedEffect

            val delayMillis = (nextReminderAt - System.currentTimeMillis()).coerceAtLeast(0L)
            kotlinx.coroutines.delay(delayMillis)
            viewModel.refreshSunscreenReminder()
        }

        Scaffold(
            containerColor = Color(0xFFFDFDFD), // Broken white
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                        text = when {
                            selectedTabIndex == 0 -> "Daily UV Index"
                            selectedTabIndex == 1 -> "Sunscreen Reminder"
                            else -> "Skin Health Insight"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827),
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = when {
                            selectedTabIndex == 0 -> "Stay protected, stay healthy"
                            selectedTabIndex == 1 -> "Set one reminder for your next sunscreen reapplication"
                            else -> "Knowledge is your best shield"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Normal
                    )
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
                                sunscreenReminderState = sunscreenReminderState,
                                onOpenReminderPage = { selectedTabIndex = 1 },
                            )
                        } else if (targetIndex == 1) {
                            ReminderSettingsPage(
                                currentReminder = sunscreenReminderState,
                                onSave = { intervalMinutes ->
                                    viewModel.setSunscreenReminder(
                                        reminderIntervalMinutes = intervalMinutes,
                                    )
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val alarmManager = context.getSystemService(AlarmManager::class.java)
                                        if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                                            context.startActivity(
                                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                            )
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Reminder saved. Allow exact alarms in Settings for a more precise 10-minute alert."
                                                )
                                            }
                                        } else {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Reminder set successfully.")
                                            }
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Reminder set successfully.")
                                        }
                                    }
                                },
                                onTurnOffReminder = { viewModel.clearSunscreenReminder() },
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
            .height(80.dp),
        color = Color.Black
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
                label = "Reminder",
                isSelected = selectedIndex == 1,
                onClick = { onItemSelected(1) },
                modifier = Modifier.weight(1f)
            )
            NavBarItem(
                icon = Icons.Default.Info,
                label = "Learn",
                isSelected = selectedIndex == 2,
                onClick = { onItemSelected(2) },
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
    val contentColor = Color.White
    val alpha = if (isSelected) 1f else 0.6f
    
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor.copy(alpha = alpha),
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            color = contentColor.copy(alpha = alpha)
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
    sunscreenReminderState: SunscreenReminderUiModel,
    onOpenReminderPage: () -> Unit,
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
                SunscreenReminderCard(
                    reminder = sunscreenReminderState,
                    onOpenReminderPage = onOpenReminderPage,
                )
                ProtectionTipCard(tip = recommendationState.content.protectionTip)
                ClothingRecommendationCard(recommendation = recommendationState.content.clothingRecommendation)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun ReminderSettingsPage(
    currentReminder: SunscreenReminderUiModel,
    onSave: (intervalMinutes: Int) -> Unit,
    onTurnOffReminder: () -> Unit,
) {
    val commonOptions = remember { listOf(30, 60, 120, 180) }

    var selectedInterval by remember(currentReminder) {
        mutableIntStateOf(currentReminder.reminderIntervalMinutes)
    }
    var customIntervalInput by remember(currentReminder) {
        mutableStateOf(
            if (currentReminder.reminderIntervalMinutes in commonOptions) "" else currentReminder.reminderIntervalMinutes.toString()
        )
    }
    val parsedCustomInterval = customIntervalInput.toIntOrNull()
    val isCustomValid = customIntervalInput.isBlank() || (parsedCustomInterval != null && parsedCustomInterval > 10)
    val saveEnabled = selectedInterval > 10 && isCustomValid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        CardSection(
            title = "Next Reapplication",
            description = "Choose a common duration or enter any whole number of minutes over 10."
        ) {
            ReminderOptionGroup(
                title = "Common options",
                selectedValue = if (selectedInterval in commonOptions) selectedInterval else null,
                options = commonOptions,
                suffix = { option ->
                    when (option) {
                        30 -> "30 min"
                        60 -> "1 h"
                        120 -> "2 h"
                        180 -> "3 h"
                        else -> "$option min"
                    }
                },
                onSelected = {
                    selectedInterval = it
                    customIntervalInput = ""
                }
            )

            OutlinedTextField(
                value = customIntervalInput,
                onValueChange = { input ->
                    val digitsOnly = input.filter { it.isDigit() }
                    customIntervalInput = digitsOnly
                    digitsOnly.toIntOrNull()?.let { value ->
                        if (value > 10) {
                            selectedInterval = value
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Custom duration in minutes") },
                placeholder = { Text("Example: 75") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    Text(
                        text = if (isCustomValid) "Reminder accepts any integer above 10 minutes."
                        else "Enter an integer greater than 10."
                    )
                },
                isError = !isCustomValid,
                shape = RoundedCornerShape(18.dp)
            )
        }

        CardSection(
            title = "Notification",
            description = "You will always receive a reminder 10 minutes before the next reapplication time."
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF8FAFF),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Text(
                    text = "Fixed pre-alert: 10 minutes before expiry",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }
        }

        Button(
            onClick = { onSave(selectedInterval) },
            enabled = saveEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
        ) {
            Text(
                text = if (currentReminder.isEnabled) "Update reminder" else "Start reminder",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }

        if (currentReminder.isEnabled) {
            OutlinedButton(
                onClick = onTurnOffReminder,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFFF3D0D0))
            ) {
                Text("Cancel reminder", color = Color(0xFFB91C1C), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderOptionGroup(
    title: String,
    selectedValue: Int?,
    options: List<Int>,
    suffix: (Int) -> String,
    onSelected: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1F2937)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selectedValue == option,
                    onClick = { onSelected(option) },
                    label = {
                        Text(
                            text = suffix(option),
                            fontWeight = if (selectedValue == option) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE0E7FF),
                        selectedLabelColor = Color(0xFF4338CA)
                    )
                )
            }
        }
    }
}

@Composable
private fun CardSection(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
                content()
            }
        )
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
