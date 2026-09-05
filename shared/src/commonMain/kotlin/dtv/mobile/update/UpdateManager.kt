package dtv.mobile.update

import androidx.compose.runtime.Composable

/**
 * 更新下载来源：更新弹窗里每个按钮对应一个来源。
 * 第一个永远是「原始地址」；其余是 gh-proxy 等加速镜像
 * （把原始 GitHub 下载地址拼到镜像前缀后即可走代理加速拉取）。
 */
data class DownloadSource(
  val label: String,
  val url: String,
  val recommended: Boolean = false,
)

/** 加速镜像前缀。把原始 GitHub 下载地址拼到 [ProxyMirror.base] 后即可走代理加速拉取。 */
internal val PROXY_MIRRORS: List<ProxyMirror> = listOf(
  ProxyMirror("cloudflare", "https://gh-proxy.org/"),
  ProxyMirror("Cloudflare (v4推荐)", "https://v4.gh-proxy.org/"),
  ProxyMirror("Cloudflare (v4/v6)", "https://v6.gh-proxy.org/"),
  ProxyMirror("Fastly (v4)", "https://cdn.gh-proxy.org/"),
  ProxyMirror("AxisNow (v4)", "https://axisnow.gh-proxy.org/"),
)

internal data class ProxyMirror(
  val label: String,
  val base: String,
)

/** GitHub Release 中解析出的新版本信息 */
data class AppUpdateInfo(
  val versionName: String,
  val tagName: String,
  val releaseName: String,
  val downloadUrl: String,
  val notes: String,
  val publishedAt: String,
  val sizeBytes: Long,
  val downloadSources: List<DownloadSource> = emptyList(),
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
  fun download(info: AppUpdateInfo, url: String = info.downloadUrl)
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
