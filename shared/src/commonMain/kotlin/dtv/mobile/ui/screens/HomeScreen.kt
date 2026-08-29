package dtv.mobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.zIndex
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dtv.mobile.state.AppState
import dtv.mobile.ui.components.LocalCardMetrics
import dtv.mobile.ui.components.HomeStreamerCard
import dtv.mobile.ui.components.PullToRefreshBox
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
  appState: AppState,
  modifier: Modifier = Modifier,
) {
  val items = appState.followedStreamers
  val displayItems = run {
    val snapshot = items.toList()
    val live = snapshot.filter { it.isLive }
    val offline = snapshot.filterNot { it.isLive }
    live + offline
  }
  val gridItems = displayItems
  var refreshing by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()
  val gridState = rememberLazyGridState()
  val cardMetrics = LocalCardMetrics.current

  var draggingKey by remember { mutableStateOf<String?>(null) }
  var dragOffset by remember { mutableStateOf(Offset.Zero) }
  var moveCount by remember { mutableIntStateOf(0) }

  if (items.isEmpty()) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text(
        text = "暂无订阅主播",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
      )
    }
    return
  }

  PullToRefreshBox(
    refreshing = refreshing,
    onRefresh = {
      if (refreshing) return@PullToRefreshBox
      scope.launch {
        refreshing = true
        runCatching { appState.refreshFollowedStreamerCards() }
        refreshing = false
      }
    },
    modifier = modifier.fillMaxSize(),
  ) {
    LazyVerticalGrid(
      modifier = Modifier.fillMaxSize(),
      state = gridState,
      columns = GridCells.Fixed(cardMetrics.columns),
      contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 92.dp),
      horizontalArrangement = Arrangement.spacedBy(if (cardMetrics.compact) 10.dp else 16.dp),
      verticalArrangement = Arrangement.spacedBy(if (cardMetrics.compact) 10.dp else 14.dp),
    ) {
      items(gridItems, key = { "${it.platform}-${it.roomId}" }, contentType = { "streamer" }) { streamer ->
        val itemKey = "${streamer.platform}-${streamer.roomId}"
        val isDragging = draggingKey == itemKey
        HomeStreamerCard(
          streamer = streamer,
          followed = true,
          onClick = { if (draggingKey == null) appState.openPlayer(streamer) },
          onToggleFollow = { appState.toggleFollow(streamer) },
          modifier = Modifier
            .then(
              if (isDragging) {
                Modifier
                  .zIndex(2f)
                  .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
              } else {
                Modifier
              },
            )
            .pointerInput(itemKey, moveCount) {
              detectDragGesturesAfterLongPress(
                onDragStart = {
                  draggingKey = itemKey
                  dragOffset = Offset.Zero
                },
                onDragCancel = {
                  draggingKey = null
                  dragOffset = Offset.Zero
                },
                onDragEnd = {
                  draggingKey = null
                  dragOffset = Offset.Zero
                },
                onDrag = { change, dragAmount ->
                  change.consume()
                  if (draggingKey != itemKey) return@detectDragGesturesAfterLongPress

                  dragOffset += dragAmount

                  val fromIndex = gridItems.indexOfFirst { "${it.platform}-${it.roomId}" == itemKey }
                  if (fromIndex < 0) return@detectDragGesturesAfterLongPress

                  val draggingInfo = gridState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == fromIndex }
                    ?: return@detectDragGesturesAfterLongPress
                  val draggingStreamer = gridItems.getOrNull(fromIndex) ?: return@detectDragGesturesAfterLongPress
                  val draggingIsLive = draggingStreamer.isLive

                  val draggingCenter = Offset(
                    x = draggingInfo.offset.x + dragOffset.x + draggingInfo.size.width / 2f,
                    y = draggingInfo.offset.y + dragOffset.y + draggingInfo.size.height / 2f,
                  )

                  val targetInfo = gridState.layoutInfo.visibleItemsInfo
                    .asSequence()
                    .filter { it.index in 0..gridItems.lastIndex }
                    .firstOrNull { info ->
                      if (info.index == draggingInfo.index) return@firstOrNull false
                      val idx = info.index
                      val s = gridItems.getOrNull(idx) ?: return@firstOrNull false
                      if (s.isLive != draggingIsLive) return@firstOrNull false
                      val left = info.offset.x.toFloat()
                      val top = info.offset.y.toFloat()
                      val right = left + info.size.width
                      val bottom = top + info.size.height
                      draggingCenter.x in left..right && draggingCenter.y in top..bottom
                    }

                  val toIndex = targetInfo?.index ?: return@detectDragGesturesAfterLongPress
                  if (toIndex == fromIndex) return@detectDragGesturesAfterLongPress
                  val targetStreamer = gridItems.getOrNull(toIndex) ?: return@detectDragGesturesAfterLongPress

                  val diff = Offset(
                    x = (draggingInfo.offset.x - targetInfo.offset.x).toFloat(),
                    y = (draggingInfo.offset.y - targetInfo.offset.y).toFloat(),
                  )

                  val fromBase = items.indexOfFirst { "${it.platform}-${it.roomId}" == itemKey }
                  val toBase = items.indexOfFirst { "${it.platform}-${it.roomId}" == "${targetStreamer.platform}-${targetStreamer.roomId}" }
                  if (fromBase < 0 || toBase < 0) return@detectDragGesturesAfterLongPress
                  appState.moveFollowedStreamer(fromIndex = fromBase, toIndex = toBase)
                  dragOffset += diff
                  moveCount += 1
                },
              )
            },
        )
      }

      item(span = { GridItemSpan(cardMetrics.columns) }) {
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}

// (removed) end-of-feed footer
