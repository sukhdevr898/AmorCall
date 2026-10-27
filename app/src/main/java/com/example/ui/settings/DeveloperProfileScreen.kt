package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FolderSpecial
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DuoraHotPink

@Composable
fun DeveloperProfileScreen(
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
                modifier = Modifier.testTag("dev_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF21102E)
                )
            }
            Text(
                text = "Developer Profile",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21102E)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Avatar & Bio
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD1DC))
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avatar_partner_demo),
                    contentDescription = "Sukhdev Singh",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Sukhdev Singh",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF21102E)
            )

            Text(
                text = "Android & Full Stack Developer",
                fontSize = 14.sp,
                color = DuoraHotPink,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Building Duora with ❤️\nFor love, privacy and simple experiences.",
                fontSize = 13.sp,
                color = Color(0xFF89818E),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Social Icons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                SocialIconButton(label = "GitHub", onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sukhdevr898"))
                    try { context.startActivity(intent) } catch (_: Exception) {}
                })
                Spacer(modifier = Modifier.width(14.dp))
                SocialIconButton(label = "Instagram", onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/sukhdevr898"))
                    try { context.startActivity(intent) } catch (_: Exception) {}
                })
                Spacer(modifier = Modifier.width(14.dp))
                SocialIconButton(label = "YouTube", onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/@sukhdevr898"))
                    try { context.startActivity(intent) } catch (_: Exception) {}
                })
                Spacer(modifier = Modifier.width(14.dp))
                SocialIconButton(label = "LinkedIn", onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://linkedin.com/in/sukhdevr898"))
                    try { context.startActivity(intent) } catch (_: Exception) {}
                })
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Actions Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column {
                DevActionRow(
                    icon = Icons.Default.FolderSpecial,
                    iconBg = Color(0xFFE0F2FE),
                    iconTint = Color(0xFF0284C7),
                    title = "View Projects",
                    subtitle = "Explore open-source Android apps",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sukhdevr898"))
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                DevActionRow(
                    icon = Icons.Default.Email,
                    iconBg = Color(0xFFEDE9FE),
                    iconTint = Color(0xFF7C3AED),
                    title = "Contact",
                    subtitle = "Get in touch for feedback or collabs",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:sukhdevsingh@example.com"))
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                DevActionRow(
                    icon = Icons.Default.Coffee,
                    iconBg = Color(0xFFFFF0E6),
                    iconTint = Color(0xFFEA580C),
                    title = "Buy Me a Coffee",
                    subtitle = "Support independent open-source development",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/sukhdevr898"))
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun SocialIconButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Code,
            contentDescription = label,
            tint = DuoraHotPink,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun DevActionRow(
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
