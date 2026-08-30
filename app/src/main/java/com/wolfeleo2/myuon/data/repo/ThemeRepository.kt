package com.wolfeleo2.myuon.data.repo

import com.wolfeleo2.myuon.data.model.AvatarConfig
import com.wolfeleo2.myuon.data.preferences.UserPreferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepository @Inject constructor(
    private val preferencesDataStore: UserPreferencesDataStore
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val useDynamicColor: StateFlow<Boolean> = preferencesDataStore.useDynamicColor
        .stateIn(scope, SharingStarted.Eagerly, true)

    val isDarkMode: StateFlow<Boolean?> = preferencesDataStore.darkMode
        .stateIn(scope, SharingStarted.Eagerly, null)

    val avatarConfig: StateFlow<AvatarConfig> = preferencesDataStore.avatarConfig
        .stateIn(scope, SharingStarted.Eagerly, AvatarConfig())

    fun setDynamicColor(enabled: Boolean) {
        scope.launch {
            preferencesDataStore.setDynamicColor(enabled)
        }
    }

    fun setDarkMode(dark: Boolean?) {
        scope.launch {
            preferencesDataStore.setDarkMode(dark)
        }
    }

    fun setAvatarConfig(config: AvatarConfig) {
        scope.launch {
            preferencesDataStore.setAvatarConfig(config)
        }
    }
}
