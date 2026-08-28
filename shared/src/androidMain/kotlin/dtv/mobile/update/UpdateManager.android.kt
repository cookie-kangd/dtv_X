package dtv.mobile.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import dtv.mobile.util.AppLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/** 检查更新所指向的仓库（发布 Release 的地方） */
private const val UPDATE_REPO_OWNER = "cookie-kangd"
private const val UPDATE_REPO_NAME = "dtv_X"

@Serializable
private data class GitHubRelease(
  @SerialName("tag_name") val tagName: String = "",
  val name: String = "",
  val body: String = "",
  @SerialName("published_at") val publishedAt: String = "",
  @SerialName("html_url") val htmlUrl: String = "",
  val assets: List<GitHubAsset> = emptyList(),
)

@Serializable
private data class GitHubAsset(
  val name: String = "",
  val size: Long = 0,
  @SerialName("browser_download_url") val browserDownloadUrl: String = "",
)

class AndroidUpdateManager(
  private val appContext: Context,
) : UpdateManager {
  override var state: UpdateState by mutableStateOf<UpdateState>(UpdateState.Idle)
    private set

  override val currentVersionName: String = runCatching {
    val pm = appContext.packageManager
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      pm.getPackageInfo(appContext.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
      @Suppress("DEPRECATION")
      pm.getPackageInfo(appContext.packageName, 0)
    }
    info.versionName
  }.getOrNull().orEmpty()

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private val json = Json { ignoreUnknownKeys = true }
  private val client = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(180, TimeUnit.SECONDS)
    .build()

  override fun check() {
    if (state is UpdateState.Checking || state is UpdateState.Downloading) return
    state = UpdateState.Checking
    scope.launch {
      val result = runCatching { fetchLatestRelease() }
      state = result.fold(
        onSuccess = { release ->
          val asset = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
            ?: release.assets.firstOrNull()
          if (asset == null || asset.browserDownloadUrl.isBlank()) {
            UpdateState.Failed("最新 Release 中没有可下载的 APK")
          } else {
            val latestVersion = normalizeVersion(release.tagName).ifBlank { release.name }
            if (compareVersion(latestVersion, currentVersionName) > 0) {
              AppLog.i("DTV-Update", "new version available: $latestVersion (current $currentVersionName)")
              UpdateState.Available(
                AppUpdateInfo(
                  versionName = latestVersion,
                  tagName = release.tagName,
                  releaseName = release.name,
                  downloadUrl = asset.browserDownloadUrl,
                  notes = release.body,
                  publishedAt = release.publishedAt,
                  sizeBytes = asset.size,
                ),
              )
            } else {
              UpdateState.UpToDate
            }
          }
        },
        onFailure = { e ->
          AppLog.e("DTV-Update", "check failed", e)
          UpdateState.Failed(e.message ?: "检查更新失败")
        },
      )
    }
  }

  override fun download(info: AppUpdateInfo) {
    if (state is UpdateState.Downloading) return
    state = UpdateState.Downloading(0f)
    scope.launch {
      val result = runCatching {
        withContext(Dispatchers.IO) {
          val dir = appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: appContext.filesDir
          if (!dir.exists()) dir.mkdirs()
          // 清理历史下载，避免堆积
          dir.listFiles()?.filter { it.name.endsWith(".apk", ignoreCase = true) }?.forEach { it.delete() }

          val target = File(dir, "dtv-X-${info.versionName}.apk")
          val request = Request.Builder()
            .url(info.downloadUrl)
            .header("User-Agent", "DTV-Mobile")
            .build()

          client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("下载失败：HTTP ${response.code}")
            val body = response.body ?: error("下载失败：空响应")
            val total = body.contentLength()
            body.byteStream().use { input ->
              target.outputStream().use { output ->
                val buffer = ByteArray(16 * 1024)
                var downloaded = 0L
                while (true) {
                  val read = input.read(buffer)
                  if (read == -1) break
                  output.write(buffer, 0, read)
                  downloaded += read
                  if (total > 0) {
                    val progress = (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    withContext(Dispatchers.Main.immediate) { state = UpdateState.Downloading(progress) }
                  }
                }
              }
            }
          }
          target
        }
      }

      result.fold(
        onSuccess = { file ->
          AppLog.i("DTV-Update", "downloaded: ${file.absolutePath}")
          state = UpdateState.Downloaded(file.absolutePath)
          install(file.absolutePath)
        },
        onFailure = { e ->
          AppLog.e("DTV-Update", "download failed", e)
          state = UpdateState.Failed(e.message ?: "下载失败")
        },
      )
    }
  }

  override fun install(fileUri: String) {
    runCatching {
      val file = File(fileUri)
      if (!file.exists()) error("安装包不存在")
      val authority = "${appContext.packageName}.fileprovider"
      val uri = FileProvider.getUriForFile(appContext, authority, file)

      // Android 8.0+ 必须在"允许安装未知应用"被授权后才能 startActivity 安装第三方包，
      // 否则 startActivity 会静默失败，用户毫无反应。这里先检查，未授权则跳转到设置。
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        !appContext.packageManager.canRequestPackageInstalls()
      ) {
        val settingsIntent = Intent(
          android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
          android.net.Uri.parse("package:${appContext.packageName}"),
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        appContext.startActivity(settingsIntent)
        state = UpdateState.Failed("请先在系统设置中允许安装来自此来源的应用，然后重新点击「下载并安装」")
        return
      }

      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      appContext.startActivity(intent)
    }.onFailure { e ->
      AppLog.e("DTV-Update", "install failed", e)
      state = UpdateState.Failed(e.message ?: "无法打开安装界面")
    }
  }

  override fun reset() {
    state = UpdateState.Idle
  }

  internal fun dispose() {
    runCatching { client.dispatcher.executorService.shutdown() }
    runCatching { scope.cancel() }
  }

  private suspend fun fetchLatestRelease(): GitHubRelease = withContext(Dispatchers.IO) {
    val url = "https://api.github.com/repos/$UPDATE_REPO_OWNER/$UPDATE_REPO_NAME/releases/latest"
    val request = Request.Builder()
      .url(url)
      .header("Accept", "application/vnd.github+json")
      .header("User-Agent", "DTV-Mobile")
      .build()
    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) error("检查更新失败：HTTP ${response.code}")
      val payload = response.body?.string() ?: error("检查更新失败：空响应")
      json.decodeFromString<GitHubRelease>(payload)
    }
  }
}

@Composable
actual fun rememberUpdateManager(): UpdateManager {
  val context = LocalContext.current
  val manager = remember(context) { AndroidUpdateManager(context.applicationContext) }
  DisposableEffect(manager) {
    onDispose { manager.dispose() }
  }
  return manager
}
