// CI（GitHub Actions）设置环境变量 DTV_NO_ALIYUN_MIRROR=true 可跳过阿里云镜像，
// 直连 google()/mavenCentral()，避免镜像不稳定导致构建失败。
// 注意：Kotlin DSL 的 pluginManagement 块无法引用脚本顶层变量，故此处内联判断。
pluginManagement {
  repositories {
    val skipAliyun = System.getenv("DTV_NO_ALIYUN_MIRROR") == "true" || System.getenv("DTV_NO_ALIYUN_MIRROR") == "1"
    if (!skipAliyun) {
      // Mirrors (helpful in regions where dl.google.com is unreliable)
      maven("https://maven.aliyun.com/repository/google")
      maven("https://maven.aliyun.com/repository/gradle-plugin")
      maven("https://maven.aliyun.com/repository/public")
    }
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    val skipAliyun = System.getenv("DTV_NO_ALIYUN_MIRROR") == "true" || System.getenv("DTV_NO_ALIYUN_MIRROR") == "1"
    if (!skipAliyun) {
      maven("https://maven.aliyun.com/repository/google")
      maven("https://maven.aliyun.com/repository/public")
    }
    google()
    mavenCentral()
  }
}

rootProject.name = "dtv_mx"

include(":shared")
include(":androidApp")
