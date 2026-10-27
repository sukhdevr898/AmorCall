package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DuoraHotPink

@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

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
                modifier = Modifier.testTag("about_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF21102E)
                )
            }
            Text(
                text = "About Duora",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21102E)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Logo Card
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
                    .background(Color.White)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_heart),
                    contentDescription = "Duora Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Duora",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF21102E)
            )

            Text(
                text = "Always Together",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = DuoraHotPink
            )

            Text(
                text = "Version 3.0.0",
                fontSize = 12.sp,
                color = Color(0xFF89818E)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Options List Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column {
                AboutOptionRow(
                    icon = Icons.Default.NewReleases,
                    iconBg = Color(0xFFFFEBF2),
                    iconTint = DuoraHotPink,
                    title = "What's New",
                    subtitle = "Version 3.0.0 avatar catalog & pairing",
                    onClick = {}
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                AboutOptionRow(
                    icon = Icons.Default.Description,
                    iconBg = Color(0xFFEDE9FE),
                    iconTint = Color(0xFF7C3AED),
                    title = "Terms & Privacy",
                    subtitle = "Read our security & privacy policies",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://duora.app/privacy"))
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                AboutOptionRow(
                    icon = Icons.Default.HelpOutline,
                    iconBg = Color(0xFFE0F2FE),
                    iconTint = Color(0xFF0284C7),
                    title = "Help & Support",
                    subtitle = "Contact support & FAQs",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sukhdevr898/duora"))
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                AboutOptionRow(
                    icon = Icons.Default.Star,
                    iconBg = Color(0xFFFEF3C7),
                    iconTint = Color(0xFFD97706),
                    title = "Rate on Play Store",
                    subtitle = "Share your love for Duora",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.aistudio.amourcall.app"))
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Made with ❤️ for couples everywhere\n© 2026 Duora. All rights reserved.",
            fontSize = 12.sp,
            color = Color(0xFF89818E),
            lineHeight = 18.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun AboutOptionRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
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

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFCCC5CE),
            modifier = Modifier.size(20.dp)
        )
    }
}
