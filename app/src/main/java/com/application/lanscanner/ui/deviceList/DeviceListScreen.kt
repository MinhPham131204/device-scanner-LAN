import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

import com.application.lanscanner.data.model.DeviceType
import com.application.lanscanner.data.model.LanDevice

// Định nghĩa màu sắc theo ảnh chụp
val DarkBackground = Color(0xFF000000)
val DarkSurface = Color(0xFF121212)
val TextGray = Color(0xFFAAAAAA)
val FingBlue = Color(0xFF2196F3)
val OnlineGreen = Color(0xFF4CAF50)
val BannerPurple = Color(0xFF3B2D4A)
val DividerColor = Color(0xFF2D2D2D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FingDeviceListScreen(
    devices: List<LanDevice>,
    subnetName: String = "Net 192.168.1.0/24",
    onUpdateClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Thiết bị", "Mạng")

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Column {
                // Top App Bar
                TopAppBar(
                    title = { Text(subnetName, color = Color.White, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = { /* Handle Back */ }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        TextButton(onClick = onUpdateClick) {
                            Text("CẬP NHẬT", color = FingBlue, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
                )

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = DarkBackground,
                    contentColor = Color.White,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = FingBlue
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedTabIndex == index) FingBlue else TextGray,
                                    fontSize = 16.sp
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Promo Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BannerPurple)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tự động quét và chặn thiết bị", color = TextGray, fontSize = 14.sp)
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGray, modifier = Modifier.size(18.dp))
            }

            // Status Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${devices.size} thiết bị", color = Color.White, fontSize = 14.sp)
                Text("2 phút trước", color = Color.White, fontSize = 14.sp)
            }

            // Device List
            LazyColumn {
                items(devices) { device ->
                    FingDeviceItem(device = device)
                    Divider(color = DividerColor, thickness = 1.dp, modifier = Modifier.padding(start = 64.dp, end = 16.dp))
                }
            }
        }
    }
}

@Composable
fun FingDeviceItem(device: LanDevice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Tương tác click */ }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon với chấm xanh online
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = when (device.deviceType) {
                    DeviceType.ROUTER -> Icons.Default.Router
                    DeviceType.PHONE -> Icons.Default.Smartphone
                    DeviceType.GENERIC -> Icons.Default.Adjust // Icon circle cho thiết bị chung
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            if (device.isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .background(DarkBackground, CircleShape)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(OnlineGreen)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Thông tin thiết bị (Tên và IP)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = device.name, color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatIpAddress(device.ipAddress),
                fontSize = 14.sp
            )
        }

        // Thông tin phụ bên phải (Brand, Model, Icon Wifi/Mũi tên)
        if (device.brand != null && device.model != null) {
            Column(horizontalAlignment = Alignment.End) {
                Text(text = device.brand, color = Color.White, fontSize = 14.sp)
                Text(text = device.model, color = TextGray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (device.deviceType == DeviceType.ROUTER) {
            Icon(Icons.Default.Wifi, contentDescription = "Wifi", tint = Color.White, modifier = Modifier.size(20.dp))
        } else {
            Icon(Icons.Default.ChevronRight, contentDescription = "More", tint = TextGray, modifier = Modifier.size(20.dp))
        }
    }
}

// Hàm hỗ trợ in đậm octet cuối cùng của địa chỉ IP
@Composable
fun formatIpAddress(ip: String) = buildAnnotatedString {
    val lastDotIndex = ip.lastIndexOf('.')
    if (lastDotIndex != -1) {
        withStyle(style = SpanStyle(color = TextGray)) {
            append(ip.substring(0, lastDotIndex + 1))
        }
        withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
            append(ip.substring(lastDotIndex + 1))
        }
    } else {
        append(ip)
    }
}