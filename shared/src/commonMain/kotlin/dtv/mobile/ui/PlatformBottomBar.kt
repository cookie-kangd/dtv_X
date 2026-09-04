package dtv.mobile.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild
import dtv.mobile.model.Platform
import dtv.mobile.state.Screen

/** 底栏浮岛在屏幕上占据的净高度（不含导航条），列表 contentPadding 用它预留空间。 */
val DockContentClearance = 96.dp

@Composable
fun PlatformBottomBar(
  hazeState: HazeState,
  selectedScreen: Screen,
  selectedPlatform: Platform,
  onHomeClick: () -> Unit,
  onPlatformClick: (Platform) -> Unit,
  switchingLoading: Boolean = false,
  // 由「设置-平台设置」决定：只包含已启用的平台，且按用户拖拽后的顺序排列
  platforms: List<Platform> = Platform.entries.filter { it != Platform.Custom },
) {
  val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
  // 毛玻璃 tint：浅色模式用偏白的半透明、深色模式用偏黑的半透明，
  // blur 由 haze 完成后叠加这层色彩，让浮岛在两种模式下都保持可读性。
  val glassTint = if (isDark) {
    Color(0xFF1C1C22).copy(alpha = 0.62f)
  } else {
    Color(0xFFF6F7FB).copy(alpha = 0.66f)
  }
  val glassStyle = HazeStyle(tint = glassTint, blurRadius = 22.dp, noiseFactor = 0.04f)
  val dockShape = RoundedCornerShape(percent = 50)
  val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.28f else 0.22f)
  val activeBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
  // 深色底用亮灰、浅色底用深灰，保证两种模式下的对比度（此前两个值写反了）
  val inactiveIcon = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 10.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(elevation = if (isDark) 6.dp else 14.dp, shape = dockShape, clip = false)
        .hazeChild(hazeState, shape = dockShape, style = glassStyle)
        .border(BorderStroke(1.dp, borderColor), dockShape)
        .padding(vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceAround,
    ) {
      DockItem(
        selected = selectedScreen == Screen.Home,
        label = "首页",
        activeBackground = activeBg,
        onClick = onHomeClick.takeUnless { switchingLoading } ?: {},
      ) {
        Icon(
          imageVector = Icons.Default.Home,
          contentDescription = "首页",
          tint = if (selectedScreen == Screen.Home) MaterialTheme.colorScheme.primary else inactiveIcon,
        )
      }

      platforms.forEach { platform ->
        val isSelected = selectedScreen == Screen.Platform && platform == selectedPlatform
        DockItem(
          selected = isSelected,
          label = platform.title,
          activeBackground = activeBg,
          onClick = { if (!switchingLoading) onPlatformClick(platform) },
        ) {
          val icon = when (platform) {
            Platform.Douyu -> Icons.Default.WaterDrop
            Platform.Huya -> Icons.Default.Pets
            Platform.Douyin -> Icons.Default.MusicNote
            Platform.Bilibili -> Icons.Default.LiveTv
            Platform.Custom -> Icons.Default.Home
          }
          Icon(
            imageVector = icon,
            contentDescription = platform.title,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else inactiveIcon,
          )
        }
      }
    }
  }
}

@Composable
private fun RowScope.DockItem(
  selected: Boolean,
  label: String,
  activeBackground: Color,
  onClick: () -> Unit,
  icon: @Composable () -> Unit,
) {
  val scale = animateFloatAsState(targetValue = if (selected) 1.08f else 1.0f, label = "dockScale").value
  val bgAlpha = animateFloatAsState(targetValue = if (selected) 1.0f else 0.0f, label = "dockBgAlpha").value

  Column(
    modifier = Modifier
      .weight(1f)
      .clickable { onClick() }
      .defaultMinSize(minHeight = 54.dp)
      .padding(vertical = 2.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(3.dp),
  ) {
    androidx.compose.material3.Surface(
      shape = RoundedCornerShape(percent = 50),
      color = activeBackground.copy(alpha = activeBackground.alpha * bgAlpha),
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
        Box(modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)) {
          icon()
        }
      }
    }

    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
      color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
