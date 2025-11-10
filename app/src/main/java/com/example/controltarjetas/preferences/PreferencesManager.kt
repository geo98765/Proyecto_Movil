package com.example.controltarjetas.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val DAYS_BEFORE_KEY = booleanPreferencesKey("days_before_3")
        private val FILTRO_FECHA_KEY = stringPreferencesKey("filtro_fecha")
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
    }

    val daysBeforeFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        if (preferences[DAYS_BEFORE_KEY] == true) 3 else 5
    }

    val filtroFechaFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[FILTRO_FECHA_KEY] ?: "PROXIMAS_3_SEMANAS"
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setDaysBefore(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[DAYS_BEFORE_KEY] = (days == 3)
        }
    }

    suspend fun setFiltroFecha(filtro: String) {
        context.dataStore.edit { preferences ->
            preferences[FILTRO_FECHA_KEY] = filtro
        }
    }
}