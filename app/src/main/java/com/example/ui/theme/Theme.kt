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

private val RockDarkColorScheme = darkColorScheme(
  primary = RockPrimary,
  secondary = RockSecondary,
  tertiary = RockTertiary,
  background = RockBackground,
  surface = RockSurface,
  surfaceVariant = RockSurfaceVariant,
  onPrimary = androidx.compose.ui.graphics.Color.White,
  onSecondary = androidx.compose.ui.graphics.Color.White,
  onTertiary = androidx.compose.ui.graphics.Color.White,
  onBackground = RockOnBackground,
  onSurface = RockOnSurface,
  error = androidx.compose.ui.graphics.Color(0xFFCF6679)
)

private val RockLightColorScheme = lightColorScheme(
  primary = RockPrimary,
  secondary = RockSecondary,
  tertiary = RockTertiary,
  background = androidx.compose.ui.graphics.Color(0xFFFAFAFC),
  surface = androidx.compose.ui.graphics.Color.White,
  surfaceVariant = androidx.compose.ui.graphics.Color(0xFFECEFF1),
  onPrimary = androidx.compose.ui.graphics.Color.White,
  onSecondary = androidx.compose.ui.graphics.Color.White,
  onTertiary = androidx.compose.ui.graphics.Color.White,
  onBackground = androidx.compose.ui.graphics.Color(0xFF1C1D24),
  onSurface = androidx.compose.ui.graphics.Color(0xFF2E303B),
  error = androidx.compose.ui.graphics.Color(0xFFB00020)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force premium dark rock theme by default
  dynamicColor: Boolean = false, // Disable dynamic material-you to enforce our stunning rock theme
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) RockDarkColorScheme else RockLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

