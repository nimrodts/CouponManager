package com.nimroddayan.couponmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nimroddayan.couponmanager.data.gemini.GeminiApiKeyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val geminiApiKeyRepository: GeminiApiKeyRepository) : ViewModel() {

    val geminiApiKey: StateFlow<String> = geminiApiKeyRepository.getApiKey
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    fun saveGeminiApiKey(apiKey: String) {
        viewModelScope.launch {
            geminiApiKeyRepository.saveApiKey(apiKey)
        }
    }
}
