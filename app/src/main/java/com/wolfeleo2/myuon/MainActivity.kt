package com.wolfeleo2.myuon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

            MyUONTheme(
                darkTheme = isDarkMode ?: systemDark,
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
