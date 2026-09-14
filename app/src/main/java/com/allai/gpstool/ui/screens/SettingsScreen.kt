package com.allai.gpstool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allai.gpstool.model.CoordinateFormat
import com.allai.gpstool.model.SpeedUnit
import com.allai.gpstool.model.ThemeStyle
import com.allai.gpstool.ui.theme.LocalCustomColors
import com.allai.gpstool.viewmodel.ThemeViewModel

@Composable
fun SettingsScreen(themeViewModel: ThemeViewModel) {
    val colors = LocalCustomColors.current
    val currentTheme by themeViewModel.currentTheme.collectAsState()
    val speedUnit by themeViewModel.speedUnit.collectAsState()
    val coordFormat by themeViewModel.coordFormat.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 72.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "仪表与界面风格定制",
            color = colors.primary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "根据使用场景自定义应用外观。适合夜视、车载、户外烈日强光等各种环境。",
            color = colors.textSecondary,
            fontSize = 12.sp
        )

        // 主题风格卡片选择
        ThemeStyle.values().forEach { style ->
            val isSelected = style == currentTheme
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { themeViewModel.setTheme(style) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { themeViewModel.setTheme(style) },
                        colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = style.title,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = style.description,
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Divider(color = colors.gridColor)

        // 速度单位设置
        Text(
            text = "速度单位",
            color = colors.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpeedUnit.values().forEach { unit ->
                val isSelected = unit == speedUnit
                Button(
                    onClick = { themeViewModel.setSpeedUnit(unit) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) colors.primary else colors.surface,
                        contentColor = if (isSelected) Color.Black else colors.textPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = unit.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 坐标格式设置
        Text(
            text = "坐标显示格式",
            color = colors.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        CoordinateFormat.values().forEach { format ->
            val isSelected = format == coordFormat
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.surface)
                    .clickable { themeViewModel.setCoordinateFormat(format) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { themeViewModel.setCoordinateFormat(format) },
                    colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = format.displayName, color = colors.textPrimary, fontSize = 13.sp)
            }
        }

        // 关于与开发者信息 (包含您的 all.ai 标牌)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "GPSTool v1.0.0", color = colors.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "开发者: all.ai (Google Play 专享版)", color = colors.textSecondary, fontSize = 11.sp)
                Text(text = "支持系统: GPS, 北斗, GLONASS, Galileo, QZSS", color = colors.textSecondary, fontSize = 11.sp)
            }
        }
    }
}
