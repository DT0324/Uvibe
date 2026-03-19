package com.example.uvibe.ui.screens

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.uvibe.network.OnboardingRemoteContent
import com.example.uvibe.network.OpenWeatherApiClient
import com.example.uvibe.network.RecommendationContentUi
import com.example.uvibe.ui.model.AwarenessChartUiModel
import com.example.uvibe.ui.model.SunscreenReminderUiModel
import com.example.uvibe.ui.model.UvForecastUiModel
import com.example.uvibe.ui.model.uvRiskLevelFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface PageSectionState<out T> {
    data object Loading : PageSectionState<Nothing>
    data class Success<T>(val content: T) : PageSectionState<T>
    data class Error(val message: String) : PageSectionState<Nothing>
}

class MainMenuViewModel(application: Application) : AndroidViewModel(application) {
    private val fixedPreAlertMinutes = 10

    private val _recommendationState = MutableStateFlow<PageSectionState<RecommendationContentUi>>(PageSectionState.Loading)
    val recommendationState: StateFlow<PageSectionState<RecommendationContentUi>> = _recommendationState.asStateFlow()

    private val _awarenessState = MutableStateFlow<PageSectionState<List<AwarenessChartUiModel>>>(PageSectionState.Loading)
    val awarenessState: StateFlow<PageSectionState<List<AwarenessChartUiModel>>> = _awarenessState.asStateFlow()

    private val _sunscreenReminderState = MutableStateFlow(SunscreenReminderUiModel())
    val sunscreenReminderState: StateFlow<SunscreenReminderUiModel> = _sunscreenReminderState.asStateFlow()

    // 缓存当前的经纬度，方便手动刷新时复用
    private var currentLat: Double? = null
    private var currentLon: Double? = null

    // 用于管理定时轮询的协程任务
    private var pollingJob: Job? = null

    init {
        // 初始化时先加载静态图表数据，推荐数据等定位拿到后再加载
        _sunscreenReminderState.value = loadSunscreenReminder()
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

    fun setSunscreenReminder(
        reminderIntervalMinutes: Int,
        startAtMillis: Long = System.currentTimeMillis(),
    ) {
        val safeIntervalMinutes = reminderIntervalMinutes.coerceAtLeast(11)

        val state = SunscreenReminderUiModel(
            isEnabled = true,
            reminderIntervalMinutes = safeIntervalMinutes,
            preAlertMinutes = fixedPreAlertMinutes,
            lastAppliedAtMillis = startAtMillis,
            nextReminderAtMillis = startAtMillis + safeIntervalMinutes * 60_000L,
        )
        _sunscreenReminderState.value = state
        saveSunscreenReminder(state)
    }

    fun clearSunscreenReminder() {
        _sunscreenReminderState.value = SunscreenReminderUiModel()
        saveSunscreenReminder(SunscreenReminderUiModel())
    }

    fun refreshSunscreenReminder() {
        _sunscreenReminderState.value = loadSunscreenReminder()
    }

    private suspend fun fetchDataWithStoredLocation() {
        _recommendationState.value = PageSectionState.Loading

        val lat = currentLat ?: return
        val lon = currentLon ?: return

        val result = runCatching {
            // 1. 获取当前所在的 Suburb (例如: Spotswood)
            val locationName = getSuburbName(lat, lon)

            // 2. 去 OpenWeatherMap 查最新的当前 UV 和未来预报 (hourly)
            val owmResponse = OpenWeatherApiClient.service.getCurrentUv(lat, lon)
            val realUvIndex = owmResponse.current?.uvi?.toInt() ?: 0

            // 3. 解析预测数据 (去掉第1个也就是现在的时间，取接下来的 5 个小时)
            val forecastList = owmResponse.hourly
                ?.drop(1)
                ?.take(5)
                ?.map { hourlyData ->
                    val uvi = hourlyData.uvi?.toInt() ?: 0
                    UvForecastUiModel(
                        time = formatUnixTime(hourlyData.dt ?: 0L),
                        uvIndex = uvi,
                        levelText = uvRiskLevelFor(uvi)
                    )
                } ?: emptyList()

            // 4. 去 AWS 请求穿搭推荐和防护建议 (保留你原有的逻辑)
            val recommendationContent = OnboardingRemoteContent.loadRecommendationContent(uvIndex = realUvIndex)

            // 5. ⭐️ 核心拼接：把 OWM 的真实数据和 AWS 的推荐数据组合起来！
            val updatedStatus = recommendationContent.status.copy(
                uvIndex = realUvIndex,
                levelText = uvRiskLevelFor(realUvIndex).label, // 👈 换成这个
                riskLevel = uvRiskLevelFor(realUvIndex),
                locationName = locationName,                       // 注入 Suburb
                forecast = forecastList                            // 注入预测列表
            )

            // 返回组装好的最终 UI 状态
            recommendationContent.copy(status = updatedStatus)
        }

        _recommendationState.value = result.fold(
            onSuccess = { PageSectionState.Success(it) },
            onFailure = { PageSectionState.Error(it.toUserMessage()) }
        )
    }

    // --- 新增的辅助方法 ---

    /**
     * 将经纬度转换为 Suburb 名称 (运行在 IO 线程防止卡顿)
     */
    private suspend fun getSuburbName(lat: Double, lon: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(getApplication(), Locale.getDefault())
                // 获取最多 1 个匹配地址
                val addresses = geocoder.getFromLocation(lat, lon, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    // subLocality 通常是 Suburb (如 Spotswood), locality 通常是 City (如 Melbourne)
                    address.subLocality ?: address.locality ?: "Current Location"
                } else {
                    "Current Location"
                }
            } catch (e: Exception) {
                // 如果没有网络或者 Geocoder 服务不可用，返回一个默认值
                "Current Location"
            }
        }
    }

    /**
     * 将 OpenWeatherMap 的 Unix 时间戳转换为 "2 PM" 格式
     */
    private fun formatUnixTime(dt: Long): String {
        if (dt == 0L) return ""
        // OWM 返回的是秒，Java 的 Date 需要毫秒，所以乘 1000
        val date = Date(dt * 1000)
        // "h a" 代表 12小时制 + AM/PM
        val format = SimpleDateFormat("h a", Locale.getDefault())
        return format.format(date)
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

    private fun saveSunscreenReminder(state: SunscreenReminderUiModel) {
        reminderPrefs().edit()
            .putBoolean(KEY_ENABLED, state.isEnabled)
            .putInt(KEY_INTERVAL_MINUTES, state.reminderIntervalMinutes)
            .putInt(KEY_PRE_ALERT_MINUTES, state.preAlertMinutes)
            .putLong(KEY_LAST_APPLIED_AT, state.lastAppliedAtMillis ?: -1L)
            .putLong(KEY_NEXT_REMINDER_AT, state.nextReminderAtMillis ?: -1L)
            .apply()
    }

    private fun loadSunscreenReminder(): SunscreenReminderUiModel {
        val prefs = reminderPrefs()
        val lastAppliedAt = prefs.getLong(KEY_LAST_APPLIED_AT, -1L).takeIf { it > 0L }
        val nextReminderAt = prefs.getLong(KEY_NEXT_REMINDER_AT, -1L).takeIf { it > 0L }

        val state = SunscreenReminderUiModel(
            isEnabled = prefs.getBoolean(KEY_ENABLED, false),
            reminderIntervalMinutes = prefs.getInt(KEY_INTERVAL_MINUTES, 120),
            preAlertMinutes = fixedPreAlertMinutes,
            lastAppliedAtMillis = lastAppliedAt,
            nextReminderAtMillis = nextReminderAt,
        )

        return if (state.nextReminderAtMillis != null && state.nextReminderAtMillis <= System.currentTimeMillis()) {
            SunscreenReminderUiModel()
        } else {
            state
        }
    }

    private fun reminderPrefs() =
        getApplication<Application>().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private companion object {
        const val PREF_NAME = "uvibe_sunscreen_reminder"
        const val KEY_ENABLED = "enabled"
        const val KEY_INTERVAL_MINUTES = "interval_minutes"
        const val KEY_PRE_ALERT_MINUTES = "pre_alert_minutes"
        const val KEY_LAST_APPLIED_AT = "last_applied_at"
        const val KEY_NEXT_REMINDER_AT = "next_reminder_at"
    }
}
