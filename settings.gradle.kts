// CI (GitHub Actions) 可通过 -Pdtv.noAliyunMirror=true 跳过阿里云镜像，
// 直连 google()/mavenCentral()，避免镜像不稳定导致的构建失败。
val useAliyunMirror = !providers.gradleProperty("dtv.noAliyunMirror").orNull.toBoolean()

pluginManagement {
  repositories {
    // Mirrors (helpful in regions where dl.google.com is unreliable)
    if (useAliyunMirror) {
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
    if (useAliyunMirror) {
      maven("https://maven.aliyun.com/repository/google")
      maven("https://maven.aliyun.com/repository/public")
    }
    google()
    mavenCentral()
  }
}

rootProject.name = "DTV-mobile"

include(":shared")
include(":androidApp")
