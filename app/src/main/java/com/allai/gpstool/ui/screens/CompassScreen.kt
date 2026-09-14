package com.allai.gpstool.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allai.gpstool.ui.theme.LocalCustomColors
import com.allai.gpstool.viewmodel.GpsViewModel
import kotlin.math.roundToInt

@Composable
fun CompassScreen(viewModel: GpsViewModel) {
    val colors = LocalCustomColors.current
    val sensorData by viewModel.sensorData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 方位角度数字展示
        val azimuth = sensorData.azimuthDegrees.roundToInt()
        val cardinalDirection = when (azimuth) {
            in 338..360, in 0..22 -> "北 (N)"
            in 23..67 -> "东北 (NE)"
            in 68..112 -> "东 (E)"
            in 113..157 -> "东南 (SE)"
            in 158..202 -> "南 (S)"
            in 203..247 -> "西南 (SW)"
            in 248..292 -> "西 (W)"
            else -> "西北 (NW)"
        }

        Text(
            text = "$azimuth° $cardinalDirection",
            color = colors.primary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "磁场强度: ${String.format("%.1f", sensorData.magneticFieldStrength)} μT",
            color = colors.textSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 旋转式指南针刻度盘
        CompassDial(
            azimuthDegrees = sensorData.azimuthDegrees,
            modifier = Modifier.size(240.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 双轴气泡水平仪
        BubbleLevelView(
            pitch = sensorData.pitchDegrees,
            roll = sensorData.rollDegrees,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 气压与海拔传感器卡片 (若硬件支持)
        if (sensorData.pressureHpa > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "气压计传感器", color = colors.textSecondary, fontSize = 11.sp)
                        Text(
                            text = "${String.format("%.1f", sensorData.pressureHpa)} hPa",
                            color = colors.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "气压推算海拔", color = colors.textSecondary, fontSize = 11.sp)
                        Text(
                            text = "${String.format("%.1f", sensorData.altitudeMeters)} m",
                            color = colors.secondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompassDial(
    azimuthDegrees: Float,
    modifier: Modifier = Modifier
) {
    val colors = LocalCustomColors.current
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width / 2f * 0.9f

        // 随着手机旋转反向转动表盘，使得正北始终指向上方
        rotate(-azimuthDegrees, pivot = center) {
            drawCircle(
                color = colors.surface,
                radius = radius,
                center = center
            )
            drawCircle(
                color = colors.primary.copy(alpha = 0.5f),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 绘制刻度
            for (angle in 0 until 360 step 15) {
                val isMajor = angle % 90 == 0
                val isMedium = angle % 45 == 0
                val tickLength = when {
                    isMajor -> 18.dp.toPx()
                    isMedium -> 12.dp.toPx()
                    else -> 6.dp.toPx()
                }
                val strokeWidth = if (isMajor) 3f else 1.5f

                rotate(angle.toFloat(), pivot = center) {
                    drawLine(
                        color = if (angle == 0) Color.Red else colors.primary.copy(alpha = 0.8f),
                        start = Offset(center.x, center.y - radius),
                        end = Offset(center.x, center.y - radius + tickLength),
                        strokeWidth = strokeWidth
                    )
                }
            }

            // 绘制标字 N/S/E/W
            drawText(textMeasurer, "N", Offset(center.x - 7f, center.y - radius + 22f), style = TextStyle(color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold))
            drawText(textMeasurer, "S", Offset(center.x - 6f, center.y + radius - 45f), style = TextStyle(color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold))
            drawText(textMeasurer, "E", Offset(center.x + radius - 40f, center.y - 12f), style = TextStyle(color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold))
            drawText(textMeasurer, "W", Offset(center.x - radius + 22f, center.y - 12f), style = TextStyle(color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold))
        }

        // 中心指针（固定红色北向指针）
        val needlePath = Path().apply {
            moveTo(center.x, center.y - radius * 0.75f)
            lineTo(center.x - 12f, center.y)
            lineTo(center.x + 12f, center.y)
            close()
        }
        drawPath(path = needlePath, color = Color.Red)

        val southPath = Path().apply {
            moveTo(center.x, center.y + radius * 0.75f)
            lineTo(center.x - 12f, center.y)
            lineTo(center.x + 12f, center.y)
            close()
        }
        drawPath(path = southPath, color = colors.textSecondary.copy(alpha = 0.7f))

        drawCircle(color = colors.textPrimary, radius = 6f, center = center)
    }
}

@Composable
fun BubbleLevelView(
    pitch: Float,
    roll: Float,
    modifier: Modifier = Modifier
) {
    val colors = LocalCustomColors.current
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "双轴水平仪 (倾角传感器)",
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 水平气泡圆形 Canvas
            Canvas(
                modifier = Modifier
                    .size(160.dp)
                    .padding(8.dp)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = size.width / 2f

                // 外框大圆
                drawCircle(
                    color = colors.primary.copy(alpha = 0.3f),
                    radius = maxRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
                // 靶心圆圈
                drawCircle(
                    color = colors.secondary.copy(alpha = 0.6f),
                    radius = 20.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                // 十字基准线
                drawLine(colors.gridColor, Offset(center.x, 0f), Offset(center.x, size.height), strokeWidth = 1f)
                drawLine(colors.gridColor, Offset(0f, center.y), Offset(size.width, center.y), strokeWidth = 1f)

                // 计算气泡偏移
                val maxOffset = maxRadius - 16.dp.toPx()
                val bubbleX = (center.x + (roll / 45f).coerceIn(-1f, 1f) * maxOffset)
                val bubbleY = (center.y + (pitch / 45f).coerceIn(-1f, 1f) * maxOffset)

                val isLevel = kotlin.math.abs(pitch) < 1.0f && kotlin.math.abs(roll) < 1.0f
                val bubbleColor = if (isLevel) colors.secondary else colors.primary

                // 绘制气泡
                drawCircle(
                    color = bubbleColor,
                    radius = 14.dp.toPx(),
                    center = Offset(bubbleX, bubbleY)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    text = "俯仰 Pitch: ${String.format("%.1f°", pitch)}",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "横滚 Roll: ${String.format("%.1f°", roll)}",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
