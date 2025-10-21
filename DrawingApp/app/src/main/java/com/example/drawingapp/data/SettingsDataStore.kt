package com.example.drawingapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class UserSettings(
    val cloudBackupEnabled: Boolean = false,
    val syncEnabled: Boolean = false,
    val userAccountName: String = "Guest User"
)

class SettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val CLOUD_BACKUP_ENABLED = booleanPreferencesKey("cloud_backup_enabled")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        val USER_ACCOUNT_NAME = stringPreferencesKey("user_account_name")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserSettings(
                cloudBackupEnabled = preferences[PreferencesKeys.CLOUD_BACKUP_ENABLED] ?: false,
                syncEnabled = preferences[PreferencesKeys.SYNC_ENABLED] ?: false,
                userAccountName = preferences[PreferencesKeys.USER_ACCOUNT_NAME] ?: "Guest User"
            )
        }

    suspend fun updateCloudBackup(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLOUD_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun updateSync(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYNC_ENABLED] = enabled
        }
    }

    suspend fun updateUserAccountName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ACCOUNT_NAME] = name
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsDataStore? = null

        fun getInstance(context: Context): SettingsDataStore {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsDataStore(context)
                INSTANCE = instance
                instance
            }
        }
    }
}