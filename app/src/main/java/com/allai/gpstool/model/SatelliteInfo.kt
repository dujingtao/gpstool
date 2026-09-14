package com.allai.gpstool.model

import android.location.GnssStatus

enum class ConstellationType(val displayName: String, val shortCode: String) {
    GPS("GPS (美国)", "GP"),
    GLONASS("GLONASS (俄罗斯)", "GL"),
    BEIDOU("北斗 (中国)", "BD"),
    GALILEO("Galileo (欧盟)", "GA"),
    QZSS("QZSS (日本)", "QZ"),
    SBAS("SBAS (星基增强)", "SB"),
    UNKNOWN("未知系统", "UN");

    companion object {
        fun fromGnssConstellation(type: Int): ConstellationType {
            return when (type) {
                GnssStatus.CONSTELLATION_GPS -> GPS
                GnssStatus.CONSTELLATION_GLONASS -> GLONASS
                GnssStatus.CONSTELLATION_BEIDOU -> BEIDOU
                GnssStatus.CONSTELLATION_GALILEO -> GALILEO
                GnssStatus.CONSTELLATION_QZSS -> QZSS
                GnssStatus.CONSTELLATION_SBAS -> SBAS
                else -> UNKNOWN
            }
        }
    }
}

data class SatelliteInfo(
    val svid: Int,
    val constellation: ConstellationType,
    val elevationDegrees: Float,
    val azimuthDegrees: Float,
    val cn0DbHz: Float,
    val usedInFix: Boolean,
    val hasCarrierFrequency: Boolean,
    val carrierFrequencyHz: Float
)
