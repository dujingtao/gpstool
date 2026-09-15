package com.allai.gpstool.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allai.gpstool.ui.theme.LocalCustomColors
import com.allai.gpstool.viewmodel.GpsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NmeaScreen(viewModel: GpsViewModel) {
    val colors = LocalCustomColors.current
    val context = LocalContext.current
    val records by viewModel.nmeaRecords.collectAsState()
    val rate by viewModel.messagesPerSec.collectAsState()
    val dopData by viewModel.dopData.collectAsState()

    var isPaused by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }
    val listState = rememberLazyListState()

    // 过滤后列表
    val filteredRecords = remember(records, selectedFilter) {
        if (selectedFilter == "ALL") records
        else records.filter { it.type.contains(selectedFilter, ignoreCase = true) }
    }

    // 自动滚动到底部 (未暂停状态下)
    LaunchedEffect(filteredRecords.size, isPaused) {
        if (!isPaused && filteredRecords.isNotEmpty()) {
            listState.scrollToItem(filteredRecords.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(12.dp)
            .padding(bottom = 72.dp)
    ) {
        // 顶部控制与吞吐量状态栏
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NMEA 0183 原始通信流",
                        color = colors.primary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "芯片速率: $rate 条/秒 | 缓冲: ${records.size}/300",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // 暂停/继续
                    IconButton(
                        onClick = {
                            isPaused = !isPaused
                            viewModel.setNmeaPaused(isPaused)
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause/Resume",
                            tint = if (isPaused) colors.secondary else colors.primary
                        )
                    }

                    // 清空
                    IconButton(
                        onClick = { viewModel.clearNmea() },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear",
                            tint = colors.textSecondary
                        )
                    }

                    // 一键复制全部
                    IconButton(
                        onClick = {
                            val text = filteredRecords.joinToString("\n") { it.rawMessage }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("NMEA Stream", text))
                            Toast.makeText(context, "已复制全部 NMEA 日志", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy All",
                            tint = colors.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 快捷过滤器 (ALL, GGA, RMC, GSA, GSV)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "GGA", "RMC", "GSA", "GSV").forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) colors.primary else colors.surface)
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) Color.Black else colors.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 终端报文滚动主视窗
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF070A0E)) // 纯黑极客终端背景
                .padding(8.dp)
        ) {
            if (filteredRecords.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isPaused) "[流已暂停]" else "正在捕获芯片原始 NMEA 报文...",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredRecords) { record ->
                        val msgColor = when {
                            record.type.contains("GGA") -> Color(0xFF00F0FF) // 激光青 (定位经纬度)
                            record.type.contains("RMC") -> Color(0xFF00FF7F) // 霓虹绿 (航速航向)
                            record.type.contains("GSA") -> Color(0xFFFFB300) // 琥珀黄 (DOP精度)
                            record.type.contains("GSV") -> Color(0xFFAA88FF) // 紫色 (卫星状态)
                            else -> Color(0xFFC0C8D0)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("NMEA Sentence", record.rawMessage))
                                    Toast.makeText(context, "已复制此语句", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            // 时间戳
                            Text(
                                text = timeFormat.format(Date(record.timestamp)),
                                color = Color(0xFF556070),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(78.dp)
                            )
                            // 报文内容
                            Text(
                                text = record.rawMessage,
                                color = msgColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
