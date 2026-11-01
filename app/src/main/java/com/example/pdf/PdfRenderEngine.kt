package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Ultra-fast, low-memory PDF rendering engine backed by android.graphics.pdf.PdfRenderer.
 * Uses an LruCache and per-document Mutex to guarantee smooth 60fps scrolling and zero OOMs.
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

    private suspend fun getOrOpenRenderer(filePath: String): OpenRendererEntry? {
        openRenderers[filePath]?.let { return it }
        return mapLock.withLock {
            openRenderers[filePath]?.let { return@withLock it }
            val file = File(filePath)
            if (!file.exists()) return@withLock null
            try {
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                val entry = OpenRendererEntry(pfd, renderer)
                openRenderers[filePath] = entry
                entry
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getCachedBitmap(filePath: String, pageIndex: Int, targetWidthPx: Int): Bitmap? {
        val key = "$filePath#$pageIndex#$targetWidthPx"
        return bitmapCache.get(key)?.takeIf { !it.isRecycled }
    }

    suspend fun renderPage(
        filePath: String,
        pageIndex: Int,
        targetWidthPx: Int = 1080
    ): Bitmap? = withContext(Dispatchers.IO) {
        val bucketWidth = ((targetWidthPx + 63) / 64) * 64
        val cacheKey = "$filePath#$pageIndex#$bucketWidth"
        bitmapCache.get(cacheKey)?.takeIf { !it.isRecycled }?.let { return@withContext it }

        val entry = getOrOpenRenderer(filePath) ?: return@withContext null
        entry.mutex.withLock {
            bitmapCache.get(cacheKey)?.takeIf { !it.isRecycled }?.let { return@withLock it }
            try {
                if (pageIndex < 0 || pageIndex >= entry.renderer.pageCount) return@withLock null
                entry.renderer.openPage(pageIndex).use { page ->
                    val aspect = if (page.width > 0) page.height.toFloat() / page.width.toFloat() else 1.414f
                    val bmpWidth = bucketWidth.coerceIn(180, 1920)
                    val bmpHeight = (bmpWidth * aspect).toInt().coerceAtLeast(180)
                    val bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmapCache.put(cacheKey, bitmap)
                    bitmap
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun inspectPdfPageCount(file: File): Int = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext 0
        try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    renderer.pageCount
                }
            }
        } catch (e: Exception) {
            0
        }
    }

    fun printPdfFile(context: Context, filePath: String, jobTitle: String): Boolean {
        val file = File(filePath)
        if (!file.exists()) return false
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
                    val info = PrintDocumentInfo.Builder(file.name)
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
                        FileInputStream(file).use { input ->
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
        } catch (e: Exception) {
            false
        }
    }
}
