package com.nimroddayan.clipit.data.gemini

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class GeminiApiKeyRepository(private val context: Context) {
    private val geminiApiKey = stringPreferencesKey("gemini_api_key")
    private val geminiModel = stringPreferencesKey("gemini_model")
    private val geminiTemperature = floatPreferencesKey("gemini_temperature")
    private val detectionMode = stringPreferencesKey("detection_mode")

    val getApiKey: Flow<String> =
            context.dataStore.data.map { preferences -> preferences[geminiApiKey] ?: "" }

    val getModel: Flow<GeminiModel> =
            context.dataStore.data.map { preferences ->
                val modelName = preferences[geminiModel] ?: GeminiModel.GEMINI_FLASH_LATEST.name
                GeminiModel.valueOf(modelName)
            }

    val getTemperature: Flow<Float> =
            context.dataStore.data.map { preferences -> preferences[geminiTemperature] ?: 0.15f }

    val getDetectionMode: Flow<DetectionMode> =
            context.dataStore.data.map { preferences ->
                val modeName = preferences[detectionMode] ?: DetectionMode.LOCAL_HEURISTIC.name
                try {
                    DetectionMode.valueOf(modeName)
                } catch (e: IllegalArgumentException) {
                    DetectionMode.LOCAL_HEURISTIC
                }
            }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { settings -> settings[geminiApiKey] = apiKey }
    }

    suspend fun saveModel(model: GeminiModel) {
        context.dataStore.edit { settings -> settings[geminiModel] = model.name }
    }

    suspend fun saveTemperature(temperature: Float) {
        context.dataStore.edit { settings -> settings[geminiTemperature] = temperature }
    }

    suspend fun saveDetectionMode(mode: DetectionMode) {
        context.dataStore.edit { settings -> settings[detectionMode] = mode.name }
    }
}

enum class DetectionMode(val displayName: String) {
    LOCAL_HEURISTIC("Local Heuristic (LLM)"),
    ONLINE_GEMINI("Online Gemini"),
    REGEX_ONLY("Regex Only"),
    ALWAYS_EXTRACT("Always Extract")
}
