package com.nimroddayan.couponmanager.data.gemini

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class GeminiApiKeyRepository(private val context: Context) {
    private val geminiApiKey = stringPreferencesKey("gemini_api_key")

    val getApiKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[geminiApiKey] ?: ""
        }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { settings ->
            settings[geminiApiKey] = apiKey
        }
    }
}
