# GPSTool 核心开发与交付规范 (Project Engineering Guidelines)

本规范为 GPSTool 项目的最高优先级开发准则，在任何代码迭代、功能修改与版本升级时必须严格执行。

---

## 1. 版本号同步更新规范 (Version Management)
- **版本号统一管理**：应用的真实版本号定义在 `app/build.gradle.kts` 的 `defaultConfig` 中：
  - `versionCode`：整数递增（Google Play 发版与本地升级凭据，每次功能迭代/发版必须 +1）。
  - `versionName`：语义化版本号（如 `1.1.0` -> `1.1.1` 或 `1.2.0`）。
- **动态引用原则**：UI 界面（如设置/关于页面）一律通过 `BuildConfig.VERSION_NAME` 和 `BuildConfig.VERSION_CODE` 动态呈现，严禁硬编码版本文本。
- **发布与升级触发**：只要包含新功能发布、UI 调整或架构升级，必须同步修改 `app/build.gradle.kts` 中的版本号。

---

## 2. 严苛自检编译准则 (Compilation Self-Check)
- **修改完代码后必须自检**：严禁在未经过真实编译器验证的情况下直接回复用户或提交代码。
- **构建环境标准**：必须使用配套的官方 OpenJDK 21 环境（配置位于 `gradle.properties` 的 `org.gradle.java.home=C:\\Users\\DuJingtao_yoga\\.jdks\\jbr-21.0.11`）。
- **执行命令**：每次修改完成后，自主在终端执行：
  ```powershell
  .\gradlew assembleDebug
  ```
  只有在输出 `BUILD SUCCESSFUL` 且无编译错误的情况下，才算修改完成。

---

## 3. 真机部署与验证准则 (Device Verification)
- **测试机就绪检测**：当检测到用户的三星真机（Samsung Galaxy Z Fold 7，设备号 `RFCY712RELJ`）通过 ADB 连接时，编译通过后应主动通过 `adb install -r app/build/outputs/apk/debug/app-debug.apk` 部署更新。
- **调试包识别**：Debug 构建的 applicationId 为 `com.allai.gpstool.debug`，启动 Intent 命令为：
  ```powershell
  adb shell am start -n com.allai.gpstool.debug/com.allai.gpstool.MainActivity
  ```

---

## 4. 罗盘与传感器交互基准 (Sensor Design Constraints)
- **罗盘指针与盘面**：指针保持绝对垂直向上（12 点钟方向固定），表盘底图根据航向角逆向旋转。
- **传感器防抖**：必须保留低通滤波（Low-Pass Filter）与死区门限（Deadband）算法，避免数值微跳引起 UI 剧烈抖动。