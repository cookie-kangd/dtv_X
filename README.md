# dtv_mx

> 轻量安卓直播聚合客户端 —— 斗鱼 / 虎牙 / 抖音 / B站（哔哩哔哩）一站式看直播，无广告、免登录、专注播放体验。

基于 [Kotlin Multiplatform + Compose Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/) 构建的开源 Android 直播聚合 App，支持直播流播放、实时弹幕、分区浏览、主播搜索、关注收藏、画中画小窗与熄屏听播。

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-brightgreen)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue)]()
[![License](https://img.shields.io/badge/License-学习交流-lightgrey)]()

上游项目：[chen-zeong/dtv_mobile](https://github.com/chen-zeong/dtv_mobile)

---

## 下载

前往 [Releases](https://github.com/cookie-kangd/dtv_mx/releases/latest) 下载最新的 `dtv-mx-<版本>.apk` 安装即可。

- **系统要求**：Android 8.0（API 26）及以上
- **覆盖安装**：所有版本使用同一签名，可直接覆盖升级，关注列表与设置均保留
- **架构支持**：`armeabi-v7a` / `arm64-v8a`（已剔除手机用不到的 x86 架构，体积更小）

---

## 功能

### 直播聚合
- **四大平台**：抖音 / 哔哩哔哩（B站）/ 斗鱼 / 虎牙，一个 App 全搞定
- **分区浏览**：各平台官方分区分类，支持二级、三级分类记忆
- **搜索主播**：按平台搜索直播间
- **关注收藏**：关注列表跨会话持久化，支持分类记忆

### 播放体验
- **画质档位**：默认取最高码率，网络差时可手动下调换取流畅
- **多线路切换**：斗鱼等平台支持 CDN 线路切换
- **横竖屏自适应**：播放器控件按视频区可用高度整体缩放，竖屏下 5 个按钮也全部可见
- **画中画（PiP）**：系统级小窗悬浮，边看直播边做别的事
- **熄屏听播**：关屏后仅保留一路音频解码，省电省流量

### 弹幕
- **实时弹幕**：四平台 WebSocket 实时接收
- **滚动弹幕**：横屏全屏下的轨道滚动弹幕，自动分配轨道避免重叠
- **关键词屏蔽**：自定义屏蔽词过滤
- **显示定制**：字号、透明度、显示区域均可调

### 外观
- **主题模式**：浅色 / 深色 / 跟随系统，三档可选并持久化
  - 浅色：明亮通透的浅色主题
  - 深色：冷色调深蓝黑主题，降低夜间观看疲劳
  - 跟随系统：随系统深色模式自动切换
- **全局强调色**：自定义主题色，应用到选中高亮、弹幕昵称等位置

### 其它
- **退出时清理缓存**（默认开启）：每次退出自动清理播放缓存与临时文件，不清除登录与设置
- **内置更新检查**：启动时检测 GitHub Release 新版本

---

## 常见问题

**从旧版 dtv_X 升级会丢数据吗？**
不会。包名与签名未变，覆盖安装即可保留关注与设置。

**「退出时清理缓存」会清掉 B站 登录吗？**
不会，只清理播放缓存与临时文件，登录 Cookie、设置、关注列表均保留。

**画中画怎么用？**
在播放器右侧控制栏（耳机按钮上方）点击画中画图标，即可把直播以小窗形式悬浮在系统中。

**深色模式在哪里开启？**
设置 → 基本设置 → 主题模式，选择「深色」。选择「跟随系统」则会读取系统的深色模式设置自动切换。

**某个平台的直播打不开了？**
第三方接口变动所致，等版本更新适配。

**支持电视 / 盒子吗？**
未做 TV 适配，目前面向手机与平板的触屏交互设计。

---

## 自行构建

**环境要求**
- JDK 17
- Android SDK，需安装 `compileSdk 36` 与 `build-tools 36.1.0`
-（可选）配置 `ANDROID_HOME` 环境变量

**构建 Debug 包**

```bash
./gradlew :androidApp:assembleDebug
```

**构建 Release 包（需签名）**

Release 构建需要签名配置。在项目根目录的 `androidApp/` 下创建 `keystore.properties`：

```properties
storeFile=/absolute/path/to/your.keystore
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

然后执行：

```bash
./gradlew :androidApp:assembleRelease
```

> 未配置 `keystore.properties` 时，Release 构建会走无签名分支，产物无法在真机安装。

**关于 R8 压缩**
Release 构建刻意**未开启** R8 代码压缩：开启后斗鱼直播间会出现黑屏 + "N5.0" 白字。为保证功能稳定，这里以体积换稳定性。

---

## 技术栈

| 类别 | 选型 |
|---|---|
| 语言 / 框架 | Kotlin 2.0.21、Kotlin Multiplatform、Compose Multiplatform 1.6.11 |
| 播放器 | androidx.media3（ExoPlayer）1.10.1 |
| 网络 | Ktor Client、OkHttp |
| 异步 | kotlinx.coroutines |
| 序列化 | kotlinx.serialization |
| 图片 | Coil |
| JS 引擎 | Rhino、QuickJS（平台签名算法的纯 Kotlin 重写） |
| 扫码 | ZXing |

---

## 说明

- 本项目仅用于**学习与技术交流**，非任何平台官方产品，与抖音、哔哩哔哩、斗鱼、虎牙均无关联
- 播放内容来自第三方公开接口，**版权归属第三方**，请遵守各平台用户协议，**勿作商业用途**
- 不提供任何形式的录制、下载或绕过平台限制的能力

---

## 关键词

Android 直播聚合 · 直播聚合客户端 · 斗鱼直播 · 虎牙直播 · 抖音直播 · B站直播 · 哔哩哔哩直播 · 弹幕播放器 · 画中画直播 · Kotlin Multiplatform · Compose Multiplatform · 开源直播 App · 免广告直播客户端
