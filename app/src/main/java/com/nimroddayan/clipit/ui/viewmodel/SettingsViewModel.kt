package com.nimroddayan.clipit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nimroddayan.clipit.data.UserPreferencesRepository
import com.nimroddayan.clipit.data.gemini.GeminiApiKeyRepository
import com.nimroddayan.clipit.data.gemini.GeminiModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
        private val geminiApiKeyRepository: GeminiApiKeyRepository,
        private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

        val selectedCurrency: StateFlow<String> =
                userPreferencesRepository.selectedCurrency.stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = "ILS"
                )

        val geminiApiKey: StateFlow<String> =
                geminiApiKeyRepository.getApiKey.stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = ""
                )

        val geminiModel: StateFlow<GeminiModel> =
                geminiApiKeyRepository.getModel.stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = GeminiModel.GEMINI_FLASH_LATEST
                )

        val geminiTemperature: StateFlow<Float> =
                geminiApiKeyRepository.getTemperature.stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = 0.15f
                )

        suspend fun saveGeminiApiKey(apiKey: String) {
                geminiApiKeyRepository.saveApiKey(apiKey)
        }

        suspend fun saveGeminiModel(model: GeminiModel) {
                geminiApiKeyRepository.saveModel(model)
        }

        suspend fun saveGeminiTemperature(temperature: Float) {
                geminiApiKeyRepository.saveTemperature(temperature)
        }

        fun saveSelectedCurrency(currencyCode: String) {
                viewModelScope.launch {
                        userPreferencesRepository.saveSelectedCurrency(currencyCode)
                }
        }
}
