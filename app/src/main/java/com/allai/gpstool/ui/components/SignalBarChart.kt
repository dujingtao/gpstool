package com.allai.gpstool.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allai.gpstool.model.ConstellationType
import com.allai.gpstool.model.SatelliteInfo
import com.allai.gpstool.ui.theme.*

@Composable
fun SignalBarChart(
    satellites: List<SatelliteInfo>,
    modifier: Modifier = Modifier
) {
    val colors = LocalCustomColors.current
    val sortedSats = satellites.sortedByDescending { it.cn0DbHz }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "卫星信号强度 (C/N0 dB-Hz)",
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "总计: ${satellites.size} | 已锁: ${satellites.count { it.usedInFix }}",
                color = colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (sortedSats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "正在搜索 GNSS 卫星信号... 请移步室外开阔地带",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                sortedSats.forEach { sat ->
                    val satColor = when (sat.constellation) {
                        ConstellationType.GPS -> ColorGps
                        ConstellationType.BEIDOU -> ColorBeidou
                        ConstellationType.GLONASS -> ColorGlonass
                        ConstellationType.GALILEO -> ColorGalileo
                        ConstellationType.QZSS -> ColorQzss
                        else -> ColorUnknown
                    }
                    val heightFraction = (sat.cn0DbHz.coerceIn(0f, 50f) / 50f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        // 信号值
                        Text(
                            text = sat.cn0DbHz.toInt().toString(),
                            color = if (sat.usedInFix) satColor else colors.textSecondary,
                            fontSize = 9.sp,
                            fontWeight = if (sat.usedInFix) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // 信号柱
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height((heightFraction * 70).dp.coerceAtLeast(4.dp))
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(
                                    if (sat.usedInFix) satColor else satColor.copy(alpha = 0.35f)
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // 卫星标识
                        Text(
                            text = "${sat.constellation.shortCode}${sat.svid}",
                            color = colors.textPrimary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
