package com.example.ui.home.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.MemoryPhoto
import com.example.ui.theme.DuoraPrimaryPink
import com.example.ui.theme.DuoraPrimaryText
import com.example.ui.theme.DuoraSecondaryText

/**
 * Our Moments Gallery matching the Duora Home Screen design reference:
 * - Title "Our Moments"
 * - Action "See All >" in primary pink
 * - 3 rounded static romantic moments: Sunset Couple, Heart Lake, Pink Garden
 * - User-uploaded moments via Coil
 * - Add Moment (+) card
 * - Graceful loading, empty, and error fallback states
 */
@Composable
fun MomentsGallery(
    moments: List<MemoryPhoto> = emptyList(),
    onAddMoment: () -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Our Moments",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DuoraPrimaryText,
                modifier = Modifier.testTag("moments_section_title")
            )

            Text(
                text = "See All >",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DuoraPrimaryPink,
                modifier = Modifier
                    .clickable(onClick = onSeeAllClick)
                    .padding(4.dp)
                    .testTag("moments_see_all_button")
            )
        }

        // Horizontal Carousel
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Card 1: Couple Sunset
            item {
                StaticMomentThumbnail(
                    drawableRes = R.drawable.moment_couple,
                    contentDescription = "Sunset Couple Moment"
                )
            }

            // Card 2: Glowing Heart Lake
            item {
                StaticMomentThumbnail(
                    drawableRes = R.drawable.moment_heart_lake,
                    contentDescription = "Heart Lake Moment"
                )
            }

            // Card 3: Pink Flower Garden
            item {
                StaticMomentThumbnail(
                    drawableRes = R.drawable.moment_pink_garden,
                    contentDescription = "Pink Garden Moment"
                )
            }

            // Custom user-uploaded moments
            items(moments) { photo ->
                DynamicMomentThumbnail(photo = photo)
            }

            // Plus (+) Card to Add New Moment
            item {
                Surface(
                    onClick = onAddMoment,
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE5D7DF)),
                    modifier = Modifier
                        .size(width = 86.dp, height = 112.dp)
                        .testTag("add_moment_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Moment",
                            tint = DuoraSecondaryText,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StaticMomentThumbnail(
    drawableRes: Int,
    contentDescription: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.size(width = 86.dp, height = 112.dp)
    ) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun DynamicMomentThumbnail(photo: MemoryPhoto) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.size(width = 86.dp, height = 112.dp)
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(photo.uriString)
                .crossfade(true)
                .build(),
            contentDescription = photo.title,
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFDEEF3)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = DuoraPrimaryPink,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFDEEF3)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = "Failed to load",
                        tint = DuoraSecondaryText,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
