package dtv.mobile.platform

/**
 * 四平台端点集中配置（唯一出处）。
 *
 * 斗鱼/虎牙/抖音/B站的 API 接口、房间流解析入口、弹幕网关地址全部收敛在本文件。
 * 官方平台更换地址时，只需要改这里对应的常量，业务代码无需再动。
 *
 * 约定：
 * - 常量均为编译期 const（可用 string 模板互相拼装）；
 * - 带尾随占位符的接口（如 H5_PLAY、BETARD）保留结尾的 `/` 或 `=`，
 *   调用方直接拼接 roomId 等参数；
 * - Referer/Origin 一律用 `XXX_HOST + "/"` 或 `XXX_HOST` 表达，不单独设常量。
 */
object DouyuEndpoints {
  /** 站点主机（Referer / Referrer / 房间页） */
  const val HOST = "https://www.douyu.com"

  /** 移动端 API 主机 */
  const val MOBILE_HOST = "https://m.douyu.com"

  /** 房间流解析：H5 播放接口（尾随 `/`，拼 roomId） */
  const val H5_PLAY = "$HOST/lapi/live/getH5Play/"

  /** 房间流解析：播放页签名接口（尾随 `rids=`，拼 roomId） */
  const val HOME_H5_ENC = "$HOST/swf_api/homeH5Enc?rids="

  /** 房间信息 betard 接口（尾随 `/`，拼 roomId） */
  const val BETARD = "$HOST/betard/"

  /** 用户搜索 */
  const val SEARCH_USER = "$HOST/japi/search/api/searchUser"

  /** 搜索页（HTML） */
  const val SEARCH_PAGE = "$HOST/search/"

  /** 全量分类列表 */
  const val CATE_LIST = "$MOBILE_HOST/api/cate/list"

  /** 分类推荐列表 */
  const val CATE_NEW_REC_LIST = "$MOBILE_HOST/hgapi/live/cate/newRecList"

  /** 二级分类目录（尾随 `2_`，拼 cate2Id/page） */
  const val DIRECTORY_MIX_2 = "$HOST/gapi/rkc/directory/mixListV1/2_"

  /** 三级分类目录（尾随 `3_`，拼 cate3Id/page） */
  const val DIRECTORY_MIX_3 = "$HOST/gapi/rkc/directory/mixListV1/3_"

  /** 三级分类聚合 */
  const val THREE_CATE = "https://capi.douyucdn.cn/api/v1/getThreeCate"

  /** 弹幕网关（WebSocket） */
  const val DANMU_WS = "wss://danmuproxy.douyu.com:8506/"
}

object HuyaEndpoints {
  /** 站点主机（房间页 / Origin） */
  const val HOST = "https://www.huya.com"

  /** 移动端主机（部分接口的 Referer） */
  const val MOBILE_HOST = "https://m.huya.com"

  /** 房间信息接口（尾随 `roomid=`，拼 roomId；可再拼 `&showSecret=1`） */
  const val PROFILE_ROOM = "https://mp.huya.com/cache.php?m=Live&do=profileRoom&roomid="

  /** 分类直播间列表（尾随 `iGid=`，拼 gid 及分页参数） */
  const val LIVE_LIST = "https://live.huya.com/liveHttpUI/getLiveList?iGid="

  /** 搜索接口（HTML/JSON 混合，尾随 `/`） */
  const val SEARCH_CDN = "https://search.cdn.huya.com/"

  /** 搜索页（HTML） */
  const val SEARCH_PAGE = "$HOST/search/"

  /** 弹幕网关（WebSocket） */
  const val DANMU_WS = "wss://cdnws.api.huya.com"
}

object DouyinEndpoints {
  /** 站点主机（房间页 / Origin / Referer） */
  const val HOST = "https://live.douyin.com"

  /** 房间进入接口（尾随 `?`，拼 query 与 a_bogus 签名） */
  const val ROOM_ENTER = "$HOST/webcast/room/web/enter/?"

  /** 分区房间列表接口（尾随 `?`，拼 query 与 a_bogus 签名） */
  const val PARTITION_DETAIL = "$HOST/webcast/web/partition/detail/room/v2/?"

  /** 弹幕网关候选（WebSocket，按顺序尝试） */
  val DANMU_WS_HOSTS = listOf(
    "wss://webcast5-ws-web-hl.douyin.com/webcast/im/push/v2/?",
    "wss://webcast3-ws-web-lq.douyin.com/webcast/im/push/v2/?",
    "wss://webcast5-ws-web-lf.douyin.com/webcast/im/push/v2/?",
  )
}

object BilibiliEndpoints {
  /** 主站主机（Referer） */
  const val HOST = "https://www.bilibili.com"

  /** 直播站主机（房间页 / Origin / Referer） */
  const val LIVE_HOST = "https://live.bilibili.com"

  /** 主站 API 主机 */
  const val API_HOST = "https://api.bilibili.com"

  /** 直播 API 主机 */
  const val LIVE_API_HOST = "https://api.live.bilibili.com"

  /** 登录 API 主机 */
  const val PASSPORT_HOST = "https://passport.bilibili.com"

  /** 房间流解析：播放信息接口（尾随 `room_id=`，拼 roomId 及参数） */
  const val ROOM_PLAY_INFO = "$LIVE_API_HOST/xlive/web-room/v2/index/getRoomPlayInfo?room_id="

  /** 弹幕握手信息接口（尾随 `?`，拼 query） */
  const val DANMU_INFO = "$LIVE_API_HOST/xlive/web-room/v1/index/getDanmuInfo?"

  /** 登录态 / 用户信息 */
  const val NAV = "$API_HOST/x/web-interface/nav"

  /** 设备指纹（buvid3/buvid4） */
  const val FINGER_SPI = "$API_HOST/x/frontend/finger/spi"

  /** 搜索接口 */
  const val SEARCH_TYPE = "$API_HOST/x/web-interface/search/type"

  /** 扫码登录：生成二维码 */
  const val QRCODE_GENERATE = "$PASSPORT_HOST/x/passport-login/web/qrcode/generate"

  /** 扫码登录：轮询扫码结果（尾随 `qrcode_key=`） */
  const val QRCODE_POLL = "$PASSPORT_HOST/x/passport-login/web/qrcode/poll?qrcode_key="

  /** 直播首页分区页（HTML，用于抓取分区列表） */
  const val LIST_PAGE_LOL = "$LIVE_HOST/lol"

  /** 二级分区房间列表 */
  const val AREA_LIST_SECOND = "$LIVE_API_HOST/xlive/web-interface/v1/second/getList"

  /** 首页推荐列表（尾随 `page=`，拼页码及 page_size） */
  const val INDEX_LIST = "$LIVE_API_HOST/xlive/web-interface/v1/index/getList?platform=web&parent_area_id=0&area_id=0&page="
}
