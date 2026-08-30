package com.wolfeleo2.myuon.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * myUON Material 3 Expressive Theme.
 *
 * Configured with Expressive Motion Scheme (physics spring curves), full 30-token
 * Expressive typography scale, and Android 12+ wallpaper dynamic color.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyUONTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColor -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> UonDarkColorScheme
        else -> UonLightColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = MyUonTypography,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}
