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
import retrofit2.HttpException

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
        return try {
            val response = apiService.getDressingRecommendation(uvIndex = uvIndex)
            response.toRecommendationContentUi(defaultUvIndex = uvIndex)
        } catch (e: HttpException) {
            // 🌟 拦截 404 错误：当后端因为 UV 过低 (如晚上的 0) 查不到防晒数据时，提供本地兜底 UI
            if (e.code() == 404) {
                val riskLevel = uvRiskLevelFor(uvIndex)
                val updatedAtLabel = "Updated " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.US))

                RecommendationContentUi(
                    status = UvStatusUiModel(
                        uvIndex = uvIndex,
                        levelText = riskLevel.label, // UV 为 0 时会显示 "Low"
                        riskLevel = riskLevel,
                        locationName = "Current Location",
                        updatedAt = updatedAtLabel
                    ),
                    protectionTip = ProtectionTipUiModel(
                        title = "No sun protection needed",
                        description = "UV levels are minimal. You can safely enjoy being outdoors without extra sun protection."
                    ),
                    clothingRecommendation = ClothingRecommendationUiModel(
                        uvIndex = uvIndex,
                        headline = "No special sun protection required right now.",
                        items = listOf(
                            ClothingItemUiModel(
                                name = "Standard clothing",
                                reason = "Dress comfortably for the weather, no extra UV gear needed."
                            )
                        )
                    )
                )
            } else {
                // 如果是 500 等其他服务器错误，继续抛出，让 ViewModel 处理成普通的 Error 状态
                throw e
            }
        }
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
            subtitle = "Incidence vs Mortality (per 100,000 people)",
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

    // 🌟 修复 1：强制使用从 ViewModel 传进来的真实 UV 指数 (即 OpenWeatherMap 测出的真实值)
    // 不再使用后端的 uvInfo?.uvIndex
    val resolvedUvIndex = defaultUvIndex

    val riskLevel = uvRiskLevelFor(resolvedUvIndex)

    // 🌟 修复 2：如果后端没有返回真实的地点字符串，兜底显示 "Current Location"
    // 不再暴露数据库的 "Location ID $it"
    val locationName = uvInfo?.location.orFallback("Current Location")

    // 🌟 修复 3：忽略后端的陈旧时间戳，强制使用手机系统当前的真实时间
    val updatedAtLabel = "Updated " + LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.US))

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
