package dtv.mobile.state

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 播放画质档位：控制允许选择的视频轨分辨率上限。
 *
 * - [Highest]（默认）：不限制分辨率，并强制取当前流的最高码率，优先保证画面清晰
 * - 其余档位：按档位限制最大分辨率，剩余部分交给 ExoPlayer 的自适应逻辑
 */
enum class VideoQuality(
  val label: String,
  val maxWidth: Int,
  val maxHeight: Int,
  val forceHighestSupportedBitrate: Boolean,
) {
  Highest("最高", Int.MAX_VALUE, Int.MAX_VALUE, true),
  High("高", 1920, 1080, false),
  Medium("中", 1280, 720, false),
  Low("低", 854, 480, false),
  ;

  companion object {
    /** 从持久化字符串还原，未知值回退到最高画质 */
    fun fromNameOrHighest(raw: String?): VideoQuality =
      entries.firstOrNull { it.name == raw } ?: Highest
  }
}

val LocalVideoQuality = staticCompositionLocalOf { VideoQuality.Highest }
