package com.example.uvibe.network

import com.example.uvibe.BuildConfig
import com.example.uvibe.ui.model.AwarenessChartUiModel
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
        return response.data
            .takeIf { it.isNotEmpty() }
            ?.map { benchmark ->
                AwarenessChartUiModel(
                    title = "Skin cancer benchmark: ${benchmark.ageGroup.orFallback("Unknown age group")}",
                    subtitle = buildString {
                        append("Incidence rate: ")
                        append(benchmark.incidenceRate.formatRate())
                        append(" | Mortality rate: ")
                        append(benchmark.mortalityRate.formatRate())
                    },
                    highlight = benchmark.riskSummary.orFallback("No additional summary provided."),
                )
            }
            ?: error("Cancer awareness data is empty.")
    }
}

private fun DressingRecommendationResponseDto.toRecommendationContentUi(
    defaultUvIndex: Int,
): RecommendationContentUi {
    if (uvInfo == null && clothingInfo == null) {
        error("Clothing recommendation data is missing.")
    }

    val resolvedUvIndex = uvInfo?.uvIndex?.toInt() ?: defaultUvIndex
    val riskLevel = uvInfo?.riskLevel.toUvRiskLevel(resolvedUvIndex)
    val recommendationText = clothingInfo?.recommendationText.orFallback(defaultClothingHeadline(riskLevel))
    val clothingItems = buildList {
        if (clothingInfo?.hatRequired == 1) {
            add(
                ClothingItemUiModel(
                    name = "Wide-brim hat",
                    reason = "Marked as required by the AWS recommendation for this UV level.",
                ),
            )
        }
        if (clothingInfo?.sunglassesRequired == 1) {
            add(
                ClothingItemUiModel(
                    name = "Sunglasses",
                    reason = "The API indicates eye protection is needed for current conditions.",
                ),
            )
        }
        if (clothingInfo?.longSleeveRequired == 1) {
            add(
                ClothingItemUiModel(
                    name = "Long-sleeve shirt",
                    reason = "The API recommends added skin coverage for safer time outdoors.",
                ),
            )
        }
        if (isEmpty()) {
            add(
                ClothingItemUiModel(
                    name = "General sun protection",
                    reason = recommendationText,
                ),
            )
        }
    }

    return RecommendationContentUi(
        status = UvStatusUiModel(
            uvIndex = resolvedUvIndex,
            levelText = uvInfo?.riskLevel.orFallback(riskLevel.label),
            riskLevel = riskLevel,
            locationName = uvInfo?.locationId?.let { "Location ID $it" } ?: "AWS UV feed",
            updatedAt = uvInfo?.recordedAt.toUpdatedAtLabel(),
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

private fun String?.toUvRiskLevel(fallbackUvIndex: Int): UvRiskLevel = when (this?.trim()?.lowercase()) {
    "low" -> UvRiskLevel.Low
    "moderate" -> UvRiskLevel.Moderate
    "high" -> UvRiskLevel.High
    "very high" -> UvRiskLevel.VeryHigh
    "extreme" -> UvRiskLevel.Extreme
    else -> uvRiskLevelFor(fallbackUvIndex)
}

private fun Double?.formatRate(): String =
    this?.let { String.format(Locale.US, "%.1f", it) } ?: "N/A"

private fun String?.orFallback(fallback: String): String =
    this?.takeIf { it.isNotBlank() } ?: fallback

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
