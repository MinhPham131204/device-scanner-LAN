package com.application.lanscanner.ui.networkInfo

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.ui.platform.LocalContext
import com.application.lanscanner.ui.portScanner.ActionBlue
import androidx.core.net.toUri

val DarkBackground = Color(0xFF000000)
val TextGray = Color(0xFFAAAAAA)
val BadgeGray = Color(0xFF333333)
val DividerDark = Color(0xFF222222)

@Composable
fun NetworkInfoScreen(viewModel: NetworkInfoViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // --- Part 1: HEADER ---
        Text(
            text = state.subnetName,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(enabled = state.coordinates.isNotBlank()) {
                    val uri = "geo:${state.coordinates}?q=${state.coordinates}(Location)".toUri()
                    val intent = Intent(Intent.ACTION_VIEW, uri)

                    // open location of network by GG Maps
                    intent.setPackage("com.google.android.apps.maps")

                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // If devices don't install Google Maps, ignore this package to open by another default location tracking app
                        intent.setPackage(null)
                        context.startActivity(intent)
                    }
                }
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = state.location,
                color = TextGray,
                fontSize = 18.sp
            )
            // Intent icon
            if (state.coordinates.isNotBlank()) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew, // Nhớ import icon này
                    contentDescription = "Open in GG Maps",
                    tint = ActionBlue, // Màu xanh như nút "BẮT ĐẦU" ở màn hình quét
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Badge WiFi
            Row(
                modifier = Modifier
                    .background(BadgeGray, shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("WiFi", color = TextGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.Wifi, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
            }

            // Badge Devices
            Row(
                modifier = Modifier
                    .background(BadgeGray, shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${state.onlineDevices}/${state.totalDevices}", color = TextGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.Smartphone, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = DividerDark, thickness = 1.dp)
        Spacer(modifier = Modifier.height(24.dp))

        // --- Part 2: Network detail ---
        Text("Access points", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        InfoRow(label = "BSSID", value = state.bssid)

        Spacer(modifier = Modifier.height(32.dp))

        Text("Network Config", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))
        InfoRow(label = "Netmask", value = state.netmask)
        InfoRow(label = "Gateway", value = state.gatewayIp)
        InfoRow(label = "DNS", value = state.dnsServers)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextGray, fontSize = 16.sp)
        Text(text = value, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(start = 16.dp))
    }
}