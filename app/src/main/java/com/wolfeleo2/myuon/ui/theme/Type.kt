package com.wolfeleo2.myuon.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.wolfeleo2.myuon.R

/**
 * Google Sans Flex variable font mapping for myUON.
 */
val GoogleSansFlex =
    FontFamily(
        Font(R.font.google_sans_flex, FontWeight.Light),
        Font(R.font.google_sans_flex, FontWeight.Normal),
        Font(R.font.google_sans_flex, FontWeight.Medium),
        Font(R.font.google_sans_flex, FontWeight.SemiBold),
        Font(R.font.google_sans_flex, FontWeight.Bold),
    )

private val M3 = Typography()

private fun TextStyle.brand(): TextStyle = copy(fontFamily = GoogleSansFlex)

/**
 * Full Material 3 Expressive typography scale with emphasized roles.
 */
val MyUonTypography =
    M3.copy(
        displayLarge = M3.displayLarge.brand(),
        displayMedium = M3.displayMedium.brand(),
        displaySmall = M3.displaySmall.brand(),
        headlineLarge = M3.headlineLarge.brand(),
        headlineMedium = M3.headlineMedium.brand(),
        headlineSmall = M3.headlineSmall.brand(),
        titleLarge = M3.titleLarge.brand(),
        titleMedium = M3.titleMedium.brand(),
        titleSmall = M3.titleSmall.brand(),
        bodyLarge = M3.bodyLarge.brand(),
        bodyMedium = M3.bodyMedium.brand(),
        bodySmall = M3.bodySmall.brand(),
        labelLarge = M3.labelLarge.brand(),
        labelMedium = M3.labelMedium.brand(),
        labelSmall = M3.labelSmall.brand(),
        displayLargeEmphasized = M3.displayLargeEmphasized.brand(),
        displayMediumEmphasized = M3.displayMediumEmphasized.brand(),
        displaySmallEmphasized = M3.displaySmallEmphasized.brand(),
        headlineLargeEmphasized = M3.headlineLargeEmphasized.brand(),
        headlineMediumEmphasized = M3.headlineMediumEmphasized.brand(),
        headlineSmallEmphasized = M3.headlineSmallEmphasized.brand(),
        titleLargeEmphasized = M3.titleLargeEmphasized.brand(),
        titleMediumEmphasized = M3.titleMediumEmphasized.brand(),
        titleSmallEmphasized = M3.titleSmallEmphasized.brand(),
        bodyLargeEmphasized = M3.bodyLargeEmphasized.brand(),
        bodyMediumEmphasized = M3.bodyMediumEmphasized.brand(),
        bodySmallEmphasized = M3.bodySmallEmphasized.brand(),
        labelLargeEmphasized = M3.labelLargeEmphasized.brand(),
        labelMediumEmphasized = M3.labelMediumEmphasized.brand(),
        labelSmallEmphasized = M3.labelSmallEmphasized.brand(),
    )
