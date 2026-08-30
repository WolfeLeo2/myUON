package com.wolfeleo2.myuon.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import com.wolfeleo2.myuon.data.model.AvatarConfig
import com.wolfeleo2.myuon.data.model.AvatarProvider
import com.wolfeleo2.myuon.data.model.StudentProfile

val LocalStudentProfile = staticCompositionLocalOf<StudentProfile?> { null }

val LocalAvatarConfig = staticCompositionLocalOf { AvatarConfig(AvatarProvider.DICEBEAR_PORTRAITS, "notionists") }

data class UonGlobalActions(
    val onProfileClick: (() -> Unit)? = null,
    val onNotificationsClick: (() -> Unit)? = null
)

val LocalUonGlobalActions = staticCompositionLocalOf { UonGlobalActions() }
