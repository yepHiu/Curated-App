# Agent Memory

本文件是 Curated Droid 项目的 agent 记忆索引。它记录后续 agent 应主动继承和维护的长期事实、规则和变更线索。

更新时间：2026-06-13
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
- 当前 Curated 播放闭环尚未实现 progress 回写、HLS session delete、played movies、watch time 统计和 direct 到 HLS 的显式 fallback。
- Android 观看历史入口位于底部导航 `History` tab，首版使用只读 `GET /api/playback/progress` 作为数据源，按 `updatedAt` 倒序展示，并调用 `GET /api/library/movies/{movieId}` 补全标题、封面和元数据；电影详情补全使用有上限并发请求，单条详情失败时跳过该行，进度列表失败时显示页面错误和重试；历史卡片点击后直接启动 `CuratedPlayerActivity` 播放，不进入电影详情页。
- Android 观看历史首版不负责播放进度回写；progress 写入仍属于后续播放闭环任务。
- Android 首页和 My media 共用 `CuratedMoviesScreen` / `CuratedMoviesViewModel`，电影列表通过 `GET /api/library/movies?limit=50&offset=N` 按滚动位置分页加载；不要通过单次提高 limit 来假装完整列表。
- 当前源码中的 `curatedStartPositionMs()` 只使用 `startPositionSec`，缺失时从 0 起播；`resumePositionSec` 当前被忽略。
- Curated 底部导航当前展示 `Home`、`My media`、`History`；`DownloadsRoute` 仍保留但不在底部导航展示。
- Curated 设置页不展示偏好音频语言、偏好字幕语言、界面分类、进度条预览图 / trickplay 相关设置；“显示额外信息”开关保留并直接显示在设置根页。
- Android UI 视觉方向是默认深色、内容优先、低干扰媒体库界面、粉色品牌主色。
- Android Compose 页面顶部栏、返回按钮、标题、筛选栏和首屏主要内容必须主动处理 `WindowInsets.safeDrawing` 或项目 `rememberSafePadding()`，避免与通知栏、挖孔屏或显示裁切区域重叠；不要只写固定顶部间距。
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
- 新增 Android 观看历史功能：底部导航增加 `History`，客户端读取 `GET /api/playback/progress`，逐条补全电影详情并展示观看进度；历史卡片点击直接播放；本次不实现 progress 写回。
- 精简 Android 设置页：移除偏好音频语言、偏好字幕语言、界面分类和进度条预览图 / trickplay 设置入口，同时保留“显示额外信息”开关并提升到设置根页。
- 新增 Git 提交规则：代码修改完成并验证后主动尝试提交；提交保持原子化；提交信息使用 `add:`、`fix:`、`enh:`、`docs:`、`test:`、`refactor:` 等成熟前缀。
- 精简 Android 播放页：移除播放控制条里的音频轨道和字幕轨道设置按钮，并清理两个播放器 Activity 中对应的弹窗绑定。
- 优化 Android 观看历史加载：电影详情补全从顺序请求调整为有上限并发请求，减少多条历史记录时的线性等待，同时保持 `updatedAt` 倒序和单条失败跳过行为。
- 修复播放器竖屏中间控制按钮过于松散且视觉偏右的问题：中心控制行改为占满父宽并内部居中，左右按钮间距从 `32dp` 收敛为对称 `8dp`，同时保留按钮触控 padding。
- 修复 Android 首页和 My media 电影列表只显示首个 50 条结果的问题：列表状态记录总数和下一页加载状态，滚动接近底部时继续用 `limit/offset` 拉取并追加后续影片。
- 修复影片详情页顶部栏与通知栏重叠的问题：详情页 header 改用 `rememberSafePadding()` 计算顶部安全区，并把“Android 页面必须处理系统栏安全区”写入 agent 规则和 UI 样式规范。
