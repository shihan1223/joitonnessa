package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "joitonnessa_preferences")

data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val userName: String = "User",
    val language: String = "bn", // Bengali default or English
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val snoozeMinutes: Int = 10,
    val lockScreenDetails: Boolean = true,
    val batteryNoticeDismissed: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val KEY_SNOOZE_MINUTES = intPreferencesKey("snooze_minutes")
        val KEY_LOCK_SCREEN_DETAILS = booleanPreferencesKey("lock_screen_details")
        val KEY_BATTERY_NOTICE_DISMISSED = booleanPreferencesKey("battery_notice_dismissed")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            onboardingCompleted = prefs[KEY_ONBOARDING_COMPLETED] ?: false,
            userName = prefs[KEY_USER_NAME] ?: "User",
            language = prefs[KEY_LANGUAGE] ?: "bn",
            themeMode = prefs[KEY_THEME_MODE] ?: "SYSTEM",
            soundEnabled = prefs[KEY_SOUND_ENABLED] ?: true,
            vibrationEnabled = prefs[KEY_VIBRATION_ENABLED] ?: true,
            snoozeMinutes = prefs[KEY_SNOOZE_MINUTES] ?: 10,
            lockScreenDetails = prefs[KEY_LOCK_SCREEN_DETAILS] ?: true,
            batteryNoticeDismissed = prefs[KEY_BATTERY_NOTICE_DISMISSED] ?: false
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { it[KEY_USER_NAME] = name }
    }

    suspend fun setLanguage(lang: String) {
        dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun setThemeMode(theme: String) {
        dataStore.edit { it[KEY_THEME_MODE] = theme }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_VIBRATION_ENABLED] = enabled }
    }

    suspend fun setSnoozeMinutes(minutes: Int) {
        dataStore.edit { it[KEY_SNOOZE_MINUTES] = minutes }
    }

    suspend fun setLockScreenDetails(allowed: Boolean) {
        dataStore.edit { it[KEY_LOCK_SCREEN_DETAILS] = allowed }
    }

    suspend fun setBatteryNoticeDismissed(dismissed: Boolean) {
        dataStore.edit { it[KEY_BATTERY_NOTICE_DISMISSED] = dismissed }
    }
}
