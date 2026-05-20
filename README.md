# PocketLedger

PocketLedger 是一个基于 Kotlin 和 Jetpack Compose 的 Android 本地记账应用，安装到手机后的显示名称为「口袋账本」。它面向个人日常账单、AA 分账和旅行支出管理。当前版本为单机应用，数据保存在应用私有目录中，不依赖后端服务。

## 功能

- 记录、编辑和删除日常支出
- 按分类查看账单，并根据使用频率优先展示常用分类
- 查看周、月、年的支出统计和分类排行
- 管理 AA 分账活动，记录垫付人、参与人、金额和结算状态
- 管理旅行计划、同行成员、旅行支出和出行清单
- 本地 JSON 文件持久化保存数据

## 技术栈

- Kotlin
- Android Gradle Plugin
- Jetpack Compose
- Material 3
- Kotlinx Serialization
- Coroutines / StateFlow

## 环境要求

- JDK 17
- Android Studio
- Android SDK Platform / Build-Tools / Platform-Tools
- Gradle Wrapper（项目已包含 `gradlew` / `gradlew.bat`）

更完整的环境准备说明见 [docs/android-bill-app-setup.md](docs/android-bill-app-setup.md)。

## 运行项目

在 Android Studio 中打开项目根目录，等待 Gradle 同步完成后，选择模拟器或真机运行 `app` 模块。

也可以在命令行中执行：

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

生成调试安装包：

```powershell
.\gradlew.bat :app:assembleDebug
```

调试 APK 输出目录通常为：

```text
app/build/outputs/apk/debug/
```

## 项目结构

```text
.
├── app/
│   └── src/main/java/com/billapp/
│       ├── data/      # 数据模型、本地仓库和统计逻辑
│       ├── ui/        # Compose 页面、主题和 ViewModel
│       └── MainActivity.kt
├── docs/              # 开发环境和项目说明文档
├── gradle/            # Gradle Wrapper 配置
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 数据存储

应用数据写入 Android 应用私有目录下的 JSON 文件，包括：

- `bills.json`
- `aa_activities.json`
- `travel.json`

这些文件属于手机或模拟器本地运行数据，不会出现在源码仓库中。

## 上传 GitHub 前检查

- 不提交 `local.properties`，其中通常包含本机 Android SDK 路径。
- 不提交 `.gradle/`、`build/`、`app/build/` 等构建缓存和产物。
- 不提交 `.idea/`、`*.iml` 等本机 IDE 配置。
- 不提交 `*.apk`、`*.aab` 等安装包产物。
- 不提交 `*.keystore`、`*.jks` 等签名文件。
- 如后续新增接口、密钥或第三方服务配置，请放入本机配置或安全的发布流程中，不要写死在源码里。

当前项目暂未包含敏感接口地址或密钥。

## 截图

README 不强制需要图片。若要让 GitHub 页面更直观，建议补充 2 到 4 张应用截图，例如：

- 账单首页
- 统计页面
- AA 分账页面
- 旅行助手页面

可以将图片放到 `docs/images/` 目录，再在 README 中引用。

## License

当前仓库尚未声明开源协议。上传公开 GitHub 仓库前，建议根据你的分发意图补充 `LICENSE` 文件。
