package com.example.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.ReadingTheme
import com.example.pdf.PdfRenderEngine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PdfThumbnailView(
    filePath: String,
    pageIndex: Int = 0,
    targetWidthPx: Int = 256,
    readingTheme: ReadingTheme = ReadingTheme.LIGHT,
    cornerRadius: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val bucketWidth = ((targetWidthPx + 63) / 64) * 64
    var bitmap by remember(filePath, pageIndex, bucketWidth) {
        mutableStateOf(PdfRenderEngine.getCachedBitmap(filePath, pageIndex, bucketWidth))
    }

    LaunchedEffect(filePath, pageIndex, bucketWidth) {
        if (bitmap == null) {
            bitmap = PdfRenderEngine.renderPage(filePath, pageIndex, bucketWidth)
        }
    }

    val colorFilter = remember(readingTheme) {
        readingTheme.toColorMatrix()?.let { ColorFilter.colorMatrix(it) }
    }

    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF2A2626))
            .border(0.8.dp, Color.White.copy(alpha = 0.12f), shape),
        contentAlignment = Alignment.Center
    ) {
        val currentBmp: Bitmap? = bitmap
        if (currentBmp != null && !currentBmp.isRecycled) {
            Image(
                bitmap = currentBmp.asImageBitmap(),
                contentDescription = "PDF page ${pageIndex + 1} preview",
                contentScale = ContentScale.Crop,
                colorFilter = colorFilter,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = "Loading PDF preview",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes < 1024L) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format(Locale.US, "%.0f KB", kb)
    val mb = kb / 1024.0
    return if (mb < 1.0) {
        String.format(Locale.US, "%.2f MB", mb)
    } else {
        String.format(Locale.US, "%.1f MB", mb)
    }
}

fun formatShortDate(timestampMillis: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
    return sdf.format(Date(timestampMillis))
}

fun formatRelativeTime(timestampMillis: Long): String {
    if (timestampMillis <= 0L) return "Never opened"
    val diffSec = ((System.currentTimeMillis() - timestampMillis) / 1000L).coerceAtLeast(1L)
    return when {
        diffSec < 60L -> "Just now"
        diffSec < 3600L -> "${diffSec / 60L} min. ago"
        diffSec < 86400L -> "${diffSec / 3600L} hr. ago"
        else -> "${diffSec / 86400L} d. ago"
    }
}
