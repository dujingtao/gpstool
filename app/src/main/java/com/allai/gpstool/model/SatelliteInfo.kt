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
) {
    val frequencyBand: String
        get() {
            if (!hasCarrierFrequency || carrierFrequencyHz <= 0f) return ""
            val freqMhz = carrierFrequencyHz / 1_000_000f
            return when (constellation) {
                ConstellationType.GPS, ConstellationType.QZSS -> when {
                    freqMhz in 1570f..1580f -> "L1"
                    freqMhz in 1170f..1182f -> "L5"
                    freqMhz in 1222f..1232f -> "L2"
                    else -> String.format("%.0fM", freqMhz)
                }
                ConstellationType.BEIDOU -> when {
                    freqMhz in 1555f..1567f -> "B1I"
                    freqMhz in 1570f..1580f -> "B1C"
                    freqMhz in 1170f..1182f -> "B2a"
                    freqMhz in 1202f..1212f -> "B2b"
                    freqMhz in 1263f..1273f -> "B3I"
                    else -> String.format("%.0fM", freqMhz)
                }
                ConstellationType.GALILEO -> when {
                    freqMhz in 1570f..1580f -> "E1"
                    freqMhz in 1170f..1182f -> "E5a"
                    freqMhz in 1202f..1212f -> "E5b"
                    else -> String.format("%.0fM", freqMhz)
                }
                ConstellationType.GLONASS -> when {
                    freqMhz in 1595f..1612f -> "G1"
                    freqMhz in 1240f..1255f -> "G2"
                    else -> String.format("%.0fM", freqMhz)
                }
                else -> String.format("%.0fM", freqMhz)
            }
        }

    val isDualBand: Boolean
        get() = frequencyBand in listOf("L5", "B2a", "B2b", "E5a", "E5b")
}
