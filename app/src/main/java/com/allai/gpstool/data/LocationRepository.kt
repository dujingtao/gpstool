package com.allai.gpstool.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.allai.gpstool.model.LocationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationRepository(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _locationData = MutableStateFlow(LocationData())
    val locationData: StateFlow<LocationData> = _locationData.asStateFlow()

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _locationData.value = _locationData.value.copy(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = if (location.hasAltitude()) location.altitude else 0.0,
                accuracy = if (location.hasAccuracy()) location.accuracy else 0.0f,
                speed = if (location.hasSpeed()) location.speed else 0.0f,
                bearing = if (location.hasBearing()) location.bearing else 0.0f,
                time = location.time,
                hasFix = true,
                provider = location.provider ?: "GPS"
            )
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {
            _locationData.value = _locationData.value.copy(hasFix = false)
        }
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    @SuppressLint("MissingPermission")
    fun startListening() {
        try {
            val hasGps = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            val provider = if (hasGps) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
            locationManager?.requestLocationUpdates(
                provider,
                1000L, // 每秒更新
                0f,
                locationListener,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun stopListening() {
        try {
            locationManager?.removeUpdates(locationListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
