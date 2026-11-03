package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "trading_journal_prefs")

class TradingPreferences(private val context: Context) {

    companion object {
        val KEY_ACTIVE_ACCOUNT_ID = longPreferencesKey("active_account_id")
        val KEY_IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val KEY_IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
        val KEY_PIN_CODE = stringPreferencesKey("pin_code")
        val KEY_CURRENCY = stringPreferencesKey("default_currency")
    }

    val activeAccountId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_ACCOUNT_ID]
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_DARK_MODE] ?: true // Default dark luxury
    }

    val isPinEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_PIN_ENABLED] ?: false
    }

    val pinCode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PIN_CODE] ?: ""
    }

    val defaultCurrency: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CURRENCY] ?: "USD"
    }

    suspend fun setActiveAccountId(id: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_ACCOUNT_ID] = id
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_DARK_MODE] = enabled
        }
    }

    suspend fun setPin(enabled: Boolean, pin: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_PIN_ENABLED] = enabled
            prefs[KEY_PIN_CODE] = pin
        }
    }

    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CURRENCY] = currency
        }
    }
}
