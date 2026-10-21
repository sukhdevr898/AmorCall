package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.privacy.PrivacyIconManager
import com.example.privacy.PrivacyIconType
import com.example.ui.theme.LoveAccentGreen
import com.example.ui.theme.LoveBlush
import com.example.ui.theme.LoveCardBorder
import com.example.ui.theme.LoveCardSurface
import com.example.ui.theme.LoveCrimson
import com.example.ui.theme.LoveGold
import com.example.ui.theme.LoveRose
import com.example.ui.theme.LoveTextMuted
import com.example.ui.theme.LoveWhite

@Composable
fun PrivacyIconDialog(
    currentIcon: PrivacyIconType,
    onDismiss: () -> Unit,
    onIconChanged: (PrivacyIconType) -> Unit
) {
    val context = LocalContext.current
    var selectedOption by remember { mutableStateOf(currentIcon) }
    var isApplying by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF1E061E),
            border = BorderStroke(1.5.dp, LoveRose.copy(alpha = 0.7f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(LoveRose, LoveCrimson)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Privacy Shield",
                            tint = LoveWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Privacy App Icon",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoveWhite
                        )
                        Text(
                            text = "Stealth Launcher Disguise",
                            fontSize = 12.sp,
                            color = LoveGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Disguise Duora on your home screen and app drawer as a regular utility so no one suspects your private couple hotline.",
                    fontSize = 12.sp,
                    color = LoveBlush,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // List of icon choices
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(PrivacyIconType.entries) { iconType ->
                        val isSelected = selectedOption == iconType
                        val isCurrentActive = currentIcon == iconType

                        IconChoiceCard(
                            type = iconType,
                            isSelected = isSelected,
                            isCurrentActive = isCurrentActive,
                            onClick = { selectedOption = iconType }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("privacy_icon_cancel_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, LoveCardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = LoveTextMuted
                        )
                    ) {
                        Text("Cancel", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            isApplying = true
                            val success = PrivacyIconManager.setPrivacyIcon(context, selectedOption)
                            if (success) {
                                onIconChanged(selectedOption)
                                Toast.makeText(
                                    context,
                                    "App icon changed to ${selectedOption.launcherLabel}!",
                                    Toast.LENGTH_LONG
                                ).show()
                                onDismiss()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Could not change icon. Try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            isApplying = false
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("privacy_icon_apply_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LoveRose,
                            contentColor = LoveWhite
                        )
                    ) {
                        Text(
                            text = if (isApplying) "Applying..." else "Apply Disguise",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IconChoiceCard(
    type: PrivacyIconType,
    isSelected: Boolean,
    isCurrentActive: Boolean,
    onClick: () -> Unit
) {
    val (gradientColors, iconVector, iconTint) = when (type) {
        PrivacyIconType.DEFAULT -> Triple(
            listOf(LoveRose, LoveCrimson),
            Icons.Default.Favorite,
            LoveWhite
        )
        PrivacyIconType.CALCULATOR -> Triple(
            listOf(Color(0xFF334155), Color(0xFF0F172A)),
            Icons.Default.Calculate,
            Color(0xFF38BDF8)
        )
        PrivacyIconType.NOTES -> Triple(
            listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
            Icons.Default.Description,
            Color(0xFFFEF3C7)
        )
        PrivacyIconType.CLOCK -> Triple(
            listOf(Color(0xFF1E293B), Color(0xFF020617)),
            Icons.Default.Schedule,
            Color(0xFF38BDF8)
        )
        PrivacyIconType.WEATHER -> Triple(
            listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
            Icons.Default.WbSunny,
            Color(0xFFFEF08A)
        )
    }

    val borderColor = if (isSelected) LoveGold else LoveCardBorder
    val bgColor = if (isSelected) Color(0xFF2A0927) else LoveCardSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("privacy_icon_option_${type.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual Icon Preview
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(gradientColors))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = type.title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = type.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) LoveGold else LoveWhite
                    )

                    if (isCurrentActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LoveAccentGreen.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, LoveAccentGreen.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = LoveAccentGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = type.subtitle,
                    fontSize = 11.sp,
                    color = LoveTextMuted,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = LoveGold,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(1.5.dp, LoveTextMuted.copy(alpha = 0.5f), CircleShape)
                )
            }
        }
    }
}
