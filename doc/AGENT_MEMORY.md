# Agent Memory

本文件是 Curated App 项目的 agent 记忆索引。它记录后续 agent 应主动继承和维护的长期事实、规则和变更线索。

更新时间：2026-07-07
维护者：agent

## 事实源

后续 agent 开始任务前，应按任务类型读取这些事实源：

| 任务类型 | 必读文件 |
| --- | --- |
| 任意项目任务 | `AGENTS.md` |
| API、网络、鉴权、DTO、媒体 URL、播放、导入、扫描、任务轮询 | `doc/API.md` |
| 播放链路、播放器迁移、Curated descriptor 播放架构 | `doc/2026-06-13-curated-droid-playback-architecture.md` |
| Android UI、颜色、主题、Compose 样式 | `doc/2026-06-08-curated-android-ui-color-style-guide.md` |
| 项目记忆、规则、目录用途、长期约定 | `doc/AGENT_MEMORY.md` |
| Android App 当前实现进度 | `doc/2026-06-08-curated-droid-mvp-progress.md` |
| 早期架构和实现背景 | `doc/2026-06-07-curated-droid-code-structure-analysis.md`、`doc/2026-06-07-curated-droid-mvp-implementation.md` |

## 长期项目事实

- `doc/` 是 agent 的文档生成和长期保留目录。
- 根目录 `AGENTS.md` 是 agent 自动读取的项目规则入口。
- `doc/AGENT_MEMORY.md` 是 agent 主动维护的项目记忆索引。
- `doc/API.md` 是当前 Curated 后端 HTTP API 的公开契约事实源。
- Android 客户端和其他衍生客户端应对接同一个 Go 后端 API。
- 所有 HTTP API 都挂在 `/api` 前缀下。
- API 成功响应直接返回 DTO 本体，不包 `{ "ok": true, "data": ... }`。
- 空成功响应使用 `204 No Content`。
- 失败响应使用 `AppError` JSON，核心字段为 `code`、`message`、`retryable`、`details`。
- PIN 解锁依赖 `curated_auth` cookie；Android / 非浏览器客户端必须使用 CookieJar 保存并自动回传。
- DTO 中的媒体 URL 可能是相对路径，客户端必须基于后端 origin 解析成绝对 URL。
- 视频流、图片资源、导出下载和大文件上传不能按 JSON 响应处理。
- 当前 Curated 电影主播放链路是 `CuratedMovieDetailScreen -> CuratedPlayerActivity -> CuratedPlayerViewModel -> GET /api/library/movies/{movieId}/playback -> Media3 ExoPlayer`。
- Curated 播放器通过 `OkHttpDataSource.Factory(shared OkHttpClient)` 播放 descriptor URL，因此 API、图片和视频流共享同一个 `CuratedCookieJar` 会话。
- 旧 `PlayerActivity` / `PlayerViewModel` / `PlaylistManager` 仍保留为 Findroid / Jellyfin 播放链路残留，不是当前 Curated 电影主播放路径。
- `PlayerActivity` 和 `CuratedPlayerActivity` 进入播放页和解锁控制层后使用 `sensor` / `SCREEN_ORIENTATION_SENSOR`，让竖屏手机保持竖屏播放，只有设备传感器触发横屏时才横屏；锁定控制层时才使用 `SCREEN_ORIENTATION_LOCKED` 保持当前方向。
- `PlayerActivity` 和 `CuratedPlayerActivity` 的播放控制条不展示音频轨道设置和字幕轨道设置入口。
- 播放页竖屏中间控制按钮必须保持紧凑并几何居中：`exo_main_controls.xml` 中心行应占满父宽、使用 `android:gravity="center"`，按钮间距保持左右对称的 `8dp`，按钮自身保留 `16dp` padding 以维持触控面积。
- 播放页控制层背景使用 `player_background = #66000000`，即约 40% 黑色遮罩；不要把暂停/控制层显示时的普通播放器 HUD 调回高不透明度，以免视频画面被过度遮挡。隐私保护遮罩是独立机制，不应与播放器控制层背景混用。
- Android 播放器手势排除区必须基于当前 `PlayerView` 实际宽高计算，不能使用 `Resources.getSystem().displayMetrics` 作为横竖屏边界；否则横屏时右半屏可能被误判为系统手势区，导致右侧音量竖滑失效。
- Android 播放器单指拖动规则：远离系统手势边缘时，横向占优拖动进入进度预览并在松手时 seek，竖向占优拖动保留左半屏亮度、右半屏系统媒体音量调节。
- 当前 Curated 播放器已实现 progress 回写：播放中约每 10 秒、暂停/停止播放和播放结束时调用 `PUT /api/playback/progress/{movieId}`，写入失败只记录日志，不中断播放。
- 当前 Curated 播放闭环尚未实现 HLS session delete、played movies、watch time 统计和 direct 到 HLS 的显式 fallback。
- Android 观看历史入口位于底部导航 `History` tab，使用 `GET /api/playback/progress` 作为数据源，按 `updatedAt` 倒序展示，并调用 `GET /api/library/movies/{movieId}` 补全标题、封面和元数据；电影详情补全使用有上限并发请求，单条详情失败时跳过该行，进度列表失败时显示页面错误和重试；历史卡片点击后直接启动 `CuratedPlayerActivity` 播放，不进入电影详情页。
- Android My media 电影卡片会加载 `GET /api/playback/progress` 并在缩略图和标题之间展示历史播放进度小横条；没有有效 duration 或 position 为 0 时不展示。
- Android `HomeRoute` 使用独立 `CuratedHomeScreen` / `CuratedHomeViewModel`，通过 `GET /api/homepage/recommendations` 读取当天推荐快照，再按 `heroMovieIds` 和 `recommendationMovieIds` 调用 `GET /api/library/movies/{movieId}` 补全影片详情并渲染 Hero 和今日推荐；单个影片详情失败时跳过该卡片。
- Android `MediaRoute` 使用 `CuratedMoviesScreen` / `CuratedMoviesViewModel` 作为完整电影库，电影列表通过 `GET /api/library/movies?limit=50&offset=N` 按滚动位置分页加载；不要通过单次提高 limit 来假装完整列表。
- Android My media 顶部栏包含搜索入口和设置入口；当前搜索能力仅限影片，使用现有 `GET /api/library/movies?q=<query>&limit=50&offset=N`，搜索结果继续按滚动位置分页加载。
- Android 影片详情页使用 `MovieDetail.previewImages` 展示横向预览图缩略图，点击缩略图打开全屏图片查看器并支持上一张 / 下一张切换；不要在 Android 端循环请求 `/api/library/movies/{movieId}/asset/preview/{index}` 探测数量。
- 当前源码中的 `curatedStartPositionMs()` 使用 `resumePositionSec ?: startPositionSec ?: 0.0`，因此影片会优先从后端返回的历史播放位置续播。
- Curated 底部导航当前展示 `Home`、`My media`、`History`；`DownloadsRoute` 仍保留但不在底部导航展示。
- Curated 首页顶部栏保留设置入口，My media 顶部栏保留搜索和设置入口，二者都不再直接展示服务器入口；服务器管理入口保留在设置页的 Servers / 服务器设置项中。
- Curated 设置页不展示偏好音频语言、偏好字幕语言、界面分类、进度条预览图 / trickplay 相关设置；“显示额外信息”开关保留并直接显示在设置根页。
- Android 隐私防护由 App 自己注册的 `Application.ActivityLifecycleCallbacks` 驱动，不依赖 `ProcessLifecycleOwner`；默认开启系统媒体静音、播放器内部音量归零、30% 黑色模糊视觉遮挡、Android 12+ blur，以及 `FLAG_SECURE` 截图/录屏/最近任务预览保护。
- Android 隐私防护的播放器内部静音只应在隐私静音状态生效；播放页处于前台时 `Player.volume` 必须恢复为可听音量，避免系统音量已调高但播放器内部仍然无声。系统媒体静音只在 App 进入前台和退到后台的边界触发，不应在 App 内 Activity 切换的 pause 事件中触发。
- Android 视觉遮挡必须区分 App 内 Activity 切换与真正前后台切换：Activity pause 时可临时显示遮罩保护最近任务快照，但如果未发生 App 退后台，原 Activity resume 时必须自动清理该 pause 遮罩，避免从播放器返回详情页后残留模糊层。
- Android 隐私防护设置位于设置页 `Privacy protection` 分类，包含 `privacyGazeProtection`、`privacyAutoMute`、`privacySecureScreen`、`privacyPlayerInternalMute` 四个本地开关，默认值均为 `true`。
- Android UI 视觉方向是默认深色、内容优先、低干扰媒体库界面、粉色品牌主色。
- Android Compose 页面顶部栏、返回按钮、标题、筛选栏和首屏主要内容必须主动处理 `WindowInsets.safeDrawing` 或项目 `rememberSafePadding()`，避免与通知栏、挖孔屏或显示裁切区域重叠；不要只写固定顶部间距。
- Curated 底部导航 / 导航栏 item 的选中态图标和文字必须使用高对比内容色（当前为 `MaterialTheme.colorScheme.onSurface`），不要依赖 Material 默认灰色 token；未选中态可使用 `onSurfaceVariant`。
- 品牌主色是 `#FE628E`，深色主背景是 `#0D0F1A`，浅色主背景是 `#F4F6FC`。
- Android 首版主题偏好应支持 `dark`、`light`、`system`，默认 `dark`。
- 主题偏好是设备本地设置，不应与后端账号、PIN 解锁状态绑定。
- Kotlin / Compose UI 组件中不要散落硬编码 hex，应通过语义 token、`MaterialTheme.colorScheme` 或主题封装使用颜色。
- 代码修改完成并通过必要验证后，agent 应主动尝试 git commit；提交必须原子化，且提交信息使用 `add:`、`fix:`、`enh:`、`docs:`、`test:`、`refactor:` 等成熟前缀。

## 维护规则

Agent 必须主动维护以下文档：

1. API 变更
   - 更新 `doc/API.md`。
   - 更新 Android 数据模型、接口封装和调用点。
   - 更新 `AGENTS.md` 中受影响的 API 规则。
   - 更新本文件的长期事实和变更记录。

2. UI / 颜色 / 主题变更
   - 更新 `doc/2026-06-08-curated-android-ui-color-style-guide.md` 或新增后续版本文档。
   - 更新代码中的主题 token 和 Compose 使用方式。
   - 更新 `AGENTS.md` 中受影响的颜色与 UI 规则。
   - 更新本文件的长期事实和变更记录。

3. 项目规则变更
   - 更新 `AGENTS.md`。
   - 更新本文件。
   - 如果规则影响开发、测试、发布或文档目录，也要更新对应 README 或 `doc/` 文档。

4. 文档保留
   - 重要分析、设计、进度、接口、主题和维护规则应写入 `doc/`。
   - 临时日志、构建产物、本机路径、密钥和缓存不要写入 `doc/`。
   - 旧文档如果被替代，应标注状态或在新文档中建立引用，不要无说明删除。

5. Git 提交
   - 完成代码修改并通过必要测试、格式化或构建验证后，主动尝试提交本次任务相关改动。
   - 保持原子化提交：一个 commit 只包含一个清晰功能、修复、文档或测试边界。
   - 工作区存在用户或其他 agent 的无关脏改动时，只 stage 本次任务相关路径，避免 `git add .` 和 `git commit -a`。
   - 如果无法安全提交，记录原因并向用户说明剩余未提交状态。

## 当前重要路径

| 路径 | 用途 |
| --- | --- |
| `AGENTS.md` | 仓库级 agent 规则入口 |
| `doc/` | agent 生成和保留项目文档的目录 |
| `doc/AGENT_MEMORY.md` | 项目记忆和长期维护索引 |
| `doc/API.md` | 后端 HTTP API 契约 |
| `doc/2026-06-13-curated-droid-playback-architecture.md` | 当前 Curated 播放链路架构说明 |
| `doc/2026-06-08-curated-android-ui-color-style-guide.md` | Android UI 颜色和样式规范 |
| `app/phone/` | Android phone app |
| `core/` | 共享核心代码、主题、资源和工具 |
| `data/` | 数据层 |
| `modes/film/` | film 模式业务功能 |
| `player/` | 播放器相关模块 |
| `settings/` | 设置模块 |
| `setup/` | 初始化、服务器、登录和用户选择模块 |

## 变更记录

### 2026-06-13

- 创建根目录 `AGENTS.md`，作为后续 agent 自动读取的项目规则入口。
- 创建 `doc/AGENT_MEMORY.md`，作为 agent 主动维护的项目记忆索引。
- 明确 `doc/` 是 agent 文档生成和长期保留目录。
- 将 `doc/API.md` 登记为 API 背景知识和接口契约事实源。
- 将 `doc/2026-06-08-curated-android-ui-color-style-guide.md` 登记为 Android UI 颜色与样式规范事实源。
- 明确后续 API、UI、主题、项目规则和长期约定变更时，agent 必须同步维护规则文档和项目记忆。
- 新增 `doc/2026-06-13-curated-droid-playback-architecture.md`，记录当前 Curated 播放主链路、共享 Cookie 会话、旧播放器残留、未完成播放闭环和续播点源码差异。
- 播放器方向策略从强制 `sensorLandscape` 调整为 `sensor` / `SCREEN_ORIENTATION_SENSOR`，避免竖屏进入播放页时被强制横屏；锁定控制层仍保持当前方向。
- 新增 Android 观看历史功能：底部导航增加 `History`，客户端读取 `GET /api/playback/progress`，逐条补全电影详情并展示观看进度；历史卡片点击直接播放；当时未包含进度写回，后续已由本日播放进度回写变更补上。
- 精简 Android 设置页：移除偏好音频语言、偏好字幕语言、界面分类和进度条预览图 / trickplay 设置入口，同时保留“显示额外信息”开关并提升到设置根页。
- 新增 Git 提交规则：代码修改完成并验证后主动尝试提交；提交保持原子化；提交信息使用 `add:`、`fix:`、`enh:`、`docs:`、`test:`、`refactor:` 等成熟前缀。
- 精简 Android 播放页：移除播放控制条里的音频轨道和字幕轨道设置按钮，并清理两个播放器 Activity 中对应的弹窗绑定。
- 优化 Android 观看历史加载：电影详情补全从顺序请求调整为有上限并发请求，减少多条历史记录时的线性等待，同时保持 `updatedAt` 倒序和单条失败跳过行为。
- 修复播放器竖屏中间控制按钮过于松散且视觉偏右的问题：中心控制行改为占满父宽并内部居中，左右按钮间距从 `32dp` 收敛为对称 `8dp`，同时保留按钮触控 padding。
- 修复 Android 首页和 My media 电影列表只显示首个 50 条结果的问题：列表状态记录总数和下一页加载状态，滚动接近底部时继续用 `limit/offset` 拉取并追加后续影片。
- 修复影片详情页顶部栏与通知栏重叠的问题：详情页 header 改用 `rememberSafePadding()` 计算顶部安全区，并把“Android 页面必须处理系统栏安全区”写入 agent 规则和 UI 样式规范。
- 修复底部导航选中态标签文字发灰导致可视度下降的问题：`NavigationSuiteScaffold` 的每个 item 显式传入导航颜色，选中图标和文字使用 `onSurface`，未选中态保留 `onSurfaceVariant`。
- 精简 Curated 首页 / My media 顶部栏：删除顶部服务器入口，首页保留设置入口，My media 保留搜索和设置入口；服务器管理继续通过设置页进入。
- 新增 Android 影片搜索入口：Curated My media 顶部栏在设置图标旁提供搜索图标，当前仅调用影片列表 API 的 `q` 参数搜索影片，并保留 `limit/offset` 自动分页。
- 新增 Android 真正首页：`HomeRoute` 不再复用电影库页，而是展示后端首页推荐快照生成的 Hero 卡片和今日推荐；`MediaRoute` 继续作为完整电影库入口。
- 新增 Curated 播放进度回写和续播：Android 播放器周期性与暂停/结束时写入 `PUT /api/playback/progress/{movieId}`，起播优先使用 `resumePositionSec`，My media 影片卡片展示历史播放进度小横条。
- 新增 Android 演员展示功能：Curated 底部导航显示 `Home`、`My media`、`Actors`、`History`，`ActorsRoute` 使用 `GET /api/library/actors?limit=50&offset=N&q=<query>&sort=movieCount` 分页列出演员并支持演员名/用户标签搜索。
- 新增 Android 演员详情页：`ActorRoute(name)` 调用 `GET /api/library/actors/profile?name=<actorName>` 展示演员资料，参演影片列表使用现有 `GET /api/library/movies?actor=<actorName>&limit=50&offset=N` 分页加载，影片卡片点击进入现有 `MovieRoute(movieId)` 详情页。
### 2026-07-05

- 品牌重塑：项目从 Findroid → Curated App，显示名 `app_name` = "Curated App"，Gradle 根项目名 `curated-app`，applicationId + Kotlin 包名 `dev.curated.app`，Compose 主题函数 `CuratedTheme`，XML 主题样式 `Theme.Curated`/`Theme.Curated.Player`。
- GitHub 仓库 URL 更新为 `https://github.com/yepHiu/Curated-App`，SSH remote 使用 `git@github.com:yepHiu/Curated-App.git`。
- 旧品牌名 `Findroid`/`findroid`/`dev.jdtech.jellyfin` 已从所有活跃源文件中清除（历史 `doc/` 分析文档中的旧路径保留作为事实记录）。
- 修复 Android 隐私防护生命周期：不再依赖被 manifest 移除的 AndroidX Startup / `ProcessLifecycleOwner` 初始化链，改用 `ActivityLifecycleCallbacks` 统计 App 前后台边界；静音在进入前台和退到后台时触发，视觉遮挡安装到所有 Activity，覆盖 MainActivity 与播放器页面。

### 2026-07-06

- 加强 Android 隐私防护：新增默认开启的本地隐私开关，设置页增加 `Privacy protection` 分类；视觉遮挡支持在 Activity pause 时立即显示以保护最近任务快照，`FLAG_SECURE` 用于阻止截图、录屏和最近任务预览；双重静音同时覆盖系统 `STREAM_MUSIC` 和播放器内部 `Player.volume = 0f`。
- 将 Android 视觉遮挡调整为 30% 黑色模糊遮挡：遮罩色为黑色 `#000000` 且 alpha 为 77，叠加在 blur 内容之上。
- 修复从视频播放页返回影片详情页后残留模糊遮罩的问题：新增 pause 遮罩状态跟踪，只有真正经历 App 退后台的遮罩会在恢复前台时保留，App 内 Activity 切换返回时自动隐藏临时遮罩。
- 修复播放页无声音的问题：播放器内部静音改为仅在播放页 pause 的隐私静音状态下生效，播放页创建和恢复前台时将 `Player.volume` 设回可听音量；系统媒体静音不再响应 App 内 Activity pause，避免详情页进入播放页时被再次归零。
- 降低播放器暂停/控制层遮罩强度：`player_background` 从 `#AA000000` 调整为 `#66000000`，让暂停时显示控制组件仍保持可读，同时减少对视频画面的遮挡。


### 2026-07-06

- About page open-source component credits are intentionally collapsed by default behind an "Open source components" toggle. Keep the AboutLibraries data complete, and only change the presentation unless the user explicitly asks to hide or remove credits.


### 2026-07-06

- Android main navigation now uses a floating bottom pill bar for Home, My media, and Settings. Secondary destinations such as Actors and History remain in the navigation drawer, and top-level scrollable pages reserve bottom content padding so their last content can scroll above the floating bar instead of being obscured.


### 2026-07-06

- Android app windows should opt into high-refresh displays where available. `MainActivity`, `BasePlayerActivity`, and `CuratedPlayerActivity` call `applyCuratedHighRefreshRatePreference()`, which prefers the highest valid refresh-rate display mode at the current physical resolution and falls back to the highest valid mode when the current resolution is unavailable.


### 2026-07-06

- Curated floating bottom navigation should use a compact Curated-themed rounded pill: a dark `surfaceContainerHigh` chrome in dark theme, equal-width vertical icon/label tabs, a subtle primary selected pill, and no low-opacity full-width strip over media content. Keep Home, My media, and Settings in the bottom bar; secondary destinations remain in the drawer.

- Refined Android floating bottom navigation to be shorter and calmer: the bar height is 58dp, item height is 46dp, dark theme chrome uses Curated `surfaceContainerHigh` instead of inverse/white glass, and selected state uses a lighter primary tint so the control sits more naturally over content.

- Floating bottom navigation now sits closer to the bottom edge with a 10dp bottom margin, uses a background-colored vertical scrim behind the bottom area so content fades naturally under the bar, and moves one shared selected pill with spring animation between equal-width items instead of snapping separate item backgrounds on/off.

- GitHub remote is now `git@github.com:yepHiu/Curated-App.git`, and the root `README.md` was rewritten as a Curated App Android client README that documents the app purpose, setup, repository layout, Curated API contract, privacy protections, visual style, and GPLv3 license.

### 2026-07-07

- 修复 Android 播放器横屏右侧音量手势失效：手势系统边缘排除区改用当前播放器视图宽高计算，并补充 `PlayerGestureExclusionPolicyTest` 覆盖横屏右半屏不应被排除、系统边缘仍排除。
- 增强 Android 播放器横向拖动 seek：通过可测试的手势策略区分横向占优进度拖动与竖向亮度/音量拖动，横向拖动使用进度 HUD 预览目标时间并在松手时提交 seek。
