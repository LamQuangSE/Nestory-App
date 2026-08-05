package com.example.nestory.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nestory.domain.model.ExpiryReminderSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.expiryReminderDataStore by preferencesDataStore(
    name = "expiry_reminder_settings",
)

class ExpiryReminderSettingsRepository(
    context: Context,
) {
    private val dataStore = context.applicationContext.expiryReminderDataStore

    val settings: Flow<ExpiryReminderSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            ExpiryReminderSettings(
                enabled = preferences[Keys.Enabled] ?: true,
                leadTimeDays = preferences[Keys.LeadTimeDays] ?: 7,
                repeatDaily = preferences[Keys.RepeatDaily] ?: true,
                inAppEnabled = preferences[Keys.InAppEnabled] ?: true,
                emailEnabled = preferences[Keys.EmailEnabled] ?: false,
                pushEnabled = preferences[Keys.PushEnabled] ?: true,
            )
        }

    suspend fun updateSettings(settings: ExpiryReminderSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.Enabled] = settings.enabled
            preferences[Keys.LeadTimeDays] = settings.leadTimeDays
            preferences[Keys.RepeatDaily] = settings.repeatDaily
            preferences[Keys.InAppEnabled] = settings.inAppEnabled
            preferences[Keys.EmailEnabled] = settings.emailEnabled
            preferences[Keys.PushEnabled] = settings.pushEnabled
        }
    }

    private object Keys {
        val Enabled = booleanPreferencesKey("enabled")
        val LeadTimeDays = intPreferencesKey("lead_time_days")
        val RepeatDaily = booleanPreferencesKey("repeat_daily")
        val InAppEnabled = booleanPreferencesKey("in_app_enabled")
        val EmailEnabled = booleanPreferencesKey("email_enabled")
        val PushEnabled = booleanPreferencesKey("push_enabled")
    }
}
