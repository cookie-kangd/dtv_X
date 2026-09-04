package dtv.mobile.ui.components

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/**
 * 列表翻到底自动加载更多。
 *
 * 实现要点：
 * 1) 触发条件同时覆盖两种情形——
 *    a. 已滚动到接近末尾（lastVisible >= count - buffer）；
 *    b. 当前内容**不足以填满一屏**（末尾元素底部仍在视口内）。
 *    情形 b 很关键：小栏目（如斗鱼「语音互动-唱歌」只有 5 个房间）一页装不满，
 *    用户根本滑不动，若只看 scroll 位置就永远不会触发加载，
 *    底部提示会一直停在「继续滑动加载更多」。
 * 2) 去重键是「(条件, 条目数)」而不只是条件本身：
 *    - 滚动抖动时两者都不变 → 不会重复发起请求；
 *    - 每次加载后条目数变化 → 会重新判定，于是可以继续补足下一页，
 *      直到填满一屏或没有更多数据。
 *    （若只对布尔条件 distinctUntilChanged，条件恒为 true 的小栏目场景
 *     会吞掉后续所有发射，导致加载一页后再也不加载。）
 * 3) 单轮补页有 guard 上限，且某页没有带来新条目时立即停止，避免死循环。
 */
@Composable
fun LazyGridLoadMoreEffect(
  gridState: LazyGridState,
  enabled: Boolean,
  itemCount: Int,
  buffer: Int = 4,
  onLoadMore: suspend () -> Unit,
) {
  val enabledState = rememberUpdatedState(enabled)
  val itemCountState = rememberUpdatedState(itemCount)
  val bufferState = rememberUpdatedState(buffer)
  val onLoadMoreState = rememberUpdatedState(onLoadMore)

  LaunchedEffect(gridState) {
    snapshotFlow {
      val info = gridState.layoutInfo
      val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
      val count = itemCountState.value
      val lastBottom = info.visibleItemsInfo.lastOrNull()?.let { it.offset + it.size } ?: 0
      val nearEnd = lastVisible >= (count - bufferState.value).coerceAtLeast(0)
      // 内容没填满视口（已扣除底部 contentPadding 的浮岛避让区）→ 也应继续翻页
      val underfilled = count > 0 && lastBottom <= info.viewportEndOffset
      // 返回 (是否触发, 当前条目数)：条目数参与去重，保证「补一页后再判定」
      (nearEnd || underfilled) to count
    }
      .distinctUntilChanged()
      .filter { it.first }
      .map { it.second }
      .collect {
        // 单轮最多补 8 页，防止任何异常情况下的死循环
        var guard = 0
        while (enabledState.value && guard < 8) {
          guard += 1
          val before = itemCountState.value
          onLoadMoreState.value()
          // 本页没带来新条目 → 服务端已无更多，停止
          if (itemCountState.value <= before) break
        }
      }
  }
}
