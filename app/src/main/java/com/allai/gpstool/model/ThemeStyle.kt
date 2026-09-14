package com.allai.gpstool.model

enum class ThemeStyle(val title: String, val description: String) {
    CYBERPUNK("暗黑极客科技 (Cyber HUD)", "纯黑底色结合霓虹青蓝，OLED省电，适合夜间与车载"),
    MATERIAL_YOU("现代 Material You", "符合 Google 官方质感设计，现代清爽优雅"),
    AVIATION_AMBER("复古航空琥珀 (Aviation)", "仿航天与舰船雷达仪表的温暖琥珀金，经典且护眼"),
    OUTDOOR_CONTRAST("户外高对比 (High Contrast)", "专为烈日强光下的户外徒步与测绘设计，超高辨识度")
}

enum class SpeedUnit(val displayName: String, val factor: Float) {
    KMH("km/h (公里/时)", 3.6f),
    MPH("mph (英里/时)", 2.23694f),
    KNOTS("knots (海里/节)", 1.94384f),
    MS("m/s (米/秒)", 1.0f)
}

enum class CoordinateFormat(val displayName: String) {
    DECIMAL("十进制度数 (DD: 39.9042°)"),
    DMS("度分秒 (DMS: 39°54'15\"N)")
}
