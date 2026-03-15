package com.example.uvibe.network

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingRemoteContentTest {

    @Test
    fun loadRecommendationContent_mapsAwsResponseIntoExistingUiModels() = runBlocking {
        val fakeService = FakeUvibeApiService(
            cancerInfoResponse = CancerInfoResponseDto(),
            dressingResponse = DressingRecommendationResponseDto(
                uvInfo = UvInfoDto(
                    uvRecordId = 7823,
                    locationId = 1,
                    uvIndex = 8.0,
                    riskLevel = "Very High",
                    recordedAt = "2024-01-06 10:17:00",
                ),
                clothingInfo = ClothingInfoDto(
                    clothingRecommendationId = 7823,
                    uvRecordId = 7823,
                    recommendationText = "Extra protection required. Wear a hat, sunglasses, and protective clothing.",
                    hatRequired = 1,
                    sunglassesRequired = 1,
                    longSleeveRequired = 1,
                ),
            ),
        )

        val content = OnboardingRemoteContent.loadRecommendationContent(
            uvIndex = 8,
            apiService = fakeService,
        )

        assertEquals(8, content.status.uvIndex)
        assertEquals("Very High", content.status.levelText)
        assertEquals("Location ID 1", content.status.locationName)
        assertEquals("Updated Jan 6, 10:17 AM", content.status.updatedAt)
        assertEquals(
            "Extra protection required. Wear a hat, sunglasses, and protective clothing.",
            content.protectionTip.description,
        )
        assertEquals(
            listOf("Wide-brim hat", "Sunglasses", "Long-sleeve shirt"),
            content.clothingRecommendation.items.map { it.name },
        )
    }

    @Test
    fun loadAwarenessCharts_mapsCancerBenchmarksIntoCards() = runBlocking {
        val fakeService = FakeUvibeApiService(
            cancerInfoResponse = CancerInfoResponseDto(
                data = listOf(
                    CancerBenchmarkItemDto(
                        id = 1,
                        ageGroup = "18-24",
                        incidenceRate = 11.2,
                        mortalityRate = 0.3,
                        riskSummary = "ABS data: 85% skip protection, yet melanoma is the most common cancer here.",
                        createTime = "2026-03-12 21:29:26",
                    ),
                ),
            ),
            dressingResponse = DressingRecommendationResponseDto(),
        )

        val charts = OnboardingRemoteContent.loadAwarenessCharts(apiService = fakeService)

        assertEquals(1, charts.size)
        assertEquals("Skin cancer benchmark: 18-24", charts.first().title)
        assertEquals("Incidence rate: 11.2 | Mortality rate: 0.3", charts.first().subtitle)
        assertTrue(charts.first().highlight.contains("melanoma"))
    }
}

private class FakeUvibeApiService(
    private val cancerInfoResponse: CancerInfoResponseDto,
    private val dressingResponse: DressingRecommendationResponseDto,
) : UvibeApiService {
    override suspend fun getCancerInfo(apiKey: String): CancerInfoResponseDto = cancerInfoResponse

    override suspend fun getDressingRecommendation(
        uvIndex: Int,
        apiKey: String,
    ): DressingRecommendationResponseDto = dressingResponse
}
