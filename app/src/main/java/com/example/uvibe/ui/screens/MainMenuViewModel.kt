package com.example.uvibe.ui.screens

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uvibe.network.OnboardingRemoteContent
import com.example.uvibe.network.OpenWeatherApiClient
import com.example.uvibe.network.RecommendationContentUi
import com.example.uvibe.ui.model.AwarenessChartUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


sealed interface PageSectionState<out T> {
    data object Loading : PageSectionState<Nothing>
    data class Success<T>(val content: T) : PageSectionState<T>
    data class Error(val message: String) : PageSectionState<Nothing>
}

class MainMenuViewModel : ViewModel() {


    private val _recommendationState = MutableStateFlow<PageSectionState<RecommendationContentUi>>(PageSectionState.Loading)
    val recommendationState: StateFlow<PageSectionState<RecommendationContentUi>> = _recommendationState.asStateFlow()

    private val _awarenessState = MutableStateFlow<PageSectionState<List<AwarenessChartUiModel>>>(PageSectionState.Loading)
    val awarenessState: StateFlow<PageSectionState<List<AwarenessChartUiModel>>> = _awarenessState.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        fetchRecommendation()
        fetchAwareness()
    }

    private fun fetchRecommendation() {
        _recommendationState.value = PageSectionState.Loading
        viewModelScope.launch {
            val result = runCatching { OnboardingRemoteContent.loadRecommendationContent() }
            _recommendationState.value = result.fold(
                onSuccess = { PageSectionState.Success(it) },
                onFailure = { PageSectionState.Error(it.toUserMessage()) }
            )
        }
    }

    private fun fetchAwareness() {
        _awarenessState.value = PageSectionState.Loading
        viewModelScope.launch {
            val result = runCatching { OnboardingRemoteContent.loadAwarenessCharts() }
            _awarenessState.value = result.fold(
                onSuccess = { PageSectionState.Success(it) },
                onFailure = { PageSectionState.Error(it.toUserMessage()) }
            )
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is HttpException -> when (code()) {
            400 -> "The request was rejected by the AWS API."
            404 -> "No onboarding data was returned for the current UV lookup."
            else -> "The AWS API returned an error (${code()})."
        }
        is IOException -> "Network error while contacting the AWS API."
        else -> message ?: "Unable to load onboarding data right now."
    }

    @SuppressLint("MissingPermission")
    fun loadDataWithLocation(lat: Double, lon: Double) {
        _recommendationState.value = PageSectionState.Loading

        viewModelScope.launch {
            val result = runCatching {
                // 1. 先去 OpenWeatherMap 查真实的 UV 指数
                val owmResponse = OpenWeatherApiClient.service.getCurrentUv(lat, lon)

                // OWM 返回的是 Double (比如 8.2)，我们向下取整，因为咱们 AWS 数据库里存的是 Int
                val realUvIndex = owmResponse.value?.toInt() ?: 11 // 如果获取失败，用 8 兜底

                // 2. 拿着这个真实的 UV 指数，去你们的 AWS 请求穿搭推荐
                OnboardingRemoteContent.loadRecommendationContent(uvIndex = realUvIndex)
            }

            _recommendationState.value = result.fold(
                onSuccess = { PageSectionState.Success(it) },
                onFailure = { PageSectionState.Error(it.toUserMessage()) }
            )
        }

        // 图表数据不需要 UV 指数，照常加载即可
        fetchAwareness()
    }
}