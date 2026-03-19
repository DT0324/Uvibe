package com.example.uvibe.ui.model

import androidx.compose.ui.graphics.Color


enum class UvRiskLevel(
    val label: String,
    val color: Color,
    val guidance: String,
) {
    Low(label = "Low", color = Color(0xFF2E7D32), guidance = "Enjoy the day, but sunscreen is still a good idea."),
    Moderate(label = "Moderate", color = Color(0xFFF9A825), guidance = "Cover up at midday and use SPF 30+."),
    High(label = "High", color = Color(0xFFEF6C00), guidance = "Seek shade and limit direct sun where possible."),
    VeryHigh(label = "Very High", color = Color(0xFFD84315), guidance = "Use full protection: hat, sleeves, sunglasses, and sunscreen."),
    Extreme(label = "Extreme", color = Color(0xFF8E24AA), guidance = "Avoid long outdoor exposure during peak sun hours.")
}


data class UvStatusUiModel(
    val uvIndex: Int,
    val levelText: String,
    val riskLevel: UvRiskLevel,
    val locationName: String,
    val updatedAt: String,
    val forecast: List<UvForecastUiModel> = emptyList()
)

data class ProtectionTipUiModel(
    val title: String,
    val description: String,
)

data class AwarenessChartUiModel(
    val title: String,
    val subtitle: String,
    val highlight: String,
    val chartData: List<ChartDataPoint> = emptyList()
)

data class ChartDataPoint(
    val ageGroup: String,
    val incidenceRate: Float,
    val mortalityRate: Float
)

data class MythInfoUiModel(
    val title: String,
    val myth: String,
    val fact: String,
)

data class ClothingItemUiModel(
    val name: String,
    val reason: String,
)

data class ClothingRecommendationUiModel(
    val uvIndex: Int,
    val headline: String,
    val items: List<ClothingItemUiModel>,
)

data class SunscreenReminderUiModel(
    val isEnabled: Boolean = false,
    val reminderIntervalMinutes: Int = 120,
    val preAlertMinutes: Int = 10,
    val lastAppliedAtMillis: Long? = null,
    val nextReminderAtMillis: Long? = null,
)


fun uvRiskLevelFor(uvIndex: Int): UvRiskLevel = when {
    uvIndex <= 2 -> UvRiskLevel.Low
    uvIndex <= 5 -> UvRiskLevel.Moderate
    uvIndex <= 7 -> UvRiskLevel.High
    uvIndex <= 10 -> UvRiskLevel.VeryHigh
    else -> UvRiskLevel.Extreme
}

fun defaultClothingHeadline(riskLevel: UvRiskLevel): String =
    "UV ${riskLevel.label.lowercase()} today. Dress for safer outdoor time."


object StaticUvData {
    val myths = listOf(
        MythInfoUiModel(
            title = "Myth: Cloudy days are safe",
            myth = "If the sun is behind clouds, UV is not a problem.",
            fact = "UV can still be high on cloudy days, so sun protection still matters.",
        ),
        MythInfoUiModel(
            title = "Myth: Tanned skin is protected",
            myth = "A tan means your skin is safe from damage.",
            fact = "A tan is a sign of skin damage and does not replace proper protection.",
        ),
    )
}

data class UvForecastUiModel(
    val time: String,
    val uvIndex: Int,
    val levelText: UvRiskLevel
)




object PreviewUvData {
    val currentUvStatus = UvStatusUiModel(
        uvIndex = 9,
        levelText = "Very High",
        riskLevel = UvRiskLevel.VeryHigh,
        locationName = "Spotswood, VIC",
        updatedAt = "Updated Just now",
    )

    val protectionTip = ProtectionTipUiModel(
        title = "Today's protection tip",
        description = "Reapply sunscreen every two hours and bring a hat before heading outside.",
    )

    val awarenessCharts = listOf(
        AwarenessChartUiModel(
            title = "Skin Cancer Rates by Age",
            subtitle = "Incidence vs Mortality (per 100,000)",
            highlight = "Incidence increases significantly with age.",
            chartData = listOf(
                ChartDataPoint("18-24", 4.2f, 0.1f),
                ChartDataPoint("25-34", 12.5f, 0.5f),
                ChartDataPoint("35-44", 28.0f, 1.2f)
            )
        )
    )

    val clothingRecommendation = ClothingRecommendationUiModel(
        uvIndex = 9,
        headline = "UV very high today. Dress for safer outdoor time.",
        items = listOf(
            ClothingItemUiModel("Wide-brim hat", "Strong coverage for face and neck."),
            ClothingItemUiModel("Sunglasses", "Protects eyes in bright conditions.")
        )
    )


}
