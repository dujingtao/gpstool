package com.allai.gpstool.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

data class SensorOrientationData(
    val azimuthDegrees: Float = 0f, // 罗盘方位角 (0-360)
    val pitchDegrees: Float = 0f,   // 俯仰角 (-90 到 90)
    val rollDegrees: Float = 0f,    // 翻滚横滚角 (-180 到 180，水平仪)
    val magneticFieldStrength: Float = 0f, // 微特斯拉 uT
    val pressureHpa: Float = 0f,    // 百帕 hPa
    val altitudeMeters: Float = 0f  // 气压推算海拔
)

class SensorRepository(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var hasAccelerometer = false
    private var hasMagnetometer = false

    private val _sensorData = MutableStateFlow(SensorOrientationData())
    val sensorData: StateFlow<SensorOrientationData> = _sensorData.asStateFlow()

    fun startListening() {
        sensorManager?.let { sm ->
            sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            sm.getDefaultSensor(Sensor.TYPE_PRESSURE)?.also {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                hasAccelerometer = true
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
                hasMagnetometer = true
                val strength = kotlin.math.sqrt(
                    (event.values[0] * event.values[0] +
                     event.values[1] * event.values[1] +
                     event.values[2] * event.values[2]).toDouble()
                ).toFloat()
                _sensorData.value = _sensorData.value.copy(magneticFieldStrength = strength)
            }
            Sensor.TYPE_PRESSURE -> {
                val pressure = event.values[0]
                val altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure)
                _sensorData.value = _sensorData.value.copy(
                    pressureHpa = pressure,
                    altitudeMeters = altitude
                )
            }
        }

        if (hasAccelerometer && hasMagnetometer) {
            val success = SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometerReading,
                magnetometerReading
            )
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                // 转换为角度
                var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                if (azimuth < 0) azimuth += 360f

                val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

                _sensorData.value = _sensorData.value.copy(
                    azimuthDegrees = azimuth,
                    pitchDegrees = pitch,
                    rollDegrees = roll
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
