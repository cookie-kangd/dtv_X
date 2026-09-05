package dtv.mobile.platform

object Env1 {
    const val HOST = "https://www.douyu.com"

    const val MOBILE_HOST = "https://m.douyu.com"

    const val H5_PLAY = "$HOST/lapi/live/getH5Play/"

    const val HOME_H5_ENC = "$HOST/swf_api/homeH5Enc?rids="

    const val BETARD = "$HOST/betard/"

    const val SEARCH_USER = "$HOST/japi/search/api/searchUser"

    const val SEARCH_PAGE = "$HOST/search/"

    const val CATE_LIST = "$MOBILE_HOST/api/cate/list"

    const val CATE_NEW_REC_LIST = "$MOBILE_HOST/hgapi/live/cate/newRecList"

    const val DIRECTORY_MIX_2 = "$HOST/gapi/rkc/directory/mixListV1/2_"

    const val DIRECTORY_MIX_3 = "$HOST/gapi/rkc/directory/mixListV1/3_"

    const val THREE_CATE = "https://capi.douyucdn.cn/api/v1/getThreeCate"

    const val DANMU_WS = "wss://danmuproxy.douyu.com:8506/"
}

object Env2 {
    const val HOST = "https://www.huya.com"

    const val MOBILE_HOST = "https://m.huya.com"

    const val PROFILE_ROOM = "https://mp.huya.com/cache.php?m=Live&do=profileRoom&roomid="

    const val LIVE_LIST = "https://live.huya.com/liveHttpUI/getLiveList?iGid="

    const val SEARCH_CDN = "https://search.cdn.huya.com/"

    const val SEARCH_PAGE = "$HOST/search/"

    const val DANMU_WS = "wss://cdnws.api.huya.com"
}

object Env3 {
    const val HOST = "https://live.douyin.com"

    const val ROOM_ENTER = "$HOST/webcast/room/web/enter/?"

    const val PARTITION_DETAIL = "$HOST/webcast/web/partition/detail/room/v2/?"

    val DANMU_WS_HOSTS = listOf(
    "wss://webcast5-ws-web-hl.douyin.com/webcast/im/push/v2/?",
    "wss://webcast3-ws-web-lq.douyin.com/webcast/im/push/v2/?",
    "wss://webcast5-ws-web-lf.douyin.com/webcast/im/push/v2/?",
  )
}

object Env4 {
    const val HOST = "https://www.bilibili.com"

    const val LIVE_HOST = "https://live.bilibili.com"

    const val API_HOST = "https://api.bilibili.com"

    const val LIVE_API_HOST = "https://api.live.bilibili.com"

    const val PASSPORT_HOST = "https://passport.bilibili.com"

    const val ROOM_PLAY_INFO = "$LIVE_API_HOST/xlive/web-room/v2/index/getRoomPlayInfo?room_id="

    const val DANMU_INFO = "$LIVE_API_HOST/xlive/web-room/v1/index/getDanmuInfo?"

    const val NAV = "$API_HOST/x/web-interface/nav"

    const val FINGER_SPI = "$API_HOST/x/frontend/finger/spi"

    const val SEARCH_TYPE = "$API_HOST/x/web-interface/search/type"

    const val QRCODE_GENERATE = "$PASSPORT_HOST/x/passport-login/web/qrcode/generate"

    const val QRCODE_POLL = "$PASSPORT_HOST/x/passport-login/web/qrcode/poll?qrcode_key="

    const val LIST_PAGE_LOL = "$LIVE_HOST/lol"

    const val AREA_LIST_SECOND = "$LIVE_API_HOST/xlive/web-interface/v1/second/getList"

    const val INDEX_LIST = "$LIVE_API_HOST/xlive/web-interface/v1/index/getList?platform=web&parent_area_id=0&area_id=0&page="
}

object Env5 {
  const val HOST = "https://www.xiaohongshu.com"
  const val LIVE_ROOM_HOST = "https://live-room.xiaohongshu.com"
  const val USER_STATUS = "$LIVE_ROOM_HOST/api/sns/v1/live/user_status?"
  const val SHARE_INFO = "$HOST/api/sns/red/live/app/v1/ecology/outside/share_info?"
  const val LIVE_PAGE = "$HOST/hina/livestream/"
  const val STREAM_CDN = "http://live-play.xhscdn.com/live/"
  const val STREAM_CDN_ALT = "http://live-source-play.xhscdn.com/live/"
}

object Env6 {
  const val HOST = "https://www.twitch.tv"
  const val GQL = "https://gql.twitch.tv/gql"
  const val CLIENT_ID = "kimne78kx3ncx6brgo4mv6wki5h1ko"
  const val USHER = "https://usher.ttvnw.net/api/channel/hls/"
  const val HELIX = "https://api.twitch.tv/helix"
  const val OAUTH_TOKEN = "https://id.twitch.tv/oauth2/token"
  const val DANMU_WS = "wss://irc-ws.chat.twitch.tv:443"
  const val PUBSUB_WS = "wss://pubsub-edge.twitch.tv"
}
