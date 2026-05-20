# Android 单机账单 App 开发环境安装清单

## 目标

在 Windows 环境下完成 Android 单机账单 App 的开发准备，并具备生成可安装 `APK` 的能力。

## 必备安装项

- `Android Studio`
- `JDK 17`
- `Android SDK Platform`
- `Android SDK Build-Tools`
- `Android SDK Platform-Tools`
- `Android SDK Command-line Tools`
- `Android Emulator`
- `USB 数据线`
- `Android 真机`
- `USB 调试驱动`
- `keystore` 签名文件

## 推荐安装顺序

1. 安装 `Android Studio`
2. 首次启动后让它自动下载基础组件
3. 在 `SDK Manager` 中补齐：
   - `Android SDK Platform`
   - `Android SDK Build-Tools`
   - `Android SDK Platform-Tools`
   - `Android SDK Command-line Tools`
4. 在 `Device Manager` 创建模拟器
5. 准备真机并开启 `开发者选项` 和 `USB 调试`
6. 安装 `Git`
7. 新建或导入 Android 项目，建议使用 `Kotlin + Empty Activity`
8. 先跑通调试包 `debug APK`
9. 配置发布签名 `keystore`
10. 生成 `release APK`

## 生成 APK 的关键点

- `debug APK`：用于开发和本机调试，通常直接运行即可生成
- `release APK`：用于手机安装和分发，必须配置签名
- `keystore`：务必长期保存，后续版本升级需要同一套签名

## 真机调试检查

- 手机开启 `USB 调试`
- 数据线连接正常
- Windows 正确识别设备
- `adb devices` 能看到设备

## 最小可用环境

如果只想尽快开始开发，最小配置是：

- `Android Studio`
- `Android SDK`
- `Build-Tools`
- `Platform-Tools`
- 模拟器或真机
- `keystore`

## 备注

- `Gradle` 一般由 Android Studio 自动管理，不需要单独手装
- 发布前建议先在真机上完成一次完整安装和启动验证
