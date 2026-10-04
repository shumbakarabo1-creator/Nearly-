package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryDark,
    onPrimary = IndigoOnPrimaryDark,
    primaryContainer = IndigoPrimaryContainerDark,
    onPrimaryContainer = IndigoOnPrimaryContainerDark,
    secondary = TealSecondaryDark,
    onSecondary = TealOnSecondaryDark,
    secondaryContainer = TealSecondaryContainerDark,
    onSecondaryContainer = TealOnSecondaryContainerDark,
    tertiary = AmberTertiaryDark,
    onTertiary = AmberOnTertiaryDark,
    background = SlateBackgroundDark,
    onBackground = SlateOnBackgroundDark,
    surface = SlateSurfaceDark,
    onSurface = SlateOnSurfaceDark,
    surfaceVariant = SlateSurfaceVariantDark,
    onSurfaceVariant = SlateOnSurfaceVariantDark,
    outline = SlateOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimaryLight,
    onPrimary = IndigoOnPrimaryLight,
    primaryContainer = IndigoPrimaryContainerLight,
    onPrimaryContainer = IndigoOnPrimaryContainerLight,
    secondary = TealSecondaryLight,
    onSecondary = TealOnSecondaryLight,
    secondaryContainer = TealSecondaryContainerLight,
    onSecondaryContainer = TealOnSecondaryContainerLight,
    tertiary = AmberTertiaryLight,
    onTertiary = AmberOnTertiaryLight,
    background = SlateBackgroundLight,
    onBackground = SlateOnBackgroundLight,
    surface = SlateSurfaceLight,
    onSurface = SlateOnSurfaceLight,
    surfaceVariant = SlateSurfaceVariantLight,
    onSurfaceVariant = SlateOnSurfaceVariantLight,
    outline = SlateOutlineLight
)

@Composable
fun NearlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false by default for brand consistency
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
