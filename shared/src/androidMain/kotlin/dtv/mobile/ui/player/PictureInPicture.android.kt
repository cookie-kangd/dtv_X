package dtv.mobile.ui.player

import android.app.Activity
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Rational
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.lang.ref.WeakReference

/**
 * 画中画关闭广播的 action（由 MainActivity 在本地注册接收器处理）。
 * 点击画中画窗口里的「退出」按钮 -> 广播 -> 清理缓存并 finish 整个 App。
 */
const val ACTION_PIP_CLOSE = "dtv.mobile.android.ACTION_PIP_CLOSE"

/**
 * 持有当前前台 Activity 的弱引用，供 [enter] 进入画中画时使用。
 * 使用弱引用：Activity 销毁后自动释放，不会造成内存泄漏。
 */
private object ActivityHolder {
  private var ref: WeakReference<Activity>? = null

  fun set(activity: Activity) {
    ref = WeakReference(activity)
  }

  fun get(): Activity? = ref?.get()
}

/**
 * 画中画能力（Android 实现）。
 *
 * 关键点（避免内存溢出 / 泄漏）：
 * - 仅持有 Activity 的 [WeakReference]，不阻止其回收；
 * - 进入画中画时**不会**释放 ExoPlayer（播放器仍由 StreamPlayer 持有并继续播放），
 *   因此不会因重建播放器而产生累积的内存占用；
 * - [isInPictureInPictureMode] 用 Compose [MutableState] 承载，进入/退出画中画时由
 *   MainActivity.onPictureInPictureModeChanged 回调更新，驱动叠加层隐藏/恢复。
 *
 * 画中画内的「退出」按钮：通过 [PictureInPictureParams.setActions] 注入一个自定义
 * RemoteAction（X 图标），其 PendingIntent 以广播形式发往本地接收器，由 MainActivity
 * 统一处理「清理缓存 + 关闭 App」，行为确定、不受各厂商对系统默认 X 的差异影响。
 */
actual object PictureInPicture {

  private val _isInPip: MutableState<Boolean> = mutableStateOf(false)

  /** 由 MainActivity.onCreate 调用，记录当前 Activity（弱引用）。 */
  fun bind(activity: Activity) {
    ActivityHolder.set(activity)
  }

  /** 由 MainActivity.onPictureInPictureModeChanged 回调，更新画中画状态。 */
  fun setInPip(value: Boolean) {
    _isInPip.value = value
  }

  actual fun isSupported(): Boolean {
    val activity = ActivityHolder.get() ?: return false
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
    return activity.packageManager
      .hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
  }

  actual fun enter(aspectRatio: Float): Boolean {
    val activity = ActivityHolder.get() ?: return false
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
    return runCatching {
      // 规整为 16:9 或 9:16，避免极端宽高比被系统拒绝。
      val ar = aspectRatio.takeIf { it.isFinite() && it > 0f } ?: (16f / 9f)
      val (num, den) = if (ar >= 1f) 16 to 9 else 9 to 16
      val builder = PictureInPictureParams.Builder()
        .setAspectRatio(Rational(num, den))
      // 注入「退出」动作（仅 O 及以上支持 setActions），点击即关闭整个 App 并触发退出清理。
      // 单独特判：若构造动作（Bitmap / PendingIntent）在个别 ROM 上失败，仅放弃该按钮，
      // 不影响进入画中画本身。
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        runCatching { builder.setActions(listOf(buildCloseAction(activity))) }
      }
      activity.enterPictureInPictureMode(builder.build())
    }.getOrDefault(false)
  }

  /** 构造画中画「退出」RemoteAction：X 图标 + 关闭 App 的广播 PendingIntent。 */
  private fun buildCloseAction(activity: Activity): RemoteAction {
    val closeIntent = Intent(ACTION_PIP_CLOSE).setPackage(activity.packageName)
    val pi = PendingIntent.getBroadcast(
      activity,
      0x5050,
      closeIntent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
    val icon = Icon.createWithBitmap(createCloseIconBitmap())
    return RemoteAction(icon, "退出", "关闭应用并清理缓存", pi)
  }

  /** 绘制一个带半透明深色圆底 + 白色 X 的 64px 图标位图。 */
  private fun createCloseIconBitmap(size: Int = 64): Bitmap {
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val bg = Paint().apply {
      isAntiAlias = true
      color = Color.argb(180, 0, 0, 0)
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f * 0.92f, bg)
    val fg = Paint().apply {
      isAntiAlias = true
      color = Color.WHITE
      strokeWidth = size * 0.12f
      strokeCap = Paint.Cap.ROUND
    }
    val m = size * 0.28f
    canvas.drawLine(m, m, size - m, size - m, fg)
    canvas.drawLine(size - m, m, m, size - m, fg)
    return bmp
  }

  actual val isInPictureInPictureMode: State<Boolean> get() = _isInPip
}
