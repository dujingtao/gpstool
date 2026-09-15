package com.allai.gpstool.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.allai.gpstool.model.ConstellationType
import com.allai.gpstool.model.SatelliteInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GnssRepository(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _satellites = MutableStateFlow<List<SatelliteInfo>>(emptyList())
    val satellites: StateFlow<List<SatelliteInfo>> = _satellites.asStateFlow()

    private val _gnssStarted = MutableStateFlow(false)
    val gnssStarted: StateFlow<Boolean> = _gnssStarted.asStateFlow()

    private val _ttffMillis = MutableStateFlow(0)
    val ttffMillis: StateFlow<Int> = _ttffMillis.asStateFlow()

    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onStarted() {
            _gnssStarted.value = true
        }

        override fun onStopped() {
            _gnssStarted.value = false
            _satellites.value = emptyList()
        }

        override fun onFirstFix(ttff: Int) {
            _gnssStarted.value = true
            _ttffMillis.value = ttff
        }

        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val count = status.satelliteCount
            val list = ArrayList<SatelliteInfo>(count)
            for (i in 0 until count) {
                val hasCarrier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    status.hasCarrierFrequencyHz(i)
                } else false

                val carrierFreq = if (hasCarrier && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    status.getCarrierFrequencyHz(i)
                } else 0f

                list.add(
                    SatelliteInfo(
                        svid = status.getSvid(i),
                        constellation = ConstellationType.fromGnssConstellation(status.getConstellationType(i)),
                        elevationDegrees = status.getElevationDegrees(i),
                        azimuthDegrees = status.getAzimuthDegrees(i),
                        cn0DbHz = status.getCn0DbHz(i),
                        usedInFix = status.usedInFix(i),
                        hasCarrierFrequency = hasCarrier,
                        carrierFrequencyHz = carrierFreq
                    )
                )
            }
            _satellites.value = list
        }
    }

    @SuppressLint("MissingPermission")
    fun startListening() {
        try {
            locationManager?.registerGnssStatusCallback(
                gnssStatusCallback,
                Handler(Looper.getMainLooper())
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun stopListening() {
        try {
            locationManager?.unregisterGnssStatusCallback(gnssStatusCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
