package dtv.mobile.ui.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import dtv.mobile.shared.R
import dtv.mobile.util.AppLog

/**
 * 「熄屏听播」前台服务。
 *
 * 作用：把 App 进程提升为前台优先级，息屏 / 切到后台之后系统不会把它回收掉，
 * 直播音频得以持续播放。
 *
 * 设计上刻意**不持有播放器**：播放器仍由播放页持有，这里只负责"保活"，
 * 因此不会产生任何额外的解码 / 播放开销（配合播放器关闭视频轨，内存占用极低）。
 */
class LiveAudioService : Service() {

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    runCatching { startAsForeground() }
      .onFailure { AppLog.w(TAG, "startForeground failed: ${it.message}") }
    // 进程被系统回收后尽量自动重建，保证熄屏听播不中断
    return START_STICKY
  }

  override fun onDestroy() {
    runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
    super.onDestroy()
  }

  private fun startAsForeground() {
    createChannelIfNeeded()
    val notification = buildNotification()
    // 带 mediaPlayback 类型启动（Android 10+ 需要）；失败则退回普通前台启动。
    runCatching {
      ServiceCompat.startForeground(
        this,
        NOTIFICATION_ID,
        notification,
        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
      )
    }.getOrElse {
      AppLog.w(TAG, "typed startForeground failed, fallback: ${it.message}")
      startForeground(NOTIFICATION_ID, notification)
    }
  }

  private fun createChannelIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = getSystemService(NotificationManager::class.java) ?: return
    if (manager.getNotificationChannel(CHANNEL_ID) != null) return
    val channel = NotificationChannel(CHANNEL_ID, "熄屏听播", NotificationManager.IMPORTANCE_LOW).apply {
      description = "后台播放直播音频时显示的常驻通知"
      setShowBadge(false)
    }
    runCatching { manager.createNotificationChannel(channel) }
  }

  private fun buildNotification(): Notification {
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
    val contentIntent = runCatching {
      packageManager.getLaunchIntentForPackage(packageName)?.let { launch ->
        launch.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        PendingIntent.getActivity(this, REQUEST_CODE, launch, flags)
      }
    }.getOrNull()

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_live_audio)
      .setContentTitle("正在后台播放直播音频")
      .setContentText("熄屏听播已开启，点击返回直播间")
      .setContentIntent(contentIntent)
      .setOngoing(true)
      .setOnlyAlertOnce(true)
      .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .build()
  }

  private fun immutableFlag(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

  companion object {
    private const val TAG = "DTV-LiveAudio"
    private const val REQUEST_CODE = 1024
    const val CHANNEL_ID = "dtv_live_audio"
    const val NOTIFICATION_ID = 20240829
  }
}
