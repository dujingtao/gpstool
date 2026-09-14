package com.allai.gpstool.model

data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val accuracy: Float = 0.0f,
    val speed: Float = 0.0f,
    val bearing: Float = 0.0f,
    val time: Long = 0L,
    val hasFix: Boolean = false,
    val satellitesInView: Int = 0,
    val satellitesUsedInFix: Int = 0,
    val provider: String = "GPS"
)
