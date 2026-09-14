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

    private var smoothedAzimuth = 0f
    private var smoothedPitch = 0f
    private var smoothedRoll = 0f

    // 低通滤波系数 (0.05f ~ 0.15f，值越小越平滑稳重)
    private val FILTER_ALPHA = 0.12f

    private val _sensorData = MutableStateFlow(SensorOrientationData())
    val sensorData: StateFlow<SensorOrientationData> = _sensorData.asStateFlow()

    fun startListening() {
        sensorManager?.let { sm ->
            sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
            sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
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
                if (!hasAccelerometer) {
                    System.arraycopy(event.values, 0, accelerometerReading, 0, 3)
                    hasAccelerometer = true
                } else {
                    for (i in 0..2) {
                        accelerometerReading[i] += FILTER_ALPHA * (event.values[i] - accelerometerReading[i])
                    }
                }
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                if (!hasMagnetometer) {
                    System.arraycopy(event.values, 0, magnetometerReading, 0, 3)
                    hasMagnetometer = true
                } else {
                    for (i in 0..2) {
                        magnetometerReading[i] += FILTER_ALPHA * (event.values[i] - magnetometerReading[i])
                    }
                }
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
                var rawAzimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                if (rawAzimuth < 0) rawAzimuth += 360f

                val rawPitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                val rawRoll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

                // 角度循环差值平滑算法 (解决 359° 跨越 0° 的跳变反转问题)
                var diff = rawAzimuth - smoothedAzimuth
                while (diff < -180f) diff += 360f
                while (diff > 180f) diff -= 360f

                // 死区过滤 (如果变化小于 0.3° 视为手部微颤静止状态，不剧烈刷新)
                if (kotlin.math.abs(diff) > 0.3f) {
                    smoothedAzimuth = (smoothedAzimuth + diff * 0.2f + 360f) % 360f
                }

                var pitchDiff = rawPitch - smoothedPitch
                if (kotlin.math.abs(pitchDiff) > 0.2f) {
                    smoothedPitch += pitchDiff * 0.2f
                }

                var rollDiff = rawRoll - smoothedRoll
                if (kotlin.math.abs(rollDiff) > 0.2f) {
                    smoothedRoll += rollDiff * 0.2f
                }

                _sensorData.value = _sensorData.value.copy(
                    azimuthDegrees = smoothedAzimuth,
                    pitchDegrees = smoothedPitch,
                    rollDegrees = smoothedRoll
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
