package dtv.mobile.ui.player

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import dtv.mobile.util.AppLog

/**
 * 「熄屏听播」控制器。
 *
 * 开启后：
 * 1. 启动 [LiveAudioService] 前台服务，把进程提升为前台优先级 —— 息屏 / 切后台
 *    都不会被系统清理，直播音频持续播放；
 * 2. 息屏或切到后台时，关闭播放器的**视频轨**：不再解码、不再渲染画面，
 *    只保留音频解码，内存占用与耗电量都极低；
 * 3. 亮屏 / 回到前台时恢复视频轨，画面继续。
 *
 * 播放器实例仍由播放页持有并在离开时释放，这里只做"保活 + 音视频轨切换"。
 */
object BackgroundAudioController {

  private const val TAG = "DTV-BgAudio"

  private var refCount = 0
  private var player: ExoPlayer? = null
  private var audioOnlyActive = false
  private var screenOff = false
  private var appBackgrounded = false
  private var receiverRegistered = false
  private var lifecycleRegistered = false
  private var startedActivities = 0

  private val screenReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      when (intent?.action) {
        Intent.ACTION_SCREEN_OFF -> {
          screenOff = true
          applyAudioOnly()
        }
        Intent.ACTION_SCREEN_ON -> {
          screenOff = false
          applyAudioOnly()
        }
      }
    }
  }

  private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
    override fun onActivityStarted(activity: Activity) {
      startedActivities++
      appBackgrounded = false
      applyAudioOnly()
    }

    override fun onActivityStopped(activity: Activity) {
      startedActivities = (startedActivities - 1).coerceAtLeast(0)
      // 旋转屏幕 / 主题切换会走 stop → start，此时不应切到纯音频，否则会产生一次无谓的重新缓冲
      if (startedActivities == 0 && !activity.isChangingConfigurations) {
        appBackgrounded = true
        applyAudioOnly()
      }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
  }

  /** 播放页进入时调用：开启后台保活并开始监听息屏 / 前后台。 */
  fun acquire(context: Context, exoPlayer: ExoPlayer) {
    val app = context.applicationContext as? Application ?: return
    synchronized(this) {
      player = exoPlayer
      if (refCount == 0) {
        registerScreenReceiver(app)
        registerLifecycle(app)
        startService(app)
      }
      refCount++
    }
    applyAudioOnly()
  }

  /** 播放页退出或开关关闭时调用：停止保活并恢复视频轨。 */
  fun release(context: Context) {
    val app = context.applicationContext as? Application ?: return
    synchronized(this) {
      refCount = (refCount - 1).coerceAtLeast(0)
      if (refCount == 0) {
        // 交还播放器前先恢复视频轨，避免复用时仍停留在纯音频状态
        setAudioOnly(false)
        player = null
        unregisterScreenReceiver(app)
        unregisterLifecycle(app)
        stopService(app)
        screenOff = false
        appBackgrounded = false
      }
    }
  }

  private fun applyAudioOnly() {
    setAudioOnly(screenOff || appBackgrounded)
  }

  private fun setAudioOnly(enabled: Boolean) {
    val p = player ?: return
    if (audioOnlyActive == enabled) return
    audioOnlyActive = enabled
    runCatching {
      p.trackSelectionParameters = p.trackSelectionParameters
        .buildUpon()
        .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, enabled)
        .build()
    }.onFailure {
      AppLog.w(TAG, "setTrackTypeDisabled($enabled) failed: ${it.message}")
    }
    AppLog.i(TAG, "audio only = $enabled (screenOff=$screenOff, background=$appBackgrounded)")
  }

  private fun registerScreenReceiver(app: Application) {
    if (receiverRegistered) return
    val filter = IntentFilter().apply {
      addAction(Intent.ACTION_SCREEN_OFF)
      addAction(Intent.ACTION_SCREEN_ON)
    }
    runCatching {
      ContextCompat.registerReceiver(app, screenReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
      receiverRegistered = true
    }.onFailure { AppLog.w(TAG, "register screen receiver failed: ${it.message}") }
  }

  private fun unregisterScreenReceiver(app: Application) {
    if (!receiverRegistered) return
    runCatching { app.unregisterReceiver(screenReceiver) }
      .onFailure { AppLog.w(TAG, "unregister screen receiver failed: ${it.message}") }
    receiverRegistered = false
  }

  private fun registerLifecycle(app: Application) {
    if (lifecycleRegistered) return
    runCatching {
      app.registerActivityLifecycleCallbacks(lifecycleCallbacks)
      lifecycleRegistered = true
    }.onFailure { AppLog.w(TAG, "register lifecycle callbacks failed: ${it.message}") }
  }

  private fun unregisterLifecycle(app: Application) {
    if (!lifecycleRegistered) return
    runCatching { app.unregisterActivityLifecycleCallbacks(lifecycleCallbacks) }
      .onFailure { AppLog.w(TAG, "unregister lifecycle callbacks failed: ${it.message}") }
    lifecycleRegistered = false
    startedActivities = 0
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
