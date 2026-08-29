package dtv.mobile.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
actual fun NetworkImage(
  url: String?,
  contentDescription: String?,
  modifier: Modifier,
  contentScale: ContentScale,
) {
  if (url.isNullOrBlank()) return
  AsyncImage(
    // 不指定 size：Coil 会按 Modifier 约束解码，避免把整张大图读进内存
    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
      .data(url)
      .crossfade(true)
      .build(),
    contentDescription = contentDescription,
    modifier = modifier,
    contentScale = contentScale,
  )
}
