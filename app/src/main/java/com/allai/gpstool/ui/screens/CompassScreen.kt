package com.allai.gpstool.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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

        // 旋转式指南针刻度盘 (古典航海罗盘风格)
        CompassDial(
            azimuthDegrees = sensorData.azimuthDegrees,
            modifier = Modifier.size(280.dp)
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

    // 连续角度跟踪，避免从 359° 跨到 1° 时发生大范围反向回旋
    var targetContinuousAngle by remember { mutableFloatStateOf(-azimuthDegrees) }
    LaunchedEffect(azimuthDegrees) {
        val target = -azimuthDegrees
        var diff = target - targetContinuousAngle
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f
        targetContinuousAngle += diff
    }

    // 柔和阻尼弹簧动画，模拟油压古典航海罗盘的稳重平顺
    val animatedAngle by animateFloatAsState(
        targetValue = targetContinuousAngle,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "compassRotation"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.width / 2f * 0.90f
        val innerRingRadius = outerRadius * 0.76f

        // ===== 1. 随手机航向整体平滑旋转的【古典航海罗盘浮动卡盘 (Compass Card)】 =====
        rotate(animatedAngle, pivot = center) {
            // 底盘背景与双层古典黄铜外环
            drawCircle(
                color = colors.surface,
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = colors.primary.copy(alpha = 0.75f),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )
            drawCircle(
                color = colors.primary.copy(alpha = 0.35f),
                radius = innerRingRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 360 度精密刻度线 (每 5° 一小格，每 15° 一中格，每 30° 一大格)
            for (angle in 0 until 360 step 5) {
                val is30 = angle % 30 == 0
                val is15 = angle % 15 == 0
                val tickLength = when {
                    is30 -> 13.dp.toPx()
                    is15 -> 8.dp.toPx()
                    else -> 4.5.dp.toPx()
                }
                val rad = Math.toRadians((angle - 90.0)).toFloat()
                val startX = center.x + outerRadius * kotlin.math.cos(rad)
                val startY = center.y + outerRadius * kotlin.math.sin(rad)
                val endX = center.x + (outerRadius - tickLength) * kotlin.math.cos(rad)
                val endY = center.y + (outerRadius - tickLength) * kotlin.math.sin(rad)

                drawLine(
                    color = if (is30) colors.primary else colors.primary.copy(alpha = 0.4f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (is30) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            // 刻度度数数字 (0°, 30°, 60° ... 330°)
            val degreeRadius = outerRadius - 19.dp.toPx()
            for (angle in 0 until 360 step 30) {
                val rad = Math.toRadians((angle - 90.0)).toFloat()
                val textLayout = textMeasurer.measure(
                    text = angle.toString(),
                    style = TextStyle(
                        color = colors.textSecondary.copy(alpha = 0.8f),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                val x = center.x + degreeRadius * kotlin.math.cos(rad) - textLayout.size.width / 2f
                val y = center.y + degreeRadius * kotlin.math.sin(rad) - textLayout.size.height / 2f
                drawText(textLayout, topLeft = Offset(x, y))
            }

            // 四大主方位标字 (N, E, S, W)
            val cardinalRadius = innerRingRadius - 16.dp.toPx()
            val cardinals = listOf(
                Triple("N", 0, Color(0xFFFF3344)),   // 北：经典朱砂红
                Triple("E", 90, colors.textPrimary),
                Triple("S", 180, colors.textPrimary),
                Triple("W", 270, colors.textPrimary)
            )
            cardinals.forEach { (label, deg, color) ->
                val rad = Math.toRadians((deg - 90.0)).toFloat()
                val layout = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        color = color,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                )
                val x = center.x + cardinalRadius * kotlin.math.cos(rad) - layout.size.width / 2f
                val y = center.y + cardinalRadius * kotlin.math.sin(rad) - layout.size.height / 2f
                drawText(layout, topLeft = Offset(x, y))
            }

            // 四副方位标字 (NE, SE, SW, NW)
            val subCardinals = listOf("NE" to 45, "SE" to 135, "SW" to 225, "NW" to 315)
            subCardinals.forEach { (label, deg) ->
                val rad = Math.toRadians((deg - 90.0)).toFloat()
                val layout = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        color = colors.textSecondary.copy(alpha = 0.65f),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                val x = center.x + cardinalRadius * kotlin.math.cos(rad) - layout.size.width / 2f
                val y = center.y + cardinalRadius * kotlin.math.sin(rad) - layout.size.height / 2f
                drawText(layout, topLeft = Offset(x, y))
            }

            // 古典八芒星花盘 (8-Point Vintage Compass Rose)
            val roseRadius = innerRingRadius * 0.72f
            val midRadius = roseRadius * 0.52f
            for (i in 0 until 8) {
                val baseAngle = i * 45f
                val isMajor = i % 2 == 0
                val length = if (isMajor) roseRadius else midRadius
                val width = if (isMajor) 14.dp.toPx() else 9.dp.toPx()

                rotate(baseAngle, pivot = center) {
                    val leftFacet = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(center.x - width * 0.5f, center.y)
                        lineTo(center.x, center.y - length)
                        close()
                    }
                    drawPath(
                        path = leftFacet,
                        color = if (i == 0) Color(0xFFFF4455) else colors.primary.copy(alpha = 0.50f)
                    )

                    val rightFacet = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(center.x + width * 0.5f, center.y)
                        lineTo(center.x, center.y - length)
                        close()
                    }
                    drawPath(
                        path = rightFacet,
                        color = if (i == 0) Color(0xFF880011) else colors.primary.copy(alpha = 0.20f)
                    )
                }
            }

            drawCircle(
                color = colors.primary.copy(alpha = 0.20f),
                radius = roseRadius * 0.42f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 古典双菱形镂空长指针 (Classic Pierced Diamond Needle)
            val needleLength = outerRadius * 0.78f
            val needleHalfWidth = 14.dp.toPx()
            val pierceRadius = 5.5.dp.toPx()
            val waistY = needleLength * 0.38f

            // 北向红针 (指向盘面的 N)
            val northLeft = Path().apply {
                moveTo(center.x, center.y - needleLength)
                lineTo(center.x - needleHalfWidth, center.y - waistY)
                lineTo(center.x, center.y)
                close()
            }
            drawPath(path = northLeft, color = Color(0xFFFF2233))

            val northRight = Path().apply {
                moveTo(center.x, center.y - needleLength)
                lineTo(center.x + needleHalfWidth, center.y - waistY)
                lineTo(center.x, center.y)
                close()
            }
            drawPath(path = northRight, color = Color(0xFF990011))

            // 南向银黑针 (指向盘面的 S)
            val southLength = needleLength * 0.82f
            val southWaistY = southLength * 0.38f
            val southLeft = Path().apply {
                moveTo(center.x, center.y + southLength)
                lineTo(center.x - needleHalfWidth * 0.85f, center.y + southWaistY)
                lineTo(center.x, center.y)
                close()
            }
            drawPath(path = southLeft, color = Color(0xFFDDDDDD))

            val southRight = Path().apply {
                moveTo(center.x, center.y + southLength)
                lineTo(center.x + needleHalfWidth * 0.85f, center.y + southWaistY)
                lineTo(center.x, center.y)
                close()
            }
            drawPath(path = southRight, color = Color(0xFF555555))

            // 指针镂空透雕孔
            drawCircle(
                color = colors.surface,
                radius = pierceRadius,
                center = Offset(center.x, center.y - waistY)
            )
            drawCircle(
                color = Color(0xFFFF2233),
                radius = pierceRadius,
                center = Offset(center.x, center.y - waistY),
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = colors.surface,
                radius = pierceRadius * 0.85f,
                center = Offset(center.x, center.y + southWaistY)
            )
            drawCircle(
                color = Color(0xFF888888),
                radius = pierceRadius * 0.85f,
                center = Offset(center.x, center.y + southWaistY),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 古典黄铜宝石中心枢轴
            drawCircle(color = Color(0xFFD4AF37), radius = 11.dp.toPx(), center = center)
            drawCircle(color = Color(0xFF8B0000), radius = 7.dp.toPx(), center = center)
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(center.x - 2f, center.y - 2f))
        }

        // ===== 2. 固定正上方航向基准游标 (Fixed Heading Lubber Line at 12 o'clock) =====
        // 倒三角游标 (指向盘面正上方，代表手机正前方的真实航向)
        val lubberSize = 12.dp.toPx()
        val lubberTopY = center.y - outerRadius - 4.dp.toPx()
        val lubberPath = Path().apply {
            moveTo(center.x, lubberTopY + lubberSize) // 尖端向下指着表盘当前刻度
            lineTo(center.x - lubberSize * 0.7f, lubberTopY)
            lineTo(center.x + lubberSize * 0.7f, lubberTopY)
            close()
        }
        // 游标外发光光晕
        drawPath(path = lubberPath, color = Color(0xFFFF3344))
        drawCircle(
            color = Color(0xFFFF3344).copy(alpha = 0.35f),
            radius = 16.dp.toPx(),
            center = Offset(center.x, lubberTopY + lubberSize * 0.5f)
        )
    }
}

@Composable
fun BubbleLevelView(
    pitch: Float,
    roll: Float,
    modifier: Modifier = Modifier
) {
    val colors = LocalCustomColors.current
    val animatedPitch by animateFloatAsState(targetValue = pitch, animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "pitch")
    val animatedRoll by animateFloatAsState(targetValue = roll, animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "roll")
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

                // 计算气泡偏移 (使用平滑动画值)
                val maxOffset = maxRadius - 16.dp.toPx()
                val bubbleX = (center.x + (animatedRoll / 45f).coerceIn(-1f, 1f) * maxOffset)
                val bubbleY = (center.y + (animatedPitch / 45f).coerceIn(-1f, 1f) * maxOffset)

                val isLevel = kotlin.math.abs(animatedPitch) < 1.0f && kotlin.math.abs(animatedRoll) < 1.0f
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
