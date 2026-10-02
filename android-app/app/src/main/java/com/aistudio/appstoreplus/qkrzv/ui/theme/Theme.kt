package com.aistudio.appstoreplus.qkrzv.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppStoreColorScheme = darkColorScheme(
  primary = PrimaryBlue,
  onPrimary = TextPrimary,
  primaryContainer = CardElevated,
  onPrimaryContainer = TextPrimary,
  secondary = AccentPurple,
  onSecondary = TextPrimary,
  background = BackgroundDark,
  onBackground = TextPrimary,
  surface = SurfaceDark,
  onSurface = TextPrimary,
  surfaceVariant = CardDark,
  onSurfaceVariant = TextSecondary,
  outline = CardBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = AppStoreColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = BackgroundDark.toArgb()
        window.navigationBarColor = BackgroundDark.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
