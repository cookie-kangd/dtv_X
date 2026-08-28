package dtv.mobile.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import dtv.mobile.state.ThemeMode
import dtv.mobile.ui.system.SystemBarsEffect

@Immutable
data class DtvExtras(
  val accentGradient: Brush,
)

private val LocalExtras = staticCompositionLocalOf<DtvExtras> {
  error("DtvExtras not provided")
}

val MaterialTheme.dtvExtras: DtvExtras
  @Composable get() = LocalExtras.current

private fun parseHexColor(hex: String, fallback: Color): Color {
  val value = hex.trim().removePrefix("#")
  if (value.length != 6 && value.length != 8) return fallback
  val raw = value.toLongOrNull(16) ?: return fallback
  return if (value.length == 8) {
    Color(raw)
  } else {
    Color(0xFF000000L or raw)
  }
}

private fun dayScheme(accent: Color): ColorScheme = lightColorScheme(
  primary = accent,
  onPrimary = DtvColors.DayTextPrimary,
  secondary = DtvColors.DayBgTertiary,
  onSecondary = DtvColors.DayTextPrimary,
  background = DtvColors.DayBgPrimary,
  onBackground = DtvColors.DayTextPrimary,
  surface = DtvColors.DayBgSecondary,
  onSurface = DtvColors.DayTextPrimary,
  outline = DtvColors.DayBorder,
)

private fun nightScheme(accent: Color): ColorScheme = darkColorScheme(
  primary = accent,
  onPrimary = DtvColors.NightTextPrimary,
  secondary = DtvColors.NightBgTertiary,
  onSecondary = DtvColors.NightTextPrimary,
  background = DtvColors.NightBgPrimary,
  onBackground = DtvColors.NightTextPrimary,
  surface = DtvColors.NightBgSecondary,
  onSurface = DtvColors.NightTextPrimary,
  outline = DtvColors.NightBorder,
)

@Composable
fun DtvTheme(
  themeMode: ThemeMode,
  accentColorHex: String = "",
  content: @Composable () -> Unit,
) {
  val dark = when (themeMode) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Dark -> true
    ThemeMode.Light -> false
  }
  val accent = parseHexColor(accentColorHex, DtvColors.HubAccent)
  val scheme = if (dark) nightScheme(accent) else dayScheme(accent)

  SystemBarsEffect(darkTheme = dark)

  // Hover/light variant derived from the user accent so gradients stay in tone.
  val accentHover = lerp(accent, if (dark) Color.White else Color(0xFF0B0B0B), 0.12f)
  val accentGradient = Brush.linearGradient(
    colors = listOf(accent, accentHover),
  )

  androidx.compose.runtime.CompositionLocalProvider(
    LocalExtras provides DtvExtras(accentGradient = accentGradient),
  ) {
    MaterialTheme(
      colorScheme = scheme,
      content = content,
    )
  }
}
