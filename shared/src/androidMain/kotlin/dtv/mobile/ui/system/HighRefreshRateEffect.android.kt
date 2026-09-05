package dtv.mobile.ui.system

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Display
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import dtv.mobile.util.AppLog

private tailrec fun Context.findActivity(): Activity? {
  return when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
  }
}

/**
 * 通过 preferredDisplayModeId 一次性选定显示模式：
 * - 开启 → 取 supportedModes 中刷新率最高的模式；
 * - 关闭 → 取不超过 60Hz 的模式中最接近 60 的（部分面板没有精确 60Hz 档）。
 *
 * 说明：修改 preferredDisplayModeId 可能伴随一次短暂的黑屏/闪烁（系统切换显示模式），
 * 属正常现象；切换仅在开关变化时发生。任何一步失败都静默回退系统默认刷新率。
 */
@Composable
actual fun HighRefreshRateEffect(enabled: Boolean) {
  val context = LocalContext.current
  DisposableEffect(context, enabled) {
    val window: Window = context.findActivity()?.window ?: return@DisposableEffect onDispose {}
    val previousModeId = window.attributes.preferredDisplayModeId

    runCatching {
      val display: Display = window.windowManager.defaultDisplay
      val modes = display.supportedModes
      if (modes.isEmpty()) return@runCatching
      val target = if (enabled) {
        modes.maxByOrNull { it.refreshRate }
      } else {
        modes.filter { it.refreshRate <= 60.5f }.maxByOrNull { it.refreshRate }
          ?: modes.minByOrNull { it.refreshRate }
      }
      val modeId = target?.modeId ?: return@runCatching
      if (modeId != previousModeId) {
        AppLog.i("DTV-Refresh", "set preferredDisplayModeId=$modeId (${target.refreshRate}Hz) enabled=$enabled")
        window.attributes = window.attributes.also { it.preferredDisplayModeId = modeId }
      }
    }.onFailure {
      AppLog.w("DTV-Refresh", "set refresh rate failed: ${it.message}")
    }

    onDispose {
      // 切换开关时由下一次 effect 直接写入新模式；这里仅在组合销毁时还原系统默认
      runCatching {
        window.attributes = window.attributes.also { it.preferredDisplayModeId = previousModeId }
      }
    }
  }
}
