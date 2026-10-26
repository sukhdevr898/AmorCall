package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.calllogs.CallLogsScreen
import com.example.viewmodel.LoveCallViewModel

/**
 * Call Logs tab destination in Duora.
 * Delegates to the full visual implementation in [CallLogsScreen].
 */
@Composable
fun CallLogsTab(
    viewModel: LoveCallViewModel,
    onRequestPermissions: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    CallLogsScreen(
        viewModel = viewModel,
        onRequestPermissions = onRequestPermissions,
        onBack = onBack,
        modifier = modifier
    )
}
