package dtv.mobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dtv.mobile.state.AppState
import dtv.mobile.ui.system.PlatformBackHandler

private enum class SettingsSection { Root, Basic }

private val PresetAccentColors = listOf(
  "默认" to "",
  "青柠" to "#A3E635",
  "红色" to "#EF4444",
  "橙色" to "#F97316",
  "黄色" to "#EAB308",
  "绿色" to "#22C55E",
  "青色" to "#14B8A6",
  "蓝色" to "#3B82F6",
  "靛蓝" to "#6366F1",
  "紫色" to "#A855F7",
  "粉色" to "#EC4899",
  "玫红" to "#F43F5E",
  "天蓝" to "#38BDF8",
  "灰色" to "#94A3B8",
)

@Composable
fun SettingsScreen(
  appState: AppState,
  modifier: Modifier = Modifier,
) {
  var section by remember { mutableStateOf(SettingsSection.Root) }

  PlatformBackHandler(enabled = section != SettingsSection.Root) { section = SettingsSection.Root }

  when (section) {
    SettingsSection.Root -> SettingsRoot(
      onOpenBasic = { section = SettingsSection.Basic },
      modifier = modifier.fillMaxSize(),
    )
    SettingsSection.Basic -> BasicSettingsSection(
      appState = appState,
      onBack = { section = SettingsSection.Root },
      modifier = modifier.fillMaxSize(),
    )
  }
}

@Composable
private fun SettingsRoot(
  onOpenBasic: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.verticalScroll(rememberScrollState()),
  ) {
    SettingsItemRow(
      icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null) },
      title = "基本设置",
      subtitle = "记住栏目、全局颜色",
      onClick = onOpenBasic,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = "更多设置项持续补充中…",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
      modifier = Modifier.padding(horizontal = 18.dp),
    )
  }
}

@Composable
private fun SettingsItemRow(
  icon: @Composable () -> Unit,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 6.dp, vertical = 4.dp),
    color = Color.Transparent,
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
      ) {
        CompositionLocalProvider(
          LocalContentColor provides MaterialTheme.colorScheme.primary,
        ) {
          icon()
        }
      }
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
      }
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
        modifier = Modifier
          .size(20.dp)
          .rotate(180f),
      )
    }
  }
}

@Composable
private fun BasicSettingsSection(
  appState: AppState,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 18.dp, vertical = 6.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
      }
      Text("基本设置", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
    }

    // 记住栏目
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("记住栏目", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(
              text = "勾选后，在斗鱼、虎牙、抖音、B站切换栏目（如网游竞技、单机热游）时会自动记住；再次打开该平台时恢复上次选择的栏目。",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
          }
          Switch(
            checked = appState.rememberCategoryEnabled,
            onCheckedChange = appState::setRememberCategoryEnabled,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // 全局颜色
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
    ) {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Text("全局颜色", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        }
        Text(
          text = "自定义主题强调色，会应用到选中栏目高亮、弹幕昵称颜色等位置。",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
        Spacer(modifier = Modifier.height(10.dp))
        AccentColorPalette(
          selectedHex = appState.accentColorHex,
          onSelect = { hex -> appState.setAccentColor(hex) },
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          TextButton(onClick = { appState.setAccentColor("") }) {
            Text("恢复默认颜色")
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccentColorPalette(
  selectedHex: String,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val selectedNormalized = selectedHex.trim().removePrefix("#").lowercase()
  FlowRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    PresetAccentColors.forEach { (label, hex) ->
      val normalized = hex.trim().removePrefix("#").lowercase()
      val isSelected = normalized == selectedNormalized && selectedNormalized.isNotEmpty()
      val isDefault = hex.isEmpty()
      val isDefaultSelected = selectedNormalized.isEmpty()
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .then(
              if (isSelected || (isDefault && isDefaultSelected)) {
                Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
              } else {
                Modifier.background(Color.Transparent)
              },
            )
            .padding(4.dp),
          contentAlignment = Alignment.Center,
        ) {
          Surface(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .clickable { onSelect(hex) },
            shape = CircleShape,
            color = if (isDefault) Color(0xFF9CA3AF) else Color(0xFF000000L or (normalized.toLongOrNull(16) ?: 0L)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
          ) {
            Box(contentAlignment = Alignment.Center) {
              if (isSelected || (isDefault && isDefaultSelected)) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = if (isDefault) Color.White else Color.Black,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
        }
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isSelected || (isDefault && isDefaultSelected)) 0.9f else 0.55f),
          fontWeight = if (isSelected || (isDefault && isDefaultSelected)) FontWeight.Bold else FontWeight.Medium,
        )
      }
    }
  }
}
