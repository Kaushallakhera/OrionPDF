package com.example.pdf

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.LruCache
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

data class NormalizedRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val centerX: Float get() = (left + right) * 0.5f
    val centerY: Float get() = (top + bottom) * 0.5f
    val width: Float get() = (right - left).coerceAtLeast(0f)
    val height: Float get() = (bottom - top).coerceAtLeast(0f)
}

data class PdfWordBox(
    val text: String,
    val rect: NormalizedRect,
    val lineIndex: Int,
    val wordIndex: Int
)

data class PdfPageTextLayout(
    val pageIndex: Int,
    val words: List<PdfWordBox>,
    val fullText: String
)

data class PdfSearchHighlightMatch(
    val pageIndex: Int,
    val rects: List<NormalizedRect>,
    val snippet: String
)

data class DocumentOutlineItem(
    val title: String,
    val subtitle: String,
    val pageIndex: Int
)

/**
 * Full-featured offline PDF text layout, spatial selection, and search highlight engine.
 * Powered by Apache PDFBox-Android (universal Unicode, Hindi/Devanagari, CID fonts, ToUnicode CMaps,
 * and exact glyph/word bounding boxes across Android 7-16) plus native Android 15+ PDFium searchText.
 */
object PdfTextExtractor {

    private val pdfBoxInitialized = AtomicBoolean(false)
    private val pageLayoutCache = LruCache<String, PdfPageTextLayout>(64)
    private val documentPagesCache = LruCache<String, List<String>>(10)

    private fun ensurePdfBoxInitialized(context: Context) {
        if (pdfBoxInitialized.compareAndSet(false, true)) {
            runCatching { PDFBoxResourceLoader.init(context.applicationContext) }
        }
    }

    private fun openPdfInputStream(context: Context, filePathOrUri: String): InputStream? {
        return try {
            if (filePathOrUri.startsWith("content://")) {
                context.contentResolver.openInputStream(Uri.parse(filePathOrUri))
            } else {
                val file = File(filePathOrUri)
                if (file.exists() && file.canRead()) file.inputStream() else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun loadPdDocument(context: Context, filePathOrUri: String): PDDocument? {
        ensurePdfBoxInitialized(context)
        return try {
            if (!filePathOrUri.startsWith("content://")) {
                val file = File(filePathOrUri)
                if (file.exists() && file.canRead()) {
                    return PDDocument.load(file)
                }
            }
            openPdfInputStream(context, filePathOrUri)?.use { input ->
                PDDocument.load(input)
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Extracts exact word-level bounding boxes and reading order for a single PDF page.
     * Cached in memory so subsequent long-presses and handle drags respond in 0ms.
     */
    suspend fun extractPageLayout(
        context: Context,
        filePathOrUri: String,
        pageIndex: Int
    ): PdfPageTextLayout = withContext(Dispatchers.IO) {
        val cacheKey = "$filePathOrUri#$pageIndex"
        pageLayoutCache.get(cacheKey)?.let { return@withContext it }

        val layout = loadPdDocument(context, filePathOrUri)?.use { pdDoc ->
            extractSinglePageLayoutFromDoc(pdDoc, pageIndex)
        } ?: PdfPageTextLayout(pageIndex = pageIndex, words = emptyList(), fullText = "")

        pageLayoutCache.put(cacheKey, layout)
        layout
    }

    private fun extractSinglePageLayoutFromDoc(pdDoc: PDDocument, pageIndex: Int): PdfPageTextLayout {
        if (pageIndex < 0 || pageIndex >= pdDoc.numberOfPages) {
            return PdfPageTextLayout(pageIndex, emptyList(), "")
        }
        return try {
            val stripper = WordBoundingBoxStripper(pageIndex)
            stripper.sortByPosition = true
            stripper.startPage = pageIndex + 1
            stripper.endPage = pageIndex + 1
            val rawText = stripper.getText(pdDoc).orEmpty().trim()
            val words = stripper.buildWords()
            val fullText = if (rawText.isNotBlank()) {
                rawText
            } else {
                words.joinToString(" ") { it.text }
            }
            PdfPageTextLayout(
                pageIndex = pageIndex,
                words = words,
                fullText = fullText
            )
        } catch (_: Exception) {
            PdfPageTextLayout(pageIndex, emptyList(), "")
        }
    }

    /**
     * Searches across all pages of the PDF and returns exact normalized [0..1] highlight rectangles
     * for EVERY individual occurrence so the reader can highlight matches right on the PDF page.
     */
    suspend fun searchDocumentWithBounds(
        context: Context,
        filePathOrUri: String,
        query: String,
        expectedPageCount: Int
    ): List<PdfSearchHighlightMatch> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()
        val safeCount = expectedPageCount.coerceAtLeast(1)

        // 1. On Android 15+ (API 35+), try native PDFium searchText first for exact C++ bounds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && safeCount <= 300) {
            val nativeMatches = mutableListOf<PdfSearchHighlightMatch>()
            var nativeAvailable = false
            for (pageIdx in 0 until safeCount) {
                currentCoroutineContext().ensureActive()
                val pageRects = PdfRenderEngine.searchNativePageMatches(context, filePathOrUri, pageIdx, trimmed)
                if (pageRects != null) {
                    nativeAvailable = true
                    pageRects.forEach { rects ->
                        nativeMatches.add(
                            PdfSearchHighlightMatch(
                                pageIndex = pageIdx,
                                rects = rects,
                                snippet = trimmed
                            )
                        )
                    }
                }
            }
            if (nativeAvailable && nativeMatches.isNotEmpty()) {
                return@withContext nativeMatches
            }
        }

        // 2. Universal PDFBox-Android spatial search (Android 7-16, Hindi/English/Unicode + exact word rects)
        val results = mutableListOf<PdfSearchHighlightMatch>()
        loadPdDocument(context, filePathOrUri)?.use { pdDoc ->
            val totalPages = pdDoc.numberOfPages.coerceAtMost(400)
            for (pageIdx in 0 until totalPages) {
                currentCoroutineContext().ensureActive()
                val cacheKey = "$filePathOrUri#$pageIdx"
                val layout = pageLayoutCache.get(cacheKey) ?: extractSinglePageLayoutFromDoc(pdDoc, pageIdx).also {
                    pageLayoutCache.put(cacheKey, it)
                }
                if (layout.words.isEmpty()) continue
                results.addAll(findQueryMatchesInLayout(layout, trimmed))
            }
        }
        results
    }

    private fun findQueryMatchesInLayout(
        layout: PdfPageTextLayout,
        query: String
    ): List<PdfSearchHighlightMatch> {
        val words = layout.words
        if (words.isEmpty()) return emptyList()

        // Build a continuous searchable string while recording each word's [startChar, endChar) span
        val sb = StringBuilder()
        val wordSpans = ArrayList<IntRange>(words.size)
        for (i in words.indices) {
            if (i > 0) sb.append(' ')
            val start = sb.length
            sb.append(words[i].text)
            val end = sb.length
            wordSpans.add(start until end)
        }

        val combined = sb.toString()
        val lowerCombined = combined.lowercase()
        val lowerQuery = query.lowercase()
        val matches = mutableListOf<PdfSearchHighlightMatch>()

        var searchFrom = 0
        while (searchFrom < lowerCombined.length) {
            val foundStart = lowerCombined.indexOf(lowerQuery, searchFrom)
            if (foundStart < 0) break
            val foundEnd = foundStart + lowerQuery.length

            // Collect all words intersecting [foundStart, foundEnd)
            val matchedWordRects = mutableListOf<Pair<Int, NormalizedRect>>()
            for (wIdx in words.indices) {
                val span = wordSpans[wIdx]
                if (span.last < foundStart) continue
                if (span.first >= foundEnd) break

                val word = words[wIdx]
                val wordLen = word.text.length.coerceAtLeast(1)
                val overlapStart = (foundStart - span.first).coerceIn(0, wordLen)
                val overlapEnd = (foundEnd - span.first).coerceIn(overlapStart, wordLen)

                // Sub-word proportional highlight rect for razor-sharp partial-word matching
                val fracStart = overlapStart.toFloat() / wordLen.toFloat()
                val fracEnd = overlapEnd.toFloat() / wordLen.toFloat()
                val wRect = word.rect
                val subLeft = (wRect.left + wRect.width * fracStart).coerceIn(0f, 1f)
                val subRight = (wRect.left + wRect.width * fracEnd).coerceIn(subLeft + 0.002f, 1f)
                matchedWordRects.add(word.lineIndex to NormalizedRect(subLeft, wRect.top, subRight, wRect.bottom))
            }

            if (matchedWordRects.isNotEmpty()) {
                val mergedRects = mergeRectsByLine(matchedWordRects)
                val snipStart = (foundStart - 24).coerceAtLeast(0)
                val snipEnd = (foundEnd + 36).coerceAtMost(combined.length)
                val snippet = "…${combined.substring(snipStart, snipEnd).trim()}…"
                matches.add(
                    PdfSearchHighlightMatch(
                        pageIndex = layout.pageIndex,
                        rects = mergedRects,
                        snippet = snippet
                    )
                )
            }

            searchFrom = (foundEnd).coerceAtLeast(foundStart + 1)
        }

        return matches
    }

    /**
     * Hit-tests a normalized (0..1, 0..1) touch point against the page's word boxes.
     * Used for both initial long-press word selection and smooth drag-handle resizing.
     */
    fun findWordIndexAt(
        layout: PdfPageTextLayout,
        nx: Float,
        ny: Float,
        allowLooseNearest: Boolean = false
    ): Int? {
        val words = layout.words
        if (words.isEmpty()) return null

        // 1. Exact (with generous touch padding) bounding-box hit
        val padX = 0.012f
        val padY = 0.014f
        words.forEachIndexed { idx, w ->
            val r = w.rect
            if (nx in (r.left - padX)..(r.right + padX) && ny in (r.top - padY)..(r.bottom + padY)) {
                return idx
            }
        }

        // 2. Nearest word on the closest visual line
        val sameLineCandidate = words
            .withIndex()
            .filter { (_, w) -> abs(w.rect.centerY - ny) <= 0.038f }
            .minByOrNull { (_, w) ->
                val dx = when {
                    nx < w.rect.left -> w.rect.left - nx
                    nx > w.rect.right -> nx - w.rect.right
                    else -> 0f
                }
                dx
            }
        if (sameLineCandidate != null) {
            return sameLineCandidate.index
        }

        // 3. Nearest word within reasonable distance (always used when dragging handles)
        var bestIdx: Int? = null
        var bestDist = Float.MAX_VALUE
        words.forEachIndexed { idx, w ->
            val dx = when {
                nx < w.rect.left -> w.rect.left - nx
                nx > w.rect.right -> nx - w.rect.right
                else -> 0f
            }
            val dy = when {
                ny < w.rect.top -> w.rect.top - ny
                ny > w.rect.bottom -> ny - w.rect.bottom
                else -> 0f
            }
            val dist = hypot(dx, dy * 1.4f)
            if (dist < bestDist) {
                bestDist = dist
                bestIdx = idx
            }
        }
        return if (allowLooseNearest || bestDist <= 0.14f) bestIdx else null
    }

    /**
     * Merges selected word rectangles on the same visual line into smooth continuous highlight bars.
     */
    fun buildSelectionRects(
        layout: PdfPageTextLayout,
        startWordIdx: Int,
        endWordIdx: Int
    ): List<NormalizedRect> {
        val words = layout.words
        if (words.isEmpty()) return emptyList()
        val lo = min(startWordIdx, endWordIdx).coerceIn(0, words.lastIndex)
        val hi = max(startWordIdx, endWordIdx).coerceIn(0, words.lastIndex)
        val pairs = (lo..hi).map { idx -> words[idx].lineIndex to words[idx].rect }
        return mergeRectsByLine(pairs)
    }

    /**
     * Extracts readable plain text from the selected word range, preserving line breaks.
     */
    fun buildSelectedText(
        layout: PdfPageTextLayout,
        startWordIdx: Int,
        endWordIdx: Int
    ): String {
        val words = layout.words
        if (words.isEmpty()) return ""
        val lo = min(startWordIdx, endWordIdx).coerceIn(0, words.lastIndex)
        val hi = max(startWordIdx, endWordIdx).coerceIn(0, words.lastIndex)
        val sb = StringBuilder()
        var prevLine = -1
        for (i in lo..hi) {
            val w = words[i]
            if (sb.isNotEmpty()) {
                sb.append(if (w.lineIndex != prevLine) '\n' else ' ')
            }
            sb.append(w.text)
            prevLine = w.lineIndex
        }
        return sb.toString().trim()
    }

    private fun mergeRectsByLine(lineRects: List<Pair<Int, NormalizedRect>>): List<NormalizedRect> {
        if (lineRects.isEmpty()) return emptyList()
        val result = mutableListOf<NormalizedRect>()
        var currentLine = lineRects.first().first
        var cur = lineRects.first().second

        for (i in 1 until lineRects.size) {
            val (line, r) = lineRects[i]
            val verticallyCompatible = line == currentLine ||
                (abs(r.centerY - cur.centerY) <= max(cur.height, r.height) * 0.55f && r.left >= cur.left - 0.02f)
            if (verticallyCompatible && (r.left - cur.right) <= 0.08f) {
                cur = NormalizedRect(
                    left = min(cur.left, r.left),
                    top = min(cur.top, r.top),
                    right = max(cur.right, r.right),
                    bottom = max(cur.bottom, r.bottom)
                )
            } else {
                result.add(cur)
                currentLine = line
                cur = r
            }
        }
        result.add(cur)
        return result
    }

    suspend fun extractAllPages(
        context: Context,
        filePathOrUri: String,
        expectedPageCount: Int
    ): List<String> = withContext(Dispatchers.IO) {
        val safeCount = expectedPageCount.coerceAtLeast(1)
        val cacheKey = "$filePathOrUri#$safeCount"
        documentPagesCache.get(cacheKey)?.let { return@withContext it }

        // Prefer PDFBox-Android (accurate Unicode across all API levels)
        val pdfBoxPages = loadPdDocument(context, filePathOrUri)?.use { pdDoc ->
            val count = pdDoc.numberOfPages.coerceAtMost(safeCount)
            val stripper = PDFTextStripper().apply { sortByPosition = true }
            List(safeCount) { idx ->
                if (idx < count) {
                    runCatching {
                        stripper.startPage = idx + 1
                        stripper.endPage = idx + 1
                        stripper.getText(pdDoc).orEmpty().trim()
                    }.getOrDefault("")
                } else ""
            }
        }
        if (pdfBoxPages != null && pdfBoxPages.any { it.isNotBlank() }) {
            documentPagesCache.put(cacheKey, pdfBoxPages)
            return@withContext pdfBoxPages
        }

        // Fallback to native Android 15+ PDFium text contents
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && safeCount <= 250) {
            val nativePages = MutableList(safeCount) { "" }
            var foundAny = false
            for (idx in 0 until safeCount) {
                val txt = PdfRenderEngine.extractNativePageText(context, filePathOrUri, idx)
                if (!txt.isNullOrBlank()) {
                    nativePages[idx] = txt
                    foundAny = true
                }
            }
            if (foundAny) {
                documentPagesCache.put(cacheKey, nativePages)
                return@withContext nativePages
            }
        }

        List(safeCount) { "" }
    }

    suspend fun extractPageText(
        context: Context,
        filePathOrUri: String,
        pageIndex: Int,
        expectedPageCount: Int
    ): String {
        val layout = extractPageLayout(context, filePathOrUri, pageIndex)
        if (layout.fullText.isNotBlank()) {
            return layout.fullText
        }
        val nativeText = PdfRenderEngine.extractNativePageText(context, filePathOrUri, pageIndex)
        if (!nativeText.isNullOrBlank()) {
            return nativeText
        }
        return "Page ${pageIndex + 1} of $expectedPageCount.\n\nNo selectable text layer was found on this page (it may be a scanned image or vector-only page)."
    }

    suspend fun buildDocumentOutline(
        context: Context,
        filePathOrUri: String,
        docTitle: String,
        pageCount: Int
    ): List<DocumentOutlineItem> = withContext(Dispatchers.IO) {
        val pages = extractAllPages(context, filePathOrUri, pageCount)
        (0 until pageCount.coerceAtLeast(1)).map { idx ->
            val pageText = pages.getOrNull(idx).orEmpty()
            val lines = pageText.lines().map { it.trim() }.filter { it.length >= 3 }
            val heading = lines.firstOrNull()?.take(54) ?: "Page ${idx + 1}"
            val subtitle = lines.drop(1).firstOrNull()?.take(72)
                ?: if (lines.isEmpty()) docTitle else "Page ${idx + 1} of $pageCount"
            DocumentOutlineItem(
                title = heading,
                subtitle = subtitle,
                pageIndex = idx
            )
        }
    }

    fun clearCacheFor(filePathOrUri: String) {
        documentPagesCache.snapshot().keys
            .filter { it.startsWith("$filePathOrUri#") }
            .forEach { documentPagesCache.remove(it) }
        pageLayoutCache.snapshot().keys
            .filter { it.startsWith("$filePathOrUri#") }
            .forEach { pageLayoutCache.remove(it) }
    }

    /**
     * Custom PDFTextStripper subclass that captures exact glyph coordinates (TextPosition)
     * and groups them into clean word bounding boxes normalized to [0..1] page coordinates.
     */
    private class WordBoundingBoxStripper(private val targetPageIndex: Int) : PDFTextStripper() {

        private val collectedWords = mutableListOf<PdfWordBox>()
        private var currentLineIdx = 0
        private var lastBaselineY = Float.NaN

        override fun writeString(text: String?, textPositions: MutableList<TextPosition>?) {
            super.writeString(text, textPositions)
            if (textPositions.isNullOrEmpty()) return

            val wordBuf = StringBuilder()
            var minX = Float.MAX_VALUE
            var minTop = Float.MAX_VALUE
            var maxRight = 0f
            var maxBottom = 0f
            var pageW = 1f
            var pageH = 1f
            var prevRight = Float.NaN
            var prevBaseline = Float.NaN

            fun flushWord() {
                val wordText = wordBuf.toString().trim()
                if (wordText.isNotEmpty() && minX < maxRight && minTop < maxBottom && pageW > 0f && pageH > 0f) {
                    val baselineRef = if (!prevBaseline.isNaN()) prevBaseline else maxBottom
                    if (!lastBaselineY.isNaN() && abs(baselineRef - lastBaselineY) > (maxBottom - minTop) * 0.65f) {
                        currentLineIdx++
                    }
                    lastBaselineY = baselineRef

                    val normRect = NormalizedRect(
                        left = (minX / pageW).coerceIn(0f, 1f),
                        top = (minTop / pageH).coerceIn(0f, 1f),
                        right = (maxRight / pageW).coerceIn(0f, 1f),
                        bottom = (maxBottom / pageH).coerceIn(0f, 1f)
                    )
                    if (normRect.width > 0.001f && normRect.height > 0.001f) {
                        collectedWords.add(
                            PdfWordBox(
                                text = wordText,
                                rect = normRect,
                                lineIndex = currentLineIdx,
                                wordIndex = collectedWords.size
                            )
                        )
                    }
                }
                wordBuf.setLength(0)
                minX = Float.MAX_VALUE
                minTop = Float.MAX_VALUE
                maxRight = 0f
                maxBottom = 0f
            }

            for (tp in textPositions) {
                val unicode = tp.unicode
                if (unicode.isNullOrEmpty()) continue

                if (unicode.all { it.isWhitespace() }) {
                    flushWord()
                    prevRight = Float.NaN
                    continue
                }

                val tpPageW = tp.pageWidth.takeIf { it > 0f } ?: 595f
                val tpPageH = tp.pageHeight.takeIf { it > 0f } ?: 842f
                pageW = tpPageW
                pageH = tpPageH

                val x = tp.xDirAdj
                val w = tp.widthDirAdj.coerceAtLeast(tp.fontSizeInPt * 0.2f).coerceAtLeast(1f)
                val baseline = tp.yDirAdj
                val glyphH = maxOf(tp.heightDir, tp.fontSizeInPt * 0.78f, 6f)
                val top = baseline - glyphH * 1.06f
                val bottom = baseline + glyphH * 0.28f

                // Split word if PDF kerning jumped horizontally by a word-space or moved to another line
                if (wordBuf.isNotEmpty() && !prevRight.isNaN() && !prevBaseline.isNaN()) {
                    val spaceThreshold = maxOf(tp.widthOfSpace * 0.45f, tp.fontSizeInPt * 0.26f, 2.2f)
                    val horizontalGap = x - prevRight
                    val verticalJump = abs(baseline - prevBaseline)
                    if (horizontalGap > spaceThreshold || horizontalGap < -glyphH * 1.5f || verticalJump > glyphH * 0.65f) {
                        flushWord()
                    }
                }

                wordBuf.append(unicode)
                minX = min(minX, x)
                minTop = min(minTop, top)
                maxRight = max(maxRight, x + w)
                maxBottom = max(maxBottom, bottom)
                prevRight = x + w
                prevBaseline = baseline
            }

            flushWord()
        }

        override fun writeLineSeparator() {
            super.writeLineSeparator()
            currentLineIdx++
        }

        fun buildWords(): List<PdfWordBox> = collectedWords.toList()
    }
}
