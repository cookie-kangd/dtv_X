package dtv.mobile.ui.player

import androidx.compose.runtime.State

/**
 * 画中画（Picture-in-Picture）能力抽象。
 *
 * - [isSupported]：当前平台/设备是否支持系统画中画。
 * - [enter]：以给定宽高比进入系统画中画；返回是否成功发起。
 * - [isInPictureInPictureMode]：当前是否处于画中画，播放器据此隐藏叠加层、只留视频。
 *
 * 在 Android 上通过 [android.app.Activity.enterPictureInPictureMode] 进入系统画中画，由 MainActivity 绑定 Activity 弱引用并在 onPictureInPictureModeChanged 中更新状态；
 * 非 Android（iOS 等）平台 [isSupported] 恒为 false，[enter] 直接返回 false。
 */
expect object PictureInPicture {
  fun isSupported(): Boolean

  fun enter(aspectRatio: Float): Boolean

  val isInPictureInPictureMode: State<Boolean>
}
