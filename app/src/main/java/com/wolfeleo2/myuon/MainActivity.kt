package com.wolfeleo2.myuon

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.wolfeleo2.myuon.data.repo.AcademicRepository
import com.wolfeleo2.myuon.data.repo.AuthRepository
import com.wolfeleo2.myuon.data.repo.ThemeRepository
import com.wolfeleo2.myuon.ui.navigation.MyUonNavDisplay
import com.wolfeleo2.myuon.ui.navigation.rememberNavigator
import com.wolfeleo2.myuon.ui.theme.MyUONTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var academicRepository: AcademicRepository

    @Inject
    lateinit var themeRepository: ThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val useDynamicColor by themeRepository.useDynamicColor.collectAsState()
            val isDarkMode by themeRepository.isDarkMode.collectAsState()
            val systemDark = isSystemInDarkTheme()

            // The app's effective dark state — user override if set, otherwise the system's.
            val appIsDark = isDarkMode ?: systemDark

            // System bar icons follow the APP's theme, not the device's. Without this,
            // app-light-on-device-dark leaves white icons on a light surface.
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !appIsDark
                        isAppearanceLightNavigationBars = !appIsDark
                    }
                }
            }

            MyUONTheme(
                darkTheme = appIsDark,
                useDynamicColor = useDynamicColor
            ) {
                val navigator = rememberNavigator(authRepository)
                MyUonNavDisplay(
                    navigator = navigator,
                    authRepository = authRepository,
                    academicRepository = academicRepository,
                    themeRepository = themeRepository
                )
            }
        }
    }
}