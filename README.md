<p align="center"><img src="screenshots/icon.png" width="96" height="96" alt="dtv_mx 安卓直播聚合 App 图标" /></p>

<h3 align="center">dtv_mx</h3>

<p align="center">
  安卓直播聚合 App —— 抖音、B站、斗鱼、虎牙 四大平台直播一站式观看（开源 / 免费 / 无广告 / 非官方）
</p>

<p align="center">
  <a href="https://github.com/cookie-kangd/dtv_mx/releases/latest"><img src="https://img.shields.io/github/v/release/cookie-kangd/dtv_mx?label=%E6%9C%80%E6%96%B0%E7%89%88%E6%9C%AC" alt="最新版本" /></a>
  <a href="https://github.com/cookie-kangd/dtv_mx/releases"><img src="https://img.shields.io/github/downloads/cookie-kangd/dtv_mx/total?label=%E4%B8%8B%E8%BD%BD%E9%87%8F" alt="下载量" /></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white" alt="Android 8.0+" />
  <img src="https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin Multiplatform" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4" alt="Jetpack Compose" />
</p>

> 本仓库基于 [chen-zeong/dtv_mobile](https://github.com/chen-zeong/dtv_mobile) 的修改版本，初衷是让自己用得更顺手。若觉得好用，打赏请去上游作者仓库，感谢作者的开源。

---

## 简介

**dtv_mx** 是一款面向 Android 手机的**轻量化直播聚合应用**，用 Kotlin Multiplatform + Jetpack Compose 编写，播放内核为 Android Media3（ExoPlayer）。它把 **抖音直播、B站（哔哩哔哩）直播、斗鱼直播、虎牙直播** 汇总到同一个 App 内，一个入口就能浏览分区、搜索主播、关注收藏、看弹幕，不必在多个平台 App 之间反复切换。

安装包约 19 MB，只保留看直播真正需要的功能：**无广告、无开屏、无推荐信息流、不需要注册账号**（仅 B站 直播如需登录态才用到扫码/Cookie 登录）。

**适合谁**：同时关注多个平台主播、嫌各家官方 App 太臃肿、想用一个 App 横屏看直播的人。

## 下载安装

- 前往 **[Releases 页面](https://github.com/cookie-kangd/dtv_mx/releases/latest)** 下载最新的 `dtv-mx-<版本号>.apk` 直接安装
- 系统要求：**Android 8.0（API 26）及以上**，支持 `arm64-v8a` / `armeabi-v7a`
- App 内可直接检查更新：**设置 → 检查更新**，会读取本仓库 Release 并下载安装
- 所有版本均由 GitHub Actions 自动构建、使用同一签名证书，可直接覆盖安装（OTA 升级不会提示签名冲突）
- 官方网站 / 文档站点：<https://cookie-kangd.github.io/dtv_mx/>（独立可索引页面，含功能介绍、截图与下载入口，便于搜索引擎收录）

## 功能

| 模块 | 说明 |
| --- | --- |
| 支持平台 | 抖音 / B站（哔哩哔哩） / 斗鱼 / 虎牙 直播 |
| 分区浏览 | 按平台分类浏览直播列表，可订阅常用分区做快速入口，并记忆各平台上次所在分类 |
| 关注管理 | 一键关注 / 取消关注；首页支持置顶与长按拖拽排序 |
| 搜索 | 按平台搜索主播与直播间；B站 支持登录 / 退出 |
| 播放 | Android Media3（ExoPlayer）播放；全屏与横竖屏适配；画质档位（最高 / 高 / 中 / 低）；多线路切换 |
| 弹幕 | 实时弹幕；关键词屏蔽；字号、透明度、显示区域可调 |
| 退出清理 | 「退出时清理缓存」开关（默认开启）：退出 App 自动清理直播缓存与临时文件，**登录状态、设置、关注列表全部保留** |
| 平台设置 | 自定义底部导航显示哪些平台以及它们的排列顺序 |
| 数据同步 | 局域网共享 / 导入关注、分区订阅与屏蔽词 |
| 外观 | 浅色 / 深色 / 跟随系统主题，可自定义主色调 |
| 更新 | 设置内一键查询仓库 Release 并下载安装 |

## 截图

| 首页                                                       | 播放                                                         | 分区（斗鱼）                                                    |
| -------------------------------------------------------- | ---------------------------------------------------------- | --------------------------------------------------------- |
| <img src="screenshots/home.jpeg" width="240" alt="dtv_mx 首页 关注列表" /> | <img src="screenshots/player.jpeg" width="240" alt="dtv_mx 直播播放 弹幕" /> | <img src="screenshots/douyu.jpeg" width="240" alt="dtv_mx 斗鱼直播分区" /> |

| B站                                                    |
| ----------------------------------------------------- |
| <img src="screenshots/b.jpeg" width="240" alt="dtv_mx B站哔哩哔哩直播" /> |

## 常见问题

**装完打不开 / 提示未知来源？**
Android 8.0 以上需要在系统里允许该来源安装应用，安装后正常开启即可。

**从旧版本 dtv_X 升级会丢数据吗？**
不会。仓库与 App 名称由 `dtv_X` 更名为 `dtv_mx`，包名与签名证书未变，直接覆盖安装即可，关注列表与设置都保留。

**「退出时清理缓存」会把 B站 登录清掉吗？**
不会。清理只针对播放缓存与临时文件目录，登录凭据、偏好设置、关注/订阅数据存放在独立位置，不参与清理。

**某个平台的直播打不开了？**
直播地址与列表都来自第三方平台的公开接口，平台改接口后可能失效，需要等版本更新适配。

**支持投屏 / 录制 / 电视端吗？**
目前不支持，仅面向 Android 手机端观看。

## 自行构建

```bash
git clone https://github.com/cookie-kangd/dtv_mx.git
cd dtv_mx
./gradlew :androidApp:assembleRelease
```

环境要求：JDK 17、Android SDK（compileSdk 36）。产物位于 `androidApp/build/outputs/apk/release/`。

## 更新日志

完整版本记录见 [CHANGELOG.md](CHANGELOG.md)。最近一次为 **v0.1.9**：新增「退出时清理缓存」、仓库与 App 更名为 dtv_mx、修复检查更新指向旧仓库名的问题。

## 说明

- 本项目仅用于学习与技术交流，非任何平台官方产品，与抖音、哔哩哔哩、斗鱼、虎牙均无关联
- 播放内容与相关数据来自第三方平台公开接口，版权归属第三方，可能随平台调整而失效
- 请遵守各平台的用户协议，不要用于商业用途或二次分发牟利

## 关键词

安卓直播软件、手机直播聚合 App、抖音直播、B站直播、哔哩哔哩直播、斗鱼直播、虎牙直播、直播弹幕、横屏看直播、开源直播播放器、无广告直播 App、多平台直播合一、直播源聚合、dtv_mx 官网、dtv_mx 下载、Android live streaming aggregator, douyin / bilibili / douyu / huya live app, Kotlin Multiplatform, Jetpack Compose, ExoPlayer, open source Android live TV player.
