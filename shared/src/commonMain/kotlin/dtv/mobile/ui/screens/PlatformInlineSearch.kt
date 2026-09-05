package dtv.mobile.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import dtv.mobile.ui.components.DtvCardDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dtv.mobile.model.Streamer
import dtv.mobile.state.AppState
import dtv.mobile.ui.components.NetworkImage
import dtv.mobile.ui.components.RoundedDropdownMenu
import dtv.mobile.util.formatViewerCountWanIfNeeded
import dtv.mobile.util.normalizeHttpUrl
import kotlinx.coroutines.delay

/**
 * 平台页顶栏的内联搜索框：直接在输入框里输入关键字，
 * 下方弹出搜索结果面板（可进直播间、可关注/取关主播），
 * 不再跳转到独立搜索页。
 */
@Composable
fun PlatformInlineSearch(
  appState: AppState,
  modifier: Modifier = Modifier,
) {
  var query by remember { mutableStateOf("") }
  var results by remember(appState.selectedPlatform) { mutableStateOf(emptyList<Streamer>()) }
  var searching by remember(appState.selectedPlatform) { mutableStateOf(false) }
  val platform = appState.selectedPlatform

  LaunchedEffect(platform, query) {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) {
      results = emptyList()
      searching = false
      return@LaunchedEffect
    }
    searching = true
    delay(220)
    results = runCatching { appState.repo.searchAnchors(platform = platform, keyword = trimmed) }
      .getOrDefault(emptyList())
    searching = false
  }

  fun closePanel() {
    query = ""
  }

  Box(modifier = modifier) {
    // 毛玻璃质感：半透明底 + 发丝描边。暗色下用白色高光描边模拟玻璃边缘反光。
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .height(44.dp)
        .clip(RoundedCornerShape(999.dp)),
      shape = RoundedCornerShape(999.dp),
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
      tonalElevation = 0.dp,
      shadowElevation = 0.dp,
      border = BorderStroke(
        1.dp,
        if (isDark) {
          Color.White.copy(alpha = 0.12f)
        } else {
          MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
        },
      ),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "搜索",
          tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
          modifier = Modifier.size(18.dp),
        )
        BasicTextField(
          value = query,
          onValueChange = { query = it },
          modifier = Modifier.weight(1f).height(44.dp),
          singleLine = true,
          textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
          ),
          cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
          decorationBox = { inner ->
            Box(contentAlignment = Alignment.CenterStart) {
              if (query.isEmpty()) {
                Text(
                  text = "搜索",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
              }
              inner()
            }
          },
        )
        if (query.isNotEmpty()) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "清空",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .clickable { closePanel() }
              .padding(8.dp),
          )
        }
      }
    }

    RoundedDropdownMenu(
      expanded = query.trim().isNotEmpty(),
      onDismissRequest = { closePanel() },
      offsetY = 50.dp,
      width = 320.dp,
      maxHeight = 420.dp,
    ) {
      when {
        searching -> {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Text(
              text = "搜索中…",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )
          }
        }
        results.isEmpty() -> {
          Text(
            text = "没有找到相关直播间",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
          )
        }
        else -> {
          // 面板内最多展示 20 条，点击结果直接进直播间并收起面板
          results.take(20).forEach { streamer ->
            val followed = appState.isFollowed(streamer)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  closePanel()
                  appState.openPlayer(streamer)
                }
                .padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              val cover = normalizeHttpUrl(streamer.coverUrl) ?: normalizeHttpUrl(streamer.avatarUrl)
              Box(
                modifier = Modifier
                  .size(width = 72.dp, height = 44.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center,
              ) {
                if (cover != null) {
                  NetworkImage(
                    url = cover,
                    contentDescription = streamer.title,
                    modifier = Modifier.matchParentSize(),
                  )
                } else {
                  Text(
                    text = streamer.name.take(1),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondary,
                  )
                }
              }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = streamer.title,
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(
                    text = streamer.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                  )
                  val viewer = streamer.viewerText.takeIf { it.isNotBlank() }
                    ?.let(::formatViewerCountWanIfNeeded)
                  if (viewer != null) {
                    Text(
                      text = viewer,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                      maxLines = 1,
                    )
                  }
                }
              }

              IconButton(onClick = { appState.toggleFollow(streamer) }) {
                Icon(
                  imageVector = if (followed) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                  contentDescription = if (followed) "取消关注" else "关注",
                  tint = if (followed) {
                    DtvCardDefaults.FollowActive
                  } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                  },
                )
              }
            }
          }
        }
      }
    }
  }
}
