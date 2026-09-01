package dtv.mobile.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.os.Build
import android.util.Rational
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.lang.ref.WeakReference

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
      val params = PictureInPictureParams.Builder()
        .setAspectRatio(Rational(num, den))
        .build()
      activity.enterPictureInPictureMode(params)
    }.getOrDefault(false)
  }

  actual val isInPictureInPictureMode: State<Boolean> get() = _isInPip
}
