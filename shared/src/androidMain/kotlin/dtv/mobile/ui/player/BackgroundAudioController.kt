package dtv.mobile.ui.player

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dtv.mobile.util.AppLog

/**
 * 「熄屏听播」控制器（播放器内的耳机按钮）。
 *
 * 开启后启动 [LiveAudioService] 前台服务，把进程提升为前台优先级 ——
 * 息屏 / 切后台都不会被系统清理，直播音频持续播放。
 *
 * ⚠️ 视频轨的开/关**绝不在这里做**：在直播播放器运行期动态切换视频轨
 * （`setTrackTypeDisabled(TRACK_TYPE_VIDEO)`）会让部分机型/直播流的视频渲染器
 * 在恢复后卡死（画面定格、音频/弹幕正常）。听播的「纯音频」由 [StreamPlayer]
 * 在运行期摘掉/挂回 surface 实现（播放器实例与直播流连接完全不动，
 * 视频解码随 surface 摘除自动停出画面——这就是听播省电低占用的来源）；
 * 这里只负责前台服务保活。
 *
 * 播放器实例仍由播放页持有并在离开时释放，这里不持有播放器引用。
 */
object BackgroundAudioController {

  private const val TAG = "DTV-BgAudio"

  private var refCount = 0

  /** 耳机按钮打开时调用：启动前台服务保活。 */
  fun acquire(context: Context) {
    val app = context.applicationContext as? Application ?: return
    synchronized(this) {
      if (refCount == 0) startService(app)
      refCount++
    }
  }

  /** 耳机按钮关闭或退出直播间时调用：停止保活服务。 */
  fun release(context: Context) {
    val app = context.applicationContext as? Application ?: return
    synchronized(this) {
      if (refCount == 0) return
      refCount--
      if (refCount == 0) {
        stopService(app)
      }
    }
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
