package com.allai.gpstool.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allai.gpstool.model.ConstellationType
import com.allai.gpstool.model.SatelliteInfo
import com.allai.gpstool.ui.theme.ColorBeidou
import com.allai.gpstool.ui.theme.ColorGalileo
import com.allai.gpstool.ui.theme.ColorGlonass
import com.allai.gpstool.ui.theme.ColorGps
import com.allai.gpstool.ui.theme.ColorQzss
import com.allai.gpstool.ui.theme.ColorUnknown
import com.allai.gpstool.ui.theme.LocalCustomColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SkyRadarView(
    satellites: List<SatelliteInfo>,
    headingDegrees: Float = 0f,
    modifier: Modifier = Modifier
) {
    val colors = LocalCustomColors.current
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.0f)
            .padding(16.dp)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = (size.width / 2f) * 0.9f

        // 1. 绘制雷达同心仰角圆圈 (0°, 30°, 60°)
        val stroke = Stroke(width = 2f)
        val dashedStroke = Stroke(
            width = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        // 最外圈: 0° 仰角 (地平线)
        drawCircle(
            color = colors.primary.copy(alpha = 0.5f),
            radius = maxRadius,
            center = center,
            style = stroke
        )
        // 中圈: 30° 仰角
        drawCircle(
            color = colors.gridColor,
            radius = maxRadius * (60f / 90f),
            center = center,
            style = dashedStroke
        )
        // 内圈: 60° 仰角
        drawCircle(
            color = colors.gridColor,
            radius = maxRadius * (30f / 90f),
            center = center,
            style = dashedStroke
        )
        // 中心圆点 (天顶 90°)
        drawCircle(
            color = colors.primary,
            radius = 4f,
            center = center
        )

        // 2. 绘制十字十字方向基准轴 (北-南, 东-西)
        drawLine(
            color = colors.gridColor,
            start = Offset(center.x, center.y - maxRadius),
            end = Offset(center.x, center.y + maxRadius),
            strokeWidth = 1.5f
        )
        drawLine(
            color = colors.gridColor,
            start = Offset(center.x - maxRadius, center.y),
            end = Offset(center.x + maxRadius, center.y),
            strokeWidth = 1.5f
        )

        // 3. 绘制方位文字标识 (N, E, S, W)
        val cardinalStyle = TextStyle(
            color = colors.primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        drawText(textMeasurer, "N", Offset(center.x - 6f, center.y - maxRadius - 38f), style = cardinalStyle)
        drawText(textMeasurer, "S", Offset(center.x - 5f, center.y + maxRadius + 8f), style = cardinalStyle)
        drawText(textMeasurer, "E", Offset(center.x + maxRadius + 10f, center.y - 12f), style = cardinalStyle)
        drawText(textMeasurer, "W", Offset(center.x - maxRadius - 32f, center.y - 12f), style = cardinalStyle)

        // 4. 绘制各卫星天球位置
        satellites.forEach { sat ->
            // 仰角 (0~90°)，0度在最外缘，90度在圆心
            val r = maxRadius * ((90f - sat.elevationDegrees.coerceIn(0f, 90f)) / 90f)
            // 方位角转换为弧度 (0度为正北向上)
            val rad = Math.toRadians((sat.azimuthDegrees - 90.0)).toFloat()
            val satX = center.x + r * cos(rad)
            val satY = center.y + r * sin(rad)

            val satColor = when (sat.constellation) {
                ConstellationType.GPS -> ColorGps
                ConstellationType.BEIDOU -> ColorBeidou
                ConstellationType.GLONASS -> ColorGlonass
                ConstellationType.GALILEO -> ColorGalileo
                ConstellationType.QZSS -> ColorQzss
                else -> ColorUnknown
            }

            // 绘制卫星光晕 (参与解算的卫星)
            if (sat.usedInFix) {
                drawCircle(
                    color = satColor.copy(alpha = 0.35f),
                    radius = 18f,
                    center = Offset(satX, satY)
                )
                drawCircle(
                    color = satColor,
                    radius = 9f,
                    center = Offset(satX, satY)
                )
            } else {
                drawCircle(
                    color = satColor,
                    radius = 8f,
                    center = Offset(satX, satY),
                    style = Stroke(width = 3f)
                )
            }

            // 标注卫星 SVID 编号
            val labelStyle = TextStyle(
                color = colors.textPrimary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
            drawText(
                textMeasurer,
                "${sat.constellation.shortCode}${sat.svid}",
                Offset(satX - 14f, satY + 10f),
                style = labelStyle
            )
        }
    }
}
