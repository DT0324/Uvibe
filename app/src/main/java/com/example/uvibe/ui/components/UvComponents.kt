package com.example.uvibe.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uvibe.data.skinTypeData
import com.example.uvibe.ui.model.AwarenessChartUiModel
import com.example.uvibe.ui.model.ClothingItemUiModel
import com.example.uvibe.ui.model.ClothingRecommendationUiModel
import com.example.uvibe.ui.model.MythInfoUiModel
import com.example.uvibe.ui.model.PreviewUvData
import com.example.uvibe.ui.model.ProtectionTipUiModel
import com.example.uvibe.ui.model.SkinTypeModel
import com.example.uvibe.ui.model.StaticUvData
import com.example.uvibe.ui.model.SunscreenReminderUiModel
import com.example.uvibe.ui.model.UvForecastUiModel
import com.example.uvibe.ui.model.UvRiskLevel
import com.example.uvibe.ui.model.UvStatusUiModel
import com.example.uvibe.ui.theme.UvibeTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.5.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            content = content
        )
    }
}



@Composable
fun UVStatusCard(
    status: UvStatusUiModel,
    onRefresh: () -> Unit,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 渐变色逻辑保持不变，或者你也可以根据 status.riskLevel 来匹配颜色
    val gradientColors = when {
        status.uvIndex <= 2 -> listOf(Color(0xFF4CAF50), Color(0xFF81C784))
        status.uvIndex <= 5 -> listOf(Color(0xFFFFB300), Color(0xFFFFD54F))
        status.uvIndex <= 7 -> listOf(Color(0xFFFB8C00), Color(0xFFFFA726))
        status.uvIndex <= 10 -> listOf(Color(0xFFE53935), Color(0xFFEF5350))
        else -> listOf(Color(0xFF8E24AA), Color(0xFFAB47BC))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 主卡片部分保持不变 ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clickable { onRefresh() }
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = gradientColors[0].copy(alpha = 0.5f)
                ),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                // ... (保留你原来的 Canvas 背景圈和文本内容) ...
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Current UV",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = status.uvIndex.toString(),
                        fontSize = 110.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 110.sp,
                        letterSpacing = (-4).sp
                    )
                    Text(
                        text = status.levelText.uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        // --- 新增：使用 locationName 显示当前位置 ---
        if (status.locationName.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Current Location",
                    tint = Color(0xFF4F46E5),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = status.locationName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F2937)
                )
            }
        }

        // --- 新增：横向滑动的未来预测 ---
        if (status.forecast.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(status.forecast) { forecastItem ->
                    ForecastCard(item = forecastItem)
                }
            }
        }

        // 刷新位置按钮和更新时间
        CurrentLocationButton(onClick = onLocationClick)

        Text(
            text = status.updatedAt.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ForecastCard(item: UvForecastUiModel) {
    // 为不同级别的 UV 准备一个柔和的背景色
    val bgColor = when {
        item.uvIndex <= 2 -> Color(0xFFE8F5E9) // 浅绿
        item.uvIndex <= 5 -> Color(0xFFFFF8E1) // 浅黄
        item.uvIndex <= 7 -> Color(0xFFFFF3E0) // 浅橙
        item.uvIndex <= 10 -> Color(0xFFFFEBEE) // 浅红
        else -> Color(0xFFF3E5F5) // 浅紫
    }

    // 字体颜色稍微深一点，保证对比度
    val textColor = when {
        item.uvIndex <= 2 -> Color(0xFF2E7D32)
        item.uvIndex <= 5 -> Color(0xFFF57F17)
        item.uvIndex <= 7 -> Color(0xFFE65100)
        item.uvIndex <= 10 -> Color(0xFFC62828)
        else -> Color(0xFF6A1B9A)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        modifier = Modifier.width(76.dp) // 稍微加宽一点点以放下 levelText
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 时间 (例如: 2 PM)
            Text(
                text = item.time,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4B5563),
                fontWeight = FontWeight.Bold
            )
            // UV 指数数字 (例如: 6)
            Text(
                text = item.uvIndex.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                fontWeight = FontWeight.ExtraBold
            )
            // 风险等级文字 (例如: High) - 适配你的数据模型
            Text(
                text = item.levelText.label,
                style = MaterialTheme.typography.labelSmall,
                color = item.levelText.color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProtectionTipCard(
    tip: ProtectionTipUiModel,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        modifier = modifier,
        containerColor = Color(0xFFF0F4FF)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF312E81),
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF4B5563),
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-0.5).sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "💡", fontSize = 48.sp)
        }
    }
}

@Composable
fun ClothingRecommendationCard(
    recommendation: ClothingRecommendationUiModel,
    modifier: Modifier = Modifier,
) {
    SectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Recommended Clothing for You",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1F2937),
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Based on current UV Index ${recommendation.uvIndex}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Normal
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF8FAFC),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                recommendation.items.forEach { item ->
                    ClothingItemRow(item = item)
                    if (item != recommendation.items.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = Color.Black.copy(alpha = 0.05f),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClothingItemRow(
    item: ClothingItemUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = when {
                item.name.contains("hat", true) -> "👒"
                item.name.contains("sunglasses", true) -> "🕶️"
                item.name.contains("sunscreen", true) -> "🧴"
                item.name.contains("shirt", true) || item.name.contains("clothing", true) -> "👕"
                else -> "✨"
            },
            fontSize = 40.sp
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF111827)
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFD1D5DB),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun AwarenessChartCard(
    chart: AwarenessChartUiModel,
    modifier: Modifier = Modifier,
) {
    SectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = chart.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1F2937),
                letterSpacing = (-0.5).sp
            )
            Text(
                text = chart.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Normal
            )
        }

        if (chart.chartData.isNotEmpty()) {
            val maxRate = chart.chartData.maxOfOrNull {
                maxOf(it.incidenceRate, it.mortalityRate)
            }?.coerceAtLeast(1f) ?: 100f
            val yAxisMax = maxRate * 1.3f

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    chart.chartData.forEach { point ->
                        val incidenceFraction = (point.incidenceRate / yAxisMax).coerceIn(0f, 1f)
                        val mortalityFraction = (point.mortalityRate / yAxisMax).coerceIn(0f, 1f)

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Incidence Bar
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = String.format(Locale.ROOT, "%.0f", point.incidenceRate),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE64A19),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(incidenceFraction)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFFFF8A65), Color(0xFFE64A19))
                                                )
                                            )
                                    )
                                }

                                // Mortality Bar
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = String.format(Locale.ROOT, "%.0f", point.mortalityRate),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1976D2),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(mortalityFraction)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF64B5F6), Color(0xFF1976D2))
                                                )
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = point.ageGroup,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF374151),
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChartLegendItem(color = Color(0xFFE64A19), label = "Incidence")
                    ChartLegendItem(color = Color(0xFF1976D2), label = "Mortality")
                }
            }
        }

        if (chart.highlight.isNotBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF7ED),
                border = BorderStroke(1.dp, Color(0xFFFFEDD5))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFC2410C),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = chart.highlight,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9A3412),
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4B5563)
        )
    }
}

@Composable
fun MythInfoCard(
    mythInfo: MythInfoUiModel,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        modifier = modifier,
        containerColor = Color.White
    ) {
        Text(
            text = "Myth vs Fact",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1F2937),
            letterSpacing = (-0.5).sp
        )

        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFEF2F2),
                border = BorderStroke(1.dp, Color(0xFFFEE2E2))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "MYTH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB91C1C),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = mythInfo.myth,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF4B5563),
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.5).sp
                    )
                }
            }
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFDCFCE7))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "FACT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF15803D),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = mythInfo.fact,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF4B5563),
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.5).sp
                    )
                }
            }
        }
    }
}

@Composable
fun CurrentLocationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF4F46E5).copy(alpha = 0.4f)),
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "📍", fontSize = 20.sp)
            Text(
                text = "Refresh My Location",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun SunscreenReminderCard(
    reminder: SunscreenReminderUiModel,
    onOpenReminderPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val now by produceState(initialValue = System.currentTimeMillis(), reminder.isEnabled) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000)
        }
    }

    val remainingMillis = ((reminder.nextReminderAtMillis ?: now) - now).coerceAtLeast(0L)
    val totalMillis = reminder.reminderIntervalMinutes * 60_000L
    SectionCard(
        modifier = modifier.clickable(onClick = onOpenReminderPage),
        containerColor = Color(0xFFF8FAFF)
    ) {
        if (reminder.isEnabled) {
            Text(
                text = formatDuration(remainingMillis),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111827)
            )
        } else {
            Text(
                text = "No reminder set",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111827)
            )
        }
    }
}

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L

    return when {
        hours > 0 -> String.format(Locale.getDefault(), "%dh %02dm %02ds left", hours, minutes, seconds)
        minutes > 0 -> String.format(Locale.getDefault(), "%dm %02ds left", minutes, seconds)
        else -> String.format(Locale.getDefault(), "%ds left", seconds)
    }
}

private fun formatClockTime(timeMillis: Long?): String {
    if (timeMillis == null) return "--"
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(timeMillis)
}

@Composable
fun LoadingStateView(
    modifier: Modifier = Modifier,
    message: String = "Syncing with satellites...",
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        CircularProgressIndicator(
            strokeWidth = 6.dp,
            modifier = Modifier.size(56.dp),
            color = Color(0xFF4F46E5),
            trackColor = Color(0xFFEEF2FF)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        border = BorderStroke(1.dp, Color(0xFFFEE2E2))
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Text(
                text = "Oops! Connection Lost",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF991B1B),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFB91C1C).copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Retry Sync",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinTypeSelectionComponent(
    modifier: Modifier = Modifier,
    titleIcon: ImageVector = Icons.Default.Palette,
    onSkinTypeSelected: (recommendedMinutes: Int) -> Unit = {}
) {
    // 状态管理：存储当前选择的肤色 ID，默认选择 "Fair"
    var selectedSkinTypeId by remember { mutableIntStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 1. 标题行
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = titleIcon,
                contentDescription = "Palette Icon",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Your Skin Type",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
        }

        // 2. 描述文字
        Text(
            text = "Select your skin tone to receive personalized UV protection advice",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 3. 肤色选择行
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(skinTypeData) { skinType ->
                SkinTypeItem(
                    skinType = skinType,
                    isSelected = skinType.id == selectedSkinTypeId,
                    onClick = {
                        selectedSkinTypeId = skinType.id
                        // 👈 核心联动：点击时，把这个肤色推荐的分钟数传出去！
                        onSkinTypeSelected(skinType.reapplyMinutes)
                    }
                )
            }
        }

        // 4. 信息卡片
        // 根据选择的 ID 查找对应的肤色模型，如果找到则显示卡片
        skinTypeData.find { it.id == selectedSkinTypeId }?.let { selectedSkinType ->
            PersonalizedProtectionCard(selectedSkinType = selectedSkinType)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinTypeItem(
    skinType: SkinTypeModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // 根据是否选中，定义边框和颜色
    val borderColor = if (isSelected) Color(0xFF007BFF) else Color(0xFFE0E0E0)
    val borderThickness = if (isSelected) 2.dp else 1.dp

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(borderThickness, borderColor),
        color = Color.White,
        modifier = Modifier
            .width(72.dp)
            .aspectRatio(1f) // 使其保持正方形
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            // 圆形肤色预览
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(skinType.color)
                    .border(1.dp, Color(0xFFE0E0E0), CircleShape) // 圆形预览本身的灰色边框
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = skinType.label,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

// 子组件：个性化防护卡片
@Composable
fun PersonalizedProtectionCard(selectedSkinType: SkinTypeModel) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE8F5FF), // 蓝色背景
        border = BorderStroke(1.dp, Color(0xFFB0E2FF)), // 蓝色边框
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. 卡片标题
            Text(
                text = "Personalized Protection for ${selectedSkinType.label} Skin",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 2. 特征行
            ProtectionDetailRow(key = "Skin Type:", value = selectedSkinType.description)
            ProtectionDetailRow(key = "Burn Time (UV Index 8):", value = selectedSkinType.burnTimeDesc)
            ProtectionDetailRow(key = "Recommended SPF:", value = selectedSkinType.spf)

            // 3. Protection Tips 部分
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Protection Tips:",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Tips 下面的蓝线
            Divider(color = Color(0xFFB0E2FF), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // 4. Tips 列表
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedSkinType.protectionTips.forEach { tip ->
                    ProtectionTipItem(tip = tip)
                }
            }
        }
    }
}

// 辅助组件：用于渲染键值对的特征行
@Composable
fun ProtectionDetailRow(key: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = key,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

// 辅助组件：用于渲染带圆点的 Tip 行
@Composable
fun ProtectionTipItem(tip: String) {
    Row(verticalAlignment = Alignment.Top) {
        // 圆点
        Box(
            modifier = Modifier
                .padding(top = 4.dp, start = 4.dp, end = 8.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )
        // Tip 文本
        Text(
            text = tip,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}



@Preview(showBackground = true)
@Composable
private fun AwarenessChartCardPreview() {
    UvibeTheme {
        AwarenessChartCard(chart = PreviewUvData.awarenessCharts.first())
    }
}

@Preview(showBackground = true)
@Composable
private fun MythInfoCardPreview() {
    UvibeTheme {
        MythInfoCard(mythInfo = StaticUvData.myths.first())
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFDFDFD)
@Composable
fun UVStatusCardPreview() {
    val mockData = UvStatusUiModel(
        uvIndex = 8,
        levelText = "Very High",
        riskLevel = UvRiskLevel.VeryHigh, // 假设你的枚举叫这个
        locationName = "Spotswood",
        updatedAt = "UPDATED MAR 19, 7:33 PM",
        forecast = listOf(
            UvForecastUiModel(time = "8 PM", uvIndex = 6, levelText = UvRiskLevel.Moderate),
            UvForecastUiModel(time = "9 PM", uvIndex = 1, levelText = UvRiskLevel.Low),
            UvForecastUiModel(time = "10 PM", uvIndex = 8, levelText = UvRiskLevel.High),
            UvForecastUiModel(time = "11 PM", uvIndex = 11, levelText = UvRiskLevel.VeryHigh)
        )
    )

    // 需要用一个简单的 Theme 或者直接调组件
    UVStatusCard(
        status = mockData,
        onRefresh = {},
        onLocationClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SkinTypeSelectionComponentPreview() {
    MaterialTheme {
        SkinTypeSelectionComponent()
    }
}
