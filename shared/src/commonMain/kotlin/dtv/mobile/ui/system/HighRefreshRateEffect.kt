package dtv.mobile.ui.system

import androidx.compose.runtime.Composable

/**
 * 屏幕刷新率适配：
 * - enabled = true  → 请求系统最高刷新率（高刷屏 90/120/144Hz 生效，滑动与动画更流畅）；
 * - enabled = false → 锁定 60Hz 标准刷新率（更省电）。
 * 默认开启。切换后立即生效，设置也会持久保存。
 */
@Composable
expect fun HighRefreshRateEffect(enabled: Boolean)
