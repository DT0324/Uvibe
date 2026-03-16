package com.example.uvibe.network

import com.example.uvibe.BuildConfig
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class CancerInfoResponseDto(
    val data: List<CancerBenchmarkItemDto> = emptyList(),
)

data class CancerBenchmarkItemDto(
    val id: Int? = null,
    @SerializedName("age_group")
    val ageGroup: String? = null,
    @SerializedName("incidence_rate")
    val incidenceRate: Double? = null,
    @SerializedName("mortality_rate")
    val mortalityRate: Double? = null,
    @SerializedName("risk_summary")
    val riskSummary: String? = null,
    @SerializedName("create_time")
    val createTime: String? = null,
)

data class DressingRecommendationResponseDto(
    @SerializedName("uv_info")
    val uvInfo: UvInfoDto? = null,
    @SerializedName("clothing_info")
    val clothingInfo: ClothingInfoDto? = null,
)

data class UvInfoDto(
    @SerializedName("uv_record_id")
    val uvRecordId: Int? = null,
    @SerializedName("uv_index")
    val uvIndex: Int? = null,
    val location: String? = null,
    val timestamp: String? = null,
    @SerializedName("location_id")
    val locationId: Int? = null,
    @SerializedName("risk_level")
    val riskLevel: String? = null,
    @SerializedName("recorded_at")
    val recordedAt: String? = null,
)

data class ClothingInfoDto(
    val id: Int? = null,
    @SerializedName("uv_record_id")
    val uvRecordId: Int? = null,
    @SerializedName("hat_type")
    val hatType: String? = null,
    @SerializedName("sunscreen_spf")
    val sunscreenSpf: String? = null,
    @SerializedName("shirt_type")
    val shirtType: String? = null,
    @SerializedName("clothing_rec_id")
    val clothingRecommendationId: Int? = null,
    @SerializedName("rec_text")
    val recommendationText: String? = null,
    @SerializedName("hat_required")
    val hatRequired: Int? = null,
    @SerializedName("sunglasses_req")
    val sunglassesRequired: Int? = null,
    @SerializedName("longsleeve_req")
    val longSleeveRequired: Int? = null,
)

interface UvibeApiService {
    @GET("getCancerInfo")
    suspend fun getCancerInfo(
        @Header("x-api-key") apiKey: String = BuildConfig.AWS_API_KEY,
    ): CancerInfoResponseDto

    @GET("getDressingRecommandation")
    suspend fun getDressingRecommendation(
        @Query("uv_index") uvIndex: Int,
        @Header("x-api-key") apiKey: String = BuildConfig.AWS_API_KEY,
    ): DressingRecommendationResponseDto
}

object UvibeApiClient {
    val service: UvibeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.AWS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UvibeApiService::class.java)
    }
}

data class OwmOneCallResponseDto(
    val lat: Double? = null,
    val lon: Double? = null,
    val timezone: String? = null,
    val current: OwmCurrentDataDto? = null // 👈 对应 JSON 里的 "current"
)

data class OwmCurrentDataDto(
    val dt: Long? = null,
    val uvi: Double? = null, // 👈 终于找到你！这就是我们要的 UV 指数
    val temp: Double? = null
)

interface OpenWeatherApiService {
    @GET("onecall") // 完整路径会变成 https://api.openweathermap.org/data/3.0/onecall
    suspend fun getCurrentUv(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("exclude") exclude: String = "minutely,hourly,daily,alerts",
        // 这里需要你在 local.properties 里配置 OWM_API_KEY
        @Query("appid") apiKey: String = BuildConfig.OWM_API_KEY
    ): OwmOneCallResponseDto
}

object OpenWeatherApiClient {
    val service: OpenWeatherApiService by lazy {
        Retrofit.Builder()
            // 绑定 OpenWeatherMap 的基础 URL
            .baseUrl("https://api.openweathermap.org/data/3.0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenWeatherApiService::class.java)
    }
}
