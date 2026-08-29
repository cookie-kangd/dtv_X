package dtv.mobile.ui.player

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import dtv.mobile.util.AppLog

/**
 * 「熄屏听播」控制器（播放器内的耳机按钮）。
 *
 * 开启后立刻做两件事：
 * 1. 启动 [LiveAudioService] 前台服务，把进程提升为前台优先级 ——
 *    息屏 / 切后台都不会被系统清理，直播音频持续播放；
 * 2. 立即关闭播放器的**视频轨**：不再解码、不再渲染画面，也不再下载视频分片，
 *    只保留音频解码，内存占用、耗电与流量都大幅下降。
 *
 * 关闭（再次点击耳机按钮 / 退出直播间）时恢复视频轨并停止保活服务。
 *
 * 播放器实例仍由播放页持有并在离开时释放，这里只做「保活 + 音视频轨切换」。
 */
object BackgroundAudioController {

  private const val TAG = "DTV-BgAudio"

  private var refCount = 0
  private var player: ExoPlayer? = null
  private var audioOnlyApplied = false

  /** 耳机按钮打开时调用：开启保活并立即切到纯音频。 */
  fun acquire(context: Context, exoPlayer: ExoPlayer) {
    val app = context.applicationContext as? Application ?: return
    synchronized(this) {
      player = exoPlayer
      if (refCount == 0) startService(app)
      refCount++
      // 播放器可能已经重建（换画质等），强制重新下发一次纯音频约束，
      // 避免沿用上一次的标记导致新播放器漏掉约束、仍在渲染画面。
      audioOnlyApplied = false
    }
    setAudioOnly(true)
  }

  /** 耳机按钮关闭或退出直播间时调用：恢复画面并停止保活。 */
  fun release(context: Context) {
    val app = context.applicationContext as? Application ?: return
    synchronized(this) {
      if (refCount == 0) return
      refCount--
      if (refCount == 0) {
        setAudioOnly(false)
        player = null
        stopService(app)
      }
    }
  }

  private fun setAudioOnly(enabled: Boolean) {
    val p = player ?: return
    if (audioOnlyApplied == enabled) return
    audioOnlyApplied = enabled
    runCatching {
      p.trackSelectionParameters = p.trackSelectionParameters
        .buildUpon()
        .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, enabled)
        .build()
    }.onFailure {
      // 播放器已释放等情况直接忽略，绝不影响主流程
      AppLog.w(TAG, "setTrackTypeDisabled($enabled) failed: ${it.message}")
    }
    AppLog.i(TAG, "audio only = $enabled")
  }

  private fun startService(app: Application) {
    runCatching {
      val intent = Intent(app, LiveAudioService::class.java)
      ContextCompat.startForegroundService(app, intent)
    }.onFailure { AppLog.w(TAG, "start foreground service failed: ${it.message}") }
  }

  private fun stopService(app: Application) {
    runCatching { app.stopService(Intent(app, LiveAudioService::class.java)) }
      .onFailure { AppLog.w(TAG, "stop foreground service failed: ${it.message}") }
  }
}
