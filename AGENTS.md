# Curated Droid Agent Rules

本文件是仓库级 agent 规则入口。后续 agent 在开始任何代码、文档、设计、API 或 UI 工作前，必须先读取本文件，并按这里列出的项目背景资料执行。

## 必读项目背景

1. `doc/API.md`
   - 这是当前 Curated 后端 HTTP API 的事实源。
   - Android 客户端、局域网客户端和其他衍生客户端都必须以它为接口契约。
   - 不要凭记忆新增、修改或推断 API 行为；涉及接口时先核对此文件，再核对实现。

2. `doc/2026-06-08-curated-android-ui-color-style-guide.md`
   - 这是 Android UI 颜色和样式规范的事实源。
   - 默认视觉方向是深色、内容优先、低干扰媒体库界面、粉色品牌主色。
   - Kotlin / Jetpack Compose 代码里不要散落硬编码 hex；优先使用语义 token、`MaterialTheme.colorScheme` 或项目主题封装。

3. `doc/AGENT_MEMORY.md`
   - 这是 agent 维护的项目记忆索引。
   - 任何改变长期项目事实、约定、架构、API、主题规范、目录用途或维护流程的工作，都要同步更新它。

## `doc/` 目录规则

- `doc/` 是 agent 的文档生成和长期保留目录。
- 需要保留的项目分析、设计决策、实现进度、接口说明、风格规范和 agent 记忆都应写入 `doc/`。
- 不要把一次性草稿、构建产物、缓存、临时日志或本机私有信息放入 `doc/`。
- 新增文档优先使用清晰、可排序的文件名：
  - 长期事实源：`doc/API.md`、`doc/AGENT_MEMORY.md`。
  - 日期型分析或阶段记录：`doc/YYYY-MM-DD-<topic>.md`。
- 如果文档内容已经被后续事实替代，应在旧文档顶部或相关段落标注状态，而不是直接删除历史背景。

## API 规则

所有 HTTP API 都挂在 `/api` 前缀下。常见 base URL 以 `doc/API.md` 为准，包括：

- 开发后端：`http://127.0.0.1:8080/api`
- 前端代理：同源 `/api`
- 打包后本机后端：通常 `http://127.0.0.1:8081/api`
- Android / 局域网客户端：`http://<server-ip>:<port>/api`

客户端实现时必须遵守这些稳定约定：

- Base URL 应可配置，并在保存前去掉末尾 `/`。
- PIN 解锁依赖 `curated_auth` cookie；非浏览器客户端必须用 CookieJar 保存并自动回传 cookie。
- DTO 里的媒体 URL 可能是相对路径，客户端必须用后端 origin 解析成绝对 URL。
- 路径参数必须 URL encode，尤其是演员名、影片 ID、精选帧 ID、HLS 文件名。
- 视频、图片、导出下载和大文件上传不要按 JSON 解析。
- 成功响应直接返回 DTO 本体，不包 `{ "ok": true, "data": ... }`。
- 空成功响应使用 `204 No Content`。
- 失败响应使用 `AppError` JSON，包含稳定的 `code`、`message`、`retryable` 和可选 `details`。

涉及 API 变更时，必须同步检查和更新：

1. 后端路由和 handler。
2. 后端 DTO、错误码和注释。
3. Android 客户端数据模型、接口封装和调用点。
4. `doc/API.md` 的端点说明、DTO 速查和全路由清单。
5. `doc/AGENT_MEMORY.md` 的项目事实或维护记录。
6. 本文件中受到影响的规则。

## 颜色与 UI 规则

Android UI 必须遵守 `doc/2026-06-08-curated-android-ui-color-style-guide.md`。关键 token：

- 品牌主色：`#FE628E`
- 主色前景：`#1D0910`
- 深色主背景：`#0D0F1A`
- 深色主文字：`#F8F7FB`
- 深色卡片 / surface：`#141826`
- 深色 elevated surface：`#171B2B`
- 深色 muted surface：`#121827`
- 浅色主背景：`#F4F6FC`
- 浅色主文字：`#0F1219`
- 浅色卡片 / surface：`#FFFFFF`
- 浅色 muted surface：`#EBEEF5`

主题行为：

- 首版默认主题是 `dark`。
- 必须支持 `dark`、`light`、`system` 三种用户偏好。
- 本地主题偏好建议使用 DataStore key `curated_ui_theme`，可选值为 `dark`、`light`、`system`。
- 主题偏好是设备本地设置，不要和后端账号、PIN 解锁状态绑定。
- 深色和浅色都要显式实现，不允许只实现深色后依赖系统反色推导浅色。
- 状态色不能只靠颜色表达含义，必须配合文字、图标或状态标签。
- 普通业务 UI 不应引入新的主色系；媒体封面和视频画面可使用局部黑色遮罩。
- 所有 Android Compose 页面顶部栏、返回按钮、标题、筛选栏和首屏主要内容都必须主动处理 `WindowInsets.safeDrawing` 或项目 `rememberSafePadding()`，避免与通知栏、挖孔屏或显示裁切区域重叠；不要只写固定 `top = 8.dp` / `16.dp`。

## Agent 记忆维护规则

Agent 不只是执行单次任务，还要主动维护项目记忆和规则文档。

必须更新 `doc/AGENT_MEMORY.md` 的情况：

- 新增、删除或修改重要 API。
- 改变鉴权、cookie、网络、播放、下载、导入、扫描、任务轮询等跨模块协议。
- 改变 Android 主题、颜色 token、组件风格、导航结构或核心交互约定。
- 新增项目目录规则、构建规则、测试规则、发布规则或文档规则。
- 发现旧文档与当前实现不一致，并完成核对。
- 用户明确给出新的长期偏好或项目约定。

更新方式：

- 把稳定事实写入“长期项目事实”。
- 把需要后续 agent 注意的维护动作写入“维护规则”。
- 把具体变更写入“变更记录”，包括日期、改动、原因和相关文件。
- 如果只是临时排查过程，不要写入长期记忆；只有会影响后续工作的事实才保留。

## Git 与本地文件规则

- 不要提交 `.gradle-home/`、`.gradle/`、`build/`、`local.properties`、IDE workspace、本机缓存或密钥。
- `.idea/` 只保留项目共享配置；本机状态文件继续忽略。
- 在修改已有用户文件前先检查 `git status`，不要覆盖未理解的用户改动。
- 文档、规则或项目记忆变更完成后，至少运行 `git status --short --branch` 验证工作区状态。
- 每次完成代码修改并通过必要验证后，agent 都应主动尝试创建 git commit；如果因为测试失败、冲突、提交身份缺失或存在无法安全拆分的改动而不能提交，必须在最终回复中说明阻塞原因和当前工作区状态。
- Git 提交必须遵循原子化提交规则：一个 commit 只表达一个清晰行为边界，不要把无关修复、功能、格式化、文档或用户既有脏改动混在一起。
- 工作区已有无关改动时，只能用显式路径或交互式分块 stage 本次任务相关文件；不要用 `git add .`、`git commit -a` 或其他会顺手带入无关改动的命令。
- 提交信息使用成熟、简短、可扫描的前缀和祈使句，例如 `add: ...`、`fix: ...`、`enh: ...`、`docs: ...`、`test: ...`、`refactor: ...`。
