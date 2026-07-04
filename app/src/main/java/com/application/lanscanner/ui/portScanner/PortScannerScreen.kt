package com.application.lanscanner.ui.portScanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Mã màu được trích xuất từ ảnh
val DarkBackground = Color(0xFF000000)
val HeaderGray = Color(0xFF888888)
val ActionBlue = Color(0xFF4285F4)
val BoxDarkSlate = Color(0xFF2D333B)
val TextLightBlue = Color(0xFF64B5F6)
val DividerDark = Color(0xFF333333)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortScannerScreen(
    viewModel: PortScannerViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Finding opening ports", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.toggleScan() }) {
                        Text(
                            text = if (state.isScanning) "STOP" else "START",
                            color = ActionBlue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header: Máy chủ đích & Mở cổng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Cột trái
                Column(modifier = Modifier.weight(1f)) {
                    Text("Destination device", color = HeaderGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search, // Tạm dùng icon kính lúp thay cho icon mạng tròn
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.targetIp.ifBlank { "Chung" },
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }

                // Dòng kẻ dọc ngăn cách
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(DividerDark)
                        .align(Alignment.CenterVertically)
                )

                // Cột phải
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text("No. opening ports", color = HeaderGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = HeaderGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${state.openPorts.size}",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = DividerDark, thickness = 1.dp)

            // Progress Bar: Scanned ports / Total ports (default = 65535)
            if (state.isScanning) {
                LinearProgressIndicator(
                    progress = { state.scannedCount.toFloat() / state.totalPortsToScan.coerceAtLeast(1) },
                    modifier = Modifier.fillMaxWidth(),
                    color = ActionBlue,
                    trackColor = DarkBackground
                )
            }

            // List of open ports
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.openPorts) { portInfo ->
                    PortItem(portInfo)
                }
            }
        }
    }
}

@Composable
fun PortItem(portInfo: PortUiModel) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Port Number Box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(BoxDarkSlate, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = portInfo.portNumber.toString(),
                    color = TextLightBlue,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Service name & Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = portInfo.serviceName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = portInfo.description,
                    color = HeaderGray,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        HorizontalDivider(color = DividerDark, thickness = 1.dp, modifier = Modifier.padding(start = 96.dp))
    }
}