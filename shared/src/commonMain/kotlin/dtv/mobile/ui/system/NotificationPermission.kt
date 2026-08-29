package dtv.mobile.ui.system

import androidx.compose.runtime.Composable

/**
 * 返回一个「请求通知权限」的回调。
 *
 * 用途：熄屏听播会启动前台服务并显示常驻通知，Android 13（API 33）起
 * 需要 POST_NOTIFICATIONS 运行时授权，通知才会出现在通知栏。
 *
 * 未授权**不影响**后台播放本身，只是用户看不到通知入口，
 * 因此调用方无需等待授权结果。
 *
 * 非 Android 平台 / 低版本系统返回空操作。
 */
@Composable
expect fun rememberNotificationPermissionRequester(): () -> Unit
