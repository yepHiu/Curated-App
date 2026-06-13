# Movie Detail Preview Images Implementation Plan

日期：2026-06-13
状态：待用户确认后执行
范围：Android 影片详情页预览图展示，不改后端 API

## 1. API 依据

事实源：`doc/API.md`

相关约定：

- `GET /api/library/movies/{movieId}` 返回 `MovieDetailDTO`。
- `MovieDetailDTO.previewImages` 是预览图 URL 数组。
- `MovieDetailDTO.previewVideoUrl` 是预览视频 URL，本计划不实现预览视频播放。
- `coverUrl`、`thumbUrl`、`previewImages` 可能是相对 API URL。
- Android 客户端必须把相对 URL 基于后端 origin 解析成绝对 URL。
- `GET /api/library/movies/{movieId}/asset/preview/{index}` 获取第 `index` 张预览图，`index` 从 1 开始。
- 预览图端点返回图片 bytes，不是 JSON；本地缓存存在时用本地缓存，本地不存在时后端可能代理远端源。

当前 Android 数据层已经具备基础能力：

- `MovieDetailDto.previewImages: List<String> = emptyList()`
- `MovieDetail.previewImages: List<String>`
- `MovieDetailDto.toDomain()` 已对 `previewImages` 调用 `CuratedUrlResolver.absoluteUrl(baseUrl, it)`

因此第一版不需要新增 API client 方法，不需要直接拼 `/asset/preview/{index}`。详情页直接使用 `MovieDetail.previewImages` 渲染即可。

## 2. 需求理解

影片详情页应支持展示影片预览图。用户进入详情页后，在封面、标题、播放按钮和简介附近能看到预览图缩略图列表；点击任意预览图后，可以查看大图并左右切换。

第一版目标：

- 有预览图时展示“Preview images”区域。
- 没有预览图时不展示该区域，不增加空态噪音。
- 缩略图横向滚动，适合手机竖屏详情页。
- 点击缩略图打开全屏或近全屏查看器。
- 查看器支持上一张、下一张、关闭。
- 所有图片使用已有 URL，不在 Android 端猜测预览图数量。
- 不实现 `previewVideoUrl` 播放，不引入新后端接口。

## 3. 推荐交互设计

推荐方案：详情页内横向缩略图条 + 图片查看器。

详情页布局顺序：

1. 顶部安全区 header。
2. Hero 图。
3. 标题、元信息、播放按钮。
4. `Preview images` 横向缩略图区域。
5. 简介。
6. 详情字段。

预览图区域：

- 使用 `LazyRow`。
- 缩略图固定宽度约 `148.dp`，比例 `16:9`。
- 图片圆角不超过现有媒体卡片风格，建议 `8.dp`。
- 图片缺省加载由 Coil 处理；如果单张加载失败，可显示 `ic_film` 占位。
- 区域只在 `movie.previewImages.isNotEmpty()` 时出现。

图片查看器：

- 使用 Compose `Dialog` 或覆盖层。
- 背景使用 `MaterialTheme.colorScheme.background` 或半透明黑色遮罩。
- 大图 `ContentScale.Fit`，避免裁切预览图关键信息。
- 顶部保留关闭按钮，并主动处理 safe drawing top。
- 底部或左右提供上一张 / 下一张按钮。
- 第一张时禁用上一张，最后一张时禁用下一张。
- 标题显示 `1 / N` 这类位置文本，帮助用户知道当前图片序号。

不推荐方案：

- 不在详情页一次性竖向铺满所有预览图；会让详情页过长，并延迟关键信息。
- 不在第一版做视频预览；`previewVideoUrl` 需要播放器或视频预览控件，范围明显大于预览图。
- 不在 Android 端循环请求 `/asset/preview/1..N` 探测数量；API 已通过 DTO 给出 URL 数组，客户端应以 DTO 为准。

## 4. 文件改动计划

### 4.1 详情页 UI

修改：

- `app/phone/src/main/java/dev/jdtech/jellyfin/presentation/curated/CuratedMovieDetailScreen.kt`

新增内部组件：

- `CuratedMoviePreviewImagesSection`
  - 输入：`previewImages: List<String>`、`onPreviewClick: (Int) -> Unit`
  - 行为：为空时不渲染；非空时展示标题和横向缩略图。

- `CuratedMoviePreviewThumbnail`
  - 输入：`imageUrl: String`、`index: Int`、`onClick: () -> Unit`
  - 行为：展示单张 `16:9` 缩略图。

- `CuratedPreviewImageDialog`
  - 输入：`previewImages: List<String>`、`initialIndex/currentIndex`、`onDismiss`
  - 行为：大图查看、上一张、下一张、关闭。

新增 helper：

- `internal fun curatedMoviePreviewImages(movie: MovieDetail): List<String>`
  - 返回 `movie.previewImages.filter { it.isNotBlank() }.distinct()`
  - 用于测试过滤空 URL 和重复 URL。

- `internal fun curatedPreviewCanGoPrevious(index: Int): Boolean`
  - 返回 `index > 0`

- `internal fun curatedPreviewCanGoNext(index: Int, total: Int): Boolean`
  - 返回 `index < total - 1`

- `internal fun curatedPreviewPositionText(index: Int, total: Int): String`
  - 返回 `"${index + 1} / $total"`；当 `total <= 0` 时返回空字符串。

### 4.2 文案

第一版可先使用英文硬编码，保持与当前 `CuratedMovieDetailScreen.kt` 中 `Summary`、`Studio`、`Actors`、`Play` 一致。

若执行时决定本地化，新增资源：

- `core/src/main/res/values/strings.xml`
  - `movie_preview_images = Preview images`
  - `movie_preview_close = Close preview`
  - `movie_preview_previous = Previous preview image`
  - `movie_preview_next = Next preview image`

- `core/src/main/res/values-zh-rCN/strings.xml`
  - `movie_preview_images = 预览图`
  - `movie_preview_close = 关闭预览`
  - `movie_preview_previous = 上一张预览图`
  - `movie_preview_next = 下一张预览图`

推荐执行时使用资源字符串，避免继续扩大详情页硬编码文案。

### 4.3 数据层

不需要新增数据层接口。

需要保留现有行为：

- `MovieDetailDto.previewImages` 默认空数组。
- `MovieDetailDto.toDomain()` 继续把 `previewImages` 转绝对 URL。
- 不直接调用 `/api/library/movies/{movieId}/asset/preview/{index}`。

## 5. 测试计划

### 5.1 纯 helper 测试

修改：

- `app/phone/src/test/java/dev/jdtech/jellyfin/presentation/curated/CuratedImageSelectionTest.kt`

新增测试：

- `movieDetailPreviewImagesDropsBlankUrlsAndDeduplicates()`
  - 输入：`["", " ", "url-a", "url-a", "url-b"]`
  - 期望：`["url-a", "url-b"]`

- `previewNavigationBoundsReflectCurrentIndex()`
  - `index = 0, total = 3`：不能上一张，可以下一张。
  - `index = 1, total = 3`：可以上一张，可以下一张。
  - `index = 2, total = 3`：可以上一张，不能下一张。

- `previewPositionTextUsesOneBasedIndex()`
  - `index = 0, total = 3` 返回 `1 / 3`
  - `index = 2, total = 3` 返回 `3 / 3`
  - `index = 0, total = 0` 返回空字符串。

### 5.2 数据映射回归测试

现有数据层测试已覆盖 `MovieDetailDto.previewImages` 绝对 URL 映射时，可不新增。

如果当前测试覆盖不足，扩展：

- `data/src/test/java/dev/jdtech/jellyfin/curated/api/CuratedDtoMapperTest.kt`
  - 确认 `previewImages` 中相对 URL 会转成绝对 URL。
  - 确认空数组保持空数组。

### 5.3 编译与格式验证

执行实现后运行：

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"

.\gradlew.bat :app:phone:testLibreDebugUnitTest --tests "dev.jdtech.jellyfin.presentation.curated.CuratedImageSelectionTest" --rerun-tasks --console=plain
.\gradlew.bat :data:testDebugUnitTest --rerun-tasks --console=plain
.\gradlew.bat :app:phone:testLibreDebugUnitTest --rerun-tasks --console=plain
.\gradlew.bat ktfmtCheck --rerun-tasks --console=plain
.\gradlew.bat :app:phone:assembleLibreDebug --console=plain
git diff --check
```

## 6. 执行步骤

1. 写 `CuratedImageSelectionTest` 红灯测试，覆盖预览图过滤、位置文案和上一张/下一张边界。
2. 运行 targeted test，确认因为 helper 不存在而失败。
3. 在 `CuratedMovieDetailScreen.kt` 中新增 helper，跑 targeted test 转绿。
4. 在详情页内容中增加 `CuratedMoviePreviewImagesSection`，插入到标题/播放按钮后、简介前。
5. 增加 `CuratedPreviewImageDialog`，用 `rememberSaveable` 记录当前打开的 preview index。
6. 接入缩略图点击、关闭、上一张、下一张。
7. 如采用资源字符串，补 `values` 和 `values-zh-rCN` 文案。
8. 运行 targeted tests、全量 unit tests、格式、assemble、`git diff --check`。
9. 更新 `doc/AGENT_MEMORY.md`：记录影片详情页支持 `MovieDetail.previewImages` 横向预览图和图片查看器。
10. 如果工作区可以安全拆分，创建原子提交：`add: support movie detail preview images`。

## 7. 验收标准

- `MovieDetail.previewImages` 非空时，影片详情页显示预览图区域。
- `MovieDetail.previewImages` 为空时，影片详情页不显示预览图区域。
- 点击任意缩略图后能查看大图。
- 大图查看器能关闭。
- 多张预览图时能上一张 / 下一张切换。
- 第一张不能继续上一张，最后一张不能继续下一张。
- 预览图 URL 使用数据层已解析好的绝对 URL，不新增后端请求探测逻辑。
- 页面顶部仍处理 safe drawing，不与通知栏冲突。
- 深色和浅色主题下文字、按钮、图标可读。
- 相关测试和构建命令通过。
