package com.allai.gpstool.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.allai.gpstool.data.GnssRepository
import com.allai.gpstool.data.LocationRepository
import com.allai.gpstool.data.SensorRepository
import com.allai.gpstool.model.CoordinateFormat
import com.allai.gpstool.model.LocationData
import com.allai.gpstool.model.SatelliteInfo
import com.allai.gpstool.model.SpeedUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import kotlin.math.abs

class GpsViewModel(application: Application) : AndroidViewModel(application) {
    private val gnssRepo = GnssRepository(application)
    private val locationRepo = LocationRepository(application)
    private val sensorRepo = SensorRepository(application)
    private val nmeaRepo = NmeaRepository(application)

    val satellites: StateFlow<List<SatelliteInfo>> = gnssRepo.satellites
    val locationData: StateFlow<LocationData> = locationRepo.locationData
    val sensorData = sensorRepo.sensorData
    val nmeaRecords = nmeaRepo.nmeaRecords
    val dopData = nmeaRepo.dopData
    val messagesPerSec = nmeaRepo.messagesPerSec
    val ttffMillis = gnssRepo.ttffMillis

    // 统计数据 (可见卫星数, 锁定解算卫星数, 平均 C/N0, 双频卫星数)
    data class SatStats(
        val inView: Int = 0,
        val inFix: Int = 0,
        val avgCn0: Float = 0f,
        val dualBandCount: Int = 0
    )

    val satelliteStats = combine(satellites, locationData) { sats, loc ->
        val inView = sats.size
        val inFix = sats.count { it.usedInFix }
        val avgCn0 = if (sats.isNotEmpty()) sats.map { it.cn0DbHz }.average().toFloat() else 0f
        val dualBand = sats.count { it.isDualBand }
        SatStats(inView, inFix, avgCn0, dualBand)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SatStats())

    fun setNmeaPaused(paused: Boolean) = nmeaRepo.setPaused(paused)
    fun clearNmea() = nmeaRepo.clear()

    fun startListening() {
        gnssRepo.startListening()
        locationRepo.startListening()
        sensorRepo.startListening()
        nmeaRepo.startListening()
    }

    fun stopListening() {
        gnssRepo.stopListening()
        locationRepo.stopListening()
        sensorRepo.stopListening()
        nmeaRepo.stopListening()
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }

    // 经纬度格式化工具
    fun formatCoordinates(lat: Double, lon: Double, format: CoordinateFormat): Pair<String, String> {
        return when (format) {
            CoordinateFormat.DECIMAL -> {
                val latStr = String.format(Locale.US, "%.6f° %s", abs(lat), if (lat >= 0) "N" else "S")
                val lonStr = String.format(Locale.US, "%.6f° %s", abs(lon), if (lon >= 0) "E" else "W")
                Pair(latStr, lonStr)
            }
            CoordinateFormat.DMS -> {
                Pair(toDms(lat, true), toDms(lon, false))
            }
        }
    }

    private fun toDms(value: Double, isLatitude: Boolean): String {
        val hemisphere = if (isLatitude) {
            if (value >= 0) "N" else "S"
        } else {
            if (value >= 0) "E" else "W"
        }
        val absVal = abs(value)
        val degrees = absVal.toInt()
        val minutesDouble = (absVal - degrees) * 60.0
        val minutes = minutesDouble.toInt()
        val seconds = (minutesDouble - minutes) * 60.0
        return String.format(Locale.US, "%d°%02d'%05.2f\"%s", degrees, minutes, seconds, hemisphere)
    }

    fun formatSpeed(speedMs: Float, unit: SpeedUnit): String {
        val converted = speedMs * unit.factor
        return String.format(Locale.US, "%.1f %s", converted, unit.name.lowercase())
    }
}
