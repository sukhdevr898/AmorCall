package com.example.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppTheme
import com.example.ui.theme.DuoraHotPink
import com.example.viewmodel.LoveCallViewModel

@Composable
fun ThemeSettingsScreen(
    viewModel: LoveCallViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val profile by viewModel.profile.collectAsState()

    val accentColors = listOf(
        0xFFFF2D8DL, // Hot Pink
        0xFF8B5CF6L, // Purple
        0xFF60A5FAL, // Pale Sky
        0xFF14B8A6L, // Turquoise
        0xFFF97316L, // Coral
        0xFFF43F5EL  // Romantic Rose
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6FA))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
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
                modifier = Modifier.testTag("theme_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF21102E)
                )
            }
            Column {
                Text(
                    text = "App Theme",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF21102E)
                )
                Text(
                    text = "Choose your favourite visual style",
                    fontSize = 12.sp,
                    color = Color(0xFF89818E)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Theme Options Cards
        Text(
            text = "APPEARANCE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF89818E),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThemeCard(
                icon = Icons.Default.LightMode,
                title = "Light Theme",
                subtitle = "Crisp, romantic pastel pink tones",
                isSelected = profile.appTheme == AppTheme.LIGHT,
                onClick = { viewModel.setAppTheme(AppTheme.LIGHT) }
            )

            ThemeCard(
                icon = Icons.Default.DarkMode,
                title = "Dark Theme",
                subtitle = "Deep romantic night mode",
                isSelected = profile.appTheme == AppTheme.DARK,
                onClick = { viewModel.setAppTheme(AppTheme.DARK) }
            )

            ThemeCard(
                icon = Icons.Default.BrightnessMedium,
                title = "Auto / System",
                subtitle = "Matches device dark mode settings",
                isSelected = profile.appTheme == AppTheme.LOVE,
                onClick = { viewModel.setAppTheme(AppTheme.LOVE) }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Accent Color Selection
        Text(
            text = "ACCENT COLOR",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF89818E),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Personalize buttons & highlights",
                    fontSize = 13.sp,
                    color = Color(0xFF89818E)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    accentColors.forEach { colorHex ->
                        val color = Color(colorHex)
                        val isSelected = profile.accentColorHex == colorHex

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable {
                                    viewModel.updateAccentColor(colorHex)
                                }
                                .then(
                                    if (isSelected) Modifier.border(3.dp, Color(0xFF21102E), CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun ThemeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) DuoraHotPink else Color(0xFFF0E8ED),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFFFFEBF2) else Color(0xFFF7F2F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) DuoraHotPink else Color(0xFF89818E),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF21102E)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF89818E)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) DuoraHotPink else Color(0xFFCCC5CE),
                        shape = CircleShape
                    )
                    .background(if (isSelected) DuoraHotPink else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
