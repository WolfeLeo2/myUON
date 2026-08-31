package com.wolfeleo2.myuon.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wolfeleo2.myuon.data.model.AvatarConfig
import com.wolfeleo2.myuon.data.model.AvatarProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val DARK_MODE = stringPreferencesKey("dark_mode") // "SYSTEM", "LIGHT", "DARK"
        val AVATAR_PROVIDER = stringPreferencesKey("avatar_provider")
        val AVATAR_STYLE = stringPreferencesKey("avatar_style")
        val LAST_SYNC_TIMESTAMP = stringPreferencesKey("last_sync_timestamp")
        val ACTIVE_STUDENT_REG_NO = stringPreferencesKey("active_student_reg_no")
    }

    val lastSyncTimestamp: Flow<String?> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] }

    val useDynamicColor: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.USE_DYNAMIC_COLOR] ?: true }

    val darkMode: Flow<Boolean?> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
            when (preferences[PreferencesKeys.DARK_MODE]) {
                "LIGHT" -> false
                "DARK" -> true
                else -> null // System default
            }
        }

    val avatarConfig: Flow<AvatarConfig> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
            val providerStr = preferences[PreferencesKeys.AVATAR_PROVIDER]
            val provider = providerStr?.let { runCatching { AvatarProvider.valueOf(it) }.getOrNull() }
                ?: AvatarProvider.DICEBEAR_PORTRAITS
            val style = preferences[PreferencesKeys.AVATAR_STYLE] ?: "notionists"
            AvatarConfig(provider = provider, style = style)
        }

    val activeStudentRegNo: Flow<String?> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[PreferencesKeys.ACTIVE_STUDENT_REG_NO] }

    suspend fun setLastSyncTimestamp(timestamp: String?) {
        context.dataStore.edit { preferences ->
            if (timestamp != null) {
                preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] = timestamp
            } else {
                preferences.remove(PreferencesKeys.LAST_SYNC_TIMESTAMP)
            }
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setDarkMode(dark: Boolean?) {
        context.dataStore.edit { preferences ->
            val value = when (dark) {
                true -> "DARK"
                false -> "LIGHT"
                null -> "SYSTEM"
            }
            preferences[PreferencesKeys.DARK_MODE] = value
        }
    }

    suspend fun setAvatarConfig(config: AvatarConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AVATAR_PROVIDER] = config.provider.name
            preferences[PreferencesKeys.AVATAR_STYLE] = config.style
        }
    }

    suspend fun setActiveStudentRegNo(regNo: String?) {
        context.dataStore.edit { preferences ->
            if (regNo != null) {
                preferences[PreferencesKeys.ACTIVE_STUDENT_REG_NO] = regNo
            } else {
                preferences.remove(PreferencesKeys.ACTIVE_STUDENT_REG_NO)
            }
        }
    }
}
