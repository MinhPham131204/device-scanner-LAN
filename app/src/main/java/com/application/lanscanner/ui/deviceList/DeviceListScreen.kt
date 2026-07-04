import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// TODO: Sửa lại đường dẫn import này cho khớp với package dự án của bạn
import com.application.lanscanner.data.model.DeviceType
import com.application.lanscanner.data.model.LanDevice
import com.application.lanscanner.ui.networkInfo.NetworkInfoScreen
import com.application.lanscanner.ui.networkInfo.NetworkInfoViewModel
import com.application.lanscanner.utils.NetworkUtils

// --- ĐỊNH NGHĨA MÀU SẮC ---
val DarkBackground = Color(0xFF000000)
val DarkSurface = Color(0xFF121212)
val TextGray = Color(0xFFAAAAAA)
val FingBlue = Color(0xFF2196F3)
val OnlineGreen = Color(0xFF4CAF50)
val BannerPurple = Color(0xFF3B2D4A)
val DividerColor = Color(0xFF2D2D2D)

// --- Main UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FingDeviceListScreen(
    devices: List<LanDevice>,
    subnetName: String,
    isScanning: Boolean,
    onUpdateClick: () -> Unit,
    onDeviceClick: (LanDevice) -> Unit,
    onBackClick: () -> Unit,
    networkInfoViewModel: NetworkInfoViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Devices", "Network")

    val context = LocalContext.current
    LaunchedEffect(devices.size) {
        networkInfoViewModel.fetchNetworkDetails(context, devices.size)
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(subnetName, color = Color.White, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        TextButton(onClick = onUpdateClick) {
                            Text("REFRESH", color = FingBlue, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
                )

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = DarkBackground,
                    contentColor = Color.White,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
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
        when (selectedTabIndex) {
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (isScanning) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BannerPurple)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Scanning devices...", color = Color.White, fontSize = 14.sp)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${devices.size} devices", color = Color.White, fontSize = 14.sp)
                    }

                    LazyColumn {
                        items(
                            items = devices,
                            key = { device -> device.ipAddress }
                        ) { device ->
                            FingDeviceItem(
                                device = device,
                                onClick = { onDeviceClick(device) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                                thickness = 1.dp,
                                color = DividerColor
                            )
                        }
                    }
                }
            }
            1 -> {
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    NetworkInfoScreen(viewModel = networkInfoViewModel)
                }
            }
        }
    }
}

@Composable
fun FingDeviceItem(device: LanDevice, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = when (device.deviceType) {
                    DeviceType.ROUTER -> Icons.Default.Router
                    DeviceType.PHONE -> Icons.Default.Smartphone
                    DeviceType.GENERIC -> Icons.Default.Adjust
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                color = Color.White,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatIpAddress(device.ipAddress),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (device.brand != null && device.model != null) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = device.brand,
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = device.model,
                    color = TextGray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        if (device.deviceType == DeviceType.ROUTER) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Wifi",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "More",
                tint = TextGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

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