package dtv.mobile.android

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dtv.mobile.state.SubscriptionStoreAndroid
import dtv.mobile.util.AppCacheCleaner
import dtv.mobile.util.AppLog
import dtv.mobile.util.CrashFileLogger

class DtvApplication : Application() {
  private val mainHandler = Handler(Looper.getMainLooper())
  private val subscriptionStore by lazy { SubscriptionStoreAndroid(this) }

  private val cleanupRunnable = Runnable {
    if (subscriptionStore.loadExitCleanupEnabled()) {
      AppLog.i("DTV-Cleanup", "exit cleanup: clearing cache on app exit")
      // 在后台线程执行删除，避免阻塞主线程
      Thread {
        runCatching { AppCacheCleaner.clearOnExit(applicationContext) }
          .onFailure { AppLog.e("DTV-Cleanup", "clear failed", it) }
      }.start()
    }
  }

  override fun onCreate() {
    super.onCreate()
    AppLog.init(this)
    CrashFileLogger.install(this)

    // 退出清理：App 进入后台（即将退出）2 秒后执行；
    // 若 2 秒内又回到前台则取消，避免临时切后台就清缓存。
    ProcessLifecycleOwner.get().lifecycle.addObserver(
      object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
          mainHandler.postDelayed(cleanupRunnable, EXIT_CLEANUP_DELAY_MS)
        }

        override fun onStart(owner: LifecycleOwner) {
          mainHandler.removeCallbacks(cleanupRunnable)
        }
      },
    )
  }

  companion object {
    private const val EXIT_CLEANUP_DELAY_MS = 2000L
  }
}
