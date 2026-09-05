package dtv.mobile.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * 卡片通用配色的唯一出处：斗鱼/虎牙/抖音/B站卡片、首页卡片、加载提示等
 * 都从这里取「卡片底色 / 描边色」，避免同一组颜色在多个文件里重复硬编码。
 *
 * 深色模式使用冷色深色盘（与 theme/Colors.kt 的 NightBgTertiary/NightBgSecondary 对齐），
 * 保证「卡片不比背景更蓝、不发灰」的层次关系。
 */
object DtvCardDefaults {

  /** 判断当前是否深色主题（以背景亮度为准，与各卡片原逻辑一致）。 */
  @Composable
  fun isDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

  /** 平台页网格卡片底色（StreamerCard）。 */
  @Composable
  fun gridCardColor(isDark: Boolean = isDarkTheme()): Color =
    if (isDark) Color(0xFF1A2230) else MaterialTheme.colorScheme.surface

  /** 首页卡片底色（HomeStreamerCard，比网格卡片略浅一档）。 */
  @Composable
  fun homeCardColor(isDark: Boolean = isDarkTheme()): Color =
    if (isDark) Color(0xFF121720) else MaterialTheme.colorScheme.surface

  /** 卡片描边色。 */
  @Composable
  fun cardBorderColor(isDark: Boolean = isDarkTheme()): Color =
    if (isDark) Color.White.copy(alpha = 0.10f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)

  /** 卡片内次要文字颜色（主播名等）。 */
  fun secondaryTextColor(isDark: Boolean): Color =
    if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

  /** 未开播/离线遮罩色。 */
  val offlineOverlayColor: Color = Color(0xFF9CA3AF).copy(alpha = 0.35f)
}
