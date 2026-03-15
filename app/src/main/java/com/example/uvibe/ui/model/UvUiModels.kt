package com.example.uvibe.ui.model

import androidx.compose.ui.graphics.Color

enum class UvRiskLevel(
    val label: String,
    val color: Color,
    val guidance: String,
) {
    Low(
        label = "Low",
        color = Color(0xFF2E7D32),
        guidance = "Enjoy the day, but sunscreen is still a good idea.",
    ),
    Moderate(
        label = "Moderate",
        color = Color(0xFFF9A825),
        guidance = "Cover up at midday and use SPF 30+.",
    ),
    High(
        label = "High",
        color = Color(0xFFEF6C00),
        guidance = "Seek shade and limit direct sun where possible.",
    ),
    VeryHigh(
        label = "Very High",
        color = Color(0xFFD84315),
        guidance = "Use full protection: hat, sleeves, sunglasses, and sunscreen.",
    ),
    Extreme(
        label = "Extreme",
        color = Color(0xFF8E24AA),
        guidance = "Avoid long outdoor exposure during peak sun hours.",
    ),
}

data class UvStatusUiModel(
    val uvIndex: Int,
    val levelText: String,
    val riskLevel: UvRiskLevel,
    val locationName: String,
    val updatedAt: String,
)

data class ProtectionTipUiModel(
    val title: String,
    val description: String,
)

data class AwarenessChartUiModel(
    val title: String,
    val subtitle: String,
    val highlight: String,
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

object MockUvData {
    val currentUvStatus = UvStatusUiModel(
        uvIndex = 9,
        levelText = "Very High",
        riskLevel = UvRiskLevel.VeryHigh,
        locationName = "Melbourne, VIC",
        updatedAt = "Updated 11:30 AM",
    )

    val protectionTip = ProtectionTipUiModel(
        title = "Today's protection tip",
        description = "Reapply sunscreen every two hours and bring a hat before heading outside.",
    )

    val awarenessCharts = listOf(
        AwarenessChartUiModel(
            title = "Skin cancer impact in Australia",
            subtitle = "Placeholder for a future chart or infographic.",
            highlight = "Use this card for a visual showing why UV awareness matters.",
        ),
        AwarenessChartUiModel(
            title = "Heat trend in Australia",
            subtitle = "Placeholder for a future temperature or UV trend chart.",
            highlight = "Keep the layout ready for API or AWS-fed visual data later.",
        ),
    )

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

    val clothingRecommendation = clothingRecommendationFor(currentUvStatus.uvIndex)

    fun clothingRecommendationFor(uvIndex: Int): ClothingRecommendationUiModel {
        val riskLevel = riskLevelFor(uvIndex)
        val items = when {
            uvIndex <= 2 -> listOf(
                ClothingItemUiModel("Sunscreen", "Good everyday protection even when UV is lower."),
                ClothingItemUiModel("Sunglasses", "Protect your eyes from glare and UV exposure."),
            )

            uvIndex <= 5 -> listOf(
                ClothingItemUiModel("Wide-brim hat", "Helps shade your face, ears, and neck."),
                ClothingItemUiModel("Sunglasses", "Useful when UV is moderate and above."),
                ClothingItemUiModel("SPF 30+ sunscreen", "Apply before outdoor activities."),
            )

            uvIndex <= 7 -> listOf(
                ClothingItemUiModel("Wide-brim hat", "Essential when UV is high."),
                ClothingItemUiModel("Long-sleeve shirt", "Adds extra skin coverage in the sun."),
                ClothingItemUiModel("Sunglasses", "Protects eyes during bright outdoor conditions."),
            )

            else -> listOf(
                ClothingItemUiModel("Wide-brim hat", "Strong coverage for face, scalp, and neck."),
                ClothingItemUiModel("Long sleeves", "Reduces direct UV exposure on arms."),
                ClothingItemUiModel("Sunglasses", "Supports eye protection in very high UV."),
                ClothingItemUiModel("Shade or umbrella", "Best added when UV becomes very high or extreme."),
            )
        }

        return ClothingRecommendationUiModel(
            uvIndex = uvIndex,
            headline = "UV ${riskLevel.label.lowercase()} today. Dress for safer outdoor time.",
            items = items,
        )
    }

    fun riskLevelFor(uvIndex: Int): UvRiskLevel = when {
        uvIndex <= 2 -> UvRiskLevel.Low
        uvIndex <= 5 -> UvRiskLevel.Moderate
        uvIndex <= 7 -> UvRiskLevel.High
        uvIndex <= 10 -> UvRiskLevel.VeryHigh
        else -> UvRiskLevel.Extreme
    }
}

fun uvRiskLevelFor(uvIndex: Int): UvRiskLevel = MockUvData.riskLevelFor(uvIndex)

fun defaultClothingHeadline(riskLevel: UvRiskLevel): String =
    "UV ${riskLevel.label.lowercase()} today. Dress for safer outdoor time."
