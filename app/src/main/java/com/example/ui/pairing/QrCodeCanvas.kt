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
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.OutputStream

object QrCodeGenerator {

    /**
     * Generates a real standard ISO/IEC 18004 QR code matrix using ZXing.
     * Uses ErrorCorrectionLevel.H so the center emblem doesn't affect readability.
     */
    fun generateMatrix(code: String): Array<BooleanArray> {
        val cleanCode = code.trim().uppercase()
        val payload = "DUORA:PAIR:$cleanCode"
        return try {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val bitMatrix: BitMatrix = QRCodeWriter().encode(
                payload,
                BarcodeFormat.QR_CODE,
                29,
                29,
                hints
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val result = Array(height) { r ->
                BooleanArray(width) { c ->
                    bitMatrix.get(c, r)
                }
            }
            result
        } catch (_: Exception) {
            // Minimal fallback matrix
            Array(25) { BooleanArray(25) }
        }
    }

    /**
     * Renders real standard QR into an Android Bitmap (for gallery export or sharing).
     */
    fun createBitmap(code: String, pixelSize: Int = 512): Bitmap {
        val cleanCode = code.trim().uppercase()
        val payload = "DUORA:PAIR:$cleanCode"
        return try {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 2,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val bitMatrix = QRCodeWriter().encode(
                payload,
                BarcodeFormat.QR_CODE,
                pixelSize,
                pixelSize,
                hints
            )
            val w = bitMatrix.width
            val h = bitMatrix.height
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val darkColor = android.graphics.Color.parseColor("#21102E")
            val lightColor = android.graphics.Color.WHITE

            for (x in 0 until w) {
                for (y in 0 until h) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) darkColor else lightColor)
                }
            }

            // Draw center heart icon
            val canvas = Canvas(bitmap)
            val center = w / 2f
            val badgeRadius = w * 0.12f
            val whitePaint = Paint().apply {
                color = android.graphics.Color.WHITE
                isAntiAlias = true
            }
            val pinkPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#FF2D8D")
                isAntiAlias = true
            }
            canvas.drawCircle(center, center, badgeRadius, whitePaint)
            canvas.drawCircle(center, center, badgeRadius * 0.8f, pinkPaint)

            bitmap
        } catch (_: Exception) {
            Bitmap.createBitmap(pixelSize, pixelSize, Bitmap.Config.ARGB_8888)
        }
    }

    /**
     * Decodes a QR code from a Bitmap (e.g. from gallery photo).
     */
    fun decodeBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val hints = mapOf(
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)
            )
            val result = MultiFormatReader().decode(binaryBitmap, hints)
            result?.text
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Extracts the clean partner code from any scanned payload string.
     * Supports formats:
     * - "DUORA:PAIR:8K9P2X"
     * - "duora://pair/8K9P2X"
     * - "DUORA-8K9P2X-1712345678"
     * - "{\"code\":\"8K9P2X\"}"
     * - "#8K9P2X" or raw "8K9P2X"
     */
    fun parseScannedCode(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("{") && clean.endsWith("}")) {
            try {
                val json = org.json.JSONObject(clean)
                clean = json.optString("code", json.optString("pairCode", json.optString("partnerCode", clean)))
            } catch (_: Exception) {}
        }
        clean = clean.replace("^DUORA:PAIR:".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("^duora://pair/".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("^DUORA-".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("^#".toRegex(), "")

        if (clean.contains("-")) {
            val parts = clean.split("-")
            if (parts.size > 1 && parts.last().length >= 9 && parts.last().all { it.isDigit() }) {
                clean = parts.dropLast(1).joinToString("")
            }
        }
        return clean.uppercase().filter { it.isLetterOrDigit() }.take(16)
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

            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                val stream: OutputStream? = resolver.openOutputStream(imageUri)
                if (stream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.flush()
                    stream.close()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Shares the QR code via Android system share sheet.
     */
    fun shareQrCode(context: Context, code: String) {
        try {
            val bitmap = createBitmap(code)
            val path = MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                "Duora_QR_$code",
                "Connect with me on Duora using code #$code"
            )
            val uri = Uri.parse(path)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Scan my Duora QR Code or enter my code #$code to link our private space 💕"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Duora QR Code"))
        } catch (_: Exception) {}
    }

    fun shareCode(context: Context, code: String) {
        shareQrCode(context, code)
    }
}

/**
 * Jetpack Compose real QR Code Visualizer Component.
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
        Canvas(modifier = Modifier.size(sizeDp - 24.dp)) {
            val matrixSize = matrix.size
            if (matrixSize > 0) {
                val cellSize = size.width / matrixSize

                for (r in 0 until matrixSize) {
                    for (c in 0 until matrixSize) {
                        if (matrix[r][c]) {
                            drawRect(
                                color = darkColor,
                                topLeft = Offset(c * cellSize, r * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                        }
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
