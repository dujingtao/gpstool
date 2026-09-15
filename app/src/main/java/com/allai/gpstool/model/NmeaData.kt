package com.allai.gpstool.model

data class NmeaRecord(
    val timestamp: Long,
    val rawMessage: String,
    val type: String
)

data class DopData(
    val pdop: Float = 0f,
    val hdop: Float = 0f,
    val vdop: Float = 0f,
    val fixMode: String = "未锁星"
) {
    val rating: DopRating
        get() = when {
            pdop in 0.1f..1.5f -> DopRating.EXCELLENT
            pdop in 1.5f..3.0f -> DopRating.GOOD
            pdop in 3.0f..6.0f -> DopRating.MODERATE
            pdop > 6.0f -> DopRating.POOR
            else -> DopRating.UNKNOWN
        }
}

enum class DopRating(val title: String, val colorHex: Long) {
    EXCELLENT("极佳 (测绘级)", 0xFF00FF7F),
    GOOD("良好 (导航级)", 0xFF00F0FF),
    MODERATE("一般 (有遮挡)", 0xFFFFB300),
    POOR("较差 (几何发散)", 0xFFFF3344),
    UNKNOWN("等待解算", 0xFF888888)
}
