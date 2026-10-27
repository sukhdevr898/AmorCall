package com.example.ui.pairing

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DuoraHotPink
import java.io.OutputStream
import kotlin.math.abs

object QrCodeGenerator {

    /**
     * Generates a 25x25 QR-like module matrix for a given 6-character pairing code.
     * Includes 7x7 corner finder patterns, timing patterns, and encoded data bits.
     */
    fun generateMatrix(code: String): Array<BooleanArray> {
        val size = 25
        val matrix = Array(size) { BooleanArray(size) }
        val reserved = Array(size) { BooleanArray(size) }

        // Helper to place 7x7 finder pattern
        fun placeFinder(top: Int, left: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    matrix[top + r][left + c] = isBorder || isCenter
                    reserved[top + r][left + c] = true
                }
            }
            // Separator around finder
            for (r in -1..7) {
                for (c in -1..7) {
                    val pr = top + r
                    val pc = left + c
                    if (pr in 0 until size && pc in 0 until size && !reserved[pr][pc]) {
                        matrix[pr][pc] = false
                        reserved[pr][pc] = true
                    }
                }
            }
        }

        // 3 Corner Finders
        placeFinder(0, 0)
        placeFinder(0, size - 7)
        placeFinder(size - 7, 0)

        // Timing patterns (row 6 and col 6)
        for (i in 8 until size - 8) {
            matrix[6][i] = i % 2 == 0
            matrix[i][6] = i % 2 == 0
            reserved[6][i] = true
            reserved[i][6] = true
        }

        // Center 5x5 reserved area for Heart Badge
        val centerStart = (size - 5) / 2
        for (r in centerStart until centerStart + 5) {
            for (c in centerStart until centerStart + 5) {
                reserved[r][c] = true
                matrix[r][c] = false
            }
        }

        // Data encoding from 6-char code
        val normalized = code.uppercase().trim().take(6).padEnd(6, 'X')
        var bitIndex = 0
        val bits = BooleanArray(256)
        var bitPointer = 0

        for (ch in normalized) {
            val v = ch.code
            for (b in 7 downTo 0) {
                if (bitPointer < bits.size) {
                    bits[bitPointer++] = ((v shr b) and 1) == 1
                }
            }
        }

        // Additional hash bits for density
        var hash = abs(normalized.hashCode())
        while (bitPointer < bits.size) {
            bits[bitPointer++] = (hash and 1) == 1
            hash = (hash shr 1) xor 0x55AA55
            if (hash == 0) hash = 0x937182
        }

        // Fill remaining unreserved cells with data
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (!reserved[r][c]) {
                    matrix[r][c] = bits[bitIndex % bits.size]
                    bitIndex++
                }
            }
        }

        return matrix
    }

    /**
     * Renders the QR code into an Android Bitmap (for downloading or sharing).
     */
    fun createBitmap(code: String, pixelSize: Int = 600): Bitmap {
        val matrix = generateMatrix(code)
        val size = matrix.size
        val bitmap = Bitmap.createBitmap(pixelSize, pixelSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply { color = android.graphics.Color.WHITE }
        canvas.drawRect(0f, 0f, pixelSize.toFloat(), pixelSize.toFloat(), bgPaint)

        val padding = pixelSize * 0.08f
        val innerSize = pixelSize - (padding * 2)
        val cellSize = innerSize / size

        val darkPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#21102E")
            isAntiAlias = true
        }

        for (r in 0 until size) {
            for (c in 0 until size) {
                if (matrix[r][c]) {
                    val left = padding + (c * cellSize)
                    val top = padding + (r * cellSize)
                    canvas.drawRoundRect(
                        left,
                        top,
                        left + cellSize - 0.5f,
                        top + cellSize - 0.5f,
                        2f,
                        2f,
                        darkPaint
                    )
                }
            }
        }

        // Center circular heart badge
        val center = pixelSize / 2f
        val badgeRadius = cellSize * 2.8f
        val badgeBg = Paint().apply {
            color = android.graphics.Color.WHITE
            isAntiAlias = true
        }
        val heartPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#FF2D8D")
            isAntiAlias = true
        }
        canvas.drawCircle(center, center, badgeRadius, badgeBg)
        canvas.drawCircle(center, center, badgeRadius * 0.72f, heartPaint)

        return bitmap
    }

    /**
     * Saves the QR code Bitmap to the user's gallery using MediaStore.
     */
    fun saveToGallery(context: Context, code: String): Boolean {
        return try {
            val bitmap = createBitmap(code)
            val filename = "Duora_QR_${code}_${System.currentTimeMillis()}.png"
            val resolver = context.contentResolver

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Duora")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { stream: OutputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Opens Android Sharesheet with the code.
     */
    fun shareCode(context: Context, code: String) {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Connect with me on Duora 💕\nHere is my 6-character partner pairing code: $code\nDownload Duora and pair with me!"
                )
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Duora Pairing Code"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * Jetpack Compose QR Code Visualizer Component.
 */
@Composable
fun DuoraQrCodeView(
    code: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 200.dp
) {
    val matrix = remember(code) { QrCodeGenerator.generateMatrix(code) }
    val darkColor = Color(0xFF21102E)

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(2.dp, Color(0xFFFFD5E5), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp - 28.dp)) {
            val matrixSize = matrix.size
            val cellSize = size.width / matrixSize

            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
                    if (matrix[r][c]) {
                        drawRect(
                            color = darkColor,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize - 0.4f, cellSize - 0.4f)
                        )
                    }
                }
            }
        }

        // Center Heart Badge
        Box(
            modifier = Modifier
                .size(sizeDp * 0.22f)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color(0xFFFFEBF2), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Duora Heart",
                tint = DuoraHotPink,
                modifier = Modifier.size(sizeDp * 0.13f)
            )
        }
    }
}
