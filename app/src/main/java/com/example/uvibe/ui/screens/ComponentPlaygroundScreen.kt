package com.example.uvibe.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.uvibe.ui.components.AwarenessChartCard
import com.example.uvibe.ui.components.ClothingRecommendationCard
import com.example.uvibe.ui.components.CurrentLocationButton
import com.example.uvibe.ui.components.ErrorStateView
import com.example.uvibe.ui.components.LoadingStateView
import com.example.uvibe.ui.components.MythInfoCard
import com.example.uvibe.ui.components.ProtectionTipCard
import com.example.uvibe.ui.components.UVStatusCard
import com.example.uvibe.ui.model.PreviewUvData
import com.example.uvibe.ui.model.StaticUvData

import com.example.uvibe.ui.theme.UvibeTheme

private enum class PlaygroundAlertState {
    Content,
    Loading,
    Error,
}

@Composable
fun ComponentPlaygroundScreen(
    modifier: Modifier = Modifier,
) {
    var alertState by remember { mutableStateOf(PlaygroundAlertState.Content) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "UVibe Component Playground",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = "Use this screen to preview reusable onboarding components with local mock data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Alert states",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    PlaygroundStateSelector(
                        selectedState = alertState,
                        onStateSelected = { alertState = it },
                    )
                    when (alertState) {
                        PlaygroundAlertState.Content -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // 👈 只需要在这里加上 onRefresh = {}
                            UVStatusCard(
                                status = PreviewUvData.currentUvStatus,
                                onRefresh = { /* Playground 暂时不需要真实刷新逻辑 */ }
                            )

                            ProtectionTipCard(tip = PreviewUvData.protectionTip)

                            CurrentLocationButton(onClick = {})
                        }

                        PlaygroundAlertState.Loading -> LoadingStateView()
                        PlaygroundAlertState.Error -> ErrorStateView(
                            message = "Location permission missing or service unavailable.",
                            onRetry = { alertState = PlaygroundAlertState.Content },
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Awareness and education",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            items(PreviewUvData.awarenessCharts) { chart ->
                AwarenessChartCard(chart = chart)
            }

            items(StaticUvData.myths) { myth ->
                MythInfoCard(mythInfo = myth)
            }

            item {
                Text(
                    text = "Clothing recommendations",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            item {
                ClothingRecommendationCard(recommendation = PreviewUvData.clothingRecommendation)
            }
        }
    }
}

@Composable
private fun PlaygroundStateSelector(
    selectedState: PlaygroundAlertState,
    onStateSelected: (PlaygroundAlertState) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(
            PlaygroundAlertState.Content to "Content",
            PlaygroundAlertState.Loading to "Loading",
            PlaygroundAlertState.Error to "Error",
        ).forEach { (state, label) ->
            FilterChip(
                selected = selectedState == state,
                onClick = { onStateSelected(state) },
                label = { Text(text = label) },
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ComponentPlaygroundScreenPreview() {
    UvibeTheme {
        ComponentPlaygroundScreen()
    }
}
