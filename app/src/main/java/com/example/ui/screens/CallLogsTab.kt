package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallRecord
import com.example.model.CallType
import com.example.ui.theme.DuoraPaleSky
import com.example.ui.theme.DuoraRaspberry
import com.example.ui.theme.LoveAccentGreen
import com.example.ui.theme.LoveGold
import com.example.ui.theme.LoveRose
import com.example.viewmodel.LoveCallViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CallFilter {
    ALL,
    MISSED,
    AUDIO,
    VIDEO
}

@Composable
fun CallLogsTab(
    viewModel: LoveCallViewModel,
    onRequestPermissions: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    BackHandler(onBack = onBack)

    val callHistory by viewModel.callHistory.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var selectedFilter by remember { mutableStateOf(CallFilter.ALL) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val filteredCalls = remember(callHistory, selectedFilter) {
        when (selectedFilter) {
            CallFilter.ALL -> callHistory
            CallFilter.MISSED -> callHistory.filter { it.durationSeconds == 0 }
            CallFilter.AUDIO -> callHistory.filter { it.type == CallType.VOICE }
            CallFilter.VIDEO -> callHistory.filter { it.type == CallType.VIDEO }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Dedicated Call Logs Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("calls_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Call History",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = DuoraRaspberry,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Calls with ${profile.partnerName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (callHistory.isNotEmpty()) {
                IconButton(
                    onClick = { showClearConfirmDialog = true },
                    modifier = Modifier.testTag("clear_all_call_logs_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Call History",
                        tint = LoveRose,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Filter Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == CallFilter.ALL,
                    onClick = { selectedFilter = CallFilter.ALL },
                    label = { Text("All (${callHistory.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DuoraRaspberry,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("filter_all_calls")
                )
            }
            item {
                val missedCount = callHistory.count { it.durationSeconds == 0 }
                FilterChip(
                    selected = selectedFilter == CallFilter.MISSED,
                    onClick = { selectedFilter = CallFilter.MISSED },
                    label = { Text("Missed ($missedCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LoveRose,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("filter_missed_calls")
                )
            }
            item {
                val audioCount = callHistory.count { it.type == CallType.VOICE }
                FilterChip(
                    selected = selectedFilter == CallFilter.AUDIO,
                    onClick = { selectedFilter = CallFilter.AUDIO },
                    label = { Text("Audio ($audioCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DuoraRaspberry,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("filter_audio_calls")
                )
            }
            item {
                val videoCount = callHistory.count { it.type == CallType.VIDEO }
                FilterChip(
                    selected = selectedFilter == CallFilter.VIDEO,
                    onClick = { selectedFilter = CallFilter.VIDEO },
                    label = { Text("Video ($videoCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DuoraRaspberry,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("filter_video_calls")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredCalls.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(DuoraPaleSky.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = DuoraRaspberry,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedFilter == CallFilter.ALL) "No call logs yet" else "No matching calls found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Call ${profile.partnerName} anytime using high-definition voice or immersive video ❤️",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    onRequestPermissions {
                                        viewModel.initiateCall(CallType.VOICE)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DuoraRaspberry),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Voice Call", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    onRequestPermissions {
                                        viewModel.initiateCall(CallType.VIDEO)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LoveRose),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Video Call", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCalls, key = { it.id }) { record ->
                    CallLogItemCard(
                        record = record,
                        partnerName = profile.partnerName,
                        onCallBack = {
                            onRequestPermissions {
                                viewModel.initiateCall(record.type)
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Clear Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Clear Call History?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to clear all call logs? This will delete the local call history.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCallHistory()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LoveRose),
                    modifier = Modifier.testTag("confirm_clear_logs_button")
                ) {
                    Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun CallLogItemCard(
    record: CallRecord,
    partnerName: String,
    onCallBack: () -> Unit
) {
    val isMissed = record.durationSeconds == 0
    val durationFormatted = remember(record.durationSeconds) {
        if (record.durationSeconds > 0) {
            val mins = record.durationSeconds / 60
            val secs = record.durationSeconds % 60
            "${mins}m ${secs}s"
        } else {
            "Missed Call"
        }
    }

    val formattedDate = remember(record.timestamp) {
        val sdf = SimpleDateFormat("MMM d • h:mm a", Locale.getDefault())
        sdf.format(Date(record.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("call_log_item_${record.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Call Type Avatar Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isMissed -> LoveRose.copy(alpha = 0.18f)
                                record.type == CallType.VIDEO -> DuoraRaspberry.copy(alpha = 0.18f)
                                else -> LoveAccentGreen.copy(alpha = 0.18f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (record.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = null,
                        tint = when {
                            isMissed -> LoveRose
                            record.type == CallType.VIDEO -> DuoraRaspberry
                            else -> LoveAccentGreen
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = partnerName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = when {
                                isMissed -> Icons.Default.CallEnd
                                record.isOutgoing -> Icons.Default.CallMade
                                else -> Icons.Default.CallReceived
                            },
                            contentDescription = null,
                            tint = if (isMissed) LoveRose else LoveAccentGreen,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = durationFormatted,
                            fontSize = 12.sp,
                            fontWeight = if (isMissed) FontWeight.Bold else FontWeight.Medium,
                            color = if (isMissed) LoveRose else LoveGold
                        )
                        Text(
                            text = " • $formattedDate",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Call Back Button
            IconButton(
                onClick = onCallBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(DuoraPaleSky.copy(alpha = 0.5f))
                    .border(1.dp, DuoraRaspberry.copy(alpha = 0.4f), CircleShape)
                    .testTag("callback_button_${record.id}")
            ) {
                Icon(
                    imageVector = if (record.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.PhoneCallback,
                    contentDescription = "Call Back",
                    tint = DuoraRaspberry,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
