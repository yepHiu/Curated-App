# Curated Droid 播放链路架构

日期：2026-06-13
状态：基于当前源码核对的播放链路说明
事实来源：`doc/API.md`、`doc/2026-06-08-curated-droid-mvp-progress.md`、Android 源码

## 1. 总览

当前项目处于从 Findroid / Jellyfin 播放体系迁移到 Curated 后端播放体系的过渡阶段。

主电影播放路径已经切到 Curated 后端的 playback descriptor，不再依赖旧 Jellyfin SDK、UUID playlist 或 Jellyfin 播放进度接口。

仓库中仍保留旧 `PlayerActivity`、`PlayerViewModel`、`PlaylistManager` 和 Jellyfin repository 播放链路，主要服务旧页面和遗留模块。当前 Curated 电影主流程不会走旧播放器。

## 2. 主播放链路

当前 Curated 主播放链路如下：

```text
CuratedMoviesScreen
  -> MovieRoute(movieId: String)
  -> CuratedMovieDetailScreen
  -> Play button
  -> CuratedPlayerActivity Intent extras
  -> CuratedPlayerViewModel
  -> CuratedRepositoryFactory.createForCurrentServer()
  -> CuratedRepositoryImpl / CuratedApiClient
  -> GET /api/library/movies/{movieId}/playback
  -> PlaybackDescriptorDTO
  -> CuratedMappers.toDomain()
  -> absolute playback URL
  -> Media3 ExoPlayer + OkHttpDataSource(shared OkHttpClient)
  -> backend direct stream or HLS URL
```

关键入口：

- `app/phone/src/main/java/dev/jdtech/jellyfin/NavigationRoot.kt`
  - `HomeRoute` 和 `MediaRoute` 进入 `CuratedMoviesScreen`。
  - `MovieRoute` 渲染 `CuratedMovieDetailScreen`。
  - `onPlayMovie` 启动 `CuratedPlayerActivity`。
- `app/phone/src/main/java/dev/jdtech/jellyfin/CuratedPlayerContract.kt`
  - Intent extras：
    - `curatedMovieId`
    - `curatedTitle`
- `app/phone/src/main/java/dev/jdtech/jellyfin/presentation/curated/CuratedMovieDetailScreen.kt`
  - Play 按钮调用 `onPlayMovie(movie.id, movie.title)`。

## 3. Activity 与 ViewModel 分工

### 3.1 `CuratedPlayerActivity`

文件：

```text
app/phone/src/main/java/dev/jdtech/jellyfin/CuratedPlayerActivity.kt
```

职责：

- 从 Intent 读取 `curatedMovieId` 和 `curatedTitle`。
- 绑定 `ActivityPlayerBinding` 和 Media3 `PlayerView`。
- 把 `PlayerView.player` 指向 `CuratedPlayerViewModel.player`。
- 初始化横屏沉浸播放 UI。
- 管理控制层显示、锁定、倍速、音轨、字幕、PiP、系统 UI 隐藏。
- 在 `onPause()` 中调用 `viewModel.updatePlaybackProgress()`。
- 结束播放时清理 video surface 并 finish Activity。

当前注意点：

- `updatePlaybackProgress()` 目前在 ViewModel 中为空实现，因此 `onPause()` 虽有调用，但不会写回后端。
- Activity 保存了 `sessionId` 到 UI state，但退出时还没有调用 session delete。

### 3.2 `CuratedPlayerViewModel`

文件：

```text
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/curated/CuratedPlayerViewModel.kt
```

职责：

- 通过 Hilt 注入：
  - `CuratedRepositoryFactory`
  - 共享 `OkHttpClient`
  - `AppPreferences`
- 构建 Media3 `ExoPlayer`。
- 使用 `OkHttpDataSource.Factory(okHttpClient)` 构建 `DefaultMediaSourceFactory`。
- 播放前调用：

```text
GET /api/library/movies/{movieId}/playback
```

- 将 `PlaybackDescriptor` 转成 Media3 `MediaItem`：
  - `mediaId = descriptor.movieId`
  - `uri = descriptor.url`
  - `mimeType = descriptor.mimeType`
  - title 优先使用页面传入 title，否则使用 `descriptor.fileName`，最后 fallback 到 `movieId`
- 调用：

```text
player.setMediaItems(listOf(mediaItem), 0, descriptor.curatedStartPositionMs())
player.prepare()
player.play()
```

- 根据播放状态更新 UI：
  - `STATE_READY` -> `fileLoaded = true`
  - `STATE_ENDED` -> 发送 `NavigateBack`
- 释放时 remove listener 并 release player。

## 4. 网络与 Cookie 会话

当前播放链路的关键设计是：API、图片、视频流共用同一个 Curated HTTP 会话。

### 4.1 Hilt 注入

文件：

```text
core/src/main/java/dev/jdtech/jellyfin/di/ApiModule.kt
```

Hilt 提供：

- 单例 `CuratedCookieJar`
- 单例 `OkHttpClient`
- 单例 `CuratedRepositoryFactory`

`OkHttpClient` 由 `CuratedOkHttpClientFactory.create()` 构建。

### 4.2 OkHttp Client

文件：

```text
data/src/main/java/dev/jdtech/jellyfin/curated/api/CuratedOkHttpClientFactory.kt
data/src/main/java/dev/jdtech/jellyfin/curated/api/CuratedCookieJar.kt
```

职责：

- 保存和回传 `curated_auth` cookie。
- 设置请求超时。
- 添加 Android 客户端识别 header：
  - `User-Agent: CuratedAndroid/<version> (Android <osVersion>)`
  - `X-Curated-Client: android`
  - `X-Curated-Client-Version`
  - `X-Curated-OS`
  - `X-Curated-OS-Version`

### 4.3 Coil 图片链路

文件：

```text
app/phone/src/main/java/dev/jdtech/jellyfin/BaseApplication.kt
```

Coil `ImageLoader` 使用 `OkHttpNetworkFetcherFactory(callFactory = { curatedHttpClient })`。

这意味着图片请求和播放请求共用同一个 `OkHttpClient`、同一个 `CuratedCookieJar`。

### 4.4 Media3 播放链路

文件：

```text
app/phone/src/main/java/dev/jdtech/jellyfin/presentation/curated/CuratedPlayerViewModel.kt
```

播放器使用：

```kotlin
val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
```

因此 direct stream 或 HLS playlist / segment 请求也会复用 Curated Cookie 会话。

## 5. Repository 与 API 层

### 5.1 Server Base URL

文件：

```text
data/src/main/java/dev/jdtech/jellyfin/curated/repository/CuratedRepositoryFactory.kt
```

`CuratedRepositoryFactory.createForCurrentServer()` 读取当前 server：

```text
AppPreferences.currentServer
  -> ServerDatabaseDao.getServerCurrentAddress(serverId)
  -> CuratedRepositoryImpl(baseUrl, shared OkHttpClient)
```

### 5.2 API Client

文件：

```text
data/src/main/java/dev/jdtech/jellyfin/curated/api/CuratedApiClient.kt
```

当前播放相关 API client 只实现了：

```text
GET /api/library/movies/{movieId}/playback
```

实现细节：

- 内部路径为 `/library/movies/{movieId}/playback`。
- `movieId` 使用 `HttpUrl.Builder.addPathSegment(movieId)`，会进行路径参数编码。
- 返回 `PlaybackDescriptorDto`。

### 5.3 URL 规范化

文件：

```text
data/src/main/java/dev/jdtech/jellyfin/curated/api/CuratedUrlResolver.kt
data/src/main/java/dev/jdtech/jellyfin/curated/api/CuratedMappers.kt
```

规则：

- server base URL 必须以 `http://` 或 `https://` 开头。
- base URL 结尾的 `/` 会被移除。
- base URL 如果以 `/api` 结尾，也会移除 `/api`。
- `apiUrl()` 会补 `/api/...`。
- DTO 中的相对媒体 URL 会通过 `absoluteUrl()` 转成绝对 URL。

`PlaybackDescriptorDto.toDomain()` 会把 descriptor 的 `url` 转成绝对播放 URL。

## 6. 后端播放协议

事实源：

```text
doc/API.md
```

播放入口：

```text
GET /api/library/movies/{movieId}/playback
```

后端返回 `PlaybackDescriptorDTO`。关键字段：

- `movieId`
- `mode`
- `sessionId`
- `sessionKind`
- `url`
- `mimeType`
- `fileName`
- `transcodeProfile`
- `durationSec`
- `startPositionSec`
- `resumePositionSec`
- `canDirectPlay`
- `reasonCode`
- `reasonMessage`
- `audioTracks`
- `subtitleTracks`

`mode` 支持：

- `direct`
  - `url` 通常指向 `/api/library/movies/{movieId}/stream`。
  - 后端视频响应支持 Range / 206。
- `hls`
  - `url` 指向 HLS `.m3u8`。
  - 后续 segment 走 `/api/playback/sessions/{sessionId}/hls/{file}`。
- `native`
  - 服务端机器启动外部本地播放器。
  - Android / 远程客户端不应把它理解成手机本地播放。

当前 Android 端没有对 `mode` 做显式业务分支，而是把 `descriptor.url` 交给 ExoPlayer，由 Media3 根据 URL 和 MIME 播放 direct 或 HLS。

## 7. 当前未完成的播放闭环

当前已经完成“能通过 descriptor 起播”的 MVP 主链路，但完整播放闭环尚未实现。

待实现项：

1. 进度回写
   - API：`PUT /api/playback/progress/{movieId}`
   - 当前状态：`CuratedPlayerViewModel.updatePlaybackProgress()` 是空实现。

2. HLS session 清理
   - API：`DELETE /api/playback/sessions/{sessionId}`
   - 当前状态：descriptor 的 `sessionId` 保存进 UI state，但退出播放器时未调用 delete。

3. 已播放标记
   - API：`POST /api/library/played-movies/{movieId}`
   - 当前状态：未实现播放阈值判断和写回。

4. 观看时长统计
   - API：`POST /api/playback/watch-time/daily`
   - 当前状态：未实现有效观看时长累计和提交。

5. direct 到 HLS 的显式 fallback
   - API：`POST /api/library/movies/{movieId}/playback-session`
   - 当前状态：direct 播放失败后没有创建 HLS session 的 fallback。

6. playback API client 扩展
   - 当前 `CuratedApiClient` 只暴露 `getPlaybackDescriptor()`。
   - 尚未暴露 playback session、progress、watch time、played movies、session delete 等方法。

## 8. 当前源码与旧文档差异

`doc/2026-06-08-curated-droid-mvp-progress.md` 中记录：

```text
resumePositionSec ?: startPositionSec 会转换为毫秒作为起播位置
```

但当前源码不是这样。

当前 `CuratedPlayerContract.curatedStartPositionMs()` 只读取：

```kotlin
val startSeconds = startPositionSec ?: 0.0
```

也就是说：

- `startPositionSec` 存在时，从该位置起播。
- `startPositionSec` 缺失时，从 0 起播。
- `resumePositionSec` 当前被忽略。

测试 `CuratedPlayerContractTest.playbackDescriptorIgnoresResumePositionWhenStartPositionIsMissing()` 也明确覆盖了这个行为。

后续如果要实现续播，应先确认业务规则：

- 使用 `resumePositionSec ?: startPositionSec`
- 还是继续只认 `startPositionSec`
- 或由 UI 提供“继续播放 / 从头播放”两种入口

确认后应同步更新：

- `CuratedPlayerContract.kt`
- `CuratedPlayerContractTest.kt`
- `doc/2026-06-08-curated-droid-mvp-progress.md` 或新进度文档
- 本文档
- `doc/AGENT_MEMORY.md`

## 9. 旧播放链路残留

旧播放链路仍在仓库中：

```text
PlayerActivity
  -> PlayerViewModel
  -> PlaylistManager
  -> JellyfinRepository
  -> Findroid / Jellyfin media sources
  -> MPVPlayer or Media3 ExoPlayer
  -> Jellyfin-style playback progress / stop
```

关键文件：

- `app/phone/src/main/java/dev/jdtech/jellyfin/PlayerActivity.kt`
- `player/local/src/main/java/dev/jdtech/jellyfin/player/local/presentation/PlayerViewModel.kt`
- `player/local/src/main/java/dev/jdtech/jellyfin/player/local/domain/PlaylistManager.kt`

旧链路特征：

- Intent 中读取 `itemId`，并用 `UUID.fromString()` 解析。
- 依赖 `itemKind` 和 Jellyfin `BaseItemKind`。
- `PlaylistManager` 通过 `JellyfinRepository` 获取 movie / series / season / episode 和 media source。
- 播放器可选 MPV 或 ExoPlayer。
- `PlayerViewModel.updatePlaybackProgress()` 调旧 `repository.postPlaybackProgress()`。
- `releasePlayer()` 调旧 `repository.postPlaybackStop()`。

当前 Curated 电影主流程不会走旧播放器。旧链路仍可能被遗留的 `MovieScreen`、`EpisodeScreen`、`SeasonScreen`、`ShowScreen` 等旧 Findroid 页面引用，但这些页面不是当前 Curated MVP 的主入口。

## 10. 后续维护建议

后续修改播放链路时，必须同步检查：

1. `doc/API.md`
2. `doc/2026-06-13-curated-droid-playback-architecture.md`
3. `doc/AGENT_MEMORY.md`
4. `AGENTS.md` 中的 API 和记忆维护规则
5. `CuratedApiClient` / `CuratedRepository` / `CuratedPlayerViewModel`
6. `CuratedPlayerContractTest` 和相关播放测试

推荐下一步实现顺序：

1. 为 Curated API client / repository 增加 playback progress、session delete、played movies、watch time 方法。
2. 在 `CuratedPlayerViewModel` 中记录当前 movieId、duration、position、sessionId。
3. 在 pause、退出、后台切换时写入一次 progress。
4. 增加周期性 progress 写回。
5. 播放结束或达到阈值时写 played movies。
6. 每约 60 秒累计 watch time，并限制单次提交 `watchedSec <= 300`。
7. 对 HLS descriptor 在退出时调用 session delete。
8. 处理 direct 失败到 HLS session 的显式 fallback。

