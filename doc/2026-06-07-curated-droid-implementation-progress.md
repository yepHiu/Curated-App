# Curated Droid 阶段性实施进度

日期：2026-06-07  
状态：Phase 0/Phase 1 早期实施中  
范围：基于 Findroid 代码底座，开始接入 Curated REST API，并先隐藏当前无需求的下载、电视剧集和用户管理入口。

## 1. 本轮已经落地的改动

### 1.1 Curated API 基础层

新增位置：

```text
data/src/main/java/dev/jdtech/jellyfin/curated/api/
data/src/test/java/dev/jdtech/jellyfin/curated/api/
```

已实现：

- `CuratedUrlResolver`
  - 统一处理用户输入的 `baseUrl`。
  - 接受带 `/api` 或不带 `/api` 的输入。
  - 将后端返回的相对媒体 URL 转为绝对 URL。
- `CuratedErrorMapper`
  - 映射 Curated `AppError`。
  - 已覆盖 `AUTH_LOCKED`、`AUTH_INVALID_PIN`、`COMMON_NOT_FOUND`、`COMMON_CONFLICT`、5xx 和非法错误体。
- Curated DTO/domain/mapper
  - 已加入 `HealthDto`、`AuthStatusDto`、`MovieListItemDto`、`MoviesPageDto`、`MovieDetailDto`、`PlaybackDescriptorDto`。
  - 已加入 `MovieListItem`、`MoviesPage`、`MovieDetail`、`PlaybackDescriptor`、`PlaybackMode`。
  - 图片 URL、预览 URL、播放 URL 会在 mapper 中转成绝对 URL。
- `CuratedApiClient`
  - 已支持：
    - `GET /api/health`
    - `GET /api/auth/status`
    - `POST /api/auth/unlock`
    - `GET /api/library/movies`
    - `GET /api/library/movies/{movieId}`
    - `GET /api/library/movies/{movieId}/playback`
  - movieId 通过 OkHttp `addPathSegment` 编码，避免字符串 ID 包含 `/` 等字符时拼坏 URL。
- `CuratedCookieJar`
  - 目前是内存 CookieJar。
  - 可保存、匹配并清理 `curated_auth` 等 Cookie。
  - 后续 release 前需要改成受保护的持久化 Cookie store。

新增测试：

```text
CuratedUrlResolverTest
CuratedErrorMapperTest
CuratedDtoMapperTest
CuratedApiClientTest
CuratedCookieJarTest
```

### 1.2 CuratedRepository 基础封装

新增位置：

```text
data/src/main/java/dev/jdtech/jellyfin/curated/repository/
data/src/test/java/dev/jdtech/jellyfin/curated/repository/
```

已实现：

- `CuratedRepository`
  - 当前只暴露 MVP 下一步会用到的电影浏览和播放 descriptor：
    - `getMovies(...)`
    - `getMovie(movieId)`
    - `getPlaybackDescriptor(movieId)`
- `CuratedRepositoryImpl`
  - 内部使用 `CuratedApiClient`。
  - 在 `Dispatchers.IO` 上执行同步 OkHttp 调用。
  - 返回 domain model，不直接把 DTO 暴露给上层。

新增测试：

```text
CuratedRepositoryTest
```

测试覆盖：

- 电影分页接口请求路径正确。
- 相对封面/缩略图 URL 转绝对 URL。
- 电影详情预览图和演员头像 URL 转绝对 URL。
- playback descriptor 的 HLS URL 转绝对 URL。

### 1.3 AddServer 先尝试 Curated health

修改位置：

```text
setup/src/main/java/dev/jdtech/jellyfin/setup/data/SetupRepositoryImpl.kt
setup/src/test/java/dev/jdtech/jellyfin/setup/data/SetupRepositoryImplTest.kt
setup/build.gradle.kts
```

已实现：

- `SetupRepositoryImpl.addServer(address)` 现在先尝试：

```text
GET {baseUrl}/api/health
```

- 如果 Curated health 成功：
  - 使用规范化后的 base URL。
  - 生成稳定的 UUID 字符串作为本地 server id。
  - 保存 `Server` 和 `ServerAddress`。
  - `currentUserId` 保持为 `null`，因为 Curated MVP 没有 Jellyfin 用户。
  - 保存后直接进入 Home，不再进入 Users/Login。
- 如果 Curated health 失败：
  - 保留原 Jellyfin discovery 作为兜底，降低当前迁移风险。

新增测试：

```text
SetupRepositoryImplTest
```

测试覆盖：

- 同一个 base URL 带不带 `/api` 会生成同一个 server id。
- server id 是导航安全的 UUID 字符串，而不是原始 URL。

### 1.4 手机端导航入口隐藏

修改位置：

```text
app/phone/src/main/java/dev/jdtech/jellyfin/NavigationRoot.kt
app/phone/src/test/java/dev/jdtech/jellyfin/NavigationRootTest.kt
app/phone/build.gradle.kts
```

已实现：

- 启动目标改为不再依赖 `hasCurrentUser`：
  - 有 server 且有 current server：进入 `HomeRoute`
  - 有 server 但无 current server：进入 `ServersRoute`
  - 无 server：进入 `WelcomeRoute`
- 底部导航只保留：

```text
Home
Media
```

- `DownloadsRoute` 不再出现在底部导航。
- 选择服务器后不再进入 Users，而是回到 Home。
- AddServer 成功后不再进入 Users，而是回到 Home。
- Settings 中收到 `NavigateToUsers` 时不再跳转 Users。
- `curatedRouteForItem` 不再为以下类型返回 route：
  - `FindroidShow`
  - `FindroidSeason`
  - `FindroidEpisode`
  - TV Shows 类型的 `FindroidCollection`

新增测试：

```text
NavigationRootTest
```

测试覆盖：

- 当前 server 存在但没有 current user 时，启动目标是 Home。
- online/offline 模式下底部导航都不包含 Downloads。
- Show/Season/Episode 不再得到可导航 route。
- Movie 仍然保留详情 route。
- TV 相关 item 不再被视为 Curated 可见 item。

### 1.5 Home/Media 中隐藏 TV 内容

修改位置：

```text
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/film/HomeScreen.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/film/MediaScreen.kt
```

已实现：

- Home 页面过滤：
  - suggestions 中的 Show/Season/Episode。
  - continue watching 中的 Show/Season/Episode。
  - next up 中的 Episode。
  - TV Shows 类型的 library view。
- Media 页面过滤：
  - TV Shows 类型的 library。
  - 搜索结果中的 Show/Season/Episode。

说明：

- 这仍是入口层隐藏，不代表底层 Jellyfin show/episode 代码已经删除。
- 后续迁移到 `CuratedRepository` 后，Home/Media 本身就不应再拿到 TV 类型。

### 1.6 电影详情隐藏下载入口

修改位置：

```text
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/film/MovieScreen.kt
```

已实现：

- `MovieScreen` 不再注入 `DownloaderViewModel`。
- 不再监听下载事件。
- `ItemButtonsBar` 传入 `downloaderState = null`，因此不展示下载、取消下载、删除下载入口。

### 1.7 Settings 隐藏下载、离线模式和用户管理

修改位置：

```text
settings/src/main/java/dev/jdtech/jellyfin/settings/presentation/settings/SettingsViewModel.kt
settings/src/test/java/dev/jdtech/jellyfin/settings/presentation/settings/SettingsViewModelTest.kt
settings/build.gradle.kts
```

已实现：

- 根级 Settings 隐藏：
  - Offline mode
  - Users
  - Downloads
- 如果通过旧 route 直接进入这些分类，返回空设置组，不继续展示旧功能。

新增测试：

```text
SettingsViewModelTest
```

测试覆盖：

- Curated MVP 下 `offline_mode`、`users`、`title_download` 被识别为隐藏顶层设置。
- Servers、Player、About 不被隐藏。

### 1.8 共享 Curated OkHttpClient 与 CookieJar

修改位置：

```text
data/src/main/java/dev/jdtech/jellyfin/curated/api/CuratedOkHttpClientFactory.kt
data/src/test/java/dev/jdtech/jellyfin/curated/api/CuratedOkHttpClientFactoryTest.kt
core/src/main/java/dev/jdtech/jellyfin/di/ApiModule.kt
setup/src/main/java/dev/jdtech/jellyfin/setup/data/SetupRepositoryImpl.kt
setup/src/main/java/dev/jdtech/jellyfin/setup/data/di/SetupDataModule.kt
app/phone/src/main/java/dev/jdtech/jellyfin/BaseApplication.kt
```

已实现：

- 新增 `CuratedOkHttpClientFactory`。
- `CuratedOkHttpClientFactory` 使用外部传入的 `CookieJar`，不再由调用点临时创建会话容器。
- 统一添加 Curated 客户端识别头：
  - `User-Agent: CuratedAndroid/<version> (Android <version>)`
  - `X-Curated-Client: android`
  - `X-Curated-Client-Version`
  - `X-Curated-OS: Android`
  - `X-Curated-OS-Version`
- `ApiModule` 通过 Hilt 提供单例 `CuratedCookieJar`。
- `ApiModule` 通过 Hilt 提供单例 `OkHttpClient`，并复用现有网络超时偏好。
- `SetupRepositoryImpl` 不再自己创建 Curated OkHttpClient，而是由 `SetupDataModule` 注入共享客户端。
- 手机端 Coil `ImageLoader` 的 `OkHttpNetworkFetcherFactory` 使用同一个注入的 OkHttpClient，因此图片请求会复用 `curated_auth` Cookie。

新增测试：

```text
CuratedOkHttpClientFactoryTest
```

测试覆盖：

- 工厂创建的 OkHttpClient 使用调用方传入的同一个 `CookieJar` 实例。
- 工厂创建的 OkHttpClient 会自动附加 Curated Android 客户端识别头。

### 1.9 停止 Curated MVP 下的 Jellyfin 用户数据同步调度

修改位置：

```text
app/phone/src/main/java/dev/jdtech/jellyfin/BaseApplication.kt
app/phone/src/test/java/dev/jdtech/jellyfin/BaseApplicationStartupWorkTest.kt
```

已实现：

- `BaseApplication.onCreate()` 不再直接调度旧的 `syncUserData` WorkManager 任务。
- 新增 `curatedMvpSchedulesFindroidUserDataSync()`，当前固定返回 `false`，显式表达 Curated MVP 不调度 Findroid/Jellyfin 用户数据同步。
- 保留 `MpvCleanupWorker` 调度。
- 旧 `SyncWorker` 代码暂未删除，后续会被 Curated progress outbox worker 替代。

新增测试：

```text
BaseApplicationStartupWorkTest
```

测试覆盖：

- Curated MVP 下不会调度 Findroid/Jellyfin 用户数据同步。

### 1.10 AuthLock 启动门控与 PIN 解锁页

修改位置：

```text
data/src/main/java/dev/jdtech/jellyfin/curated/api/CuratedAuthGate.kt
data/src/test/java/dev/jdtech/jellyfin/curated/api/CuratedAuthGateTest.kt
core/src/main/java/dev/jdtech/jellyfin/viewmodels/MainViewModel.kt
setup/src/main/java/dev/jdtech/jellyfin/setup/domain/SetupRepository.kt
setup/src/main/java/dev/jdtech/jellyfin/setup/data/SetupRepositoryImpl.kt
setup/src/main/java/dev/jdtech/jellyfin/setup/presentation/authlock/
setup/src/test/java/dev/jdtech/jellyfin/setup/presentation/authlock/AuthLockViewModelTest.kt
setup/src/main/res/values/strings.xml
app/phone/src/main/java/dev/jdtech/jellyfin/MainActivity.kt
app/phone/src/main/java/dev/jdtech/jellyfin/NavigationRoot.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/setup/authlock/AuthLockScreen.kt
app/phone/src/test/java/dev/jdtech/jellyfin/NavigationRootTest.kt
```

已实现：

- 新增 `AuthStatusDto.requiresUnlock()`，统一判断 `pinEnabled && !unlocked`。
- `MainViewModel` 在已有 current server 时调用 `GET /api/auth/status`，并把锁定状态写入 `MainState.isCuratedAuthLocked`。
- `NavigationRoot` 新增 `AuthLockRoute`。
- 当前 server 已锁定时，启动起点进入 `AuthLockRoute`，不直接进入 Home。
- AddServer 成功和切换服务器后先进入 auth gate；如果后端没有启用 PIN，`AuthLockViewModel` 会立即放行到 Home。
- 新增 `AuthLockViewModel`：
  - 加载 auth status。
  - PIN 输入只保留数字，并按后端 `pinLength` 限长。
  - 调用 `POST /api/auth/unlock`。
  - 将 `AUTH_INVALID_PIN` 映射为明确错误。
  - 支持 `trustedForever`。
- 新增手机端 `AuthLockScreen`，包含 PIN 输入、Trust this device、Unlock、Change server。

新增测试：

```text
CuratedAuthGateTest
AuthLockViewModelTest
NavigationRootTest
```

测试覆盖：

- 只有 `pinEnabled=true && unlocked=false` 时需要进入 AuthLock。
- AuthLock PIN 输入会过滤非数字并按最大长度截断。
- 当前 server 被锁定时，导航起点是 `AuthLockRoute`。

## 2. 已新增或修改的 Gradle 测试依赖

```text
gradle/libs.versions.toml
data/build.gradle.kts
app/phone/build.gradle.kts
settings/build.gradle.kts
setup/build.gradle.kts
```

新增/使用：

- `junit = 4.13.2`
- `okhttp`
- `okhttp-mockwebserver`

## 3. 当前仍保留但不应从 UI 触达的旧代码

以下代码目前仍保留，主要是为了避免早期迁移时大规模删除导致编译和回归风险：

```text
app/phone/.../DownloadsScreen.kt
app/phone/.../ShowScreen.kt
app/phone/.../SeasonScreen.kt
app/phone/.../EpisodeScreen.kt
app/phone/.../setup/users/UsersScreen.kt
app/phone/.../setup/login/LoginScreen.kt
setup/.../users/*
setup/.../login/*
core/.../downloader/*
data/.../JellyfinRepository*
data/.../FindroidShow*
data/.../FindroidSeason*
data/.../FindroidEpisode*
```

当前策略是：

1. 先隐藏入口。
2. 再把新 UI/ViewModel 切到 Curated 数据源。
3. 最后删除或隔离旧 Jellyfin/下载/剧集/用户管理代码。

## 4. 已执行的验证命令

注意：当前 shell 的 `JAVA_HOME` 需要在命令前设置：

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
```

本轮已执行并通过的关键命令：

```powershell
.\gradlew.bat :data:testDebugUnitTest --tests dev.jdtech.jellyfin.curated.api.*
.\gradlew.bat :data:testDebugUnitTest --tests dev.jdtech.jellyfin.curated.*
.\gradlew.bat :app:phone:testLibreDebugUnitTest --tests dev.jdtech.jellyfin.NavigationRootTest
.\gradlew.bat :app:phone:testLibreDebugUnitTest --tests dev.jdtech.jellyfin.BaseApplicationStartupWorkTest
.\gradlew.bat :settings:testDebugUnitTest --tests dev.jdtech.jellyfin.settings.presentation.settings.SettingsViewModelTest
.\gradlew.bat :setup:testDebugUnitTest --tests dev.jdtech.jellyfin.setup.data.SetupRepositoryImplTest
.\gradlew.bat :setup:testDebugUnitTest --tests dev.jdtech.jellyfin.setup.presentation.authlock.AuthLockViewModelTest
.\gradlew.bat :app:phone:assembleLibreDebug
```

## 5. 下一步计划

### 5.1 共享 Curated OkHttp/CookieJar（已完成基础接入）

当前状态：

- `CuratedCookieJar` 已由 Hilt 作为单例提供。
- Curated OkHttpClient 已由 Hilt 作为单例提供。
- Setup 的 Curated health 检查已使用共享 OkHttpClient。
- 手机端 Coil `ImageLoader` 已使用共享 OkHttpClient。

后续仍需补齐：

```text
player/local 或 PlayerActivity 的 Media3 data source
CuratedRepository 的 Hilt 注入/工厂
```

目标：

- API、图片、后续播放器请求最终共用同一个 CookieJar。
- `423 AUTH_LOCKED` 后续可以统一清理 Cookie 并进入 AuthLock flow。

### 5.2 AuthLockScreen（已完成最小闭环）

当前状态：

- 已有 `AuthLockRoute`。
- 启动时会根据 `GET /api/auth/status` 决定是否进入 PIN 页面。
- 已有手机端 PIN 输入页。
- 已支持 `POST /api/auth/unlock`。
- 已支持 `trustedForever`。
- 已将 `AUTH_INVALID_PIN` 映射为明确错误。

后续仍需补齐：

```text
全局 423 AUTH_LOCKED 拦截
Cookie 持久化
setupRequired=true 的专门提示
解锁成功后刷新正在等待的原始请求
```

目标：

- 任意受保护 API 返回 `423 AUTH_LOCKED` 时，都能进入 AuthLock flow。
- App 重启后按安全策略恢复或重新解锁会话。

### 5.3 电影列表切到 CuratedRepository

当前 UI 仍主要消费 `JellyfinRepository` / `FindroidItem`。

下一步建议修改：

```text
modes/film/src/main/java/dev/jdtech/jellyfin/film/presentation/library/LibraryViewModel.kt
modes/film/src/main/java/dev/jdtech/jellyfin/film/presentation/search/SearchViewModel.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/film/LibraryScreen.kt
```

目标：

- `GET /api/library/movies` 分页。
- `q` 搜索。
- `actor/studio/mode` 后续作为筛选参数。
- UI 开始消费 Curated `MovieListItem`，不再消费 Jellyfin `FindroidItem`。

### 5.4 电影详情切到 CuratedRepository

下一步建议修改：

```text
modes/film/src/main/java/dev/jdtech/jellyfin/film/presentation/movie/MovieViewModel.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/film/MovieScreen.kt
```

目标：

- `movieId` 改为 `String`。
- 调用 `GET /api/library/movies/{movieId}`。
- 使用 Curated `MovieDetail` 展示字段。
- 收藏/评分后续通过 `PATCH /api/library/movies/{movieId}`。

### 5.5 播放入口切到 PlaybackDescriptor

下一步建议修改：

```text
app/phone/src/main/java/dev/jdtech/jellyfin/PlayerActivity.kt
player/local/src/main/java/dev/jdtech/jellyfin/player/local/presentation/PlayerViewModel.kt
player/local/src/main/java/dev/jdtech/jellyfin/player/local/domain/PlaylistManager.kt
```

目标：

- 播放前调用 `GET /api/library/movies/{movieId}/playback`。
- Media3 播放 `PlaybackDescriptor.url`。
- 使用 `resumePositionSec` seek。
- 退出时写回 progress，HLS session 存在时调用 delete。

### 5.6 停止 Jellyfin SyncWorker 调度（已完成调度关闭）

当前状态：

- `BaseApplication` 已停止默认调度 `syncUserData`。
- `SyncWorker` 代码暂时保留，避免大范围删除引入无关回归。

后续仍需补齐：

- 新增 Curated progress outbox worker。
- 当电影播放进度 UI/播放器切到 Curated 后，用新 worker 替代旧 userdata sync。


## 6. 当前风险

- AddServer 已能优先尝试 Curated health，但旧 server 数据结构仍是 Findroid 的 `Server + ServerAddress + User`。
- 已有 PIN 解锁 UI，但还没有全局 `423 AUTH_LOCKED` 拦截和原请求重试。
- `CuratedCookieJar` 已在 API/Setup/Coil 路径共享，但仍是内存版；App 重启后不会保留 `curated_auth`。
- 播放器的 Media3 请求还没有接入共享 OkHttpClient，PIN 开启后视频 URL 仍可能缺少 Cookie。
- UI 入口已经隐藏下载/TV/user，但底层旧 ViewModel/Repository 仍存在。
- 电影列表、详情、播放器还没有真正切到 `CuratedRepository`。
- App 仍显示 Findroid 包名、applicationId、namespace 和部分文案；品牌迁移尚未开始。
