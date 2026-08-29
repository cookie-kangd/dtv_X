package dtv.mobile.ui.system

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberNotificationPermissionRequester(): () -> Unit {
  val context = LocalContext.current
  val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission(),
  ) { /* 授权结果只决定通知是否可见，不影响后台播放，这里无需处理 */ }

  return remember(context, launcher) {
    {
      // POST_NOTIFICATIONS 从 Android 13（API 33）开始需要运行时申请
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val granted = ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
          runCatching { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            .onFailure { /* 申请失败不影响听播，静默忽略 */ }
        }
      }
    }
  }
}
