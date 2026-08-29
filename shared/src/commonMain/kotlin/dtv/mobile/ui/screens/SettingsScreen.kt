package dtv.mobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import dtv.mobile.state.ThemeMode
import dtv.mobile.state.VideoQuality
import dtv.mobile.ui.system.PlatformBackHandler
import dtv.mobile.update.AppUpdateInfo
import dtv.mobile.update.UpdateState
import dtv.mobile.update.rememberUpdateManager

private enum class SettingsSection { Root, Basic, Sync }

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
      onOpenSync = { section = SettingsSection.Sync },
      modifier = modifier.fillMaxSize(),
    )
    SettingsSection.Basic -> BasicSettingsSection(
      appState = appState,
      onBack = { section = SettingsSection.Root },
      modifier = modifier.fillMaxSize(),
    )
    SettingsSection.Sync -> SyncSettingsSection(
      appState = appState,
      onBack = { section = SettingsSection.Root },
      modifier = modifier.fillMaxSize(),
    )
  }
}

@Composable
private fun SettingsRoot(
  onOpenBasic: () -> Unit,
  onOpenSync: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.verticalScroll(rememberScrollState()),
  ) {
    SettingsItemRow(
      icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null) },
      title = "基本设置",
      subtitle = "记住栏目、主题模式、全局颜色",
      onClick = onOpenBasic,
    )
    SettingsItemRow(
      icon = { Icon(imageVector = Icons.Default.Devices, contentDescription = null) },
      title = "数据同步",
      subtitle = "局域网共享 / 导入关注、分区与屏蔽词",
      onClick = onOpenSync,
    )
    UpdateCheckerCard()
  }
}

@Composable
private fun UpdateCheckerCard(
  modifier: Modifier = Modifier,
) {
  val updateManager = rememberUpdateManager()
  val state = updateManager.state
  var expanded by remember { mutableStateOf(false) }
  val currentVersion = updateManager.currentVersionName

  Column(modifier = modifier.fillMaxWidth()) {
    SettingsItemRow(
      icon = { Icon(imageVector = Icons.Default.Download, contentDescription = null) },
      title = "检查更新",
      subtitle = if (currentVersion.isBlank()) "检查 GitHub Release 是否有新版本" else "当前版本 v$currentVersion",
      onClick = {
        expanded = true
        updateManager.check()
      },
    )

    if (expanded) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 6.dp, vertical = 2.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          when (state) {
            UpdateState.Idle -> Unit
            UpdateState.Checking -> UpdateStatusText(text = "正在检查更新…")
            UpdateState.UpToDate -> UpdateStatusText(
              text = "已是最新版本（v$currentVersion）",
              positive = true,
            )
            is UpdateState.Available -> UpdateAvailableBlock(
              info = state.info,
              onDownload = { updateManager.download(state.info) },
            )
            is UpdateState.Downloading -> {
              val percent = (state.progress * 100).toInt()
              UpdateStatusText(text = "正在下载新版本… $percent%")
              UpdateProgressBar(progress = state.progress)
            }
            is UpdateState.Downloaded -> UpdateStatusText(text = "下载完成，正在唤起安装…", positive = true)
            is UpdateState.Failed -> {
              UpdateStatusText(text = state.message, positive = false)
              OutlinedButton(onClick = { updateManager.check() }) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("重试")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun VideoQualitySelector(
  selected: VideoQuality,
  onSelect: (VideoQuality) -> Unit,
  modifier: Modifier = Modifier,
) {
  val options = VideoQuality.entries
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
      .padding(4.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    options.forEach { quality ->
      val isSelected = quality == selected
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(9.dp))
          .background(
            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
          )
          .clickable { onSelect(quality) }
          .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = quality.label,
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
          color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
          maxLines = 1,
        )
      }
    }
  }
}

@Composable
private fun UpdateStatusText(
  text: String,
  positive: Boolean? = null,
) {
  val color = when (positive) {
    true -> Color(0xFF16A34A)
    false -> Color(0xFFDC2626)
    null -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
  }
  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    color = color,
  )
}

@Composable
private fun UpdateProgressBar(progress: Float) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(6.dp)
      .clip(RoundedCornerShape(3.dp))
      .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(progress.coerceIn(0f, 1f))
        .height(6.dp)
        .background(MaterialTheme.colorScheme.primary),
    )
  }
}

@Composable
private fun UpdateAvailableBlock(
  info: AppUpdateInfo,
  onDownload: () -> Unit,
) {
  // commonMain 不可使用 String.format，这里手动保留一位小数
  val sizeText = if (info.sizeBytes > 0) {
    val mb = (info.sizeBytes / 1048576.0 * 10.0).toLong() / 10.0
    "$mb MB"
  } else {
    ""
  }

  Text(
    text = "发现新版本 v${info.versionName}",
    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
    color = MaterialTheme.colorScheme.primary,
  )
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    if (sizeText.isNotBlank()) {
      Text(
        text = sizeText,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
      )
    }
    if (info.publishedAt.isNotBlank()) {
      Text(
        text = info.publishedAt.replace("T", " ").removeSuffix("Z"),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
      )
    }
  }
  if (info.notes.isNotBlank()) {
    Text(
      text = info.notes.take(300),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
      maxLines = 6,
    )
  }
  Button(onClick = onDownload) {
    Icon(imageVector = Icons.Default.Download, contentDescription = null)
    Spacer(modifier = Modifier.width(6.dp))
    Text("下载并安装")
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
private fun SettingsSectionHeader(
  title: String,
  onBack: () -> Unit,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    IconButton(onClick = onBack) {
      Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
    }
    Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
  }
}

@Composable
private fun SettingsCard(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
    tonalElevation = 0.dp,
    shadowElevation = 0.dp,
  ) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), content = content)
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
    SettingsSectionHeader(title = "基本设置", onBack = onBack)

    // 记住栏目（默认开启）
    SettingsCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text("记住栏目", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
          Text(
            text = "在斗鱼、虎牙、抖音、B站切换栏目（如网游竞技、单机热游）时自动记住，再次打开该平台时恢复上次选择的栏目。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
          )
        }
        Switch(
          checked = appState.rememberCategoryEnabled,
          onCheckedChange = appState::updateRememberCategoryEnabled,
        )
      }
    }

    // 小卡片模式（默认开启）
    SettingsCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text("小卡片模式", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
          Text(
            text = "开启后直播间卡片变小、首页与平台页切换到 3 列，一屏可展示更多直播间；关闭则恢复 2 列大卡片。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
          )
        }
        Switch(
          checked = appState.compactCardEnabled,
          onCheckedChange = appState::updateCompactCardEnabled,
        )
      }
    }

    // 默认横屏（默认关闭）
    SettingsCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text("默认横屏", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
          Text(
            text = "开启后点进任意直播间默认以横屏全屏观看，更贴合大屏看直播的体验；关闭则保持竖屏进入。该设置会持久保存，重启 App 后仍保持当前开关状态。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
          )
        }
        Switch(
          checked = appState.landscapeEnabled,
          onCheckedChange = appState::updateLandscapeEnabled,
        )
      }
    }

    // 播放画质（默认最高）
    SettingsCard {
      Text("播放画质", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "默认「最高」：不限制分辨率上限，优先取流中的最高码率，保证画面最清晰。网络较差时可下调档位换取流畅。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
      )
      Spacer(modifier = Modifier.height(10.dp))
      VideoQualitySelector(
        selected = appState.videoQuality,
        onSelect = appState::updateVideoQuality,
      )
    }

    // 主题模式（亮色 / 暗色 / 跟随系统）
    SettingsCard {
      Text("主题模式", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "选择浅色、深色或跟随系统。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
      )
      Spacer(modifier = Modifier.height(10.dp))
      ThemeModeSelector(
        selected = appState.themeMode,
        onSelect = appState::updateThemeMode,
      )
    }

    // 全局颜色
    SettingsCard {
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

@Composable
private fun ThemeModeSelector(
  selected: ThemeMode,
  onSelect: (ThemeMode) -> Unit,
  modifier: Modifier = Modifier,
) {
  val options = listOf(
    ThemeMode.System to "跟随系统",
    ThemeMode.Light to "亮色",
    ThemeMode.Dark to "暗色",
  )
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
      .padding(4.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    options.forEach { (mode, label) ->
      val isSelected = mode == selected
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(9.dp))
          .background(
            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
          )
          .clickable { onSelect(mode) }
          .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
          color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
          maxLines = 1,
        )
      }
    }
  }
}

@Composable
private fun SyncSettingsSection(
  appState: AppState,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 18.dp, vertical = 6.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    SettingsSectionHeader(title = "数据同步", onBack = onBack)
    // 功能与原「首页 → 数据同步」完全一致，仅入口位置调整到设置内
    SyncScreen(
      appState = appState,
      modifier = Modifier.fillMaxSize(),
    )
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
      val active = isSelected || (isDefault && isDefaultSelected)
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
              if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else Color.Transparent,
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
              if (active) {
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
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (active) 0.9f else 0.55f),
          fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
        )
      }
    }
  }
}
