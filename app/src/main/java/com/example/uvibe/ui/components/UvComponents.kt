package com.example.uvibe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.uvibe.ui.model.AwarenessChartUiModel
import com.example.uvibe.ui.model.ClothingItemUiModel
import com.example.uvibe.ui.model.ClothingRecommendationUiModel
import com.example.uvibe.ui.model.MockUvData
import com.example.uvibe.ui.model.MythInfoUiModel
import com.example.uvibe.ui.model.ProtectionTipUiModel
import com.example.uvibe.ui.model.UvRiskLevel
import com.example.uvibe.ui.model.UvStatusUiModel
import com.example.uvibe.ui.theme.UvibeTheme

@Composable
fun UVStatusCard(
    status: UvStatusUiModel,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Chart placeholder",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = chart.highlight,
                style = MaterialTheme.typography.bodySmall,
            )
        }
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
        UVStatusCard(status = MockUvData.currentUvStatus)
    }
}

@Preview(showBackground = true)
@Composable
private fun UVRiskBadgePreview() {
    UvibeTheme {
        UVRiskBadge(riskLevel = MockUvData.currentUvStatus.riskLevel)
    }
}

@Preview(showBackground = true)
@Composable
private fun ProtectionTipCardPreview() {
    UvibeTheme {
        ProtectionTipCard(tip = MockUvData.protectionTip)
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
        AwarenessChartCard(chart = MockUvData.awarenessCharts.first())
    }
}

@Preview(showBackground = true)
@Composable
private fun MythInfoCardPreview() {
    UvibeTheme {
        MythInfoCard(mythInfo = MockUvData.myths.first())
    }
}

@Preview(showBackground = true)
@Composable
private fun ClothingRecommendationCardPreview() {
    UvibeTheme {
        ClothingRecommendationCard(recommendation = MockUvData.clothingRecommendation)
    }
}

@Preview(showBackground = true)
@Composable
private fun ClothingItemRowPreview() {
    UvibeTheme {
        ClothingItemRow(item = MockUvData.clothingRecommendation.items.first())
    }
}
