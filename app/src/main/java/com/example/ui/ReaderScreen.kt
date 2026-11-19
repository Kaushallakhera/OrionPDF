package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Rect
import android.speech.tts.TextToSpeech
import android.view.ActionMode
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.AnnotationEntity
import com.example.data.AnnotationTool
import com.example.data.BookmarkEntity
import com.example.data.PdfDocumentEntity
import com.example.data.ReadingTheme
import com.example.data.parseNormalizedPoints
import com.example.pdf.NormalizedRect
import com.example.pdf.PdfRenderEngine
import com.example.pdf.PdfTextExtractor
import com.example.viewmodel.ActiveReaderState
import com.example.viewmodel.PdfSearchMatch
import com.example.viewmodel.ReaderSettingsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

private val AnnotationColorPalette = listOf(
    0xFFE53935L, // Crimson Red
    0xFFFDD835L, // Highlighter Yellow
    0xFF1E88E5L, // Cobalt Blue
    0xFF43A047L, // Emerald Green
    0xFFFB8C00L, // Warm Amber
    0xFF8E24AAL  // Royal Purple
)

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun ReaderScreen(
    readerState: ActiveReaderState,
    settingsState: ReaderSettingsState,
    bookmarks: List<BookmarkEntity>,
    annotations: List<AnnotationEntity>,
    onBack: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onToggleScrollLock: () -> Unit,
    onRotatePage: () -> Unit,
    onToggleLandscape: () -> Unit,
    onToggleReflow: () -> Unit,
    onAdjustReflowFont: (Float) -> Unit,
    onToggleAnnotationSidebar: () -> Unit,
    onSelectAnnotationTool: (AnnotationTool) -> Unit,
    onSelectAnnotationColor: (Long) -> Unit,
    onAddStrokeAnnotation: (Int, List<Offset>) -> Unit,
    onAddStickyNote: (Int, Float, Float, String) -> Unit,
    onHighlightSelection: (Int, List<NormalizedRect>) -> Unit,
    onDeleteAnnotation: (AnnotationEntity) -> Unit,
    onUndoAnnotation: () -> Unit,
    onRedoAnnotation: () -> Unit,
    onToggleBookmark: () -> Unit,
    onSaveQuote: (String, String, Int) -> Unit,
    onOpenContentsSheet: () -> Unit,
    onOpenReadingSettingsSheet: () -> Unit,
    onOpenDocumentMoreSheet: (PdfDocumentEntity) -> Unit,
    onTogglePdfSearch: (Boolean) -> Unit,
    onPdfSearchQueryChange: (String) -> Unit,
    onNavigateSearchMatch: (Boolean) -> Unit,
    onStopAutoScroll: () -> Unit,
    onAdjustAutoScrollSpeed: (Float) -> Unit,
    onStopReadAloud: () -> Unit,
    onAdjustReadAloudSpeed: (Float) -> Unit,
    onExportAnnotatedPdf: () -> Unit,
    onStatusMessage: (String) -> Unit
) {
    val doc = readerState.document ?: return
    val context = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var isChromeVisible by remember { mutableStateOf(true) }

    // True direct on-page PDF text selection state + native Android floating ActionMode
    var textSelection by remember(doc.filePath) { mutableStateOf<PdfPageSelectionState?>(null) }
    var activeActionMode by remember { mutableStateOf<ActionMode?>(null) }
    var suppressActionModeDestroyClear by remember { mutableStateOf(false) }
    var showFallbackSelectionToolbar by remember { mutableStateOf(false) }
    val pageBoundsInRoot = remember(doc.filePath) { mutableStateMapOf<Int, androidx.compose.ui.geometry.Rect>() }

    fun finishActionModeSilently() {
        val current = activeActionMode ?: return
        suppressActionModeDestroyClear = true
        activeActionMode = null
        runCatching { current.finish() }
        suppressActionModeDestroyClear = false
    }

    fun clearTextSelection() {
        finishActionModeSilently()
        showFallbackSelectionToolbar = false
        textSelection = null
    }

    DisposableEffect(Unit) {
        onDispose {
            finishActionModeSilently()
        }
    }

    // Clear selection when activating drawing tools or switching to reflow mode
    LaunchedEffect(readerState.activeTool, readerState.isReflowMode) {
        if (readerState.activeTool != AnnotationTool.NONE || readerState.isReflowMode) {
            clearTextSelection()
        }
    }

    // Launch/reposition native Android floating ActionMode toolbar directly over the blue selected text
    LaunchedEffect(
        textSelection?.pageIndex,
        textSelection?.loIdx,
        textSelection?.hiIdx,
        textSelection?.isDraggingHandle
    ) {
        val sel = textSelection
        if (sel == null || sel.isDraggingHandle) {
            finishActionModeSilently()
            showFallbackSelectionToolbar = false
            return@LaunchedEffect
        }

        val rects = sel.selectedRects
        if (rects.isEmpty()) {
            clearTextSelection()
            return@LaunchedEffect
        }

        val pageRect = pageBoundsInRoot[sel.pageIndex]
        val anchorRect = if (pageRect != null && pageRect.width > 1f && pageRect.height > 1f) {
            val minL = rects.minOf { it.left }
            val minT = rects.minOf { it.top }
            val maxR = rects.maxOf { it.right }
            val maxB = rects.maxOf { it.bottom }
            Rect(
                (pageRect.left + minL * pageRect.width).roundToInt(),
                (pageRect.top + minT * pageRect.height).roundToInt(),
                (pageRect.left + maxR * pageRect.width).roundToInt(),
                (pageRect.top + maxB * pageRect.height).roundToInt()
            )
        } else {
            Rect(120, 260, 420, 320)
        }

        finishActionModeSilently()
        val mode = launchNativeSelectionActionMode(
            view = view,
            context = context,
            anchorRectInView = anchorRect,
            getSelectedText = { textSelection?.selectedText.orEmpty() },
            onCopy = {
                val selected = textSelection?.selectedText.orEmpty()
                if (selected.isNotBlank()) {
                    copyTextToClipboard(context, doc.title, selected)
                    onStatusMessage("Copied to clipboard")
                }
            },
            onSelectAll = {
                textSelection?.let { current ->
                    if (current.layout.words.isNotEmpty()) {
                        textSelection = current.copy(
                            startWordIdx = 0,
                            endWordIdx = current.layout.words.lastIndex,
                            isDraggingHandle = false
                        )
                    }
                }
            },
            onShare = {
                val selected = textSelection?.selectedText.orEmpty()
                if (selected.isNotBlank()) {
                    shareSelectedPlainText(context, doc.title, selected)
                }
            },
            onHighlight = {
                textSelection?.let { current ->
                    onHighlightSelection(current.pageIndex, current.selectedRects)
                }
            },
            onQuote = {
                textSelection?.let { current ->
                    val selected = current.selectedText
                    if (selected.isNotBlank()) {
                        onSaveQuote(selected, "", current.pageIndex)
                    }
                }
            },
            onWebSearch = {
                val selected = textSelection?.selectedText.orEmpty()
                if (selected.isNotBlank()) {
                    searchSelectedTextOnWeb(context, selected)
                }
            },
            onDismissSelection = {
                if (!suppressActionModeDestroyClear) {
                    activeActionMode = null
                    showFallbackSelectionToolbar = false
                    textSelection = null
                }
            }
        )
        activeActionMode = mode
        showFallbackSelectionToolbar = (mode == null)
    }

    // Synchronize Android system bars (status & nav bars) with immersive chrome visibility
    DisposableEffect(isChromeVisible, view) {
        val window = context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (isChromeVisible) {
            controller?.show(WindowInsetsCompat.Type.systemBars())
        } else {
            controller?.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Ensure chrome stays visible when user opens search or activates an annotation tool
    LaunchedEffect(readerState.isPdfSearchOpen, readerState.isAnnotationSidebarExpanded) {
        if (readerState.isPdfSearchOpen || readerState.isAnnotationSidebarExpanded) {
            isChromeVisible = true
        }
    }

    BackHandler {
        if (textSelection != null) {
            clearTextSelection()
        } else if (readerState.isPdfSearchOpen) {
            onTogglePdfSearch(false)
        } else if (readerState.activeTool != AnnotationTool.NONE) {
            onSelectAnnotationTool(AnnotationTool.NONE)
        } else if (!isChromeVisible) {
            isChromeVisible = true
        } else {
            onBack()
        }
    }

    // Horizontal Pager is used for BOTH Horizontal Scroll mode AND Book Mode (3D page turn)
    val useHorizontalPager = settingsState.bookMode || settingsState.horizontalScroll

    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = readerState.currentPage)
    val pagerState = rememberPagerState(
        initialPage = readerState.currentPage,
        pageCount = { doc.pageCount.coerceAtLeast(1) }
    )

    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var renderWidthPx by remember { mutableIntStateOf(1152) }
    var showJumpPageDialog by remember { mutableStateOf(false) }
    var pendingStickyNoteTarget by remember { mutableStateOf<Triple<Int, Float, Float>?>(null) }
    var selectedStickyNoteToView by remember { mutableStateOf<AnnotationEntity?>(null) }
    var showAddQuoteDialog by remember { mutableStateOf(false) }

    val startDirectTextSelection: (Int, Float, Float) -> Unit = { targetPageIdx, nx, ny ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        coroutineScope.launch {
            val layout = PdfTextExtractor.extractPageLayout(context, doc.filePath, targetPageIdx)
            val hitIdx = PdfTextExtractor.findWordIndexAt(layout, nx, ny, allowLooseNearest = false)
            if (hitIdx != null) {
                textSelection = PdfPageSelectionState(
                    pageIndex = targetPageIdx,
                    layout = layout,
                    startWordIdx = hitIdx,
                    endWordIdx = hitIdx,
                    isDraggingHandle = false
                )
            } else if (layout.words.isEmpty()) {
                onStatusMessage("No selectable text layer on Page ${targetPageIdx + 1} (scanned image)")
            } else {
                val nearest = PdfTextExtractor.findWordIndexAt(layout, nx, ny, allowLooseNearest = true)
                if (nearest != null) {
                    textSelection = PdfPageSelectionState(
                        pageIndex = targetPageIdx,
                        layout = layout,
                        startWordIdx = nearest,
                        endWordIdx = nearest,
                        isDraggingHandle = false
                    )
                }
            }
        }
    }

    val updateSelectionHandle: (Boolean, Float, Float) -> Unit = { isStartHandle, nx, ny ->
        textSelection?.let { current ->
            val targetIdx = PdfTextExtractor.findWordIndexAt(current.layout, nx, ny, allowLooseNearest = true)
            if (targetIdx != null) {
                textSelection = if (isStartHandle) {
                    current.copy(startWordIdx = targetIdx)
                } else {
                    current.copy(endWordIdx = targetIdx)
                }
            }
        }
    }

    val activeSearchMatch = readerState.pdfSearchMatches.getOrNull(readerState.activeSearchMatchIndex)

    // Debounced high-DPI render width so zoomed text stays razor-sharp without janking pinch gestures
    LaunchedEffect(zoomScale) {
        val targetWidth = when {
            zoomScale > 2.15f -> 2496
            zoomScale > 1.35f -> 1792
            else -> 1152
        }
        if (targetWidth != renderWidthPx) {
            delay(160L)
            renderWidthPx = targetWidth
        }
    }

    // Background prefetch adjacent pages (-1, +1, +2) for instant zero-wait page turns
    LaunchedEffect(doc.filePath, readerState.currentPage, doc.pageCount) {
        PdfRenderEngine.prefetchPages(
            context = context,
            pathOrUri = doc.filePath,
            centerPage = readerState.currentPage,
            totalPages = doc.pageCount,
            targetWidthPx = 1152
        )
    }

    // Synchronize LazyList visible item -> ViewModel currentPage
    val visibleListPage by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex }
    }

    LaunchedEffect(visibleListPage, useHorizontalPager) {
        if (!useHorizontalPager) {
            onPageChanged(visibleListPage)
        }
    }

    LaunchedEffect(pagerState.currentPage, useHorizontalPager) {
        if (useHorizontalPager) {
            onPageChanged(pagerState.currentPage)
        }
    }

    // External page jump & mode-switch synchronization (Contents, Bookmarks, Search, or Horizontal/Book toggle)
    LaunchedEffect(readerState.currentPage, useHorizontalPager) {
        if (useHorizontalPager) {
            if (pagerState.currentPage != readerState.currentPage) {
                pagerState.scrollToPage(readerState.currentPage)
            }
        } else {
            if (lazyListState.firstVisibleItemIndex != readerState.currentPage) {
                lazyListState.scrollToItem(readerState.currentPage)
            }
        }
    }

    // Auto-scroll loop (vertical continuous mode)
    LaunchedEffect(readerState.isAutoScrollActive, readerState.autoScrollSpeed, useHorizontalPager) {
        if (readerState.isAutoScrollActive && !useHorizontalPager) {
            while (readerState.isAutoScrollActive) {
                lazyListState.scrollBy(readerState.autoScrollSpeed * 2.2f)
                delay(16L)
            }
        }
    }

    // Android TextToSpeech for Read Aloud
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.US
                isTtsReady = true
            }
        }
        ttsEngine = engine
        onDispose {
            engine?.stop()
            engine?.shutdown()
        }
    }

    LaunchedEffect(readerState.isReadAloudActive, readerState.currentPage, readerState.readAloudSpeed, isTtsReady) {
        val engine = ttsEngine ?: return@LaunchedEffect
        if (readerState.isReadAloudActive && isTtsReady) {
            engine.setSpeechRate(readerState.readAloudSpeed)
            val textToSpeak = PdfTextExtractor.extractPageText(
                context,
                doc.filePath,
                readerState.currentPage,
                doc.pageCount
            )
            engine.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "page_${readerState.currentPage}")
        } else {
            engine.stop()
        }
    }

    val isCurrentPageBookmarked = remember(bookmarks, readerState.currentPage) {
        bookmarks.any { it.pageIndex == readerState.currentPage }
    }

    val toggleChromeAction = remember {
        { isChromeVisible = !isChromeVisible }
    }

    val toggleQuickZoomAction: () -> Unit = {
        if (zoomScale > 1.05f) {
            zoomScale = 1f
            panOffset = Offset.Zero
        } else {
            zoomScale = 2.1f
            panOffset = Offset.Zero
        }
    }

    // Full-bleed root container so tapping hides Top Bar, Bottom Bar, and Floating Sidebar cleanly
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(settingsState.readingTheme.canvasBackground)
    ) {
        val availableHeight = maxHeight
        val isCompactHeight = availableHeight < 500.dp
        val viewportWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        var containerHeightPx by remember { mutableFloatStateOf(constraints.maxHeight.toFloat().coerceAtLeast(1f)) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { containerHeightPx = it.height.toFloat().coerceAtLeast(1f) }
        ) {
            if (readerState.isReflowMode) {
                ReflowReaderView(
                    doc = doc,
                    currentPage = readerState.currentPage,
                    readingTheme = settingsState.readingTheme,
                    fontSizeSp = readerState.reflowFontSizeSp,
                    isChromeVisible = isChromeVisible,
                    onAdjustFontSize = onAdjustReflowFont,
                    onPageChange = onPageChanged,
                    onOpenSaveQuoteDialog = { showAddQuoteDialog = true },
                    onToggleChrome = toggleChromeAction
                )
            } else {
                // Multi-touch pinch-to-zoom + pan-when-zoomed without blocking single-finger scroll/swipe!
                val zoomModifier = if (!readerState.isScrollLocked && readerState.activeTool == AnnotationTool.NONE) {
                    Modifier.pointerInput(readerState.isScrollLocked, readerState.activeTool, viewportWidthPx, containerHeightPx) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            do {
                                val event = awaitPointerEvent()
                                val pointerCount = event.changes.count { it.pressed }
                                if (pointerCount >= 2 || zoomScale > 1.02f) {
                                    val zoomChange = event.calculateZoom()
                                    val panChange = event.calculatePan()
                                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                                    val isPinching = pointerCount >= 2 && (abs(zoomChange - 1f) > 0.004f || centroidSize > 0f)
                                    val isPanningZoomed = zoomScale > 1.02f && panChange != Offset.Zero

                                    if (isPinching || isPanningZoomed) {
                                        val newScale = (zoomScale * zoomChange).coerceIn(1f, 3.5f)
                                        zoomScale = newScale
                                        panOffset = if (newScale > 1.01f) {
                                            val maxX = ((newScale - 1f) * viewportWidthPx * 0.55f).coerceAtLeast(0f)
                                            val maxY = ((newScale - 1f) * containerHeightPx * 0.55f).coerceAtLeast(0f)
                                            Offset(
                                                x = (panOffset.x + panChange.x).coerceIn(-maxX, maxX),
                                                y = (panOffset.y + panChange.y).coerceIn(-maxY, maxY)
                                            )
                                        } else {
                                            Offset.Zero
                                        }
                                        event.changes.forEach { change ->
                                            if (change.positionChanged()) change.consume()
                                        }
                                    }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
                } else {
                    Modifier
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(zoomModifier)
                        .graphicsLayer {
                            scaleX = zoomScale
                            scaleY = zoomScale
                            translationX = panOffset.x
                            translationY = panOffset.y
                        }
                ) {
                    if (useHorizontalPager) {
                        // Full-screen Horizontal Scroll OR Real 3D Book Mode Page Flip
                        HorizontalPager(
                            state = pagerState,
                            beyondViewportPageCount = 1,
                            pageSpacing = 0.dp,
                            userScrollEnabled = !readerState.isScrollLocked &&
                                readerState.activeTool == AnnotationTool.NONE &&
                                zoomScale <= 1.02f &&
                                textSelection?.isDraggingHandle != true,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag(if (settingsState.bookMode) "pdf_book_pager" else "pdf_horizontal_pager")
                        ) { pageIndex ->
                            val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction)
                            val bookModifier = if (settingsState.bookMode) {
                                Modifier.bookPageFlipEffect(pageOffset)
                            } else {
                                Modifier
                            }
                            val pageMatches = remember(readerState.pdfSearchMatches, pageIndex) {
                                readerState.pdfSearchMatches.filter { it.pageIndex == pageIndex }
                            }
                            val activeMatchOnPage = activeSearchMatch?.takeIf { it.pageIndex == pageIndex }
                            val pageSelection = textSelection?.takeIf { it.pageIndex == pageIndex }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(bookModifier)
                                    .background(settingsState.readingTheme.canvasBackground)
                                    .then(
                                        if (readerState.activeTool == AnnotationTool.NONE) {
                                            Modifier.pointerInput(pageIndex, textSelection != null) {
                                                detectTapGestures(
                                                    onTap = {
                                                        if (textSelection != null) {
                                                            clearTextSelection()
                                                        } else {
                                                            toggleChromeAction()
                                                        }
                                                    },
                                                    onDoubleTap = {
                                                        clearTextSelection()
                                                        toggleQuickZoomAction()
                                                    }
                                                )
                                            }
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                PdfSinglePageCanvas(
                                    doc = doc,
                                    pageIndex = pageIndex,
                                    renderWidthPx = renderWidthPx,
                                    fillViewport = true,
                                    readingTheme = settingsState.readingTheme,
                                    rotationDegrees = readerState.pageRotationDegrees,
                                    activeTool = readerState.activeTool,
                                    activeColorHex = readerState.activeColorHex,
                                    activeStrokeWidth = readerState.activeStrokeWidth,
                                    pageAnnotations = annotations.filter { it.pageIndex == pageIndex },
                                    pageSearchMatches = pageMatches,
                                    activeSearchMatch = activeMatchOnPage,
                                    pageSelection = pageSelection,
                                    onPageBoundsChanged = { bounds -> pageBoundsInRoot[pageIndex] = bounds },
                                    onCommitStroke = { pts -> onAddStrokeAnnotation(pageIndex, pts) },
                                    onRequestStickyNote = { nx, ny ->
                                        pendingStickyNoteTarget = Triple(pageIndex, nx, ny)
                                    },
                                    onTapExistingStickyNote = { selectedStickyNoteToView = it },
                                    onEraseAnnotation = onDeleteAnnotation,
                                    onTapPageBackground = {
                                        if (textSelection != null) {
                                            clearTextSelection()
                                        } else {
                                            toggleChromeAction()
                                        }
                                    },
                                    onDoubleTapPage = {
                                        clearTextSelection()
                                        toggleQuickZoomAction()
                                    },
                                    onLongPressWord = { nx, ny -> startDirectTextSelection(pageIndex, nx, ny) },
                                    onUpdateSelectionHandle = updateSelectionHandle,
                                    onSelectionHandleDragState = { dragging ->
                                        textSelection = textSelection?.copy(isDraggingHandle = dragging)
                                    }
                                )
                            }
                        }
                    } else {
                        // Default: Continuous Vertical Scroll
                        LazyColumn(
                            state = lazyListState,
                            userScrollEnabled = !readerState.isScrollLocked &&
                                readerState.activeTool == AnnotationTool.NONE &&
                                zoomScale <= 1.02f &&
                                textSelection?.isDraggingHandle != true,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(
                                top = if (isChromeVisible) 88.dp else 8.dp,
                                bottom = if (isChromeVisible) 96.dp else 16.dp
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("pdf_lazy_column")
                        ) {
                            items(doc.pageCount, key = { "page_$it" }) { pageIndex ->
                                val pageMatches = remember(readerState.pdfSearchMatches, pageIndex) {
                                    readerState.pdfSearchMatches.filter { it.pageIndex == pageIndex }
                                }
                                val activeMatchOnPage = activeSearchMatch?.takeIf { it.pageIndex == pageIndex }
                                val pageSelection = textSelection?.takeIf { it.pageIndex == pageIndex }

                                PdfSinglePageCanvas(
                                    doc = doc,
                                    pageIndex = pageIndex,
                                    renderWidthPx = renderWidthPx,
                                    fillViewport = false,
                                    readingTheme = settingsState.readingTheme,
                                    rotationDegrees = readerState.pageRotationDegrees,
                                    activeTool = readerState.activeTool,
                                    activeColorHex = readerState.activeColorHex,
                                    activeStrokeWidth = readerState.activeStrokeWidth,
                                    pageAnnotations = annotations.filter { it.pageIndex == pageIndex },
                                    pageSearchMatches = pageMatches,
                                    activeSearchMatch = activeMatchOnPage,
                                    pageSelection = pageSelection,
                                    onPageBoundsChanged = { bounds -> pageBoundsInRoot[pageIndex] = bounds },
                                    onCommitStroke = { pts -> onAddStrokeAnnotation(pageIndex, pts) },
                                    onRequestStickyNote = { nx, ny ->
                                        pendingStickyNoteTarget = Triple(pageIndex, nx, ny)
                                    },
                                    onTapExistingStickyNote = { selectedStickyNoteToView = it },
                                    onEraseAnnotation = onDeleteAnnotation,
                                    onTapPageBackground = {
                                        if (textSelection != null) {
                                            clearTextSelection()
                                        } else {
                                            toggleChromeAction()
                                        }
                                    },
                                    onDoubleTapPage = {
                                        clearTextSelection()
                                        toggleQuickZoomAction()
                                    },
                                    onLongPressWord = { nx, ny -> startDirectTextSelection(pageIndex, nx, ny) },
                                    onUpdateSelectionHandle = updateSelectionHandle,
                                    onSelectionHandleDragState = { dragging ->
                                        textSelection = textSelection?.copy(isDraggingHandle = dragging)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Right Interactive Scroll Bar (hides with chrome)
            AnimatedVisibility(
                visible = isChromeVisible && settingsState.showScrollBar && doc.pageCount > 1 && !readerState.isReflowMode,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                val fraction = (readerState.currentPage.toFloat() / (doc.pageCount - 1).coerceAtLeast(1).toFloat())
                    .coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(24.dp)
                        .padding(top = 92.dp, bottom = 96.dp, start = 4.dp, end = 4.dp)
                        .pointerInput(doc.pageCount) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val trackH = (containerHeightPx - 240f).coerceAtLeast(120f)
                                val ratio = (change.position.y / trackH).coerceIn(0f, 1f)
                                val target = (ratio * (doc.pageCount - 1)).roundToInt()
                                coroutineScope.launch {
                                    onPageChanged(target)
                                }
                            }
                        }
                ) {
                    val trackRange = (containerHeightPx - 300f).coerceAtLeast(80f)
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = (trackRange * fraction).roundToInt().coerceAtLeast(0)
                                )
                            }
                            .width(6.dp)
                            .height(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD99B93).copy(alpha = 0.85f))
                    )
                }
            }

            // Top App Bar Overlay (slides out smoothly on screen tap)
            AnimatedVisibility(
                visible = isChromeVisible || readerState.isPdfSearchOpen,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141111).copy(alpha = 0.96f))
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("reader_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to library",
                                tint = Color(0xFFECE6E4)
                            )
                        }

                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFECE6E4),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp)
                        )

                        IconButton(
                            onClick = { onTogglePdfSearch(!readerState.isPdfSearchOpen) },
                            modifier = Modifier.testTag("reader_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search inside PDF",
                                tint = Color(0xFFECE6E4)
                            )
                        }

                        IconButton(
                            onClick = { onOpenDocumentMoreSheet(doc) },
                            modifier = Modifier.testTag("reader_more_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Document menu",
                                tint = Color(0xFFECE6E4)
                            )
                        }
                    }

                    // In-PDF Search Bar with guaranteed high-contrast text on dark chrome in both Light & Dark themes
                    AnimatedVisibility(visible = readerState.isPdfSearchOpen) {
                        val searchFocusRequester = remember { FocusRequester() }
                        LaunchedEffect(Unit) {
                            runCatching { searchFocusRequester.requestFocus() }
                        }

                        Surface(
                            color = Color(0xFF211D1D),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = readerState.pdfSearchQuery,
                                    onValueChange = onPdfSearchQueryChange,
                                    placeholder = {
                                        Text(
                                            text = "Search in ${doc.pageCount} pages…",
                                            color = Color(0xFFA29A97),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFECE6E4)),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFFECE6E4),
                                        unfocusedTextColor = Color(0xFFECE6E4),
                                        cursorColor = Color(0xFFD99B93),
                                        focusedBorderColor = Color(0xFFD99B93),
                                        unfocusedBorderColor = Color(0xFF5C504E),
                                        focusedContainerColor = Color(0xFF2D2828),
                                        unfocusedContainerColor = Color(0xFF282323),
                                        focusedPlaceholderColor = Color(0xFFA29A97),
                                        unfocusedPlaceholderColor = Color(0xFFA29A97)
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = { onNavigateSearchMatch(true) }
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(searchFocusRequester)
                                        .testTag("in_pdf_search_input")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val matchCount = readerState.pdfSearchMatches.size
                                Text(
                                    text = when {
                                        readerState.isSearchingPdf -> "…"
                                        matchCount > 0 -> "${readerState.activeSearchMatchIndex + 1}/$matchCount"
                                        else -> "0/0"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFD99B93)
                                )
                                IconButton(
                                    onClick = { onNavigateSearchMatch(false) },
                                    enabled = matchCount > 0
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous match", tint = Color(0xFFECE6E4))
                                }
                                IconButton(
                                    onClick = { onNavigateSearchMatch(true) },
                                    enabled = matchCount > 0
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next match", tint = Color(0xFFECE6E4))
                                }
                                IconButton(onClick = { onTogglePdfSearch(false) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close search", tint = Color(0xFFECE6E4))
                                }
                            }
                        }
                    }
                }
            }

            // Active Tool Indicator Banner (only visible while edit mode & a tool are active)
            AnimatedVisibility(
                visible = readerState.isAnnotationSidebarExpanded && readerState.activeTool != AnnotationTool.NONE,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = if (isChromeVisible) 66.dp else 12.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF211C1C).copy(alpha = 0.94f),
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(readerState.activeColorHex))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (readerState.activeTool) {
                                AnnotationTool.STICKY_NOTE -> "Sticky Note: Tap anywhere on page to pin"
                                AnnotationTool.ERASER -> "Eraser: Tap over annotations to erase"
                                else -> "${readerState.activeTool.label} active • Draw on PDF"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFECE6E4),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFD99B93),
                            modifier = Modifier
                                .clickable { onSelectAnnotationTool(AnnotationTool.NONE) }
                                .testTag("active_tool_done_button")
                        )
                    }
                }
            }

            // Floating Left Sidebar Pill (landscape-safe & hides with chrome!)
            AnimatedVisibility(
                visible = isChromeVisible,
                enter = slideInHorizontally { -it } + fadeIn(),
                exit = slideOutHorizontally { -it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(
                        start = 14.dp,
                        end = if (isCompactHeight) 14.dp else 0.dp,
                        bottom = if (isCompactHeight) 74.dp else 90.dp
                    )
            ) {
                FloatingReaderSidebarPill(
                    readerState = readerState,
                    isCompactHeight = isCompactHeight,
                    maxAvailableHeight = availableHeight,
                    onToggleScrollLock = onToggleScrollLock,
                    onRotatePage = onRotatePage,
                    onToggleLandscape = {
                        onToggleLandscape()
                        if (zoomScale > 1f) {
                            zoomScale = 1f
                            panOffset = Offset.Zero
                        }
                    },
                    onToggleAnnotationSidebar = onToggleAnnotationSidebar,
                    onSelectTool = onSelectAnnotationTool,
                    onSelectColor = onSelectAnnotationColor,
                    onUndo = onUndoAnnotation,
                    onRedo = onRedoAnnotation,
                    onExportAnnotatedPdf = onExportAnnotatedPdf
                )
            }

            // Floating Auto-Scroll Controller Pill
            AnimatedVisibility(
                visible = readerState.isAutoScrollActive,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(end = 16.dp, bottom = if (isChromeVisible) 90.dp else 20.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF231E1E),
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = OrionIcons.KeyboardDoubleArrowDown,
                            contentDescription = null,
                            tint = Color(0xFFD99B93),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = { onAdjustAutoScrollSpeed(-0.25f) }, modifier = Modifier.size(28.dp)) {
                            Icon(OrionIcons.Remove, contentDescription = "Slower", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = String.format(Locale.US, "%.2fx", readerState.autoScrollSpeed),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(onClick = { onAdjustAutoScrollSpeed(0.25f) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Faster", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onStopAutoScroll, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Stop auto-scroll", tint = Color(0xFFD99B93), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Floating Read Aloud (TTS) Controller Pill
            AnimatedVisibility(
                visible = readerState.isReadAloudActive,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(
                        end = 16.dp,
                        bottom = when {
                            readerState.isAutoScrollActive && isChromeVisible -> 142.dp
                            readerState.isAutoScrollActive -> 72.dp
                            isChromeVisible -> 90.dp
                            else -> 20.dp
                        }
                    )
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF231E1E),
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = OrionIcons.Headphones,
                            contentDescription = null,
                            tint = Color(0xFFD99B93),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reading Page ${readerState.currentPage + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(
                            onClick = { onAdjustReadAloudSpeed(if (readerState.readAloudSpeed >= 1.5f) -0.5f else 0.25f) }
                        ) {
                            Text(
                                text = "${readerState.readAloudSpeed}x",
                                color = Color(0xFFD99B93),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        IconButton(onClick = onStopReadAloud, modifier = Modifier.size(28.dp)) {
                            Icon(OrionIcons.Stop, contentDescription = "Stop Read Aloud", tint = Color(0xFFD99B93), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Bottom Bar Overlay (slides down smoothly on screen tap)
            AnimatedVisibility(
                visible = isChromeVisible,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                ReaderBottomBar(
                    currentPage = readerState.currentPage,
                    pageCount = doc.pageCount,
                    isReflowMode = readerState.isReflowMode,
                    isBookmarked = isCurrentPageBookmarked,
                    onOpenContents = onOpenContentsSheet,
                    onToggleReflow = onToggleReflow,
                    onOpenJumpToPage = { showJumpPageDialog = true },
                    onToggleBookmark = onToggleBookmark,
                    onOpenDisplaySettings = onOpenReadingSettingsSheet
                )
            }

            // Custom Brightness Dim Overlay (when Use system brightness is false)
            if (!settingsState.useSystemBrightness) {
                val dimAlpha = (1f - settingsState.brightness).coerceIn(0f, 0.78f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = dimAlpha))
                )
            }

            // Fallback floating toolbar ONLY if the device returns null from native ActionMode.TYPE_FLOATING
            AnimatedVisibility(
                visible = showFallbackSelectionToolbar && textSelection != null && textSelection?.isDraggingHandle != true,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = if (isChromeVisible) 70.dp else 18.dp)
            ) {
                FallbackFloatingSelectionToolbar(
                    onCopy = {
                        val selected = textSelection?.selectedText.orEmpty()
                        if (selected.isNotBlank()) {
                            copyTextToClipboard(context, doc.title, selected)
                            onStatusMessage("Copied to clipboard")
                        }
                        clearTextSelection()
                    },
                    onSelectAll = {
                        textSelection?.let { current ->
                            if (current.layout.words.isNotEmpty()) {
                                textSelection = current.copy(
                                    startWordIdx = 0,
                                    endWordIdx = current.layout.words.lastIndex,
                                    isDraggingHandle = false
                                )
                            }
                        }
                    },
                    onHighlight = {
                        textSelection?.let { current ->
                            onHighlightSelection(current.pageIndex, current.selectedRects)
                        }
                        clearTextSelection()
                    },
                    onShare = {
                        val selected = textSelection?.selectedText.orEmpty()
                        if (selected.isNotBlank()) {
                            shareSelectedPlainText(context, doc.title, selected)
                        }
                        clearTextSelection()
                    }
                )
            }
        }
    }

    // Jump to Page Dialog
    if (showJumpPageDialog) {
        var sliderPage by remember(readerState.currentPage) {
            mutableFloatStateOf((readerState.currentPage + 1).toFloat())
        }
        AlertDialog(
            onDismissRequest = { showJumpPageDialog = false },
            title = { Text("Jump to Page") },
            text = {
                Column {
                    Text(
                        text = "Page ${sliderPage.roundToInt()} of ${doc.pageCount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (doc.pageCount > 1) {
                        Slider(
                            value = sliderPage,
                            onValueChange = { sliderPage = it },
                            valueRange = 1f..doc.pageCount.toFloat(),
                            steps = (doc.pageCount - 2).coerceAtLeast(0)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPageChanged(sliderPage.roundToInt() - 1)
                        showJumpPageDialog = false
                    }
                ) {
                    Text("Go to Page")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpPageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Sticky Note Callout Dialog
    pendingStickyNoteTarget?.let { (pageIdx, nx, ny) ->
        var noteText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { pendingStickyNoteTarget = null },
            title = { Text("Add Sticky Note Callout (Page ${pageIdx + 1})") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Enter annotation note or formula tip…") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            onAddStickyNote(pageIdx, nx, ny, noteText)
                        }
                        pendingStickyNoteTarget = null
                    }
                ) {
                    Text("Pin Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingStickyNoteTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // View / Delete Existing Sticky Note Dialog
    selectedStickyNoteToView?.let { noteAnnotation ->
        AlertDialog(
            onDismissRequest = { selectedStickyNoteToView = null },
            title = { Text("Sticky Note • Page ${noteAnnotation.pageIndex + 1}") },
            text = {
                Text(
                    text = noteAnnotation.noteText,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAnnotation(noteAnnotation)
                        selectedStickyNoteToView = null
                    }
                ) {
                    Text("Delete Note", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                Button(onClick = { selectedStickyNoteToView = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Save Quote Dialog (from Reflow / Text selection)
    if (showAddQuoteDialog) {
        var quoteInput by remember(doc.filePath, readerState.currentPage) { mutableStateOf("") }
        var noteInput by remember { mutableStateOf("") }

        LaunchedEffect(doc.filePath, readerState.currentPage) {
            val extracted = PdfTextExtractor.extractPageText(
                context,
                doc.filePath,
                readerState.currentPage,
                doc.pageCount
            )
            quoteInput = extracted
                .lines()
                .firstOrNull { it.length > 20 }
                ?: "Selected passage from Page ${readerState.currentPage + 1}"
        }

        AlertDialog(
            onDismissRequest = { showAddQuoteDialog = false },
            title = { Text("Save Passage to Quotes") },
            text = {
                Column {
                    OutlinedTextField(
                        value = quoteInput,
                        onValueChange = { quoteInput = it },
                        label = { Text("Selected Passage") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Personal Note (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveQuote(quoteInput, noteInput, readerState.currentPage)
                        showAddQuoteDialog = false
                    }
                ) {
                    Text("Save Quote")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddQuoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Realistic 3D Book Page-Flip animation modifier for Book Mode.
 * Anchors the turning page along the left spine edge with perspective rotationY,
 * dynamic paper-curl lighting, and a cast shadow on the revealed underlying sheet.
 */
private fun Modifier.bookPageFlipEffect(pageOffset: Float): Modifier {
    val clamped = pageOffset.coerceIn(-1f, 1f)
    return this
        .graphicsLayer {
            cameraDistance = 26f * density
            if (clamped >= 0f) {
                // Turning front page hinged at the left spine
                transformOrigin = TransformOrigin(0f, 0.5f)
                translationX = size.width * clamped
                rotationY = (-clamped * 94f).coerceIn(-94f, 0f)
                alpha = if (clamped > 0.85f) {
                    ((1f - clamped) / 0.15f).coerceIn(0f, 1f)
                } else {
                    1f
                }
            } else {
                // Next page lying flat underneath, revealed as the front page turns away
                transformOrigin = TransformOrigin(0.5f, 0.5f)
                translationX = size.width * clamped
                val depthScale = (1f + clamped * 0.03f).coerceIn(0.96f, 1f)
                scaleX = depthScale
                scaleY = depthScale
                alpha = 1f
            }
        }
        .drawWithContent {
            drawContent()
            // Subtle tactile book spine gutter along the left edge
            val spineWidth = (size.width * 0.055f).coerceAtLeast(18f)
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.22f),
                        Color.Black.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    startX = 0f,
                    endX = spineWidth
                ),
                topLeft = Offset.Zero,
                size = Size(spineWidth, size.height)
            )

            if (clamped > 0.01f) {
                // Turning page shading + warm edge highlight as the page curls
                val foldDarkness = (clamped * 0.34f).coerceIn(0f, 0.34f)
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = foldDarkness * 0.65f),
                            Color.White.copy(alpha = (clamped * 0.14f).coerceIn(0f, 0.14f)),
                            Color.Black.copy(alpha = foldDarkness)
                        ),
                        startX = 0f,
                        endX = size.width
                    )
                )
            } else if (clamped < -0.01f) {
                // Cast shadow from the turning page onto the underlying next sheet
                val castAlpha = (-clamped * 0.48f).coerceIn(0f, 0.48f)
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = castAlpha),
                            Color.Black.copy(alpha = castAlpha * 0.45f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = size.width
                    )
                )
            }
        }
}

@Composable
private fun PdfSinglePageCanvas(
    doc: PdfDocumentEntity,
    pageIndex: Int,
    renderWidthPx: Int,
    fillViewport: Boolean,
    readingTheme: ReadingTheme,
    rotationDegrees: Int,
    activeTool: AnnotationTool,
    activeColorHex: Long,
    activeStrokeWidth: Float,
    pageAnnotations: List<AnnotationEntity>,
    pageSearchMatches: List<PdfSearchMatch>,
    activeSearchMatch: PdfSearchMatch?,
    pageSelection: PdfPageSelectionState?,
    onPageBoundsChanged: (androidx.compose.ui.geometry.Rect) -> Unit,
    onCommitStroke: (List<Offset>) -> Unit,
    onRequestStickyNote: (Float, Float) -> Unit,
    onTapExistingStickyNote: (AnnotationEntity) -> Unit,
    onEraseAnnotation: (AnnotationEntity) -> Unit,
    onTapPageBackground: () -> Unit,
    onDoubleTapPage: () -> Unit,
    onLongPressWord: (Float, Float) -> Unit,
    onUpdateSelectionHandle: (Boolean, Float, Float) -> Unit,
    onSelectionHandleDragState: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember(doc.filePath, pageIndex) {
        mutableStateOf(PdfRenderEngine.getBestCachedBitmap(doc.filePath, pageIndex, renderWidthPx))
    }

    LaunchedEffect(doc.filePath, pageIndex, renderWidthPx) {
        val exactCached = PdfRenderEngine.getCachedBitmap(doc.filePath, pageIndex, renderWidthPx)
        if (exactCached != null) {
            bitmap = exactCached
        } else {
            val rendered = PdfRenderEngine.renderPage(context, doc.filePath, pageIndex, renderWidthPx)
            if (rendered != null) {
                bitmap = rendered
            }
        }
    }

    val colorFilter = remember(readingTheme) {
        readingTheme.toColorMatrix()?.let { ColorFilter.colorMatrix(it) }
    }

    val currentStrokePoints = remember { mutableStateListOf<Offset>() }
    val pageAspect = remember(bitmap) {
        val b = bitmap
        if (b != null && !b.isRecycled && b.height > 0) {
            (b.width.toFloat() / b.height.toFloat()).coerceIn(0.45f, 2.2f)
        } else {
            720f / 1020f
        }
    }

    if (fillViewport) {
        // Fill the entire viewport cleanly with the largest exact aspect-fitted page (zero awkward gaps!)
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val containerAspect = if (maxHeight > 0.dp) maxWidth / maxHeight else 0.7f
            val pageSizeModifier = if (containerAspect > pageAspect) {
                Modifier
                    .fillMaxHeight()
                    .aspectRatio(pageAspect, matchHeightConstraintsFirst = true)
            } else {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(pageAspect, matchHeightConstraintsFirst = false)
            }

            PdfPageSurfaceWithOverlay(
                docTitle = doc.title,
                pageIndex = pageIndex,
                bitmap = bitmap,
                readingTheme = readingTheme,
                colorFilter = colorFilter,
                rotationDegrees = rotationDegrees,
                activeTool = activeTool,
                activeColorHex = activeColorHex,
                activeStrokeWidth = activeStrokeWidth,
                pageAnnotations = pageAnnotations,
                pageSearchMatches = pageSearchMatches,
                activeSearchMatch = activeSearchMatch,
                pageSelection = pageSelection,
                currentStrokePoints = currentStrokePoints,
                onPageBoundsChanged = onPageBoundsChanged,
                onCommitStroke = onCommitStroke,
                onRequestStickyNote = onRequestStickyNote,
                onTapExistingStickyNote = onTapExistingStickyNote,
                onEraseAnnotation = onEraseAnnotation,
                onTapPageBackground = onTapPageBackground,
                onDoubleTapPage = onDoubleTapPage,
                onLongPressWord = onLongPressWord,
                onUpdateSelectionHandle = onUpdateSelectionHandle,
                onSelectionHandleDragState = onSelectionHandleDragState,
                modifier = pageSizeModifier
            )
        }
    } else {
        PdfPageSurfaceWithOverlay(
            docTitle = doc.title,
            pageIndex = pageIndex,
            bitmap = bitmap,
            readingTheme = readingTheme,
            colorFilter = colorFilter,
            rotationDegrees = rotationDegrees,
            activeTool = activeTool,
            activeColorHex = activeColorHex,
            activeStrokeWidth = activeStrokeWidth,
            pageAnnotations = pageAnnotations,
            pageSearchMatches = pageSearchMatches,
            activeSearchMatch = activeSearchMatch,
            pageSelection = pageSelection,
            currentStrokePoints = currentStrokePoints,
            onPageBoundsChanged = onPageBoundsChanged,
            onCommitStroke = onCommitStroke,
            onRequestStickyNote = onRequestStickyNote,
            onTapExistingStickyNote = onTapExistingStickyNote,
            onEraseAnnotation = onEraseAnnotation,
            onTapPageBackground = onTapPageBackground,
            onDoubleTapPage = onDoubleTapPage,
            onLongPressWord = onLongPressWord,
            onUpdateSelectionHandle = onUpdateSelectionHandle,
            onSelectionHandleDragState = onSelectionHandleDragState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(pageAspect)
        )
    }
}

@Composable
private fun PdfPageSurfaceWithOverlay(
    docTitle: String,
    pageIndex: Int,
    bitmap: Bitmap?,
    readingTheme: ReadingTheme,
    colorFilter: ColorFilter?,
    rotationDegrees: Int,
    activeTool: AnnotationTool,
    activeColorHex: Long,
    activeStrokeWidth: Float,
    pageAnnotations: List<AnnotationEntity>,
    pageSearchMatches: List<PdfSearchMatch>,
    activeSearchMatch: PdfSearchMatch?,
    pageSelection: PdfPageSelectionState?,
    currentStrokePoints: MutableList<Offset>,
    onPageBoundsChanged: (androidx.compose.ui.geometry.Rect) -> Unit,
    onCommitStroke: (List<Offset>) -> Unit,
    onRequestStickyNote: (Float, Float) -> Unit,
    onTapExistingStickyNote: (AnnotationEntity) -> Unit,
    onEraseAnnotation: (AnnotationEntity) -> Unit,
    onTapPageBackground: () -> Unit,
    onDoubleTapPage: () -> Unit,
    onLongPressWord: (Float, Float) -> Unit,
    onUpdateSelectionHandle: (Boolean, Float, Float) -> Unit,
    onSelectionHandleDragState: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var pagePixelSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .background(readingTheme.previewBg)
            .rotate(rotationDegrees.toFloat())
            .onSizeChanged { pagePixelSize = it }
            .onGloballyPositioned { coords ->
                onPageBoundsChanged(coords.boundsInRoot())
            },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null && !bitmap.isRecycled) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Page ${pageIndex + 1} of $docTitle",
                contentScale = ContentScale.FillBounds,
                colorFilter = colorFilter,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "Rendering Page ${pageIndex + 1}…",
                style = MaterialTheme.typography.bodyMedium,
                color = readingTheme.previewText
            )
        }

        val isDrawingTool = activeTool == AnnotationTool.PEN ||
            activeTool == AnnotationTool.MARKER ||
            activeTool == AnnotationTool.HIGHLIGHTER ||
            activeTool == AnnotationTool.UNDERLINE

        val gestureModifier = when {
            isDrawingTool -> {
                Modifier.pointerInput(activeTool, activeColorHex, activeStrokeWidth, pageIndex) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            currentStrokePoints.clear()
                            val nx = (startOffset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            val ny = (startOffset.y / size.height.toFloat()).coerceIn(0f, 1f)
                            currentStrokePoints.add(Offset(nx, ny))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val nx = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                            val ny = (change.position.y / size.height.toFloat()).coerceIn(0f, 1f)
                            currentStrokePoints.add(Offset(nx, ny))
                        },
                        onDragEnd = {
                            if (currentStrokePoints.size >= 2) {
                                onCommitStroke(currentStrokePoints.toList())
                            }
                            currentStrokePoints.clear()
                        },
                        onDragCancel = {
                            currentStrokePoints.clear()
                        }
                    )
                }
            }
            activeTool == AnnotationTool.STICKY_NOTE -> {
                Modifier.pointerInput(pageIndex) {
                    detectTapGestures { tapOffset ->
                        val nx = (tapOffset.x / size.width.toFloat()).coerceIn(0.05f, 0.95f)
                        val ny = (tapOffset.y / size.height.toFloat()).coerceIn(0.05f, 0.95f)
                        onRequestStickyNote(nx, ny)
                    }
                }
            }
            activeTool == AnnotationTool.ERASER -> {
                Modifier.pointerInput(pageAnnotations, pageIndex) {
                    detectTapGestures { tapOffset ->
                        val nx = tapOffset.x / size.width.toFloat()
                        val ny = tapOffset.y / size.height.toFloat()
                        pageAnnotations.firstOrNull { ann ->
                            val pts = ann.parseNormalizedPoints()
                            if (pts.isNotEmpty()) {
                                pts.any { pt -> hypot(pt.x - nx, pt.y - ny) < 0.06f }
                            } else {
                                hypot(ann.anchorX - nx, ann.anchorY - ny) < 0.07f
                            }
                        }?.let { onEraseAnnotation(it) }
                    }
                }
            }
            else -> {
                // True PDF reader browse mode:
                // - Single tap: clears blue selection (if active), opens sticky note, or toggles chrome
                // - Double tap: quick zoom
                // - Long press on any word: highlights word in blue directly on page + native Android Copy/Share menu!
                Modifier.pointerInput(pageAnnotations, pageIndex, pageSelection != null) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            val nx = tapOffset.x / size.width.toFloat()
                            val ny = tapOffset.y / size.height.toFloat()
                            val hitNote = pageAnnotations.firstOrNull { ann ->
                                ann.toolType == AnnotationTool.STICKY_NOTE.name &&
                                    hypot(ann.anchorX - nx, ann.anchorY - ny) < 0.06f
                            }
                            if (hitNote != null) {
                                onTapExistingStickyNote(hitNote)
                            } else {
                                onTapPageBackground()
                            }
                        },
                        onDoubleTap = { onDoubleTapPage() },
                        onLongPress = { pressOffset ->
                            val nx = (pressOffset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            val ny = (pressOffset.y / size.height.toFloat()).coerceIn(0f, 1f)
                            onLongPressWord(nx, ny)
                        }
                    )
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(gestureModifier)
        ) {
            val w = size.width
            val h = size.height

            // 1. In-PDF Search Match Highlights (yellow for all matches, vivid orange-amber for active match)
            pageSearchMatches.forEach { match ->
                val isActiveMatch = match === activeSearchMatch ||
                    (activeSearchMatch != null &&
                        match.pageIndex == activeSearchMatch.pageIndex &&
                        match.rects == activeSearchMatch.rects)
                val fillColor = if (isActiveMatch) Color(0xA6FF9800) else Color(0x78FFEB3B)
                val borderColor = if (isActiveMatch) Color(0xFFE65100) else Color(0x99F9A825)
                val corner = CornerRadius(3.dp.toPx(), 3.dp.toPx())

                match.rects.forEach { r ->
                    val rectTopLeft = Offset(r.left * w, r.top * h)
                    val rectSize = Size((r.width * w).coerceAtLeast(4f), (r.height * h).coerceAtLeast(4f))
                    drawRoundRect(
                        color = fillColor,
                        topLeft = rectTopLeft,
                        size = rectSize,
                        cornerRadius = corner
                    )
                    drawRoundRect(
                        color = borderColor,
                        topLeft = rectTopLeft,
                        size = rectSize,
                        cornerRadius = corner,
                        style = Stroke(width = if (isActiveMatch) 1.6.dp.toPx() else 0.8.dp.toPx())
                    )
                }
            }

            // 2. Saved Annotations (strokes, highlights, sticky notes)
            pageAnnotations.forEach { ann ->
                if (ann.toolType == AnnotationTool.STICKY_NOTE.name) {
                    val cx = ann.anchorX * w
                    val cy = ann.anchorY * h
                    drawCircle(
                        color = Color(0xFF1F1A1A),
                        radius = 16.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color(ann.colorHex),
                        radius = 13.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                } else {
                    val pts = ann.parseNormalizedPoints()
                    if (pts.size >= 2) {
                        val path = Path().apply {
                            moveTo(pts.first().x * w, pts.first().y * h)
                            for (i in 1 until pts.size) {
                                lineTo(pts[i].x * w, pts[i].y * h)
                            }
                        }
                        val isHighlight = ann.toolType == AnnotationTool.HIGHLIGHTER.name
                        val isMarker = ann.toolType == AnnotationTool.MARKER.name
                        val alpha = when {
                            isHighlight -> 0.38f
                            isMarker -> 0.68f
                            else -> 0.95f
                        }
                        drawPath(
                            path = path,
                            color = Color(ann.colorHex).copy(alpha = alpha),
                            style = Stroke(
                                width = ann.strokeWidth.dp.toPx(),
                                cap = if (isHighlight) StrokeCap.Square else StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            // 3. In-progress annotation stroke
            if (currentStrokePoints.size >= 2) {
                val path = Path().apply {
                    val first = currentStrokePoints.first()
                    moveTo(first.x * w, first.y * h)
                    if (activeTool == AnnotationTool.UNDERLINE) {
                        val last = currentStrokePoints.last()
                        lineTo(last.x * w, first.y * h)
                    } else {
                        for (i in 1 until currentStrokePoints.size) {
                            lineTo(currentStrokePoints[i].x * w, currentStrokePoints[i].y * h)
                        }
                    }
                }
                val alpha = when (activeTool) {
                    AnnotationTool.HIGHLIGHTER -> 0.38f
                    AnnotationTool.MARKER -> 0.68f
                    else -> 0.95f
                }
                drawPath(
                    path = path,
                    color = Color(activeColorHex).copy(alpha = alpha),
                    style = Stroke(
                        width = activeStrokeWidth.dp.toPx(),
                        cap = if (activeTool == AnnotationTool.HIGHLIGHTER) StrokeCap.Square else StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // 4. Direct On-Page Blue Text Selection Highlight + Native Blue Teardrop Handles
            if (pageSelection != null) {
                val selectionBlue = Color(0x5A1A73E8)
                val handleBlue = Color(0xFF1A73E8)
                val corner = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())

                pageSelection.selectedRects.forEach { r ->
                    drawRoundRect(
                        color = selectionBlue,
                        topLeft = Offset(r.left * w, r.top * h),
                        size = Size((r.width * w).coerceAtLeast(4f), (r.height * h).coerceAtLeast(4f)),
                        cornerRadius = corner
                    )
                }

                // Draw crisp native-style vertical caret bars + teardrop handles at start & end
                val dropRadius = 8.5.dp.toPx()
                pageSelection.startHandleAnchor?.let { startR ->
                    val ax = startR.left * w
                    val topY = startR.top * h
                    val botY = startR.bottom * h
                    drawLine(
                        color = handleBlue,
                        start = Offset(ax, topY),
                        end = Offset(ax, botY),
                        strokeWidth = 2.dp.toPx()
                    )
                    // Start teardrop pointing top-right toward selection start
                    val cx = ax - dropRadius * 0.72f
                    val cy = botY + dropRadius * 0.92f
                    drawCircle(color = handleBlue, radius = dropRadius, center = Offset(cx, cy))
                    val tipPath = Path().apply {
                        moveTo(ax, botY)
                        lineTo(cx, cy - dropRadius * 0.6f)
                        lineTo(cx + dropRadius * 0.85f, cy)
                        close()
                    }
                    drawPath(path = tipPath, color = handleBlue)
                }

                pageSelection.endHandleAnchor?.let { endR ->
                    val ax = endR.right * w
                    val topY = endR.top * h
                    val botY = endR.bottom * h
                    drawLine(
                        color = handleBlue,
                        start = Offset(ax, topY),
                        end = Offset(ax, botY),
                        strokeWidth = 2.dp.toPx()
                    )
                    // End teardrop pointing top-left toward selection end
                    val cx = ax + dropRadius * 0.72f
                    val cy = botY + dropRadius * 0.92f
                    drawCircle(color = handleBlue, radius = dropRadius, center = Offset(cx, cy))
                    val tipPath = Path().apply {
                        moveTo(ax, botY)
                        lineTo(cx, cy - dropRadius * 0.6f)
                        lineTo(cx - dropRadius * 0.85f, cy)
                        close()
                    }
                    drawPath(path = tipPath, color = handleBlue)
                }
            }
        }

        // 5. Interactive Draggable Touch Targets for Start & End Blue Selection Handles
        if (pageSelection != null && pagePixelSize.width > 0 && pagePixelSize.height > 0) {
            val handleHitPx = with(density) { 48.dp.toPx() }
            val pageW = pagePixelSize.width.toFloat()
            val pageH = pagePixelSize.height.toFloat()
            val latestOnUpdateHandle by rememberUpdatedState(onUpdateSelectionHandle)
            val latestOnDragState by rememberUpdatedState(onSelectionHandleDragState)

            pageSelection.startHandleAnchor?.let { startR ->
                val latestStartR by rememberUpdatedState(startR)
                var isDraggingStart by remember(pageSelection.pageIndex) { mutableStateOf(false) }
                var anchoredOffset by remember(pageSelection.pageIndex) {
                    mutableStateOf(
                        IntOffset(
                            x = (startR.left * pageW - handleHitPx * 0.72f).roundToInt(),
                            y = (startR.bottom * pageH - handleHitPx * 0.15f).roundToInt()
                        )
                    )
                }
                if (!isDraggingStart) {
                    anchoredOffset = IntOffset(
                        x = (startR.left * pageW - handleHitPx * 0.72f).roundToInt(),
                        y = (startR.bottom * pageH - handleHitPx * 0.15f).roundToInt()
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset { anchoredOffset }
                        .size(48.dp)
                        .pointerInput(pageSelection.pageIndex, pageW, pageH) {
                            var dragPosX = 0f
                            var dragPosY = 0f
                            detectDragGestures(
                                onDragStart = {
                                    isDraggingStart = true
                                    dragPosX = latestStartR.left * pageW
                                    dragPosY = latestStartR.centerY * pageH
                                    latestOnDragState(true)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragPosX = (dragPosX + dragAmount.x).coerceIn(0f, pageW)
                                    dragPosY = (dragPosY + dragAmount.y).coerceIn(0f, pageH)
                                    latestOnUpdateHandle(true, dragPosX / pageW, dragPosY / pageH)
                                },
                                onDragEnd = {
                                    isDraggingStart = false
                                    latestOnDragState(false)
                                },
                                onDragCancel = {
                                    isDraggingStart = false
                                    latestOnDragState(false)
                                }
                            )
                        }
                        .testTag("selection_handle_start")
                )
            }

            pageSelection.endHandleAnchor?.let { endR ->
                val latestEndR by rememberUpdatedState(endR)
                var isDraggingEnd by remember(pageSelection.pageIndex) { mutableStateOf(false) }
                var anchoredOffset by remember(pageSelection.pageIndex) {
                    mutableStateOf(
                        IntOffset(
                            x = (endR.right * pageW - handleHitPx * 0.28f).roundToInt(),
                            y = (endR.bottom * pageH - handleHitPx * 0.15f).roundToInt()
                        )
                    )
                }
                if (!isDraggingEnd) {
                    anchoredOffset = IntOffset(
                        x = (endR.right * pageW - handleHitPx * 0.28f).roundToInt(),
                        y = (endR.bottom * pageH - handleHitPx * 0.15f).roundToInt()
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset { anchoredOffset }
                        .size(48.dp)
                        .pointerInput(pageSelection.pageIndex, pageW, pageH) {
                            var dragPosX = 0f
                            var dragPosY = 0f
                            detectDragGestures(
                                onDragStart = {
                                    isDraggingEnd = true
                                    dragPosX = latestEndR.right * pageW
                                    dragPosY = latestEndR.centerY * pageH
                                    latestOnDragState(true)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragPosX = (dragPosX + dragAmount.x).coerceIn(0f, pageW)
                                    dragPosY = (dragPosY + dragAmount.y).coerceIn(0f, pageH)
                                    latestOnUpdateHandle(false, dragPosX / pageW, dragPosY / pageH)
                                },
                                onDragEnd = {
                                    isDraggingEnd = false
                                    latestOnDragState(false)
                                },
                                onDragCancel = {
                                    isDraggingEnd = false
                                    latestOnDragState(false)
                                }
                            )
                        }
                        .testTag("selection_handle_end")
                )
            }
        }
    }
}

@Composable
private fun FloatingReaderSidebarPill(
    readerState: ActiveReaderState,
    isCompactHeight: Boolean,
    maxAvailableHeight: Dp,
    onToggleScrollLock: () -> Unit,
    onRotatePage: () -> Unit,
    onToggleLandscape: () -> Unit,
    onToggleAnnotationSidebar: () -> Unit,
    onSelectTool: (AnnotationTool) -> Unit,
    onSelectColor: (Long) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onExportAnnotatedPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    var colorIndex by remember { mutableIntStateOf(0) }

    val annotationToolsContent: @Composable () -> Unit = {
        SidebarToolIconButton(
            icon = Icons.Default.Edit,
            label = "Pen",
            selected = readerState.activeTool == AnnotationTool.PEN,
            activeColor = Color(readerState.activeColorHex),
            onClick = { onSelectTool(AnnotationTool.PEN) },
            testTag = "tool_pen_button"
        )
        SidebarToolIconButton(
            icon = OrionIcons.Brush,
            label = "Marker",
            selected = readerState.activeTool == AnnotationTool.MARKER,
            activeColor = Color(readerState.activeColorHex),
            onClick = { onSelectTool(AnnotationTool.MARKER) },
            testTag = "tool_marker_button"
        )
        SidebarToolIconButton(
            icon = OrionIcons.Highlight,
            label = "Highlighter",
            selected = readerState.activeTool == AnnotationTool.HIGHLIGHTER,
            activeColor = Color(readerState.activeColorHex),
            onClick = { onSelectTool(AnnotationTool.HIGHLIGHTER) },
            testTag = "tool_highlighter_button"
        )
        SidebarToolIconButton(
            icon = OrionIcons.FormatUnderlined,
            label = "Underline",
            selected = readerState.activeTool == AnnotationTool.UNDERLINE,
            activeColor = Color(readerState.activeColorHex),
            onClick = { onSelectTool(AnnotationTool.UNDERLINE) },
            testTag = "tool_underline_button"
        )
        SidebarToolIconButton(
            icon = OrionIcons.StickyNote2,
            label = "Sticky Note",
            selected = readerState.activeTool == AnnotationTool.STICKY_NOTE,
            activeColor = Color(readerState.activeColorHex),
            onClick = { onSelectTool(AnnotationTool.STICKY_NOTE) },
            testTag = "tool_sticky_note_button"
        )
        SidebarToolIconButton(
            icon = OrionIcons.AutoFixNormal,
            label = "Eraser",
            selected = readerState.activeTool == AnnotationTool.ERASER,
            activeColor = Color(0xFFD99B93),
            onClick = { onSelectTool(AnnotationTool.ERASER) },
            testTag = "tool_eraser_button"
        )

        // Color Swatch Cycler
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable {
                    colorIndex = (colorIndex + 1) % AnnotationColorPalette.size
                    onSelectColor(AnnotationColorPalette[colorIndex])
                }
                .testTag("tool_color_swatch_button"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(readerState.activeColorHex))
                    .border(2.dp, Color.White, CircleShape)
            )
        }

        IconButton(
            onClick = onUndo,
            modifier = Modifier
                .size(36.dp)
                .testTag("tool_undo_button")
        ) {
            Icon(
                imageVector = OrionIcons.Undo,
                contentDescription = "Undo annotation",
                tint = Color(0xFFBDB6B3),
                modifier = Modifier.size(18.dp)
            )
        }

        IconButton(
            onClick = onRedo,
            modifier = Modifier
                .size(36.dp)
                .testTag("tool_redo_button")
        ) {
            Icon(
                imageVector = OrionIcons.Redo,
                contentDescription = "Redo annotation",
                tint = Color(0xFFBDB6B3),
                modifier = Modifier.size(18.dp)
            )
        }

        IconButton(
            onClick = onExportAnnotatedPdf,
            enabled = !readerState.isExportingAnnotatedPdf,
            modifier = Modifier
                .size(36.dp)
                .testTag("tool_export_annotated_pdf_button")
        ) {
            Icon(
                imageVector = OrionIcons.SaveAlt,
                contentDescription = "Export annotated PDF copy",
                tint = Color(0xFFD99B93),
                modifier = Modifier.size(18.dp)
            )
        }
    }

    val coreButtonsContent: @Composable () -> Unit = {
        IconButton(
            onClick = onToggleAnnotationSidebar,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (readerState.isAnnotationSidebarExpanded) Color(0xFF422927)
                    else Color.Transparent
                )
                .testTag("sidebar_toggle_annotations_button")
        ) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = "Toggle annotation tools",
                tint = if (readerState.isAnnotationSidebarExpanded) Color(0xFFD99B93) else Color(0xFFBDB6B3),
                modifier = Modifier.size(21.dp)
            )
        }

        IconButton(
            onClick = onToggleScrollLock,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (readerState.isScrollLocked) Color(0xFF422927) else Color.Transparent)
                .testTag("sidebar_lock_button")
        ) {
            Icon(
                imageVector = if (readerState.isScrollLocked) Icons.Default.Lock else OrionIcons.LockOpen,
                contentDescription = "Lock or unlock scrolling",
                tint = if (readerState.isScrollLocked) Color(0xFFD99B93) else Color(0xFFBDB6B3),
                modifier = Modifier.size(21.dp)
            )
        }

        IconButton(
            onClick = onRotatePage,
            modifier = Modifier
                .size(40.dp)
                .testTag("sidebar_rotate_button")
        ) {
            Icon(
                imageVector = OrionIcons.RotateRight,
                contentDescription = "Rotate page 90 degrees",
                tint = if (readerState.pageRotationDegrees != 0) Color(0xFFD99B93) else Color(0xFFBDB6B3),
                modifier = Modifier.size(22.dp)
            )
        }

        IconButton(
            onClick = onToggleLandscape,
            modifier = Modifier
                .size(40.dp)
                .testTag("sidebar_orientation_button")
        ) {
            Icon(
                imageVector = OrionIcons.ScreenRotation,
                contentDescription = "Toggle landscape orientation",
                tint = if (readerState.isLandscapeLocked) Color(0xFFD99B93) else Color(0xFFBDB6B3),
                modifier = Modifier.size(21.dp)
            )
        }
    }

    if (isCompactHeight) {
        // Landscape / Compact Height layout: horizontal pills so ALL buttons stay 100% visible & unclipped!
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(visible = readerState.isAnnotationSidebarExpanded) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Color(0xFF221E1E).copy(alpha = 0.94f),
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .height(48.dp)
                        .widthIn(max = 520.dp)
                        .border(0.8.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(26.dp))
                        .testTag("sidebar_annotation_dock")
                ) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        annotationToolsContent()
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color(0xFF221E1E).copy(alpha = 0.94f),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .height(48.dp)
                    .border(0.8.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(26.dp))
                    .testTag("floating_left_sidebar")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    coreButtonsContent()
                }
            }
        }
    } else {
        // Portrait layout: vertical floating pills on the left edge with bounded scrollable annotation dock
        val dockMaxHeight = (maxAvailableHeight - 400.dp).coerceIn(140.dp, 420.dp)
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimatedVisibility(visible = readerState.isAnnotationSidebarExpanded) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFF221E1E).copy(alpha = 0.94f),
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .width(54.dp)
                        .heightIn(max = dockMaxHeight)
                        .border(0.8.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                        .testTag("sidebar_annotation_dock")
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        annotationToolsContent()
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(30.dp),
                color = Color(0xFF221E1E).copy(alpha = 0.94f),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .width(54.dp)
                    .border(0.8.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(30.dp))
                    .testTag("floating_left_sidebar")
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    coreButtonsContent()
                }
            }
        }
    }
}

@Composable
private fun SidebarToolIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (selected) activeColor.copy(alpha = 0.25f) else Color.Transparent)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) activeColor else Color(0xFFBDB6B3),
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun ReflowReaderView(
    doc: PdfDocumentEntity,
    currentPage: Int,
    readingTheme: ReadingTheme,
    fontSizeSp: Float,
    isChromeVisible: Boolean,
    onAdjustFontSize: (Float) -> Unit,
    onPageChange: (Int) -> Unit,
    onOpenSaveQuoteDialog: () -> Unit,
    onToggleChrome: () -> Unit
) {
    val context = LocalContext.current
    var pageText by remember(doc.filePath, currentPage) {
        mutableStateOf("Extracting text from Page ${currentPage + 1}…")
    }

    LaunchedEffect(doc.filePath, currentPage, doc.pageCount) {
        pageText = PdfTextExtractor.extractPageText(context, doc.filePath, currentPage, doc.pageCount)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(readingTheme.previewBg)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = if (isChromeVisible) 96.dp else 20.dp,
                bottom = if (isChromeVisible) 88.dp else 20.dp
            )
    ) {
        // Reflow toolbar
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF221E1E),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Text Reflow • Page ${currentPage + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFECE6E4),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onAdjustFontSize(-2f) }, modifier = Modifier.size(32.dp)) {
                    Icon(OrionIcons.Remove, contentDescription = "Smaller text", tint = Color(0xFFECE6E4))
                }
                Text(
                    text = "${fontSizeSp.toInt()}sp",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFD99B93),
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                IconButton(onClick = { onAdjustFontSize(2f) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Larger text", tint = Color(0xFFECE6E4))
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onOpenSaveQuoteDialog, modifier = Modifier.size(32.dp)) {
                    Icon(OrionIcons.FormatQuote, contentDescription = "Save quote from page", tint = Color(0xFFD99B93))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onToggleChrome() })
                }
        ) {
            SelectionContainer {
                Text(
                    text = pageText,
                    fontSize = fontSizeSp.sp,
                    lineHeight = (fontSizeSp * 1.55f).sp,
                    color = readingTheme.previewText
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { if (currentPage > 0) onPageChange(currentPage - 1) },
                    enabled = currentPage > 0
                ) {
                    Text("Previous Page")
                }
                Button(
                    onClick = { if (currentPage < doc.pageCount - 1) onPageChange(currentPage + 1) },
                    enabled = currentPage < doc.pageCount - 1
                ) {
                    Text("Next Page")
                }
            }
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun ReaderBottomBar(
    currentPage: Int,
    pageCount: Int,
    isReflowMode: Boolean,
    isBookmarked: Boolean,
    onOpenContents: () -> Unit,
    onToggleReflow: () -> Unit,
    onOpenJumpToPage: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenDisplaySettings: () -> Unit
) {
    Surface(
        color = Color(0xFF1A1717).copy(alpha = 0.96f),
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReaderBottomBarAction(
                icon = OrionIcons.FormatListBulleted,
                label = "Contents",
                active = false,
                onClick = onOpenContents,
                testTag = "reader_bottom_contents"
            )

            ReaderBottomBarAction(
                icon = OrionIcons.Article,
                label = "Reflow",
                active = isReflowMode,
                onClick = onToggleReflow,
                testTag = "reader_bottom_reflow"
            )

            // Center Page Counter Pill
            Surface(
                shape = CircleShape,
                color = Color(0xFF252121),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onOpenJumpToPage)
                    .testTag("reader_page_counter_pill")
            ) {
                Text(
                    text = "${currentPage + 1} / $pageCount",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                    color = Color(0xFFD6CECB),
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            ReaderBottomBarAction(
                icon = if (isBookmarked) OrionIcons.Bookmark else OrionIcons.BookmarkBorder,
                label = "Bookmark",
                active = isBookmarked,
                onClick = onToggleBookmark,
                testTag = "reader_bottom_bookmark"
            )

            ReaderBottomBarAction(
                icon = OrionIcons.Tune,
                label = "Display",
                active = false,
                onClick = onOpenDisplaySettings,
                testTag = "reader_bottom_display"
            )
        }
    }
}

@Composable
private fun ReaderBottomBarAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) Color(0xFFD99B93) else Color(0xFF9E9693),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
            color = if (active) Color(0xFFD99B93) else Color(0xFF9E9693),
            maxLines = 1,
            softWrap = false
        )
    }
}
