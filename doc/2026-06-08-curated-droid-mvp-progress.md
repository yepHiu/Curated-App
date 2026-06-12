# Curated Droid MVP 进展：影片库、详情、播放闭环

日期：2026-06-08  
状态：Phase 0 MVP 主链路已接入  
后端临时地址：`http://192.168.31.251:8081/`

## 1. 本轮目标

本轮按当前 MVP 要求继续实施：

- 能够连接暂定 Curated 后端 `http://192.168.31.251:8081/`。
- 能够拉取影片库。
- 能够展示影片封面。
- 能够进入影片详情页。
- 能够通过后端 playback descriptor 播放影片。

当前仍不做下载、电视剧集和用户管理；这些入口继续隐藏或不可从主流程触达。

## 2. 默认后端地址

修改位置：

```text
setup/src/main/java/dev/jdtech/jellyfin/setup/presentation/addserver/AddServerState.kt
setup/src/main/java/dev/jdtech/jellyfin/setup/presentation/addserver/AddServerAction.kt
setup/src/main/java/dev/jdtech/jellyfin/setup/presentation/addserver/AddServerViewModel.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/setup/addserver/AddServerScreen.kt
setup/src/test/java/dev/jdtech/jellyfin/setup/presentation/addserver/AddServerStateTest.kt
```

已实现：

- AddServer 输入框默认预填 `http://192.168.31.251:8081/`。
- 用户仍可手动修改地址。
- 输入框状态从页面本地状态收敛到 `AddServerState.serverAddress`，方便测试和后续持久化。

## 3. Curated Repository 工厂

新增 / 修改位置：

```text
data/src/main/java/dev/jdtech/jellyfin/curated/repository/CuratedRepositoryFactory.kt
core/src/main/java/dev/jdtech/jellyfin/di/ApiModule.kt
```

已实现：

- `CuratedRepositoryFactory.createForCurrentServer()` 会读取当前 server profile。
- 当前 server 地址来自：

```text
AppPreferences.currentServer
ServerDatabaseDao.getServerCurrentAddress(serverId)
```

- factory 使用应用级共享 `OkHttpClient` 创建 `CuratedRepositoryImpl`。
- Hilt 通过 `ApiModule` 提供单例 factory，供列表、详情和播放器复用。

## 4. 影片库页面

新增 / 修改位置：

```text
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/curated/CuratedMoviesViewModel.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/curated/CuratedMoviesScreen.kt
app/phone/src/main/java/dev/jdtech/jellyfin/NavigationRoot.kt
```

已实现：

- `HomeRoute` 和 `MediaRoute` 当前都进入新的 `CuratedMoviesScreen`。
- `CuratedMoviesViewModel` 调用：

```text
GET /api/library/movies?limit=50&offset=0
```

- 列表使用 `coverUrl ?: thumbUrl` 展示封面。
- 卡片展示 `title`、`code`、`studio`、`year` 等基础信息。
- 请求失败时显示错误与 Retry。
- 空库时显示空状态。
- 顶部保留 Settings 和 Servers 入口。
- 旧 Media tab 的“重复点击展开搜索”行为已关闭，避免没有搜索 UI 时隐藏底部导航。

## 5. 影片详情页

新增 / 修改位置：

```text
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/curated/CuratedMovieDetailViewModel.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/curated/CuratedMovieDetailScreen.kt
app/phone/src/main/java/dev/jdtech/jellyfin/NavigationRoot.kt
app/phone/src/test/java/dev/jdtech/jellyfin/NavigationRootTest.kt
```

已实现：

- `MovieRoute.movieId` 继续使用 `String`。
- 新增 `curatedMovieIdForRoute(MovieRoute)`。
- 测试覆盖非 UUID movie id，例如：

```text
movie/ABC-001
```

- `MovieRoute` 不再调用 `UUID.fromString(route.movieId)`。
- 详情页调用：

```text
GET /api/library/movies/{movieId}
```

- 详情页展示 title、封面/缩略图、summary、studio、actors、tags、resolution、rating 等字段。
- 详情页 Play 按钮进入新的 Curated 播放器。

## 6. Curated 播放器

新增 / 修改位置：

```text
app/phone/src/main/java/dev/jdtech/jellyfin/CuratedPlayerContract.kt
app/phone/src/main/java/dev/jdtech/jellyfin/CuratedPlayerActivity.kt
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/curated/CuratedPlayerViewModel.kt
app/phone/src/main/AndroidManifest.xml
app/phone/build.gradle.kts
gradle/libs.versions.toml
app/phone/src/test/java/dev/jdtech/jellyfin/CuratedPlayerContractTest.kt
```

已实现：

- 新增独立 `CuratedPlayerActivity`，避免旧 `PlayerActivity` 的 UUID / Jellyfin playlist 假设。
- 播放入口通过 Intent extras 传递：

```text
curatedMovieId
curatedTitle
```

- 测试覆盖 `curatedMovieId` 可以保留任意字符串 ID。
- `CuratedPlayerViewModel` 播放前调用：

```text
GET /api/library/movies/{movieId}/playback
```

- 使用 `PlaybackDescriptor.url` 创建 Media3 `MediaItem`。
- `resumePositionSec ?: startPositionSec` 会转换为毫秒作为起播位置。
- 新增 `media3-datasource-okhttp` 依赖。
- 播放器使用 `OkHttpDataSource.Factory(shared OkHttpClient)`，因此视频流请求会复用与 API / Coil 相同的 `CookieJar`。
- 支持 direct 和 HLS URL 的基础播放能力；实际播放模式由后端 descriptor URL 决定。
- 保留横屏播放、播放控制、PiP 基础能力、倍速选择、音轨/字幕轨选择。

## 7. 当前仍未完成

本轮先完成“能拉库、能看详情、能播放”的 MVP 主链路。以下事项仍在下一步：

- `PUT /api/playback/progress/{movieId}` 进度回写。
- 播放退出时 `DELETE /api/playback/sessions/{sessionId}`。
- 播放达到阈值后的 `POST /api/library/played-movies/{movieId}`。
- watch time 统计 `POST /api/playback/watch-time/daily`。
- direct 失败后显式 fallback 到 HLS session。
- 全局 `423 AUTH_LOCKED` 拦截、跳转 AuthLock、解锁后重试原请求。
- 列表分页加载下一页、搜索、actor/studio/favorites 筛选。
- 收藏、评分、评论、用户标签等详情页交互。

## 8. 已通过验证

使用的 Java：

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
```

已通过命令：

```powershell
.\gradlew.bat :setup:testDebugUnitTest --tests dev.jdtech.jellyfin.setup.presentation.addserver.AddServerStateTest
.\gradlew.bat :app:phone:testLibreDebugUnitTest --tests dev.jdtech.jellyfin.CuratedPlayerContractTest --tests dev.jdtech.jellyfin.NavigationRootTest
.\gradlew.bat :data:testDebugUnitTest --tests dev.jdtech.jellyfin.curated.*
.\gradlew.bat :app:phone:assembleLibreDebug
.\gradlew.bat ktfmtCheck
```

构建产物目录：

```text
app/phone/build/outputs/apk/libre/debug/
```

当前生成的 debug APK 包含 ABI 分包：

```text
phone-libre-arm64-v8a-debug.apk
phone-libre-armeabi-v7a-debug.apk
phone-libre-x86-debug.apk
phone-libre-x86_64-debug.apk
```
