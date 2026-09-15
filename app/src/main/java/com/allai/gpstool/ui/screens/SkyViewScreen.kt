package com.allai.gpstool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allai.gpstool.model.ConstellationType
import com.allai.gpstool.ui.components.SignalBarChart
import com.allai.gpstool.ui.components.SkyRadarView
import com.allai.gpstool.ui.theme.*
import com.allai.gpstool.viewmodel.GpsViewModel

@Composable
fun SkyViewScreen(viewModel: GpsViewModel) {
    val colors = LocalCustomColors.current
    val satellites by viewModel.satellites.collectAsState()
    val stats by viewModel.satelliteStats.collectAsState()
    val sensorData by viewModel.sensorData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 72.dp)
    ) {
        // 顶部统计栏卡片 (4 列布局)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatColumn(label = "可见卫星", value = "${stats.inView}", color = colors.primary)
                StatColumn(label = "解算定位", value = "${stats.inFix}", color = colors.secondary)
                StatColumn(label = "双频(L5/B2)", value = "${stats.dualBandCount}", color = Color(0xFFFFD700))
                StatColumn(label = "平均信号", value = String.format("%.1f dB", stats.avgCn0), color = colors.textPrimary)
            }
        }

        // 星座系统图例快速识别
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ConstellationBadge("GPS", ColorGps, satellites.count { it.constellation == ConstellationType.GPS })
            ConstellationBadge("北斗", ColorBeidou, satellites.count { it.constellation == ConstellationType.BEIDOU })
            ConstellationBadge("Galileo", ColorGalileo, satellites.count { it.constellation == ConstellationType.GALILEO })
            ConstellationBadge("GLONASS", ColorGlonass, satellites.count { it.constellation == ConstellationType.GLONASS })
        }

        // 天球圆形雷达视图
        SkyRadarView(
            satellites = satellites,
            headingDegrees = sensorData.azimuthDegrees
        )

        // 底部信号柱状图
        SignalBarChart(satellites = satellites)
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: Color) {
    val colors = LocalCustomColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = colors.textSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ConstellationBadge(name: String, color: Color, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, shape = RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$name: $count",
            color = LocalCustomColors.current.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
