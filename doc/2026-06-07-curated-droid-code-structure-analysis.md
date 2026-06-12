# Curated Droid 当前代码结构分析与修改计划

日期：2026-06-07  
状态：Code Structure Analysis  
目标：说明当前 Findroid 代码基底的结构、关键依赖、Curated 改造影响面，以及计划修改的具体位置。

## 1. 总览

当前仓库是 Findroid Android 客户端代码基底。项目使用 Kotlin、Gradle Kotlin DSL、Jetpack Compose、Hilt、Room、Paging、Coil、WorkManager、Media3/ExoPlayer、mpv，并通过 Jellyfin SDK 访问 Jellyfin 服务端。

Curated Droid 的目标是保留原生媒体库浏览和播放体验，但移除 Jellyfin 协议和 Jellyfin 用户模型，改为直接消费 Curated Go 后端 REST API。

当前代码中与 Curated MVP 目标冲突最大的部分是：

1. 网络层绑定 Jellyfin SDK。
2. 本地数据库 schema 绑定 Findroid/Jellyfin server、user、movie/show/season/episode/source/userdata。
3. setup 流程绑定 Jellyfin 服务发现、用户登录、QuickConnect。
4. 媒体库 UI 同时支持电影、电视剧、季、集、合集、下载。
5. 播放器入口绑定 Jellyfin media source 和 ticks 进度。
6. WorkManager 仍调度 Jellyfin 用户数据同步。
7. 应用品牌、包名、资源、README、fastlane 仍是 Findroid/Jellyfin。

第一版 Curated Droid 不需要下载、电视剧/剧集、用户管理。这些功能应先从 UI 和导航隐藏，再逐步从数据流和依赖中移除。

## 2. Gradle 模块结构

当前模块定义在 `settings.gradle.kts`：

```text
rootProject.name = "findroid"

:app:phone
:core
:data
:player:core
:player:local
:setup
:modes:film
:settings
```

Note: the TV app module was removed on 2026-06-08. Curated Droid MVP only keeps the phone app entry point.

当前 Kotlin/Gradle/XML 文件约 626 个。主要模块职责如下：

| 模块 | 当前职责 | Curated 改造判断 |
|---|---|---|
| `app:phone` | 手机端 Application、Activity、NavigationRoot、手机 UI 组合 | MVP 主入口，需要重点改 |
| `core` | 通用资源、DI、数据库注入、WorkManager、下载工具、主题和工具类 | 需要拆出 Curated network/session、停用 Jellyfin sync |
| `data` | JellyfinApi、JellyfinRepository、Room database、Findroid models | 改造核心，需要新增 Curated DTO/domain/repository |
| `setup` | Jellyfin server discovery、server list、users、login、QuickConnect | 需要替换成 Curated server profile + PIN flow |
| `modes:film` | 媒体库 ViewModel 和 domain helper，包含 home/library/movie/show/season/episode/search/favorites/downloads/person | 需要收敛为电影库、详情、收藏、搜索、演员 |
| `player:core` | PlayerItem、Track、Chapter、Subtitle、Trickplay domain | 部分可复用，但 id/ticks/source 要调整 |
| `player:local` | ExoPlayer/mpv 播放器、PlaylistManager、PlayerViewModel | 播放控制可复用，播放入口和进度上报必须重写 |
| `settings` | AppPreferences、设置 domain/presentation | 需要隐藏 Jellyfin/下载/离线相关项，增加 Curated 诊断 |

## 3. 当前依赖结构

### 3.1 仍依赖 Jellyfin SDK 的模块

以下模块直接依赖 `libs.jellyfin.core`：

- `app:phone`
- `core`
- `data`
- `setup`
- `modes:film`
- `player:local`
- `settings`

这说明 Jellyfin SDK 不是单点依赖，已经渗透到 app、data、setup、film、player、settings。移除 Jellyfin SDK 不能只删 `JellyfinApi`，必须先建立 Curated domain 边界，再逐步让 UI/ViewModel 不再引用 Jellyfin 类型。

### 3.2 现有可继续保留的技术依赖

以下依赖符合 Curated Droid 目标：

- Compose：保留，用于 UI。
- Hilt：保留，用于 API、repository、database、player 注入。
- Room：保留，用于 server profile、movie cache、progress outbox。
- Paging：保留，用于 `/api/library/movies` 分页。
- Coil：保留，用于封面、缩略图、演员头像。
- Media3/ExoPlayer：保留，用于 direct/HLS 播放。
- WorkManager：保留，用于 progress outbox retry。
- OkHttp：保留并提升为 Curated network 统一底座。

### 3.3 需要降级或隐藏的依赖能力

- mpv：可以暂时保留为高级播放器选项，但 MVP 优先验证 Media3。
- DownloadManager/下载工具：第一版隐藏，不应触达。
- Jellyfin SDK：最终移除。
- TV app：第一版不作为主要交付面。

## 4. 当前关键调用链

### 4.1 App 启动链路

手机端入口：

```text
app/phone/MainActivity
  -> MainViewModel
  -> NavigationRoot
```

当前 `MainViewModel` 判断：

```text
hasServers
hasCurrentServer
hasCurrentUser
isOfflineMode
```

当前导航起点：

```text
有 server + current server + current user -> HomeRoute
有 server + current server -> UsersRoute
有 server -> ServersRoute
否则 -> WelcomeRoute
```

问题：

- Curated 没有 Jellyfin user。
- PIN 是后端 App Lock，不是用户登录。
- 启动判断应从 `hasCurrentUser` 改为 `authStatus.unlocked || pinEnabled=false`。

计划修改：

- 修改 `core/src/main/java/dev/jdtech/jellyfin/viewmodels/MainViewModel.kt` 或新增 Curated 启动 ViewModel。
- 修改 `app/phone/src/main/java/dev/jdtech/jellyfin/NavigationRoot.kt` 的 start destination。
- 用 `ServerProfile + AuthStatus` 替换 `Server + User` 判断。

### 4.2 网络调用链路

当前网络入口：

```text
data/JellyfinApi
  -> org.jellyfin.sdk.createJellyfin
  -> jellyfin.createApi
  -> api.update(baseUrl, accessToken)
```

当前 DI：

```text
core/di/ApiModule
  -> provideJellyfinApi
  -> 从 AppPreferences.currentServer 读取 Server/User
  -> 用 ServerAddress + User.accessToken 更新 Jellyfin SDK client
```

问题：

- Curated 不使用 Jellyfin SDK。
- Curated 使用 Cookie，不使用 Jellyfin access token。
- 图片和播放器也必须共享 Cookie。

计划修改：

- 新增 `CuratedApiClient` 或 `CuratedApi`。
- 新增 `CuratedSessionManager` 管理 baseUrl、CookieJar、auth lock 状态。
- 修改 `ApiModule`，提供共享 OkHttpClient、CookieJar、Curated API。
- 逐步删除 `JellyfinApi` 的注入路径。

### 4.3 Setup / Auth 链路

当前 setup 入口：

```text
setup/domain/SetupRepository
setup/data/SetupRepositoryImpl
setup/presentation/addserver/AddServerViewModel
setup/presentation/servers/ServersViewModel
setup/presentation/users/UsersViewModel
setup/presentation/login/LoginViewModel
```

当前能力：

- Jellyfin local discovery。
- 手动添加 Jellyfin server。
- 保存 Server、ServerAddress。
- 查询 public users。
- username/password 登录。
- QuickConnect。
- current user 切换。

问题：

- Curated 第一版只有手动 baseUrl。
- Curated PIN 解锁不等于用户登录。
- Users/Login/QuickConnect 入口必须隐藏。

计划修改：

- 将 AddServer 改为 Curated health check。
- 新增 AuthLock screen/viewmodel。
- 隐藏 UsersScreen、LoginScreen、QuickConnect。
- 新建或改造 `SetupRepository` 为 Curated server/auth repository。
- 本地模型从 `Server + ServerAddress + User` 收敛为 `ServerProfile`。

### 4.4 媒体库链路

当前 film 入口：

```text
modes/film/presentation/home/HomeViewModel
modes/film/presentation/media/MediaViewModel
modes/film/presentation/library/LibraryViewModel
modes/film/presentation/movie/MovieViewModel
modes/film/presentation/show/ShowViewModel
modes/film/presentation/season/SeasonViewModel
modes/film/presentation/episode/EpisodeViewModel
modes/film/presentation/downloads/DownloadsViewModel
modes/film/presentation/search/SearchViewModel
modes/film/presentation/favorites/FavoritesViewModel
modes/film/presentation/person/PersonViewModel
```

当前 repository：

```text
data/repository/JellyfinRepository
data/repository/JellyfinRepositoryImpl
data/repository/JellyfinRepositoryOfflineImpl
data/repository/ItemsPagingSource
```

当前模型：

```text
FindroidItem
FindroidMovie
FindroidShow
FindroidSeason
FindroidEpisode
FindroidCollection
FindroidFolder
FindroidBoxSet
FindroidPerson
FindroidSource
FindroidImages
```

问题：

- Curated MVP 只需要 MovieListItem、MovieDetail、Actor、PlaybackProgress。
- Current model 使用 `UUID`，Curated DTO id 是 `String`。
- 当前 LibraryViewModel 使用 Jellyfin `BaseItemKind` 和 `CollectionType`。
- 当前 HomeViewModel 使用 Jellyfin suggestions/resume/nextUp/userViews。

计划修改：

- 新增 Curated domain model，不再复用 `FindroidItem` 作为新 UI 的核心类型。
- 新增 MoviesPagingSource，调用 `GET /api/library/movies`。
- 修改 LibraryScreen/ViewModel 为 Curated movies grid。
- 修改 MovieScreen/ViewModel 为 Curated movie detail。
- Favorites 改为 `GET /api/library/movies?mode=favorites`。
- Search 改为 movies query `q`。
- Person/Actor 功能改成 actor name string，不再使用 Jellyfin person UUID。
- 隐藏 Show/Season/Episode/Downloads 入口。

### 4.5 播放链路

当前播放入口：

```text
MovieScreen / EpisodeScreen
  -> PlayerActivity
  -> PlayerViewModel.initializePlayer(itemId: UUID, itemKind: String, startFromBeginning: Boolean)
  -> PlaylistManager.getInitialItem
  -> JellyfinRepository.getMovie/getEpisode/getSeason/getMediaSources
  -> PlayerItem
  -> MediaItem(uri = mediaSourceUri)
```

当前进度上报：

```text
postPlaybackStart(itemId)
postPlaybackProgress(itemId, positionTicks, isPaused)
postPlaybackStop(itemId, positionTicks, playedPercentage)
```

问题：

- Curated 播放入口是 playback descriptor。
- Curated 进度单位是 seconds，不是 ticks。
- Curated 没有 episode playlist。
- 当前 PlaylistManager 大量依赖 movie/show/season/episode。
- HLS session 退出时需要显式 delete。

计划修改：

- 新增 `CuratedPlayerViewModel` 或改造 `PlayerViewModel` 的初始化参数。
- `movieId` 使用 String。
- 点击播放先调用 `GET /api/library/movies/{movieId}/playback`。
- `PlaybackDescriptor.url` 归一化后传给 Media3。
- `resumePositionSec` 转毫秒 seek。
- 周期性调用 `PUT /api/playback/progress/{movieId}`。
- 达到阈值调用 `POST /api/library/played-movies/{movieId}`。
- 有效播放累计调用 `POST /api/playback/watch-time/daily`。
- 退出时删除 descriptor/session 中的 HLS session。
- 第一版隐藏 trickplay、chapter、segments/skip intro，直到 Curated API 有明确契约。

### 4.6 图片链路

当前图片模型：

```text
FindroidImages
  primary/backdrop/logo/showPrimary/showBackdrop/showLogo
  使用 Jellyfin /items/{id}/Images/... 构造 URI
```

当前 Coil 配置：

```text
BaseApplication.newImageLoader
  -> OkHttpNetworkFetcherFactory(cacheStrategy = CacheControlCacheStrategy)
```

问题：

- Curated 图片 URL 来自 `coverUrl`、`thumbUrl`、`previewImages`、`actorAvatarUrls`。
- Curated 图片 endpoint 可能受 PIN Cookie 保护。
- 当前 Coil client 没有明确共享 Curated CookieJar。

计划修改：

- 新增 URL 归一化工具。
- Curated Movie mapper 中把图片 URL 转为绝对 URL。
- BaseApplication 的 ImageLoader 使用注入的共享 OkHttpClient 或共享 CookieJar。
- 图片失败时展示占位，不暴露服务端本地路径。

### 4.7 Room / Cache 链路

当前数据库：

```text
ServerDatabase
  Server
  ServerAddress
  User
  FindroidMovieDto
  FindroidShowDto
  FindroidSeasonDto
  FindroidEpisodeDto
  FindroidSourceDto
  FindroidMediaStreamDto
  FindroidUserDataDto
  FindroidTrickplayInfoDto
  FindroidSegmentDto
```

当前 DAO：

```text
ServerDatabaseDao
  server/address/user CRUD
  movie/show/season/episode/source cache
  userdata played/favorite/playbackPositionTicks
  search local movies/shows/episodes
  trickplay/segments
```

问题：

- 当前 schema 过重，并且绑定 Jellyfin 用户和剧集模型。
- Curated 第一版不需要 show/season/episode/source/trickplay/segment。
- Curated progress 使用 seconds，且需要 outbox。

计划修改：

- 新建 Curated Room entities：
  - `ServerProfileEntity`
  - `MovieListCacheEntity`
  - `MovieDetailCacheEntity`
  - `ActorCacheEntity` 可选
  - `PlaybackProgressOutboxEntity`
- 新建 Curated DAO。
- 短期可以保留旧数据库类，新增 Curated database 或新增表；长期删除旧 Findroid schema。
- WorkManager 使用 outbox 表做重试。

### 4.8 WorkManager / 后台任务链路

当前后台任务：

```text
BaseApplication.scheduleUserDataSync
  -> SyncWorker
  -> 遍历 Server/User
  -> Jellyfin itemsApi.updateItemUserData

BaseApplication.scheduleMpvCleanup
  -> MpvCleanupWorker
```

当前问题：

- `SyncWorker` 完全是 Jellyfin 用户数据同步。
- 第一版没有下载，不应保留下载相关 worker 入口。
- `ImagesDownloaderWorker` 使用独立 `OkHttpClient()`，与 Curated CookieJar 不共享。

计划修改：

- 停止调度 `syncUserData`。
- 新增 `PlaybackProgressSyncWorker`。
- worker 只处理 Curated progress outbox。
- `ImagesDownloaderWorker` 第一版不使用；如果保留，必须共享 Curated OkHttpClient。
- `MpvCleanupWorker` 可保留。

### 4.9 设置与偏好

当前 `AppPreferences` 包含：

- currentServer
- preferred audio/subtitle language
- theme/dynamicColors/home sections
- player/mpv/gestures/trickplay/segments/pip
- download preferences
- network timeout
- image cache
- sort
- offlineMode

问题：

- `currentServer` 仍指 Jellyfin server id。
- 下载和 offlineMode 第一版不需要显示。
- player segments/trickplay 当前是 Jellyfin 能力。

计划修改：

- 新增 Curated server profile preference 或 DataStore。
- 保留 theme、dynamicColors、network timeout、image cache、sort、基础 player seek。
- 隐藏 download、offlineMode、segments、trickplay 设置。
- Settings 页面增加 Curated 当前服务器、health、auth、lock、clear session、播放诊断。

### 4.10 资源与品牌

当前状态：

- `app_name` 是 `Findroid`。
- Manifest theme 是 `Theme.Findroid`。
- README、fastlane、图片、隐私文案仍指向 Findroid/Jellyfin。
- 包名和 namespace 仍是 `dev.jdtech.jellyfin`。

计划修改：

- Phase 0/1 先改 UI 可见品牌：app name、基础文案、隐藏 Jellyfin 用户文案。
- Phase 3 再统一迁移 package、namespace、applicationId、图标、README、fastlane。
- 保留 GPLv3 许可和 fork 来源说明。

## 5. 计划修改文件清单

### 5.1 必改：网络与会话

计划新增：

```text
data/src/main/java/dev/jdtech/jellyfin/curated/api/CuratedApi.kt
data/src/main/java/dev/jdtech/jellyfin/curated/api/CuratedApiClient.kt
data/src/main/java/dev/jdtech/jellyfin/curated/api/CuratedDtos.kt
data/src/main/java/dev/jdtech/jellyfin/curated/api/CuratedError.kt
data/src/main/java/dev/jdtech/jellyfin/curated/api/CuratedUrlResolver.kt
data/src/main/java/dev/jdtech/jellyfin/curated/session/CuratedCookieJar.kt
data/src/main/java/dev/jdtech/jellyfin/curated/session/CuratedSessionManager.kt
```

计划修改：

```text
core/src/main/java/dev/jdtech/jellyfin/di/ApiModule.kt
app/phone/src/main/java/dev/jdtech/jellyfin/BaseApplication.kt
```

目的：

- 用 Curated API client 替代 Jellyfin SDK client。
- API、Coil、Media3 使用同一 CookieJar。
- 统一处理 baseUrl、相对 URL、AppError、AUTH_LOCKED。

### 5.2 必改：本地模型与数据库

计划新增：

```text
data/src/main/java/dev/jdtech/jellyfin/curated/model/ServerProfile.kt
data/src/main/java/dev/jdtech/jellyfin/curated/model/MovieModels.kt
data/src/main/java/dev/jdtech/jellyfin/curated/model/PlaybackModels.kt
data/src/main/java/dev/jdtech/jellyfin/curated/database/CuratedDatabase.kt
data/src/main/java/dev/jdtech/jellyfin/curated/database/CuratedDatabaseDao.kt
data/src/main/java/dev/jdtech/jellyfin/curated/database/CuratedEntities.kt
```

计划修改：

```text
core/src/main/java/dev/jdtech/jellyfin/di/DatabaseModule.kt
settings/src/main/java/dev/jdtech/jellyfin/settings/domain/AppPreferences.kt
```

目的：

- 建立不依赖 Jellyfin UUID/User/Show/Episode 的 Curated domain。
- 缓存 server profile、movie snapshot、progress outbox。

### 5.3 必改：Repository

计划新增：

```text
data/src/main/java/dev/jdtech/jellyfin/curated/repository/CuratedRepository.kt
data/src/main/java/dev/jdtech/jellyfin/curated/repository/CuratedRepositoryImpl.kt
data/src/main/java/dev/jdtech/jellyfin/curated/repository/MoviesPagingSource.kt
```

计划修改：

```text
core/src/main/java/dev/jdtech/jellyfin/di/RepositoryModule.kt
```

后续删除或停止注入：

```text
data/src/main/java/dev/jdtech/jellyfin/repository/JellyfinRepository.kt
data/src/main/java/dev/jdtech/jellyfin/repository/JellyfinRepositoryImpl.kt
data/src/main/java/dev/jdtech/jellyfin/repository/JellyfinRepositoryOfflineImpl.kt
data/src/main/java/dev/jdtech/jellyfin/repository/ItemsPagingSource.kt
```

目的：

- 让新 UI/ViewModel 只消费 CuratedRepository。
- 先做到并行存在，等 UI 迁移完成后再移除 JellyfinRepository。

### 5.4 必改：启动与 setup/auth

计划修改：

```text
core/src/main/java/dev/jdtech/jellyfin/viewmodels/MainViewModel.kt
app/phone/src/main/java/dev/jdtech/jellyfin/NavigationRoot.kt
setup/src/main/java/dev/jdtech/jellyfin/setup/domain/SetupRepository.kt
setup/src/main/java/dev/jdtech/jellyfin/setup/data/SetupRepositoryImpl.kt
setup/src/main/java/dev/jdtech/jellyfin/setup/presentation/addserver/AddServerViewModel.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/setup/addserver/AddServerScreen.kt
```

计划新增：

```text
setup/src/main/java/dev/jdtech/jellyfin/setup/presentation/authlock/AuthLockViewModel.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/setup/authlock/AuthLockScreen.kt
```

计划隐藏：

```text
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/setup/users/UsersScreen.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/setup/login/LoginScreen.kt
setup/src/main/java/dev/jdtech/jellyfin/setup/presentation/users/UsersViewModel.kt
setup/src/main/java/dev/jdtech/jellyfin/setup/presentation/login/LoginViewModel.kt
```

目的：

- 启动门控改为 ServerProfile + AuthStatus。
- AddServer 调 Curated health。
- AuthLock 调 Curated unlock。
- 用户管理和 QuickConnect 不再出现在第一版 UI。

### 5.5 必改：手机端导航和主界面

计划修改：

```text
app/phone/src/main/java/dev/jdtech/jellyfin/NavigationRoot.kt
app/phone/src/main/java/dev/jdtech/jellyfin/MainActivity.kt
```

需要隐藏的 route：

```text
DownloadsRoute
ShowRoute
SeasonRoute
EpisodeRoute
UsersRoute
LoginRoute
ServerAddressesRoute
```

第一版保留或新增的 route：

```text
AddServerRoute
AuthLockRoute
HomeRoute
LibraryRoute
MovieRoute(movieId: String)
ActorsRoute 可选
SettingsRoute
```

底部导航第一版：

```text
Home
Library
Settings
```

目的：

- 防止进入下载、电视剧/剧集、用户管理等非需求功能。
- 降低 Jellyfin 残留逻辑被触发的概率。

### 5.6 必改：电影列表、详情、收藏、搜索

计划修改：

```text
modes/film/src/main/java/dev/jdtech/jellyfin/film/presentation/home/HomeViewModel.kt
modes/film/src/main/java/dev/jdtech/jellyfin/film/presentation/library/LibraryViewModel.kt
modes/film/src/main/java/dev/jdtech/jellyfin/film/presentation/movie/MovieViewModel.kt
modes/film/src/main/java/dev/jdtech/jellyfin/film/presentation/search/SearchViewModel.kt
modes/film/src/main/java/dev/jdtech/jellyfin/film/presentation/favorites/FavoritesViewModel.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/film/HomeScreen.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/film/LibraryScreen.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/film/MovieScreen.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/film/components/ItemCard.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/film/components/ItemButtonsBar.kt
```

计划隐藏：

```text
modes/film/src/main/java/dev/jdtech/jellyfin/film/presentation/show/*
modes/film/src/main/java/dev/jdtech/jellyfin/film/presentation/season/*
modes/film/src/main/java/dev/jdtech/jellyfin/film/presentation/episode/*
modes/film/src/main/java/dev/jdtech/jellyfin/film/presentation/downloads/*
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/film/ShowScreen.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/film/SeasonScreen.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/film/EpisodeScreen.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/film/DownloadsScreen.kt
```

目的：

- 列表改为 `/api/library/movies`。
- 详情改为 `/api/library/movies/{movieId}`。
- 收藏和评分改为 `PATCH /api/library/movies/{movieId}`。
- 搜索改为 `q` query。
- Favorites 改为 `mode=favorites`。
- 下载按钮、剧集入口、离线状态不显示。

### 5.7 必改：播放器

计划修改：

```text
player/core/src/main/java/dev/jdtech/jellyfin/player/core/domain/models/PlayerItem.kt
player/local/src/main/java/dev/jdtech/jellyfin/player/local/presentation/PlayerViewModel.kt
player/local/src/main/java/dev/jdtech/jellyfin/player/local/domain/PlaylistManager.kt
app/phone/src/main/java/dev/jdtech/jellyfin/PlayerActivity.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/film/MovieScreen.kt
```

第一版计划隐藏或停用：

```text
trickplay scrubber
chapter navigation
media segments skip button
episode playlist
media source selector
download playback source
```

目的：

- 播放入口改为 playback descriptor。
- `movieId` 用 String。
- direct/HLS URL 归一化后交给 Media3。
- progress 使用 seconds。
- 退出时清理 HLS session。

### 5.8 必改：WorkManager

计划修改：

```text
app/phone/src/main/java/dev/jdtech/jellyfin/BaseApplication.kt
core/src/main/java/dev/jdtech/jellyfin/work/SyncWorker.kt
core/src/main/java/dev/jdtech/jellyfin/work/ImagesDownloaderWorker.kt
```

计划新增：

```text
core/src/main/java/dev/jdtech/jellyfin/work/PlaybackProgressSyncWorker.kt
```

目的：

- 停止调度 Jellyfin `syncUserData`。
- 新增 Curated progress outbox retry。
- 不使用下载和图片预下载 worker，除非共享 Curated CookieJar。

### 5.9 后续修改：品牌和发布

计划修改：

```text
settings.gradle.kts
app/phone/build.gradle.kts
core/src/main/res/values/strings.xml
core/src/main/res/values/themes.xml
README.md
PRIVACY
fastlane/**
images/**
```

目的：

- `findroid` 改为 `curated-droid`。
- `Findroid` 文案改为 `Curated Droid`。
- `dev.jdtech.jellyfin` applicationId/namespace 迁移到新包名。
- 保留 GPLv3 和 fork 来源说明。

该阶段建议放在核心功能跑通之后，避免包名迁移和业务迁移交织。

## 6. 第一版隐藏清单

### 6.1 必须从 UI 隐藏

- Downloads tab。
- 下载按钮。
- 下载进度卡片。
- 离线模式入口。
- TV Shows / Shows。
- Season。
- Episode。
- Next Up。
- Users。
- Login。
- QuickConnect。
- Server address 管理中的 Jellyfin 多地址语义。
- Media source selector。
- Trickplay。
- Skip intro / media segments。

### 6.2 可以暂时保留源码但不可触达

- 下载实现。
- Show/Season/Episode ViewModel 和 Screen。
- Jellyfin Users/Login ViewModel 和 Screen。
- JellyfinRepositoryOfflineImpl。
- mpv 播放器。
- TV app。

### 6.3 应尽快停用

- JellyfinApi 注入。
- SyncWorker 的 Jellyfin userdata 同步。
- AddServer 的 Jellyfin discovery。
- Login/QuickConnect 流程。
- 使用 UUID 解析 Curated movieId 的路径。

## 7. 推荐修改顺序

### Step 1：建立 Curated network/session 基础

先新增 Curated API client、DTO、error mapper、URL resolver、CookieJar。此步骤不改 UI，只提供可测试的基础设施。

原因：

- Cookie 会影响 API、图片、播放三条链路。
- 如果先改 UI，后续会重复补鉴权和 URL 处理。

### Step 2：替换启动门控和 AddServer/AuthLock

将启动流程从 Server/User 改为 ServerProfile/AuthStatus。AddServer 只负责 health check，AuthLock 只负责 PIN 解锁。

原因：

- 这是进入任何媒体库 API 前的前置条件。
- 可以尽早验证真机访问、防火墙、PIN、Cookie。

### Step 3：实现影片列表和详情最小闭环

新增 Curated movie paging、movie detail mapper，并改造 Library/Movie ViewModel 和 Screen。

原因：

- 这是用户最先看到的核心体验。
- 同时验证 Paging、Coil、URL 归一化和 Cookie。

### Step 4：接入 playback descriptor

改造 PlayerViewModel 或新增 CuratedPlayerViewModel，让播放只依赖 PlaybackDescriptor。

原因：

- 当前播放器逻辑最复杂，先做单片 direct/HLS，不做 playlist、trickplay、segments。
- 直接验证 Range、seek、resumePositionSec。

### Step 5：接入 progress、played、watch time

播放中周期同步 progress，达到阈值 mark played，每 60 秒累计 watch time，失败写入 outbox。

原因：

- 这是跨 Web/Electron/Android 一致性的关键。
- 需要等播放器位置和 duration 可靠后再做。

### Step 6：隐藏非需求入口

从 NavigationRoot、底部导航、详情按钮、Home section 和 settings 中隐藏下载、剧集、用户管理。

原因：

- 前面步骤可能仍需要保留旧代码方便编译。
- 入口隐藏可以先保证 MVP 用户不会触达残留逻辑。

### Step 7：停用 Jellyfin sync 和下载相关后台任务

停止调度 SyncWorker，新增 progress outbox worker。下载相关 worker 第一版不使用。

原因：

- 防止后台继续请求 Jellyfin API。
- 避免用户以为下载/离线已支持。

### Step 8：测试与清理

补单元测试和 MockWebServer 集成测试，再做真机验证。核心功能稳定后，再清理 Jellyfin SDK、旧 schema、品牌和包名。

## 8. 验收标准

结构层验收：

1. 新 UI/ViewModel 不再依赖 `JellyfinRepository`。
2. 新 Curated domain model 不使用 `UUID` 表达 `movieId`。
3. API client、Coil、Media3 共享 CookieJar。
4. `NavigationRoot` 第一版不会进入 Downloads、Users、Login、Show、Season、Episode。
5. BaseApplication 不再调度 Jellyfin userdata sync。

功能层验收：

1. 可手动输入 Curated 后端地址并通过 `/api/health` 保存 server profile。
2. PIN 开启时可通过 `/api/auth/unlock` 解锁，后续 API 自动带 Cookie。
3. 影片列表分页、搜索、收藏模式可用。
4. 影片详情图片和字段可显示。
5. direct playback descriptor 可播放。
6. HLS playback descriptor 可播放并在退出时清理 session。
7. progress、played、watch time 可写回后端。
8. 后端关闭、IP 错误、PIN 错误、资源 404 都有明确错误提示。

隐藏范围验收：

1. 底部导航没有 Downloads。
2. 详情页没有下载按钮。
3. Home 没有 Next Up。
4. 媒体库没有 TV Shows/Season/Episode 入口。
5. setup 流程没有 Users/Login/QuickConnect。
6. 设置页没有离线下载和 Jellyfin 用户管理入口。

## 9. 当前建议

建议先不要做全量包名迁移，也不要先删除大量 Findroid/Jellyfin 源码。第一阶段更稳妥的方式是：

1. 新增 Curated 并行实现。
2. 让手机端 MVP UI 切到 Curated repository。
3. 从导航隐藏旧功能。
4. 跑通 Curated 核心链路。
5. 再删除或隔离 Jellyfin SDK、旧数据库、下载、剧集和用户管理残留。

这样可以避免“包名/资源大重构”和“后端协议迁移”同时发生，降低编译和回归风险。
