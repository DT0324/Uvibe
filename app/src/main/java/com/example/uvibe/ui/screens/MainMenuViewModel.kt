package com.example.uvibe.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uvibe.network.OnboardingRemoteContent
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
}