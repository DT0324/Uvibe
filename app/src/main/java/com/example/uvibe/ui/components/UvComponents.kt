package com.example.uvibe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
fun UVStatusCard(
    status: UvStatusUiModel,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        modifier = modifier.clickable { onRefresh() }
    ) {
        Text(
            text = "Current UV",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = status.locationName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = status.uvIndex.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = status.levelText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            UVRiskBadge(riskLevel = status.riskLevel)
        }

        Text(
            text = status.updatedAt,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun UVRiskBadge(
    riskLevel: UvRiskLevel,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = riskLevel.color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, riskLevel.color.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(riskLevel.color),
            )
            Text(
                text = riskLevel.label,
                style = MaterialTheme.typography.labelLarge,
                color = riskLevel.color,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun ProtectionTipCard(
    tip: ProtectionTipUiModel,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = tip.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = tip.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            .height(54.dp),
        enabled = enabled,
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(
            text = "Use Current Location",
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun LoadingStateView(
    modifier: Modifier = Modifier,
    message: String = "Loading UV information...",
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            TextButton(onClick = onRetry) {
                Text(text = "Try Again")
            }
        }
    }
}

@Composable
fun AwarenessChartCard(
    chart: AwarenessChartUiModel,
    modifier: Modifier = Modifier,
) {
    SectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = chart.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = chart.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (chart.chartData.isNotEmpty()) {
            val maxRate = chart.chartData.maxOfOrNull {
                maxOf(it.incidenceRate, it.mortalityRate)
            }?.coerceAtLeast(1f) ?: 100f
            val yAxisMax = maxRate * 1.15f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                chart.chartData.forEach { point ->
                    val incidenceFraction = (point.incidenceRate / yAxisMax).coerceIn(0f, 1f)
                    val mortalityFraction = (point.mortalityRate / yAxisMax).coerceIn(0f, 1f)

                    val incidenceText = String.format(java.util.Locale.US, "%.1f", point.incidenceRate)
                    val mortalityText = String.format(java.util.Locale.US, "%.1f", point.mortalityRate)

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Text(
                                    text = incidenceText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(incidenceFraction)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(Color(0xFFFF6B57))
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Text(
                                    text = mortalityText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(mortalityFraction)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(Color(0xFF5AA9FF))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = point.ageGroup,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendItem(color = Color(0xFFFF6B57), label = "Incidence")
                Spacer(modifier = Modifier.width(20.dp))
                ChartLegendItem(color = Color(0xFF5AA9FF), label = "Mortality")
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No chart data available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (chart.highlight.isNotBlank()) {
            Text(
                text = chart.highlight,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            text = mythInfo.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        LabelValueText(
            label = "Myth",
            value = mythInfo.myth,
            accent = Color(0xFFE4572E)
        )

        LabelValueText(
            label = "Fact",
            value = mythInfo.fact,
            accent = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ClothingRecommendationCard(
    recommendation: ClothingRecommendationUiModel,
    modifier: Modifier = Modifier,
) {
    SectionCard(modifier = modifier) {
        Text(
            text = "Clothing recommendations",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "Current UV Index: ${recommendation.uvIndex}",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = recommendation.headline,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            recommendation.items.forEach { item ->
                ClothingItemRow(item = item)
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
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = item.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LabelValueText(
    label: String,
    value: String,
    accent: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = accent,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UVStatusCardPreview() {
    UvibeTheme {
        UVStatusCard(status = PreviewUvData.currentUvStatus,onRefresh = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun UVRiskBadgePreview() {
    UvibeTheme {
        UVRiskBadge(riskLevel = PreviewUvData.currentUvStatus.riskLevel)
    }
}

@Preview(showBackground = true)
@Composable
private fun ProtectionTipCardPreview() {
    UvibeTheme {
        ProtectionTipCard(tip = PreviewUvData.protectionTip)
    }
}

@Preview(showBackground = true)
@Composable
private fun CurrentLocationButtonPreview() {
    UvibeTheme {
        CurrentLocationButton(onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingStateViewPreview() {
    UvibeTheme {
        LoadingStateView()
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorStateViewPreview() {
    UvibeTheme {
        ErrorStateView(message = "Unable to load UV data right now.", onRetry = {})
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

@Preview(showBackground = true)
@Composable
private fun ClothingRecommendationCardPreview() {
    UvibeTheme {
        ClothingRecommendationCard(recommendation = PreviewUvData.clothingRecommendation)
    }
}

@Preview(showBackground = true)
@Composable
private fun ClothingItemRowPreview() {
    UvibeTheme {
        ClothingItemRow(item = PreviewUvData.clothingRecommendation.items.first())
    }
}
