package dtv.mobile.platform.douyu
import dtv.mobile.platform.Env1

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder

class DouyuCate2DirectoryApi(
  private val client: HttpClient,
) {
  suspend fun fetchMixListV1(
    cate2Id: String,
    page: Int,
    limit: Int,
  ): DouyuMixListV1Response {
    val currentPage = if (page <= 0) 1 else page
    val path = "${Env1.DIRECTORY_MIX_2}${cate2Id}/$currentPage"
    val url = URLBuilder(path).apply { parameters.append("limit", limit.toString()) }.buildString()
    return client.get(url).body()
  }
}

