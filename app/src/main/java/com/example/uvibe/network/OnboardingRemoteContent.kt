package com.example.uvibe.network

import com.example.uvibe.BuildConfig
import com.example.uvibe.ui.model.AwarenessChartUiModel
import com.example.uvibe.ui.model.ChartDataPoint
import com.example.uvibe.ui.model.ClothingItemUiModel
import com.example.uvibe.ui.model.ClothingRecommendationUiModel
import com.example.uvibe.ui.model.ProtectionTipUiModel
import com.example.uvibe.ui.model.UvRiskLevel
import com.example.uvibe.ui.model.UvStatusUiModel
import com.example.uvibe.ui.model.defaultClothingHeadline
import com.example.uvibe.ui.model.uvRiskLevelFor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class RecommendationContentUi(
    val status: UvStatusUiModel,
    val protectionTip: ProtectionTipUiModel,
    val clothingRecommendation: ClothingRecommendationUiModel,
)

object OnboardingRemoteContent {
    suspend fun loadRecommendationContent(
        uvIndex: Int = BuildConfig.DEFAULT_DRESSING_UV_INDEX,
        apiService: UvibeApiService = UvibeApiClient.service,
    ): RecommendationContentUi {
        val response = apiService.getDressingRecommendation(uvIndex = uvIndex)
        return response.toRecommendationContentUi(defaultUvIndex = uvIndex)
    }

    suspend fun loadAwarenessCharts(
        apiService: UvibeApiService = UvibeApiClient.service,
    ): List<AwarenessChartUiModel> {
        val response = apiService.getCancerInfo()
        val rawData = response.data

        if (rawData.isNullOrEmpty()) {
            error("Cancer awareness data is empty.")
        }


        val chartPoints = rawData.map { benchmark ->
            ChartDataPoint(
                ageGroup = benchmark.ageGroup.orFallback("Unknown"),
                incidenceRate = benchmark.incidenceRate?.toFloat() ?: 0f,
                mortalityRate = benchmark.mortalityRate?.toFloat() ?: 0f
            )
        }


        val singleAggregatedChart = AwarenessChartUiModel(
            title = "Skin Cancer Rates by Age",
            subtitle = "Incidence vs Mortality (Percentage)",

            highlight = rawData.firstOrNull()?.riskSummary.orFallback("Compare incidence and mortality trends across different age groups."),
            chartData = chartPoints
        )


        return listOf(singleAggregatedChart)
    }
}

private fun DressingRecommendationResponseDto.toRecommendationContentUi(
    defaultUvIndex: Int,
): RecommendationContentUi {
    if (uvInfo == null && clothingInfo == null) {
        error("Clothing recommendation data is missing.")
    }

    val resolvedUvIndex = uvInfo?.uvIndex ?: defaultUvIndex
    val riskLevel = uvRiskLevelFor(resolvedUvIndex)
    val locationName = uvInfo?.location
        .orFallback(uvInfo?.locationId?.let { "Location ID $it" } ?: "AWS UV feed")
    val updatedAtLabel = (uvInfo?.timestamp ?: uvInfo?.recordedAt)
        .toUpdatedAtLabel()
    val clothingItems = buildList {
        clothingInfo?.hatType?.takeIf { it.isNotBlank() }?.let { hatType ->
            add(
                ClothingItemUiModel(
                    name = hatType,
                    reason = "Recommended headwear for the current UV level.",
                ),
            )
        }
        clothingInfo?.sunscreenSpf?.takeIf { it.isNotBlank() }?.let { sunscreenSpf ->
            add(
                ClothingItemUiModel(
                    name = "Sunscreen",
                    reason = "Use $sunscreenSpf sunscreen for sun protection.",
                ),
            )
        }
        clothingInfo?.shirtType?.takeIf { it.isNotBlank() }?.let { shirtType ->
            add(
                ClothingItemUiModel(
                    name = shirtType,
                    reason = "Recommended clothing coverage for safer outdoor time.",
                ),
            )
        }
        if (clothingInfo?.hatRequired == 1 && none { it.name.equals("Wide-brim hat", ignoreCase = true) }) {
            add(
                ClothingItemUiModel(
                    name = "Wide-brim hat",
                    reason = "Required by the live recommendation response for this UV level.",
                ),
            )
        }
        if (clothingInfo?.sunglassesRequired == 1 && none { it.name.equals("Sunglasses", ignoreCase = true) }) {
            add(
                ClothingItemUiModel(
                    name = "Sunglasses",
                    reason = "Required by the live recommendation response for this UV level.",
                ),
            )
        }
        if (clothingInfo?.longSleeveRequired == 1 && none { it.name.contains("Long", ignoreCase = true) }) {
            add(
                ClothingItemUiModel(
                    name = "Long-sleeve shirt",
                    reason = "Required by the live recommendation response for this UV level.",
                ),
            )
        }
        if (isEmpty()) {
            add(
                ClothingItemUiModel(
                    name = "General sun protection",
                    reason = clothingInfo?.recommendationText.orFallback(defaultClothingHeadline(riskLevel)),
                ),
            )
        }
    }
    val recommendationText = clothingInfo?.recommendationText
        .orFallback(buildRecommendationHeadline(riskLevel, clothingItems))

    return RecommendationContentUi(
        status = UvStatusUiModel(
            uvIndex = resolvedUvIndex,
            levelText = riskLevel.label,
            riskLevel = riskLevel,
            locationName = locationName,
            updatedAt = updatedAtLabel,
        ),
        protectionTip = ProtectionTipUiModel(
            title = "Today's protection tip",
            description = recommendationText,
        ),
        clothingRecommendation = ClothingRecommendationUiModel(
            uvIndex = resolvedUvIndex,
            headline = recommendationText,
            items = clothingItems,
        ),
    )
}

private fun Double?.formatRate(): String =
    this?.let { String.format(Locale.US, "%.1f", it) } ?: "N/A"

private fun String?.orFallback(fallback: String): String =
    this?.takeIf { it.isNotBlank() } ?: fallback

private fun buildRecommendationHeadline(
    riskLevel: UvRiskLevel,
    clothingItems: List<ClothingItemUiModel>,
): String {
    val itemSummary = clothingItems
        .takeIf { it.isNotEmpty() }
        ?.joinToString(separator = ", ") { it.name }
        ?.takeIf { it.isNotBlank() }

    return itemSummary?.let {
        "${defaultClothingHeadline(riskLevel)} Recommended: $it."
    } ?: defaultClothingHeadline(riskLevel)
}

private fun String?.toUpdatedAtLabel(): String {
    if (this.isNullOrBlank()) {
        return "Updated recently"
    }

    val parser = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)
    val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.US)

    return runCatching {
        "Updated ${LocalDateTime.parse(this, parser).format(formatter)}"
    }.getOrElse {
        "Updated $this"
    }
}
