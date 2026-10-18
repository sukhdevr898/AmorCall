package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.model.MemoryPhoto
import com.example.ui.theme.LoveDarkBg
import com.example.ui.theme.LoveDarkSurface

@Composable
fun MemoryBackground(
    memory: MemoryPhoto?,
    modifier: Modifier = Modifier,
    dimAmount: Float = 0.65f,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (!memory?.uriString.isNullOrBlank()) {
            AsyncImage(
                model = memory?.uriString,
                contentDescription = "Romantic Memory Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Elegant romantic velvet gradient fallback
            val startColor = Color(memory?.gradientStartHex ?: 0xFF9F1239)
            val endColor = Color(memory?.gradientEndHex ?: 0xFF1E0A1A)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                startColor.copy(alpha = 0.8f),
                                endColor.copy(alpha = 0.95f),
                                LoveDarkBg
                            )
                        )
                    )
            )
        }

        // Dark plum ambient overlay for readable text and controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            LoveDarkSurface.copy(alpha = dimAmount * 0.7f),
                            LoveDarkBg.copy(alpha = dimAmount),
                            LoveDarkBg.copy(alpha = (dimAmount + 0.2f).coerceAtMost(0.95f))
                        )
                    )
                )
        )

        content()
    }
}
