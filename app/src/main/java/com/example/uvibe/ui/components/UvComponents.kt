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

@Composable
fun UVStatusCard(
    status: UvStatusUiModel,
    onRefresh: () -> Unit, // 保持这个回调参数不变
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onRefresh() }, // 👈 核心魔法：让整张卡片变得可点击！
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 恢复成最清爽的纯文本标题
            Text(
                text = "Current UV",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = status.locationName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = status.uvIndex.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = status.levelText,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                UVRiskBadge(riskLevel = status.riskLevel)
            }
            Text(
                text = status.updatedAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun UVRiskBadge(
    riskLevel: UvRiskLevel,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = riskLevel.color.copy(alpha = 0.16f),
        contentColor = riskLevel.color,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                fontWeight = FontWeight.SemiBold,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
    ) {
        Text(text = "Use Current Location")
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
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. 标题和副标题
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = chart.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = chart.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 2. 核心图表区域
            if (chart.chartData.isNotEmpty()) {

                val maxRate = chart.chartData.maxOfOrNull {
                    maxOf(it.incidenceRate, it.mortalityRate)
                }?.coerceAtLeast(1f) ?: 100f
                val yAxisMax = maxRate * 1.2f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    chart.chartData.forEach { point ->
                        val incidenceFraction = (point.incidenceRate / yAxisMax).coerceIn(0f, 1f)
                        val mortalityFraction = (point.mortalityRate / yAxisMax).coerceIn(0f, 1f)

                        // 格式化百分比文本 (保留一位小数)
                        val incidenceText = java.lang.String.format(java.util.Locale.US, "%.1f", point.incidenceRate)
                        val mortalityText = java.lang.String.format(java.util.Locale.US, "%.1f", point.mortalityRate)

                        // 每一组（年龄段的X轴标签 + 两根带数字的柱子）
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(horizontal = 2.dp)
                        ) {
                            // 两根柱子的容器
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // === 发病率 (Incidence) 柱子 + 顶部数字 ===
                                Column(
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = incidenceText,
                                        fontSize = 8.sp, // 字体设得很小，防止相邻的数字挤在一起
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        softWrap = false // 禁止文字换行，确保排版整洁
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(incidenceFraction)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(Color(0xFFEF5350))
                                    )
                                }

                                // === 死亡率 (Mortality) 柱子 + 顶部数字 ===
                                Column(
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = mortalityText,
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(mortalityFraction)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(Color(0xFF42A5F5))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // X 轴标签
                            Text(
                                text = point.ageGroup,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                // 3. 底部图例 (Legend)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChartLegendItem(color = Color(0xFFEF5350), label = "Incidence Rate")
                    Spacer(modifier = Modifier.width(24.dp))
                    ChartLegendItem(color = Color(0xFF42A5F5), label = "Mortality Rate")
                }

            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No chart data available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 4. 高亮总结文本
            if (chart.highlight.isNotBlank()) {
                Text(
                    text = chart.highlight,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
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
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MythInfoCard(
    mythInfo: MythInfoUiModel,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = mythInfo.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            LabelValueText(label = "Myth", value = mythInfo.myth, accent = MaterialTheme.colorScheme.error)
            LabelValueText(label = "Fact", value = mythInfo.fact, accent = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ClothingRecommendationCard(
    recommendation: ClothingRecommendationUiModel,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Clothing recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Current UV Index: ${recommendation.uvIndex}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = recommendation.headline,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recommendation.items.forEach { item ->
                    ClothingItemRow(item = item)
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
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = accent,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
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
