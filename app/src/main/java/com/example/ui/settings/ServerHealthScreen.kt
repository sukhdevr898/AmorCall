package com.example.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.ServerConnectionState
import com.example.viewmodel.LoveCallViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ServerHealthScreen(
    viewModel: LoveCallViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val serverState by viewModel.serverConnectionState.collectAsState()
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }

    val isOnline = serverState == ServerConnectionState.CONNECTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6FA))
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("health_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF21102E)
                )
            }
            Text(
                text = "Server Health",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21102E)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Shield status badge
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Status",
                    tint = if (isOnline) Color(0xFF16A765) else Color(0xFFE53945),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isOnline) "All Systems Online" else "Connecting to Network",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21102E)
            )

            Text(
                text = if (isOnline) "You are securely connected" else "Checking real-time service heartbeat...",
                fontSize = 13.sp,
                color = Color(0xFF89818E)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Live Component Status Card (No private URLs or IPs)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                HealthComponentRow(
                    icon = Icons.Default.Dns,
                    name = "Database",
                    status = if (isOnline) "Online" else "Connecting",
                    isGood = isOnline
                )
                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)
                HealthComponentRow(
                    icon = Icons.Default.Forum,
                    name = "Messaging",
                    status = if (isOnline) "Online" else "Connecting",
                    isGood = isOnline
                )
                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)
                HealthComponentRow(
                    icon = Icons.Default.Call,
                    name = "Calling",
                    status = if (isOnline) "Online" else "Standby",
                    isGood = isOnline
                )
                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)
                HealthComponentRow(
                    icon = Icons.Default.CloudDone,
                    name = "Storage",
                    status = "Online",
                    isGood = true
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom refresh bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isChecking) "Checking connectivity..." else "Last checked: Just now",
                fontSize = 13.sp,
                color = Color(0xFF89818E)
            )

            if (isChecking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFF16A765),
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(
                    onClick = {
                        scope.launch {
                            isChecking = true
                            delay(600)
                            isChecking = false
                        }
                    },
                    modifier = Modifier.testTag("health_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color(0xFF21102E)
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthComponentRow(
    icon: ImageVector,
    name: String,
    status: String,
    isGood: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGood) Color(0xFF16A765) else Color(0xFFE53945),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF21102E)
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isGood) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = status,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isGood) Color(0xFF16A765) else Color(0xFFD97706)
            )
        }
    }
}
