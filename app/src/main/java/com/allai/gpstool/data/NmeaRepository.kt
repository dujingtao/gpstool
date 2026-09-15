package com.allai.gpstool.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Handler
import android.os.Looper
import com.allai.gpstool.model.DopData
import com.allai.gpstool.model.NmeaRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NmeaRepository(context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _nmeaRecords = MutableStateFlow<List<NmeaRecord>>(emptyList())
    val nmeaRecords: StateFlow<List<NmeaRecord>> = _nmeaRecords.asStateFlow()

    private val _dopData = MutableStateFlow(DopData())
    val dopData: StateFlow<DopData> = _dopData.asStateFlow()

    private val _messagesPerSec = MutableStateFlow(0)
    val messagesPerSec: StateFlow<Int> = _messagesPerSec.asStateFlow()

    private val internalList = ArrayDeque<NmeaRecord>(300)
    private var isPaused = false

    private var msgCountThisSec = 0
    private var lastRateCalcTime = System.currentTimeMillis()

    private val nmeaListener = OnNmeaMessageListener { message, timestamp ->
        // 计算速率
        msgCountThisSec++
        val now = System.currentTimeMillis()
        if (now - lastRateCalcTime >= 1000L) {
            _messagesPerSec.value = msgCountThisSec
            msgCountThisSec = 0
            lastRateCalcTime = now
        }

        // 解析 DOP 语句 (GSA 语句中包含 2D/3D 模式与 PDOP, HDOP, VDOP)
        // 例如: $GNGSA,A,3,01,02,03,04,05,06,07,08,,,,,1.2,0.8,0.9*1A
        if (message.startsWith("$") && (message.contains("GSA"))) {
            parseGsaSentence(message)
        }

        if (isPaused) return@OnNmeaMessageListener

        val cleanMsg = message.trim()
        val type = if (cleanMsg.length >= 6 && cleanMsg.startsWith("$")) {
            cleanMsg.substring(1, 6)
        } else "OTHER"

        synchronized(internalList) {
            if (internalList.size >= 300) {
                internalList.removeFirst()
            }
            internalList.addLast(NmeaRecord(timestamp, cleanMsg, type))
            _nmeaRecords.value = internalList.toList()
        }
    }

    private fun parseGsaSentence(sentence: String) {
        try {
            val parts = sentence.split("*")[0].split(",")
            if (parts.size >= 18) {
                val mode = when (parts.getOrNull(2)) {
                    "2" -> "2D 定位"
                    "3" -> "3D 定位"
                    else -> "未定位"
                }
                val pdop = parts.getOrNull(15)?.toFloatOrNull() ?: 0f
                val hdop = parts.getOrNull(16)?.toFloatOrNull() ?: 0f
                val vdop = parts.getOrNull(17)?.toFloatOrNull() ?: 0f

                if (pdop > 0f || hdop > 0f) {
                    _dopData.value = DopData(
                        pdop = pdop,
                        hdop = hdop,
                        vdop = vdop,
                        fixMode = mode
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setPaused(paused: Boolean) {
        isPaused = paused
    }

    fun clear() {
        synchronized(internalList) {
            internalList.clear()
            _nmeaRecords.value = emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun startListening() {
        try {
            locationManager?.addNmeaListener(
                nmeaListener,
                Handler(Looper.getMainLooper())
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun stopListening() {
        try {
            locationManager?.removeNmeaListener(nmeaListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
