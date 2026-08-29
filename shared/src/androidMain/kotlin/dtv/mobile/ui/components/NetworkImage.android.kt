package dtv.mobile.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
actual fun NetworkImage(
  url: String?,
  contentDescription: String?,
  modifier: Modifier,
  contentScale: ContentScale,
) {
  if (url.isNullOrBlank()) return
  // 部分平台图床（如抖音）会对不带 Referer / User-Agent 的请求返回 403，
  // 导致封面/头像加载不出来。补充自源站 Referer 与移动端 UA，提升图片加载成功率。
  val origin = runCatching {
    val u = android.net.Uri.parse(url)
    "${u.scheme ?: "https"}://${u.host ?: "live.douyin.com"}"
  }.getOrDefault("https://live.douyin.com")
  AsyncImage(
    // 不指定 size：Coil 会按 Modifier 约束解码，避免把整张大图读进内存
    model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
      .data(url)
      .crossfade(true)
      .setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
      .setHeader("Referer", origin)
      .build(),
    contentDescription = contentDescription,
    modifier = modifier,
    contentScale = contentScale,
  )
}
