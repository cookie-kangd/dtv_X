package dtv.mobile.update

import androidx.compose.runtime.Composable

/** GitHub Release 中解析出的新版本信息 */
data class AppUpdateInfo(
  val versionName: String,
  val tagName: String,
  val releaseName: String,
  val downloadUrl: String,
  val notes: String,
  val publishedAt: String,
  val sizeBytes: Long,
)

/** 检查更新的状态机 */
sealed interface UpdateState {
  data object Idle : UpdateState
  data object Checking : UpdateState
  data object UpToDate : UpdateState
  data class Available(val info: AppUpdateInfo) : UpdateState
  data class Downloading(val progress: Float) : UpdateState
  data class Downloaded(val fileUri: String) : UpdateState
  data class Failed(val message: String) : UpdateState
}

interface UpdateManager {
  val state: UpdateState
  val currentVersionName: String
  fun check()
  fun download(info: AppUpdateInfo)
  fun install(fileUri: String)
  fun reset()
}

@Composable
expect fun rememberUpdateManager(): UpdateManager

/** 去掉 v 前缀、前后空格，用于版本名比较 */
internal fun normalizeVersion(raw: String): String = raw.trim().removePrefix("v").removePrefix("V").trim()

/**
 * 比较版本号：按 . 分段逐位比较数字，非数字段按字典序比较。
 * 返回 >0 表示 a 更新，<0 表示 b 更新，0 表示相同。
 */
internal fun compareVersion(a: String, b: String): Int {
  val left = normalizeVersion(a).split('.')
  val right = normalizeVersion(b).split('.')
  val size = maxOf(left.size, right.size)
  for (i in 0 until size) {
    val l = left.getOrNull(i) ?: "0"
    val r = right.getOrNull(i) ?: "0"
    val ln = l.toLongOrNull()
    val rn = r.toLongOrNull()
    val cmp = if (ln != null && rn != null) {
      ln.compareTo(rn)
    } else {
      l.compareTo(r)
    }
    if (cmp != 0) return cmp
  }
  return 0
}
