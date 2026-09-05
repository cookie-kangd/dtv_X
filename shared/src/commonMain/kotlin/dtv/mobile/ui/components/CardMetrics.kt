package dtv.mobile.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 直播间卡片的尺寸规格。
 *
 * 通过 [LocalCardMetrics] 下发，卡片组件与各列表页统一读取，
 * 这样「小卡片模式」只需在主题层切换一份规格即可全局生效。
 */
@Immutable
data class CardMetrics(
  /** 瀑布/宫格列数 */
  val columns: Int,
  /** 网格间距 */
  val gridSpacing: Dp,
  /** 封面宽高比 */
  val coverRatio: Float,
  /** 卡片圆角 */
  val cornerRadius: Dp,
  /** 人气角标圆角 */
  val badgeCorner: Dp,
  /** 人气角标距边缘间距 */
  val badgeInsetPadding: Dp,
  /** 标题字号 */
  val titleSize: TextUnit,
  /** 主播名字号 */
  val nameSize: TextUnit,
  /** 人气字号 */
  val viewerSize: TextUnit,
  /** 文本内容水平内边距 */
  val contentPaddingH: Dp,
  /** 文本内容垂直内边距 */
  val contentPaddingV: Dp,
  /** 头像尺寸 */
  val avatarSize: Dp,
  /** 标题与主播名之间的间距 */
  val contentSpacing: Dp,
  /** 主播名与头像之间的间距 */
  val infoSpacing: Dp,
  /** 是否紧凑模式 */
  val compact: Boolean,
)

/** 常规卡片（2 列，信息完整） */
val NormalCardMetrics = CardMetrics(
  columns = 2,
  gridSpacing = 10.dp,
  coverRatio = 16f / 10f,
  cornerRadius = 18.dp,
  badgeCorner = 8.dp,
  badgeInsetPadding = 10.dp,
  titleSize = 12.sp,
  nameSize = 11.sp,
  viewerSize = 10.sp,
  contentPaddingH = 12.dp,
  contentPaddingV = 8.dp,
  avatarSize = 18.dp,
  contentSpacing = 4.dp,
  infoSpacing = 8.dp,
  compact = false,
)

/** 小卡片（3 列，一屏可展示更多直播间） */
val CompactCardMetrics = CardMetrics(
  columns = 3,
  gridSpacing = 8.dp,
  coverRatio = 16f / 10f,
  cornerRadius = 12.dp,
  badgeCorner = 6.dp,
  badgeInsetPadding = 6.dp,
  titleSize = 11.sp,
  nameSize = 9.sp,
  viewerSize = 9.sp,
  contentPaddingH = 7.dp,
  contentPaddingV = 6.dp,
  avatarSize = 14.dp,
  contentSpacing = 2.dp,
  infoSpacing = 5.dp,
  compact = true,
)

val LocalCardMetrics = staticCompositionLocalOf { NormalCardMetrics }
