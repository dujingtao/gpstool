package com.allai.gpstool.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.allai.gpstool.model.CoordinateFormat
import com.allai.gpstool.model.SpeedUnit
import com.allai.gpstool.model.ThemeStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("gpstool_prefs", Context.MODE_PRIVATE)

    private val _currentTheme = MutableStateFlow(
        ThemeStyle.valueOf(prefs.getString("theme_style", ThemeStyle.CYBERPUNK.name) ?: ThemeStyle.CYBERPUNK.name)
    )
    val currentTheme: StateFlow<ThemeStyle> = _currentTheme.asStateFlow()

    private val _speedUnit = MutableStateFlow(
        SpeedUnit.valueOf(prefs.getString("speed_unit", SpeedUnit.KMH.name) ?: SpeedUnit.KMH.name)
    )
    val speedUnit: StateFlow<SpeedUnit> = _speedUnit.asStateFlow()

    private val _coordFormat = MutableStateFlow(
        CoordinateFormat.valueOf(prefs.getString("coord_format", CoordinateFormat.DECIMAL.name) ?: CoordinateFormat.DECIMAL.name)
    )
    val coordFormat: StateFlow<CoordinateFormat> = _coordFormat.asStateFlow()

    fun setTheme(theme: ThemeStyle) {
        _currentTheme.value = theme
        prefs.edit().putString("theme_style", theme.name).apply()
    }

    fun setSpeedUnit(unit: SpeedUnit) {
        _speedUnit.value = unit
        prefs.edit().putString("speed_unit", unit.name).apply()
    }

    fun setCoordinateFormat(format: CoordinateFormat) {
        _coordFormat.value = format
        prefs.edit().putString("coord_format", format.name).apply()
    }
}
