# Curated Droid MVP 实施文档

日期：2026-06-07  
状态：Implementation Draft  
适用仓库：当前 `curated-droid`，基于 Findroid Android 代码改造  
目标：把现有 Findroid 原生 Android 体验收敛为 Curated 后端的移动客户端第一版

## 1. 背景

当前仓库是 Findroid 代码基底，现有能力包括 Kotlin、Jetpack Compose、Hilt、Room、Paging、Coil、WorkManager、Media3/ExoPlayer、mpv、本地下载、离线缓存、Jellyfin 服务发现、Jellyfin 用户登录、电影/剧集/季/集浏览与播放。

Curated Droid 的目标不是兼容 Jellyfin，也不是让 Android 端继续通过 Jellyfin SDK 访问服务端。目标是保留 Findroid 的原生媒体库浏览与播放体验，将后端连接、鉴权、媒体库、图片、播放、进度同步全部替换为 Curated Go 后端的 REST API。

后端契约以同目录 `API.md` 为准；产品与架构范围以 `2026-06-07-curated-android-app-foundation.md` 为准。本实施文档在此基础上补充当前代码仓库的具体改造策略，并明确第一版需要隐藏的功能。

## 2. 第一版范围

### 2.1 MVP 必须实现

1. 手动添加 Curated 服务端地址。
   - 用户输入形如 `http://192.168.1.100:8080` 或 `http://192.168.1.100:8081` 的地址。
   - 输入可以兼容末尾 `/` 或 `/api`，保存前统一归一化为不带 `/api` 的 `baseUrl`。

2. 服务端连通性校验。
   - 调用 `GET {baseUrl}/api/health`。
   - 展示并缓存后端 `name`、`version`、`channel`、`installerVersion`、`transport`、最后连接时间。

3. PIN App Lock。
   - 调用 `GET /api/auth/status` 判断是否需要解锁。
   - 调用 `POST /api/auth/unlock` 提交 PIN。
   - 使用共享 OkHttp `CookieJar` 持久化并回传 `curated_auth`。
   - 任意受保护 API 返回 `423 AUTH_LOCKED` 时进入解锁流程。

4. 影片列表。
   - 调用 `GET /api/library/movies`。
   - 支持分页 `limit`、`offset`。
   - 支持搜索 `q`。
   - 支持基础筛选 `actor`、`studio`。
   - 支持收藏视图 `mode=favorites`。

5. 影片详情。
   - 调用 `GET /api/library/movies/{movieId}`。
   - 展示标题、番号、厂商、演员、标签、用户标签、简介、评分、时长、年份、发行日期、分辨率、封面、缩略图、预览图。
   - 详情页使用字符串 `movieId`，不再强制转成 `UUID`。

6. 图片加载。
   - 处理 `coverUrl`、`thumbUrl`、`previewImages`、`actorAvatarUrls`。
   - 后端返回相对 URL 时，按当前 `baseUrl` 转为绝对 URL。
   - Coil 必须使用与 API client 相同的 CookieJar，否则 PIN 开启后图片会被 `423` 拦截。

7. 播放。
   - 播放入口必须是 `GET /api/library/movies/{movieId}/playback`。
   - 根据 `PlaybackDescriptorDTO.mode` 播放 `direct` 或 `hls`。
   - `descriptor.url` 为相对路径时转为绝对 URL。
   - `resumePositionSec` 转换为毫秒后 seek。
   - 如果 descriptor 中存在 `sessionId`，退出播放器时调用 `DELETE /api/playback/sessions/{sessionId}`。

8. 进度同步。
   - 播放中周期性调用 `PUT /api/playback/progress/{movieId}`。
   - pause、退出播放器、App 进入后台时立即写入一次。
   - 使用秒作为协议单位，不再使用 Jellyfin ticks。
   - 网络失败时写入本地 progress outbox，恢复网络后只提交每个 movieId 的最新值。

9. 已播放与观看时长。
   - 播放达到业务阈值后调用 `POST /api/library/played-movies/{movieId}`。
   - 有效播放时长每约 60 秒调用 `POST /api/playback/watch-time/daily`。
   - `dayKey` 使用 Android 设备本地日期 `YYYY-MM-DD`。

10. 收藏与评分。
    - 收藏调用 `PATCH /api/library/movies/{movieId}`，body 为 `{ "isFavorite": true|false }`。
    - 评分调用同一 endpoint，body 为 `{ "rating": number|null }`。
    - 更新成功后刷新详情和列表缓存。

11. 设置与诊断。
    - 设置页展示当前服务端、health 快照、最后连接时间。
    - 提供重新测试连接、锁定当前会话、清除本地会话的入口。
    - 可读取 `GET /api/settings` 展示播放相关只读诊断，例如 `player.streamPushEnabled`。

### 2.2 第一版建议实现

1. 演员列表和演员详情。
   - `GET /api/library/actors`
   - `GET /api/library/actors/profile?name=`
   - `GET /api/library/actors/{name}/asset/avatar`

2. 首页轻量内容。
   - 继续观看：来自 `GET /api/playback/progress` 与影片详情合并。
   - 收藏入口：复用 `GET /api/library/movies?mode=favorites`。
   - 每日推荐：可读取 `GET /api/homepage/recommendations`，再按 movie id 拉取详情。

3. 影片评论和用户标签。
   - 评论：`GET/PUT /api/library/movies/{movieId}/comment`。
   - 用户标签：`PATCH /api/library/movies/{movieId}` 的 `userTags` 字段。

这些建议项不阻塞第一版联调。若影响核心链路进度，可以推迟到第二阶段。

## 3. 明确隐藏的功能

当前没有下载、电视剧集和用户管理需求。第一版必须从入口层隐藏这些功能，避免用户进入半成品或 Jellyfin 残留流程。

### 3.1 下载功能隐藏

隐藏范围：

- 底部导航中的 Downloads tab。
- 详情页、列表卡片、按钮栏中的下载按钮。
- 离线模式入口和文案。
- Android 下载任务 UI、下载进度卡片、下载存储选择。
- 与 Jellyfin media source 下载绑定的后台同步路径。

保留策略：

- 第一阶段可以保留底层未触达代码，减少重构风险。
- UI 和导航不可出现下载入口。
- WorkManager 不应继续调度 Findroid 的用户数据同步或下载相关流程。
- 不使用 `/api/library/movies/{movieId}/stream` 偷跑整片下载。

后续恢复条件：

- Curated 后端设计受控离线下载 API。
- Android 端明确存储配额、断点续传、清理、隐私提示和网络策略。

### 3.2 电视剧集功能隐藏

隐藏范围：

- TV shows、series、season、episode、next up 相关导航。
- `ShowScreen`、`SeasonScreen`、`EpisodeScreen` 入口。
- Home 中的 Next Up。
- 媒体库类型选择中的 TV Shows、Episodes。
- 与 Jellyfin `BaseItemKind.SERIES/SEASON/EPISODE` 相关的 UI 分支。

保留策略：

- Curated 当前 MVP 只对接影片库，即 `/api/library/movies*`。
- 现有剧集相关代码可以暂时保留在源码中，但不应被导航和 UI 触达。
- 新的 Curated domain model 不应包含 show/season/episode 类型。

后续恢复条件：

- Curated 后端新增剧集模型、列表、详情、播放、进度 API。
- Android 再按新的 Curated 契约设计，而不是复用 Jellyfin 剧集模型。

### 3.3 用户管理隐藏

隐藏范围：

- Findroid 的 UsersScreen。
- Jellyfin public users。
- username/password 登录。
- QuickConnect。
- 多用户切换。
- `User` 表作为当前服务端登录身份的概念。

替代策略：

- Curated Droid 第一版只有“服务端 profile + 当前 Cookie 会话”。
- PIN 是后端 App Lock，不是用户账号系统。
- 启动判断应从“是否有 current user”改为“是否有 server profile，且 auth status 已解锁或无需 PIN”。

后续恢复条件：

- Curated 后端引入真实用户、权限、token 或设备配对模型。
- Android 再实现账号管理或设备管理。

## 4. 目标导航结构

第一版手机端导航应收敛为：

```text
ServerGate
  ├─ AddServerScreen
  ├─ AuthLockScreen
  └─ MainShell
       ├─ HomeScreen
       ├─ LibraryScreen
       ├─ MovieDetailScreen
       ├─ PlayerScreen
       ├─ ActorsScreen
       └─ SettingsScreen
```

底部导航建议第一版只保留：

```text
Home
Library
Settings
```

Actors 可以从影片详情或 Library 筛选入口进入，不必第一版放到底部导航。

## 5. 当前代码改造映射

### 5.1 Gradle 与品牌

当前状态：

- `rootProject.name = "findroid"`。
- `applicationId = "dev.jdtech.jellyfin"`。
- `namespace = "dev.jdtech.jellyfin"`。
- `app_name = "Findroid"`。
- `data` 和 `app:phone` 仍依赖 Jellyfin SDK。

第一阶段策略：

- Phase 0 可以暂缓包名整体重构，降低大范围移动文件的风险。
- UI 可先改显示名为 Curated Droid。
- 当 Curated API 链路稳定后，再统一迁移 package、namespace、applicationId、图标、README、fastlane 文案。

最终目标：

- 项目名：`curated-droid`。
- 应用名：`Curated Droid` 或 `Curated`。
- applicationId 使用新的唯一包名。
- 移除 Jellyfin SDK 依赖。

### 5.2 网络层

现有状态：

- `data/src/main/java/dev/jdtech/jellyfin/api/JellyfinApi.kt` 基于 Jellyfin SDK。
- `core/src/main/java/dev/jdtech/jellyfin/di/ApiModule.kt` 根据当前 Server/User 更新 Jellyfin SDK client。

目标状态：

- 新建 Curated API client。
- 使用 OkHttp + kotlinx.serialization 或 Retrofit + kotlinx.serialization。
- 所有 API request 使用共享 OkHttpClient。
- 共享 CookieJar 同时服务 API、Coil、Media3。
- 统一添加：
  - `User-Agent: CuratedAndroid/<version> (Android <sdk>; <device>)`
  - `X-Curated-Client: android`
  - `X-Curated-Client-Version`
  - `X-Curated-OS: Android`
  - `X-Curated-OS-Version`

必须提供的基础能力：

- `normalizeBaseUrl(input: String): String`
- `apiUrl(path: String): String`
- `absoluteUrl(value: String?): String?`
- `AppError` 解析。
- HTTP status 到 domain error 的映射。
- `423 AUTH_LOCKED` 的全局拦截信号。

### 5.3 本地存储

现有状态：

- Room schema 以 Server、ServerAddress、User、FindroidMovieDto、FindroidShowDto、FindroidEpisodeDto、Source、UserData 为核心。
- `AppPreferences.currentServer` 保存当前 Jellyfin server id。
- `offlineMode` 和下载相关偏好仍存在。

目标状态：

第一版建议建立新的 Curated 本地模型：

```text
ServerProfile
  id: String
  displayName: String
  baseUrl: String
  lastHealthName: String
  lastVersion: String
  lastChannel: String
  lastInstallerVersion: String
  lastTransport: String
  lastConnectedAt: String
  trustedForever: Boolean

MovieListCache
  movieId: String
  title: String
  code: String
  studio: String
  actorsJson: String
  tagsJson: String
  userTagsJson: String
  runtimeMinutes: Int
  rating: Double
  isFavorite: Boolean
  addedAt: String
  resolution: String
  year: Int
  releaseDate: String?
  coverUrl: String?
  thumbUrl: String?
  pageMode: String
  queryKey: String
  cachedAt: String

MovieDetailCache
  movieId: String
  detailJson: String
  cachedAt: String

PlaybackProgressOutbox
  movieId: String
  positionSec: Double
  durationSec: Double
  updatedAt: String
```

敏感信息策略：

- `curated_auth` Cookie 不放普通 SharedPreferences 明文。
- CookieJar 应使用受保护存储；早期技术验证可先用文件 CookieJar，但 release 前必须替换或明确风险。
- PIN 明文和 PIN hash 都不在 Android 端保存。

### 5.4 Repository

现有状态：

- `JellyfinRepository` 暴露 Findroid/Jellyfin 模型。
- `JellyfinRepositoryImpl` 直接调用 Jellyfin SDK。
- `JellyfinRepositoryOfflineImpl` 负责离线下载缓存。

目标状态：

新增或替换为 `CuratedRepository`，第一版接口建议如下：

```kotlin
interface CuratedRepository {
    suspend fun getHealth(baseUrl: String? = null): Health
    suspend fun getAuthStatus(): AuthStatus
    suspend fun unlock(pin: String, trustedForever: Boolean): AuthStatus
    suspend fun lock(): AuthStatus

    fun getMoviesPaged(query: MovieQuery): Flow<PagingData<MovieListItem>>
    suspend fun getMovie(movieId: String): MovieDetail
    suspend fun updateMovie(movieId: String, patch: MoviePatch): MovieDetail

    suspend fun getPlayback(movieId: String): PlaybackDescriptor
    suspend fun createPlaybackSession(movieId: String, request: PlaybackSessionRequest): PlaybackDescriptor
    suspend fun deletePlaybackSession(sessionId: String)

    suspend fun getPlaybackProgress(): List<PlaybackProgress>
    suspend fun putPlaybackProgress(movieId: String, positionSec: Double, durationSec: Double)
    suspend fun markPlayed(movieId: String)
    suspend fun addWatchTime(movieId: String, dayKey: String, watchedSec: Int)

    suspend fun getSettings(): CuratedSettings
}
```

不进入第一版 repository 的能力：

- 下载整片。
- Jellyfin user views。
- Shows/seasons/episodes。
- QuickConnect。
- Android 本地扫描或导入。

### 5.5 Setup 与启动门控

现有状态：

- `MainViewModel` 判断 `hasServers`、`hasCurrentServer`、`hasCurrentUser`。
- 导航起点取决于是否存在 Jellyfin server/user。
- AddServer 使用 Jellyfin discovery。
- Login 使用 username/password 或 QuickConnect。

目标状态：

启动门控改为：

```text
读取当前 ServerProfile
  -> 不存在：AddServerScreen
  -> 存在：GET /api/health
      -> 失败：ServerConnectionErrorScreen 或 AddServerScreen 可编辑地址
      -> 成功：GET /api/auth/status
          -> setupRequired=true：提示用户回桌面端设置 PIN，或提供 setup-pin 可选入口
          -> pinEnabled=false：MainShell
          -> pinEnabled=true && unlocked=true：MainShell
          -> pinEnabled=true && unlocked=false：AuthLockScreen
```

AddServerScreen 职责：

- 输入 URL。
- 归一化 URL。
- 调用 health。
- 保存 ServerProfile。
- 进入 auth status 判断。

AuthLockScreen 职责：

- 输入 4 到 8 位数字 PIN。
- 勾选 trusted forever。
- 调用 unlock。
- 显示 `AUTH_INVALID_PIN`。
- 成功后进入 MainShell。

### 5.6 影片列表与详情

列表实现：

- 使用 Paging 继续保留现有分页体验。
- PagingSource 调用 `GET /api/library/movies?limit=&offset=&q=&actor=&studio=&mode=`。
- `getRefreshKey` 以 offset 为准。
- 每页响应写入列表缓存。
- 搜索输入 debounce 后重新创建 query。

详情实现：

- `MovieRoute` 参数从 `UUID` 改为 `String`。
- 详情页直接使用 `MovieDetail`。
- 演员点击可以进入 `ActorsScreen` 或回到 Library 并设置 `actor` 筛选。
- 收藏和评分调用 patch 后用返回的 `MovieDetailDTO` 刷新 UI。

需要移除或隐藏的详情页元素：

- 下载按钮。
- 电视剧/集数相关展示。
- Jellyfin trailer、media source selector、离线标记。

### 5.7 播放器

可复用：

- Media3/ExoPlayer 初始化。
- 播放控制层。
- 横屏播放器 Activity。
- PiP 基础能力。
- 快进/快退偏好。

需要替换：

- `PlaylistManager` 不再从 Jellyfin movie/show/episode 推导播放列表。
- `PlayerItem.itemId` 从 `UUID` 改为 `String`。
- 播放 URL 来自 `PlaybackDescriptorDTO.url`。
- `playbackPositionTicks` 改为 `resumePositionSec`。
- `postPlaybackStart/Stop/Progress` 改为 Curated progress、played、watch time API。
- 当前没有 Curated chapter、media segment、trickplay 契约，第一版隐藏跳章节、skip intro、trickplay scrubber。

播放器退出策略：

```text
onPause / onStop / onCleared
  -> putPlaybackProgress(movieId, currentPositionSec, durationSec)
  -> if played threshold reached: markPlayed(movieId)
  -> if descriptor.sessionId != null: deletePlaybackSession(sessionId)
```

watch time 策略：

- 只在 `player.isPlaying == true` 且非 buffering 时累计。
- 每 60 秒提交一次。
- 单次 `watchedSec` 保证 `1..300`。
- 快进、暂停、后台不累计。

### 5.8 WorkManager

现有 `SyncWorker` 是 Jellyfin userdata 同步，不适用于 Curated。

第一版调整：

- 停止调度 Findroid `syncUserData`。
- 新增 progress outbox sync worker。
- worker 只处理 `PlaybackProgressOutbox`。
- 网络恢复后按 movieId 提交最新进度，成功后删除 outbox 项。

保留：

- mpv 清理 worker 可以保留，前提是第一版仍允许启用 mpv。

## 6. 阶段计划

### Phase 0：Curated API 技术验证

目标：真机或模拟器跑通连接、PIN、列表、图片和单片播放。

实施内容：

1. 新增 Curated DTO 和 API client。
2. 新增 base URL 归一化、相对 URL 转绝对 URL。
3. 新增 CookieJar，并让 API 和 Coil 共享。
4. AddServerScreen 改为 `/api/health`。
5. 新增 AuthLockScreen。
6. LibraryScreen 加载 `/api/library/movies` 前 20 到 50 条。
7. 图片显示 `coverUrl` 或 `thumbUrl`。
8. 点击影片请求 playback descriptor 并播放 direct URL。
9. 退出播放器写入一次 progress。

验收：

- Android Emulator 可通过 `http://10.0.2.2:8080` 访问开发后端。
- 真机可通过 `http://<Windows 主机 IP>:8080` 访问开发后端。
- 后端开启 PIN 时，未解锁进入 PIN 页面，解锁后列表和图片正常。
- direct 播放能开始、暂停、seek、退出后继续播放。

### Phase 1：MVP 功能完整

目标：完成第一版用户可用闭环。

实施内容：

1. 完整 ServerProfile 管理。
2. Auth status 启动门控。
3. `423 AUTH_LOCKED` 全局处理。
4. 影片分页、搜索、演员筛选、片商筛选、收藏模式。
5. 影片详情完整字段展示。
6. 收藏和评分 patch。
7. direct/HLS descriptor 播放。
8. HLS session 退出清理。
9. progress 周期同步和 outbox。
10. played 标记。
11. watch time 周期统计。
12. Settings 诊断页。
13. 隐藏下载、电视剧集、用户管理所有入口。

验收：

- 列表分页不重复、不乱序、不一次性拉全量。
- 搜索和 actor/studio 筛选生效。
- 图片、详情、播放全部使用同一个 Curated 会话。
- HLS descriptor 可播放。
- 播放进度在 Web/Electron/Android 间一致。
- 收藏/评分更新后刷新可见。
- 底部导航没有 Downloads。
- UI 中没有 TV Shows、Season、Episode、Users、Login、QuickConnect 入口。

### Phase 2：体验完善

目标：补齐媒体库体验，但不扩展到下载和剧集。

实施内容：

1. 首页继续观看。
2. 首页每日推荐。
3. 演员列表和演员详情。
4. 演员筛选回到影片列表。
5. 用户标签展示与修改。
6. 影片评论读写。
7. 平板布局优化。
8. 播放失败时 direct 到 HLS 的显式 fallback。

验收：

- Home 可展示继续观看和推荐内容。
- 演员头像和演员资料可加载。
- 用户标签和评论在 Web/Electron/Android 间一致。
- 播放失败错误能区分网络、未解锁、视频不支持、HLS session 创建失败。

### Phase 3：品牌与发布准备

目标：从 Findroid fork 形态整理为 Curated Droid 产品。

实施内容：

1. 应用名、图标、主题名、README、隐私文案改为 Curated Droid。
2. applicationId 和 namespace 迁移。
3. 移除 Jellyfin SDK 依赖。
4. 移除或隔离下载、剧集、用户管理残留代码。
5. release cleartext 风险提示和安全策略。
6. GPLv3 合规说明保留。
7. Android client marker 与 connected clients 展示联调。

验收：

- 构建产物不再显示 Findroid/Jellyfin 品牌。
- 核心路径不依赖 Jellyfin SDK。
- 公开文档能说明 Curated Droid 与 Findroid/GPLv3 的关系。

## 7. 测试策略

### 7.1 单元测试

必须覆盖：

- `normalizeBaseUrl`
  - `http://host:8080`
  - `http://host:8080/`
  - `http://host:8080/api`
  - `http://host:8080/api/`
  - 非 http/https 输入失败

- `absoluteUrl`
  - `/api/library/movies/a/asset/cover`
  - `api/library/movies/a/asset/cover`
  - `http://other/asset.jpg`
  - 空字符串和 null

- `AppError` mapper
  - `423 AUTH_LOCKED`
  - `401 AUTH_INVALID_PIN`
  - `404 COMMON_NOT_FOUND`
  - `409 COMMON_CONFLICT`
  - `500 COMMON_INTERNAL`
  - 非 JSON 错误体

- DTO mapper
  - `MovieListItemDTO -> MovieListItem`
  - `MovieDetailDTO -> MovieDetail`
  - `PlaybackDescriptorDTO -> PlaybackDescriptor`
  - 可选字段缺失

- progress outbox
  - 同一 movieId 多次写入只保留最新值。
  - 成功提交后删除 outbox。
  - 失败时保留 outbox。

### 7.2 集成测试

建议使用 MockWebServer 覆盖：

- health 成功保存 server profile。
- auth unlock 收到 `Set-Cookie` 后后续请求自动带 `Cookie`。
- protected API 返回 `423` 时发出 Lock flow 信号。
- movies paging 正确传 `limit` 和 `offset`。
- playback descriptor 相对 URL 被转为绝对 URL。

### 7.3 手工验收

开发后端：

```bash
cd backend
go run ./cmd/curated
```

Android Emulator：

```text
http://10.0.2.2:8080
```

物理设备：

```text
http://<Windows 主机局域网 IP>:8080
```

手工检查：

- Windows 防火墙允许后端端口入站。
- 手机和电脑在同一局域网。
- 输入错误 IP 时有清晰错误。
- PIN 错误显示明确提示。
- 后端关闭后 App 不崩溃。
- 后端重启后 Cookie 失效时能重新解锁。
- 图片不会暴露服务端本地磁盘路径。
- 播放 URL 由 descriptor 提供，不由 UI 拼接 `/stream`。

## 8. 关键风险与处理

### 8.1 Cookie 共享风险

风险：

API、Coil、Media3 如果使用不同 HTTP client，PIN 开启后会出现 API 成功但图片或视频 423 的问题。

处理：

- 建立应用级 CuratedOkHttpClient。
- API client、Coil ImageLoader、Media3 data source 都使用同一个 CookieJar。
- Auth lock 时清理 CookieJar，并刷新 UI 状态。

### 8.2 UUID 假设风险

风险：

Findroid 大量模型和导航参数使用 `UUID`，Curated `movieId` 是字符串。强转 UUID 会导致非 UUID id 崩溃。

处理：

- Curated domain 的 id 全部使用 `String`。
- 新导航 route 使用 `String`。
- 只在保留的 Findroid 代码内部继续使用 UUID；Curated 页面不得依赖 UUID。

### 8.3 Jellyfin 残留入口风险

风险：

未隐藏的 Downloads、Users、TV Shows、Episodes 入口会把用户带回 Jellyfin 逻辑，造成崩溃或错误请求。

处理：

- 先从 NavigationRoot 和按钮层隐藏入口。
- 再逐步删除 DI 中不可达的 Jellyfin repository 分支。
- 每阶段验收都检查 UI 是否还有这些入口。

### 8.4 Cleartext HTTP 风险

风险：

Curated 后端当前默认 HTTP，局域网内 Cookie 和媒体请求是明文。

处理：

- MVP 允许局域网 HTTP，但只面向可信家庭/个人 LAN。
- 设置页展示安全提示。
- 不鼓励公网暴露端口。
- 后续配合 HTTPS、配对 token 或 LAN policy。

### 8.5 播放能力风险

风险：

Direct URL 不一定被设备硬解支持，HLS session 依赖后端 `streamPushEnabled` 和 FFmpeg。

处理：

- 先按 descriptor 播放。
- direct 失败时读取 settings 判断是否可尝试 HLS session。
- HLS session 创建失败时提示用户检查 Curated Settings -> Playback 的 Stream Push / FFmpeg。
- 不把播放失败静默标记为已播放。

## 9. 完成定义

第一版完成时必须满足：

1. App 启动后只出现 Curated 连接、PIN、媒体库、影片详情、播放器、设置诊断相关界面。
2. UI 中没有下载、电视剧集、Jellyfin 用户管理入口。
3. 所有后端请求走 Curated REST API。
4. 播放入口使用 playback descriptor。
5. API、图片、播放共享同一 Cookie 会话。
6. 进度、已播放、观看时长能写回 Curated 后端。
7. 常见错误有明确提示，不崩溃。
8. 真机可在同一局域网访问 Windows 主机上的 Curated 后端。

## 10. 推荐执行顺序

1. 先做 Curated network/session 基础设施。
2. 再做 AddServer 和 AuthLock，替换启动门控。
3. 然后做 Movie list/detail 的最小闭环。
4. 接着接入 playback descriptor 和 progress。
5. 再隐藏 Downloads、TV、Users 的所有入口。
6. 最后补 Settings 诊断、watch time、收藏评分、HLS 清理和测试。

该顺序的原因是 Cookie 会话会影响 API、图片、播放三条链路；如果先改 UI，后续很容易在图片和播放器里重复补鉴权逻辑。
