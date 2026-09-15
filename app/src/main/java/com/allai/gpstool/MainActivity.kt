package com.allai.gpstool

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import com.allai.gpstool.ui.screens.*
import com.allai.gpstool.ui.theme.GPSToolTheme
import com.allai.gpstool.ui.theme.LocalCustomColors
import com.allai.gpstool.viewmodel.GpsViewModel
import com.allai.gpstool.viewmodel.ThemeViewModel

enum class NavigationItem(val title: String, val icon: ImageVector) {
    SKY_VIEW("雷达卫星", Icons.Default.Radar),
    DASHBOARD("仪表盘", Icons.Default.Dashboard),
    COMPASS("罗盘水平", Icons.Default.Explore),
    NMEA("NMEA对话", Icons.Default.Terminal),
    SETTINGS("风格定制", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    private val gpsViewModel: GpsViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    private var hasLocationPermission by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasLocationPermission = fineGranted || coarseGranted
        if (hasLocationPermission) {
            gpsViewModel.startListening()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()

        setContent {
            val currentTheme by themeViewModel.currentTheme.collectAsState()
            var selectedTab by remember { mutableStateOf(NavigationItem.SKY_VIEW) }

            GPSToolTheme(themeStyle = currentTheme) {
                val colors = LocalCustomColors.current
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = colors.surface,
                            contentColor = colors.textPrimary
                        ) {
                            NavigationItem.values().forEach { item ->
                                val selected = selectedTab == item
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { selectedTab = item },
                                    icon = { Icon(item.icon, contentDescription = item.title) },
                                    label = { Text(item.title) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = colors.primary,
                                        selectedTextColor = colors.primary,
                                        indicatorColor = colors.primary.copy(alpha = 0.2f),
                                        unselectedIconColor = colors.textSecondary,
                                        unselectedTextColor = colors.textSecondary
                                    )
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        when (selectedTab) {
                            NavigationItem.SKY_VIEW -> SkyViewScreen(gpsViewModel)
                            NavigationItem.DASHBOARD -> DashboardScreen(gpsViewModel, themeViewModel)
                            NavigationItem.COMPASS -> CompassScreen(gpsViewModel)
                            NavigationItem.NMEA -> NmeaScreen(gpsViewModel)
                            NavigationItem.SETTINGS -> SettingsScreen(themeViewModel)
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true
            gpsViewModel.startListening()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasLocationPermission) {
            gpsViewModel.startListening()
        }
    }

    override fun onPause() {
        super.onPause()
        gpsViewModel.stopListening()
    }
}
