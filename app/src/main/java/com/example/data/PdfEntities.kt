package com.example.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdf_documents")
data class PdfDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val filePath: String,
    val folderName: String,
    val storageVolume: String = "Internal storage",
    val author: String = "NCERT & Exam Board",
    val sizeBytes: Long,
    val pageCount: Int,
    val currentPage: Int = 0,
    val lastOpenedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isPinnedNotification: Boolean = false
)

val PdfDocumentEntity.progressFraction: Float
    get() = if (pageCount <= 0) 0f else ((currentPage + 1).toFloat() / pageCount.toFloat()).coerceIn(0f, 1f)

val PdfDocumentEntity.progressPercent: Int
    get() = (progressFraction * 100f).toInt().coerceIn(0, 100)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val documentId: Long,
    val documentTitle: String,
    val pageIndex: Int,
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val documentId: Long,
    val documentTitle: String,
    val pageIndex: Int,
    val quoteText: String,
    val note: String = "",
    val colorHex: Long = 0xFFE6C86EL,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "annotations")
data class AnnotationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val documentId: Long,
    val pageIndex: Int,
    val toolType: String, // PEN, MARKER, HIGHLIGHTER, UNDERLINE, STICKY_NOTE
    val colorHex: Long,
    val strokeWidth: Float,
    val pointsData: String, // "x1,y1;x2,y2;..." normalized 0..1
    val noteText: String = "",
    val anchorX: Float = 0.5f,
    val anchorY: Float = 0.5f,
    val createdAt: Long = System.currentTimeMillis()
)

fun AnnotationEntity.parseNormalizedPoints(): List<Offset> {
    if (pointsData.isBlank()) return emptyList()
    return pointsData.split(";").mapNotNull { pair ->
        val parts = pair.split(",")
        if (parts.size == 2) {
            val x = parts[0].toFloatOrNull()
            val y = parts[1].toFloatOrNull()
            if (x != null && y != null) Offset(x, y) else null
        } else null
    }
}

fun encodeAnnotationPoints(points: List<Offset>): String {
    return points.joinToString(";") { pt ->
        "${"%.4f".format(java.util.Locale.US, pt.x)},${"%.4f".format(java.util.Locale.US, pt.y)}"
    }
}

enum class AnnotationTool(val label: String, val defaultStroke: Float, val isHighlightStyle: Boolean) {
    NONE("Browse", 0f, false),
    PEN("Pen", 4.5f, false),
    MARKER("Marker", 10f, false),
    HIGHLIGHTER("Highlight", 24f, true),
    UNDERLINE("Underline", 4f, false),
    STICKY_NOTE("Note Pin", 0f, false),
    ERASER("Eraser", 22f, false)
}

enum class ReadingTheme(
    val displayName: String,
    val previewBg: Color,
    val previewText: Color,
    val canvasBackground: Color,
    val isProFeature: Boolean,
    val matrixValues: FloatArray?
) {
    LIGHT(
        displayName = "Light",
        previewBg = Color(0xFFD0CECC),
        previewText = Color(0xFF171515),
        canvasBackground = Color(0xFFCFD3D6),
        isProFeature = false,
        matrixValues = null
    ),
    SEPIA(
        displayName = "Sepia",
        previewBg = Color(0xFFCEC8B6),
        previewText = Color(0xFF1E1A14),
        canvasBackground = Color(0xFFBDB6A2),
        isProFeature = false,
        matrixValues = floatArrayOf(
            0.92f, 0f, 0f, 0f, 10f,
            0f, 0.88f, 0f, 0f, 6f,
            0f, 0f, 0.76f, 0f, -4f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    DARK(
        displayName = "Dark",
        previewBg = Color(0xFF040404),
        previewText = Color(0xFF949290),
        canvasBackground = Color(0xFF0A0A0A),
        isProFeature = false,
        matrixValues = floatArrayOf(
            -0.88f, 0f, 0f, 0f, 235f,
            0f, -0.88f, 0f, 0f, 235f,
            0f, 0f, -0.88f, 0f, 235f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    MIST(
        displayName = "Mist",
        previewBg = Color(0xFFBAC1C7),
        previewText = Color(0xFF1D2226),
        canvasBackground = Color(0xFFA7B0B8),
        isProFeature = true,
        matrixValues = floatArrayOf(
            0.84f, 0f, 0f, 0f, 8f,
            0f, 0.88f, 0f, 0f, 12f,
            0f, 0f, 0.92f, 0f, 16f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    SAGE(
        displayName = "Sage",
        previewBg = Color(0xFFB3BDB5),
        previewText = Color(0xFF1B231D),
        canvasBackground = Color(0xFF9EAAA1),
        isProFeature = true,
        matrixValues = floatArrayOf(
            0.80f, 0f, 0f, 0f, 8f,
            0f, 0.87f, 0f, 0f, 14f,
            0f, 0f, 0.82f, 0f, 8f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    SAND(
        displayName = "Sand",
        previewBg = Color(0xFFC8BEAF),
        previewText = Color(0xFF282018),
        canvasBackground = Color(0xFFB5A998),
        isProFeature = true,
        matrixValues = floatArrayOf(
            0.90f, 0f, 0f, 0f, 14f,
            0f, 0.85f, 0f, 0f, 8f,
            0f, 0f, 0.76f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    DUSK(
        displayName = "Dusk",
        previewBg = Color(0xFF2C3137),
        previewText = Color(0xFFC5CDD4),
        canvasBackground = Color(0xFF1E2227),
        isProFeature = true,
        matrixValues = floatArrayOf(
            -0.72f, 0f, 0f, 0f, 226f,
            0f, -0.70f, 0f, 0f, 231f,
            0f, 0f, -0.66f, 0f, 238f,
            0f, 0f, 0f, 1f, 0f
        )
    ),
    CANDLE(
        displayName = "Candle",
        previewBg = Color(0xFF090707),
        previewText = Color(0xFFC28246),
        canvasBackground = Color(0xFF090605),
        isProFeature = true,
        matrixValues = floatArrayOf(
            -0.76f, 0f, 0f, 0f, 205f,
            0f, -0.52f, 0f, 0f, 140f,
            0f, 0f, -0.28f, 0f, 78f,
            0f, 0f, 0f, 1f, 0f
        )
    );

    fun toColorMatrix(): ColorMatrix? = matrixValues?.let { ColorMatrix(it) }
}
