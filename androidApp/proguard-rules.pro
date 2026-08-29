# ---------------------------------------------------------------------------
# DTV X release 构建规则
#
# 策略：保留全部业务代码（dtv.mobile.**）不动，只让 R8 压缩 / 移除未使用的
# 第三方库代码与资源。这样既能在体积与启动速度上拿到收益，又不会影响
# 序列化、反射或 JSON 解析等可能踩坑的地方。
# ---------------------------------------------------------------------------

# ---- 业务代码：整体保留 ---------------------------------------------------
-keep class dtv.mobile.** { *; }
-keepclassmembers class dtv.mobile.** { *; }
-keepclasseswithmembers class dtv.mobile.** { *; }

# ---- 通用属性 -------------------------------------------------------------
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- kotlinx.serialization ------------------------------------------------
# 业务代码已整体保留，这里只消除 R8 对序列化生成类的警告
-keepclassmembers class **$serializer { *; }
-keepclassmembers class **$Companion { kotlinx.serialization.KSerializer serializer(...); }
-dontwarn kotlinx.serialization.**
-dontnote kotlinx.serialization.**

# ---- Ktor -----------------------------------------------------------------
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.**
-dontwarn org.slf4j.**
-keep class io.ktor.client.plugins.** { *; }

# ---- OkHttp / Okio --------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn com.google.android.gms.**

# ---- Media3 (ExoPlayer) ---------------------------------------------------
-dontwarn androidx.media3.**
-keepclassmembers class androidx.media3.** { *; }
-dontwarn com.google.android.exoplayer2.**

# ---- ZXing (二维码生成) ---------------------------------------------------
-dontwarn com.google.zxing.**
-keep class com.google.zxing.** { *; }

# ---- WebView JS 接口（B站账号密码登录页）-----------------------------------
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ---- Room / 资源 ----------------------------------------------------------
-keep class **.R$* { *; }
-keep class **.R { *; }

# ---- 移除日志（release 下不再输出）----------------------------------------
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}
