package com.example.uvibe.ui.screens

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uvibe.network.OnboardingRemoteContent
import com.example.uvibe.network.OpenWeatherApiClient
import com.example.uvibe.network.RecommendationContentUi
import com.example.uvibe.ui.model.AwarenessChartUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
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

    // 缓存当前的经纬度，方便手动刷新时复用
    private var currentLat: Double? = null
    private var currentLon: Double? = null

    // 用于管理定时轮询的协程任务
    private var pollingJob: Job? = null

    init {
        // 初始化时先加载静态图表数据，推荐数据等定位拿到后再加载
        fetchAwareness()
    }

    fun loadAllData() {
        fetchRecommendation() // 降级方案，不带定位的默认请求
        fetchAwareness()
    }

    /**
     * 开启实时更新：保存坐标，并启动定时轮询
     */
    fun startRealtimeUpdates(lat: Double, lon: Double) {
        currentLat = lat
        currentLon = lon

        // 取消之前的轮询任务，防止重复开启
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                fetchDataWithStoredLocation()
                // 每 10 分钟 (600,000 毫秒) 自动刷新一次，可根据需求调整
                delay(600_000)
            }
        }
    }

    /**
     * 供 UI 层的 onRetry/onRefresh 手动调用
     */
    fun refreshCurrentLocationData() {
        if (currentLat != null && currentLon != null) {
            // 重新触发轮询（立即刷新并重新开始计时）
            startRealtimeUpdates(currentLat!!, currentLon!!)
        } else {
            loadAllData()
        }
    }

    private suspend fun fetchDataWithStoredLocation() {
        _recommendationState.value = PageSectionState.Loading

        val lat = currentLat ?: return
        val lon = currentLon ?: return

        val result = runCatching {
            // 1. 去 OpenWeatherMap 查最新的 UV 指数
            val owmResponse = OpenWeatherApiClient.service.getCurrentUv(lat, lon)
            val realUvIndex = owmResponse.current?.uvi?.toInt() ?: 0

            // 2. 拿着最新的 UV 指数，去 AWS 请求穿搭推荐
            OnboardingRemoteContent.loadRecommendationContent(uvIndex = realUvIndex)
        }

        _recommendationState.value = result.fold(
            onSuccess = { PageSectionState.Success(it) },
            onFailure = { PageSectionState.Error(it.toUserMessage()) }
        )
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
}