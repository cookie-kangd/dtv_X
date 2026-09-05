package dtv.mobile.platform.bilibili
import dtv.mobile.platform.BilibiliEndpoints

import dtv.mobile.repo.BilibiliQrCode
import dtv.mobile.repo.BilibiliQrPollResult
import dtv.mobile.repo.BilibiliQrStatus
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

class BilibiliAuthApiAndroid(
  private val client: HttpClient,
  private val cookieStore: BilibiliCookieStoreAndroid,
) {
  private val json = Json { ignoreUnknownKeys = true; isLenient = true }

  private fun JsonElement?.stringValueOrNull(): String? {
    val p = this as? kotlinx.serialization.json.JsonPrimitive ?: return null
    if (p is JsonNull) return null
    return p.content
  }

  suspend fun generateQrCode(): BilibiliQrCode {
    val url = BilibiliEndpoints.QRCODE_GENERATE
    val text = client.get(url) {
      headers { append("User-Agent", "Mozilla/5.0") }
    }.bodyAsText()
    val root = json.parseToJsonElement(text).jsonObject
    val data = root["data"]?.jsonObject
    val qrUrl = data?.get("url").stringValueOrNull().orEmpty()
    val key = data?.get("qrcode_key").stringValueOrNull().orEmpty()
    if (qrUrl.isBlank() || key.isBlank()) error("B站二维码生成失败")
    return BilibiliQrCode(url = qrUrl, qrcodeKey = key)
  }

  suspend fun pollQrCode(qrcodeKey: String): BilibiliQrPollResult {
    val url = "${BilibiliEndpoints.QRCODE_POLL}$qrcodeKey"
    val resp: HttpResponse = client.get(url) {
      headers { append("User-Agent", "Mozilla/5.0") }
    }
    val text = resp.bodyAsText()
    val root = json.parseToJsonElement(text).jsonObject
    val data = root["data"]?.jsonObject
    val code = data?.get("code")?.jsonPrimitive?.longOrNull ?: -1L
    val message = data?.get("message").stringValueOrNull()

    val status = when (code) {
      86101L -> BilibiliQrStatus.Waiting
      86090L -> BilibiliQrStatus.Scanned
      86038L -> BilibiliQrStatus.Expired
      0L -> BilibiliQrStatus.Confirmed
      else -> BilibiliQrStatus.Failed
    }

    if (status == BilibiliQrStatus.Confirmed) {
      // 1) 响应头的 Set-Cookie
      val setCookie = resp.headers.getAll("Set-Cookie").orEmpty()
      if (setCookie.isNotEmpty()) cookieStore.mergeFromSetCookieHeaders(setCookie)

      // 2) B站 登录成功后把 SESSDATA / bili_jct / DedeUserID 等放在 data.url 的 query 里，
      //    必须一并取出，否则只靠 Set-Cookie 往往拿不到完整登录态（表现为"退出 App 就掉登录"）。
      val redirectUrl = data?.get("url").stringValueOrNull()
      if (!redirectUrl.isNullOrBlank()) {
        runCatching {
          val params = io.ktor.http.Url(redirectUrl).parameters
          val pairs = params.entries()
            .filter { (key, _) -> key.isNotBlank() }
            .mapNotNull { (key, values) ->
              val value = values.firstOrNull()
              if (value.isNullOrBlank()) null else "$key=$value"
            }
          if (pairs.isNotEmpty()) cookieStore.mergeFromCookieHeader(pairs.joinToString("; "))
        }
      }
    }

    return BilibiliQrPollResult(status = status, message = message)
  }
}

