package com.allai.gpstool.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allai.gpstool.ui.theme.LocalCustomColors
import com.allai.gpstool.viewmodel.GpsViewModel
import com.allai.gpstool.viewmodel.ThemeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    gpsViewModel: GpsViewModel,
    themeViewModel: ThemeViewModel
) {
    val colors = LocalCustomColors.current
    val context = LocalContext.current
    val loc by gpsViewModel.locationData.collectAsState()
    val speedUnit by themeViewModel.speedUnit.collectAsState()
    val coordFormat by themeViewModel.coordFormat.collectAsState()

    val (latStr, lonStr) = gpsViewModel.formatCoordinates(loc.latitude, loc.longitude, coordFormat)
    val speedStr = gpsViewModel.formatSpeed(loc.speed, speedUnit)
    val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val timeStr = if (loc.time > 0) timeFormat.format(Date(loc.time)) else "等待定位更新..."

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 72.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 定位锁星状态条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (loc.hasFix) colors.secondary.copy(alpha = 0.2f) else colors.surface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (loc.hasFix) "● GPS 已锁定 (3D Fix)" else "○ 正在搜索卫星信号...",
                color = if (loc.hasFix) colors.secondary else colors.textSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "信号源: ${loc.provider.uppercase()}",
                color = colors.primary,
                fontSize = 12.sp
            )
        }

        // 经纬度主要大卡片 (支持点击一键复制)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Coordinates", "$latStr, $lonStr")
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "坐标已复制到剪贴板", Toast.LENGTH_SHORT).show()
                },
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "当前地理坐标 (点击复制)", color = colors.textSecondary, fontSize = 12.sp)
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "纬度: $latStr",
                    color = colors.primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "经度: $lonStr",
                    color = colors.primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 速度与精度仪表并列卡片
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "当前速度",
                value = speedStr,
                subtitle = "地面速度",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "定位精度",
                value = if (loc.accuracy > 0) String.format(Locale.US, "±%.1f m", loc.accuracy) else "--",
                subtitle = "水平误差估计",
                modifier = Modifier.weight(1f)
            )
        }

        // 海拔与航向角卡片
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "GPS 海拔高度",
                value = if (loc.hasFix) String.format(Locale.US, "%.1f m", loc.altitude) else "--",
                subtitle = "WGS84椭球高",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "航向角度",
                value = if (loc.bearing > 0) String.format(Locale.US, "%.1f°", loc.bearing) else "--",
                subtitle = "真北航行角",
                modifier = Modifier.weight(1f)
            )
        }

        // 时间戳卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "定位时间", color = colors.textSecondary, fontSize = 13.sp)
                Text(text = timeStr, color = colors.textPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalCustomColors.current
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = colors.textSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = colors.textSecondary, fontSize = 10.sp)
        }
    }
}
