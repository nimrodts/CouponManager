package com.nimroddayan.clipit.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by
        preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {
    private val SELECTED_CURRENCY = stringPreferencesKey("selected_currency")
    private val SORT_OPTION = stringPreferencesKey("sort_option")
    private val SMS_SENDER_WHITELIST = stringSetPreferencesKey("sms_sender_whitelist")

    val sortOption: Flow<String?> =
            context.userPreferencesDataStore.data.map { preferences -> preferences[SORT_OPTION] }

    val selectedCurrency: Flow<String> =
            context.userPreferencesDataStore.data.map { preferences ->
                preferences[SELECTED_CURRENCY] ?: "ILS"
            }

    val smsSenderWhitelist: Flow<Set<String>> =
            context.userPreferencesDataStore.data.map { preferences ->
                preferences[SMS_SENDER_WHITELIST] ?: emptySet()
            }

    suspend fun saveSortOption(sortOption: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[SORT_OPTION] = sortOption
        }
    }

    suspend fun saveSelectedCurrency(currencyCode: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[SELECTED_CURRENCY] = currencyCode
        }
    }

    suspend fun addSenderToWhitelist(sender: String) {
        context.userPreferencesDataStore.edit { preferences ->
            val current = preferences[SMS_SENDER_WHITELIST] ?: emptySet()
            preferences[SMS_SENDER_WHITELIST] = current + sender
        }
    }

    suspend fun removeSenderFromWhitelist(sender: String) {
        context.userPreferencesDataStore.edit { preferences ->
            val current = preferences[SMS_SENDER_WHITELIST] ?: emptySet()
            preferences[SMS_SENDER_WHITELIST] = current - sender
        }
    }
}
