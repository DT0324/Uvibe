package com.example.uvibe.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uvibe.ui.model.AwarenessChartUiModel
import com.example.uvibe.ui.model.ClothingItemUiModel
import com.example.uvibe.ui.model.ClothingRecommendationUiModel
import com.example.uvibe.ui.model.MythInfoUiModel
import com.example.uvibe.ui.model.PreviewUvData
import com.example.uvibe.ui.model.ProtectionTipUiModel
import com.example.uvibe.ui.model.StaticUvData
import com.example.uvibe.ui.model.UvRiskLevel
import com.example.uvibe.ui.model.UvStatusUiModel
import com.example.uvibe.ui.theme.UvibeTheme

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
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.12f),
                        radius = size.minDimension / 1.2f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 1.1f, size.height * -0.1f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = size.minDimension / 1.8f,
                        center = androidx.compose.ui.geometry.Offset(size.width * -0.1f, size.height * 1.1f)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Current UV",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 1.sp
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
                modifier = Modifier.padding(vertical = 8.dp),
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
            .padding(20.dp),
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
                fontWeight = FontWeight.ExtraBold,
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
            val yAxisMax = maxRate * 1.2f

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Incidence Bar
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(incidenceFraction)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFFFF8A65), Color(0xFFE64A19))
                                            )
                                        )
                                )

                                // Mortality Bar
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(mortalityFraction)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF64B5F6), Color(0xFF1976D2))
                                            )
                                        )
                                )
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
    SectionCard(modifier = modifier) {
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
                        color = Color(0xFF7F1D1D),
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium
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
                        color = Color(0xFF064E3B),
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium
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
                text = "Use Current Location",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
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

@Preview(showBackground = true)
@Composable
private fun UVStatusCardPreview() {
    UvibeTheme {
        UVStatusCard(status = PreviewUvData.currentUvStatus, onRefresh = {}, onLocationClick = {})
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
