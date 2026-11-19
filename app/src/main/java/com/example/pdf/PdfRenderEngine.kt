package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.util.LruCache
import com.example.data.AnnotationEntity
import com.example.data.AnnotationTool
import com.example.data.parseNormalizedPoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Ultra-fast, low-memory PDF rendering & export engine backed by android.graphics.pdf.PdfRenderer
 * and android.graphics.pdf.PdfDocument.
 * Supports both direct filesystem paths and Android Storage Access Framework (content://) URIs.
 */
object PdfRenderEngine {

    private val maxCacheKb: Int = (Runtime.getRuntime().maxMemory() / 1024 / 6).toInt().coerceAtLeast(4096)

    private val bitmapCache = object : LruCache<String, Bitmap>(maxCacheKb) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    private data class OpenRendererEntry(
        val descriptor: ParcelFileDescriptor,
        val renderer: PdfRenderer,
        val mutex: Mutex = Mutex()
    )

    private val openRenderers = ConcurrentHashMap<String, OpenRendererEntry>()
    private val mapLock = Mutex()

    private fun openDescriptor(context: Context, pathOrUri: String): ParcelFileDescriptor? {
        return try {
            if (pathOrUri.startsWith("content://")) {
                context.contentResolver.openFileDescriptor(Uri.parse(pathOrUri), "r")
            } else {
                val file = File(pathOrUri)
                if (!file.exists() || !file.canRead()) null
                else ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun getOrOpenRenderer(context: Context, pathOrUri: String): OpenRendererEntry? {
        openRenderers[pathOrUri]?.let { return it }
        return mapLock.withLock {
            openRenderers[pathOrUri]?.let { return@withLock it }
            val pfd = openDescriptor(context, pathOrUri) ?: return@withLock null
            try {
                val renderer = PdfRenderer(pfd)
                val entry = OpenRendererEntry(pfd, renderer)
                openRenderers[pathOrUri] = entry
                entry
            } catch (_: Exception) {
                runCatching { pfd.close() }
                null
            }
        }
    }

    suspend fun closeRenderer(pathOrUri: String) {
        mapLock.withLock {
            openRenderers.remove(pathOrUri)?.let { entry ->
                runCatching { entry.renderer.close() }
                runCatching { entry.descriptor.close() }
            }
        }
        val snapshot = bitmapCache.snapshot()
        snapshot.keys.filter { it.startsWith("$pathOrUri#") }.forEach {
            bitmapCache.remove(it)
        }
        PdfTextExtractor.clearCacheFor(pathOrUri)
    }

    fun getCachedBitmap(pathOrUri: String, pageIndex: Int, targetWidthPx: Int): Bitmap? {
        val bucketWidth = ((targetWidthPx + 63) / 64) * 64
        val key = "$pathOrUri#$pageIndex#$bucketWidth"
        return bitmapCache.get(key)?.takeIf { !it.isRecycled }
    }

    /**
     * Returns the exact bucket cache hit if present, or any already-cached resolution for the same page
     * so zoom re-renders stay flicker-free while the sharper high-DPI bitmap renders asynchronously.
     */
    fun getBestCachedBitmap(pathOrUri: String, pageIndex: Int, preferredWidthPx: Int): Bitmap? {
        getCachedBitmap(pathOrUri, pageIndex, preferredWidthPx)?.let { return it }
        val prefix = "$pathOrUri#$pageIndex#"
        val snapshot = bitmapCache.snapshot()
        return snapshot.entries
            .filter { it.key.startsWith(prefix) && !it.value.isRecycled }
            .maxByOrNull { it.value.width }
            ?.value
    }

    suspend fun renderPage(
        context: Context,
        pathOrUri: String,
        pageIndex: Int,
        targetWidthPx: Int = 1080
    ): Bitmap? = withContext(Dispatchers.IO) {
        val bucketWidth = ((targetWidthPx + 63) / 64) * 64
        val cacheKey = "$pathOrUri#$pageIndex#$bucketWidth"
        bitmapCache.get(cacheKey)?.takeIf { !it.isRecycled }?.let { return@withContext it }

        val entry = getOrOpenRenderer(context, pathOrUri) ?: return@withContext null
        entry.mutex.withLock {
            bitmapCache.get(cacheKey)?.takeIf { !it.isRecycled }?.let { return@withLock it }
            try {
                if (pageIndex < 0 || pageIndex >= entry.renderer.pageCount) return@withLock null
                entry.renderer.openPage(pageIndex).use { page ->
                    val aspect = if (page.width > 0) page.height.toFloat() / page.width.toFloat() else 1.414f
                    val bmpWidth = bucketWidth.coerceIn(180, 2560)
                    val bmpHeight = (bmpWidth * aspect).toInt().coerceIn(180, 4096)
                    val bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmapCache.put(cacheKey, bitmap)
                    bitmap
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun prefetchPages(
        context: Context,
        pathOrUri: String,
        centerPage: Int,
        totalPages: Int,
        targetWidthPx: Int = 1152
    ) = withContext(Dispatchers.IO) {
        if (totalPages <= 1) return@withContext
        val candidates = listOf(centerPage + 1, centerPage - 1, centerPage + 2)
            .filter { it in 0 until totalPages }
        for (idx in candidates) {
            if (getCachedBitmap(pathOrUri, idx, targetWidthPx) == null) {
                renderPage(context, pathOrUri, idx, targetWidthPx)
            }
        }
    }

    /**
     * Uses Android 15+ (API 35+) native PdfRenderer.Page.getTextContents() backed directly by PDFium
     * for exact selectable text extraction (including Unicode, Hindi/Devanagari, and complex font CMaps).
     * Returns null on API < 35 or if no text layer exists on the page.
     */
    suspend fun extractNativePageText(
        context: Context,
        pathOrUri: String,
        pageIndex: Int
    ): String? = withContext(Dispatchers.IO) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return@withContext null
        }
        val entry = getOrOpenRenderer(context, pathOrUri) ?: return@withContext null
        entry.mutex.withLock {
            try {
                if (pageIndex < 0 || pageIndex >= entry.renderer.pageCount) return@withLock null
                entry.renderer.openPage(pageIndex).use { page ->
                    val contents = page.textContents
                    if (contents.isEmpty()) return@withLock null
                    val joined = contents
                        .mapNotNull { it.text?.trim() }
                        .filter { it.isNotEmpty() }
                        .joinToString("\n")
                        .trim()
                    joined.ifEmpty { null }
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Uses Android 15+ (API 35+) native PDFium PdfRenderer.Page.searchText(query) to return exact
     * normalized [0..1] highlight rectangles for every search match on a page.
     * Returns null on API < 35 or if native search is unavailable.
     */
    suspend fun searchNativePageMatches(
        context: Context,
        pathOrUri: String,
        pageIndex: Int,
        query: String
    ): List<List<NormalizedRect>>? = withContext(Dispatchers.IO) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return@withContext null
        }
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()
        val entry = getOrOpenRenderer(context, pathOrUri) ?: return@withContext null
        entry.mutex.withLock {
            try {
                if (pageIndex < 0 || pageIndex >= entry.renderer.pageCount) return@withContext null
                entry.renderer.openPage(pageIndex).use { page ->
                    val pw = page.width.toFloat().coerceAtLeast(1f)
                    val ph = page.height.toFloat().coerceAtLeast(1f)
                    val matches = page.searchText(trimmed)
                    matches.mapNotNull { match ->
                        val rects = match.bounds.mapNotNull { r ->
                            val left = (r.left / pw).coerceIn(0f, 1f)
                            val top = (r.top / ph).coerceIn(0f, 1f)
                            val right = (r.right / pw).coerceIn(0f, 1f)
                            val bottom = (r.bottom / ph).coerceIn(0f, 1f)
                            if (right > left && bottom > top) {
                                NormalizedRect(left, top, right, bottom)
                            } else null
                        }
                        rects.takeIf { it.isNotEmpty() }
                    }
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    fun inspectPdfPageCountSync(context: Context, pathOrUri: String): Int {
        val pfd = openDescriptor(context, pathOrUri) ?: return 0
        return try {
            pfd.use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    renderer.pageCount
                }
            }
        } catch (_: Exception) {
            0
        }
    }

    suspend fun inspectPdfPageCount(context: Context, pathOrUri: String): Int = withContext(Dispatchers.IO) {
        inspectPdfPageCountSync(context, pathOrUri)
    }

    /**
     * Flattens all saved strokes, highlights, underlines, and sticky note callouts onto the source PDF pages
     * and writes a brand-new standard PDF file either to a user-chosen SAF destination URI or Downloads/OrionPDF.
     */
    suspend fun exportAnnotatedPdf(
        context: Context,
        sourcePathOrUri: String,
        docTitle: String,
        annotations: List<AnnotationEntity>,
        destinationUri: Uri? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val sourcePfd = openDescriptor(context, sourcePathOrUri)
            ?: return@withContext Result.failure(IllegalStateException("Could not open source PDF"))

        val pdfDocument = PdfDocument()
        try {
            sourcePfd.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val annotationsByPage = annotations.groupBy { it.pageIndex }
                    val totalPages = renderer.pageCount

                    for (pageIndex in 0 until totalPages) {
                        renderer.openPage(pageIndex).use { srcPage ->
                            val pageW = srcPage.width.coerceAtLeast(300)
                            val pageH = srcPage.height.coerceAtLeast(400)

                            // Render source page at 2x resolution for crisp exported quality
                            val scale = 2
                            val renderW = (pageW * scale).coerceAtMost(2048)
                            val renderH = (renderW.toFloat() * pageH / pageW).toInt().coerceAtLeast(400)
                            val pageBitmap = Bitmap.createBitmap(renderW, renderH, Bitmap.Config.ARGB_8888)
                            pageBitmap.eraseColor(Color.WHITE)
                            srcPage.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, pageIndex + 1).create()
                            val outPage = pdfDocument.startPage(pageInfo)
                            val canvas = outPage.canvas

                            canvas.drawBitmap(
                                pageBitmap,
                                null,
                                RectF(0f, 0f, pageW.toFloat(), pageH.toFloat()),
                                Paint(Paint.FILTER_BITMAP_FLAG)
                            )
                            pageBitmap.recycle()

                            val pageAnns = annotationsByPage[pageIndex].orEmpty()
                            drawAnnotationsOnCanvas(canvas, pageW.toFloat(), pageH.toFloat(), pageAnns)

                            pdfDocument.finishPage(outPage)
                        }
                    }
                }
            }

            if (destinationUri != null) {
                val outStream: OutputStream = context.contentResolver.openOutputStream(destinationUri)
                    ?: return@withContext Result.failure(IllegalStateException("Cannot write to selected location"))
                outStream.use { pdfDocument.writeTo(it) }
                return@withContext Result.success(destinationUri.toString())
            } else {
                val safeName = docTitle.replace(Regex("[^a-zA-Z0-9._ -]"), "_").ifBlank { "Document" }
                val targetDir = try {
                    val dlDir = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "OrionPDF"
                    )
                    if (dlDir.exists() || dlDir.mkdirs()) dlDir else null
                } catch (_: Exception) {
                    null
                } ?: File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "OrionPDF").apply { mkdirs() }

                val outFile = File(targetDir, "${safeName}_annotated_${System.currentTimeMillis() % 10000}.pdf")
                FileOutputStream(outFile).use { pdfDocument.writeTo(it) }
                return@withContext Result.success(outFile.absolutePath)
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            runCatching { pdfDocument.close() }
        }
    }

    private fun drawAnnotationsOnCanvas(
        canvas: Canvas,
        w: Float,
        h: Float,
        annotations: List<AnnotationEntity>
    ) {
        val scaleFactor = (w / 412f).coerceIn(0.8f, 3.0f)

        annotations.forEach { ann ->
            val argb = ann.colorHex.toInt()
            if (ann.toolType == AnnotationTool.STICKY_NOTE.name) {
                val cx = ann.anchorX * w
                val cy = ann.anchorY * h

                val pinOuter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(31, 26, 26)
                    style = Paint.Style.FILL
                }
                val pinInner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = argb
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(cx, cy, 8f * scaleFactor, pinOuter)
                canvas.drawCircle(cx, cy, 6f * scaleFactor, pinInner)

                if (ann.noteText.isNotBlank()) {
                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.rgb(30, 24, 22)
                        textSize = 9.5f * scaleFactor
                    }
                    val noteLabel = ann.noteText.take(64)
                    val textWidth = textPaint.measureText(noteLabel)
                    val pad = 5f * scaleFactor
                    val boxLeft = (cx + 10f * scaleFactor).coerceAtMost((w - textWidth - pad * 2 - 8f).coerceAtLeast(8f))
                    val boxTop = (cy - 10f * scaleFactor).coerceAtLeast(8f)
                    val boxRect = RectF(boxLeft, boxTop, boxLeft + textWidth + pad * 2, boxTop + 16f * scaleFactor)

                    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.argb(235, 255, 248, 220)
                        style = Paint.Style.FILL
                    }
                    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = argb
                        style = Paint.Style.STROKE
                        strokeWidth = 1.2f * scaleFactor
                    }
                    canvas.drawRoundRect(boxRect, 4f * scaleFactor, 4f * scaleFactor, bgPaint)
                    canvas.drawRoundRect(boxRect, 4f * scaleFactor, 4f * scaleFactor, borderPaint)
                    canvas.drawText(noteLabel, boxRect.left + pad, boxRect.bottom - 4.5f * scaleFactor, textPaint)
                }
            } else {
                val pts = ann.parseNormalizedPoints()
                if (pts.size >= 2) {
                    val isHighlight = ann.toolType == AnnotationTool.HIGHLIGHTER.name
                    val isMarker = ann.toolType == AnnotationTool.MARKER.name
                    val alphaInt = when {
                        isHighlight -> 96
                        isMarker -> 175
                        else -> 242
                    }
                    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.argb(alphaInt, Color.red(argb), Color.green(argb), Color.blue(argb))
                        style = Paint.Style.STROKE
                        strokeWidth = (ann.strokeWidth * scaleFactor).coerceAtLeast(1.5f)
                        strokeCap = if (isHighlight) Paint.Cap.SQUARE else Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                    }
                    val path = Path().apply {
                        moveTo(pts.first().x * w, pts.first().y * h)
                        for (i in 1 until pts.size) {
                            lineTo(pts[i].x * w, pts[i].y * h)
                        }
                    }
                    canvas.drawPath(path, strokePaint)
                }
            }
        }
    }

    fun printPdfFile(context: Context, pathOrUri: String, jobTitle: String): Boolean {
        val canOpen = if (pathOrUri.startsWith("content://")) {
            true
        } else {
            File(pathOrUri).let { it.exists() && it.canRead() }
        }
        if (!canOpen) return false

        return try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return false
            val adapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val safeFileName = if (jobTitle.endsWith(".pdf", ignoreCase = true)) jobTitle else "$jobTitle.pdf"
                    val info = PrintDocumentInfo.Builder(safeFileName)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    if (destination == null) {
                        callback?.onWriteFailed("Destination unavailable")
                        return
                    }
                    try {
                        val inputStream: InputStream? = if (pathOrUri.startsWith("content://")) {
                            context.contentResolver.openInputStream(Uri.parse(pathOrUri))
                        } else {
                            FileInputStream(File(pathOrUri))
                        }
                        if (inputStream == null) {
                            callback?.onWriteFailed("Source PDF unreadable")
                            return
                        }
                        inputStream.use { input ->
                            FileOutputStream(destination.fileDescriptor).use { output ->
                                input.copyTo(output)
                            }
                        }
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    }
                }
            }
            printManager.print(jobTitle, adapter, PrintAttributes.Builder().build())
            true
        } catch (_: Exception) {
            false
        }
    }
}
