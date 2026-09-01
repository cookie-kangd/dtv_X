package dtv.mobile.android

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import dtv.mobile.App
import dtv.mobile.repo.android.AndroidDtvRepository
import dtv.mobile.state.SubscriptionStoreAndroid
import dtv.mobile.ui.player.PictureInPicture
import dtv.mobile.util.AppLog

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppLog.init(applicationContext)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    // 让画中画控制器能拿到当前 Activity（弱引用，离开即释放，不会泄漏）。
    PictureInPicture.bind(this)
    setContent {
      val repo = remember { AndroidDtvRepository(applicationContext) }
      val subscriptionStore = remember { SubscriptionStoreAndroid(applicationContext) }
      App(repo = repo, subscriptionStore = subscriptionStore)
    }
  }

  // 进入/退出画中画时同步状态，使播放器叠加层在画中画窗口里只显示视频画面。
  override fun onPictureInPictureModeChanged(
    isInPictureInPictureMode: Boolean,
    newConfig: Configuration,
  ) {
    super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    PictureInPicture.setInPip(isInPictureInPictureMode)
  }
}
