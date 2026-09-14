# GPSTool - Modern Android GNSS & Sensor Utility

<p align="center">
  <b>🛰️ 专业级卫星信号雷达 · 精密定位仪表盘 · 多传感器融合罗盘 · 4套可定制科技HUD</b>
</p>

---

## 🌟 核心特性 (Features)

- **🛰️ 全球卫星天球雷达 (Sky View Radar)**:
  - 动态仰角/方位角同心圆天球视图。
  - 自动识别并彩色标识四大全球卫星导航系统：
    - 🇺🇸 **GPS (Navstar)**
    - 🇨🇳 **BeiDou (北斗)**
    - 🇪🇺 **Galileo (伽利略)**
    - 🇷🇺 **GLONASS (格洛纳斯)**
    - 🇯🇵 **QZSS (准天顶)**
  - 实时信噪比（C/N0 dB-Hz）柱状图展示，标定解算锁定（Fixed）卫星状态。

- **🧭 精密定位仪表盘 (Precision Dashboard)**:
  - 高精度经纬度解析（支持十进制度数 DD 与 度分秒 DMS 一键切换）。
  - 支持一键点击复制当前坐标到剪贴板。
  - 实时地面航速（km/h、mph、海里/节 knots 换算）。
  - 水平定位精度误差范围（±m）、WGS84 椭球海拔高度与航向角。

- **📐 多传感器融合罗盘与水平仪 (Sensor Fusion)**:
  - 结合手机内置磁力计与加速度计解算 3D 旋转电子罗盘，实时指示真北与地磁强度（μT）。
  - 极具实用性的双轴倾角气泡水平仪（装修、户外安装必备）。
  - 气压计硬件（Barometer）实时气压读数与高程推算。

- **🎨 4 套用户可定制风格引擎 (Customizable Themes)**:
  1. **暗黑极客科技 (Cyber HUD)**: 纯黑底色 + 激光青蓝 + 霓虹绿，夜视与 OLED 极致省电。
  2. **现代 Material You**: 遵循 Google 官方设计语言，清爽现代。
  3. **复古航空琥珀 (Aviation Amber)**: 仿经典航天与舰船雷达仪表的琥珀金荧光质感。
  4. **户外强光高对比 (High Contrast)**: 烈日强光直射下的户外徒步高辨识度模式。

---

## 🛠️ 技术架构 (Tech Stack)

- **Language**: 100% Kotlin
- **UI Toolkit**: Jetpack Compose + Material 3
- **Architecture**: MVVM + StateFlow + Clean Architecture
- **APIs**:
  - `android.location.GnssStatus`
  - `android.location.LocationManager`
  - `android.hardware.SensorManager`
- **Target SDK**: Android 15 (API 35) / Min SDK: Android 8.0 (API 26)

---

## 📦 开发者信息

- **Developer**: `all.ai`
- **License**: MIT
