package com.example.ui

import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AnnotationEntity
import com.example.data.AnnotationTool
import com.example.data.BookmarkEntity
import com.example.data.PdfDocumentEntity
import com.example.data.ReadingTheme
import com.example.data.parseNormalizedPoints
import com.example.pdf.BundledPdfSeeder
import com.example.pdf.PdfRenderEngine
import com.example.viewmodel.ActiveReaderState
import com.example.viewmodel.ReaderSettingsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
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
    onAdjustReadAloudSpeed: (Float) -> Unit
) {
    val doc = readerState.document ?: return
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        if (readerState.isPdfSearchOpen) {
            onTogglePdfSearch(false)
        } else if (readerState.activeTool != AnnotationTool.NONE) {
            onSelectAnnotationTool(AnnotationTool.NONE)
        } else {
            onBack()
        }
    }

    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = readerState.currentPage)
    val pagerState = rememberPagerState(
        initialPage = readerState.currentPage,
        pageCount = { doc.pageCount.coerceAtLeast(1) }
    )

    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var showJumpPageDialog by remember { mutableStateOf(false) }
    var pendingStickyNoteTarget by remember { mutableStateOf<Triple<Int, Float, Float>?>(null) }
    var selectedStickyNoteToView by remember { mutableStateOf<AnnotationEntity?>(null) }
    var showAddQuoteDialog by remember { mutableStateOf(false) }

    // Synchronize LazyList visible item -> ViewModel currentPage
    val visibleListPage by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex }
    }

    LaunchedEffect(visibleListPage, settingsState.swipeByPage) {
        if (!settingsState.swipeByPage) {
            onPageChanged(visibleListPage)
        }
    }

    LaunchedEffect(pagerState.currentPage, settingsState.swipeByPage) {
        if (settingsState.swipeByPage) {
            onPageChanged(pagerState.currentPage)
        }
    }

    // External page jump synchronization (e.g. from Contents, Bookmarks, or Search)
    LaunchedEffect(readerState.currentPage) {
        if (settingsState.swipeByPage) {
            if (pagerState.currentPage != readerState.currentPage) {
                pagerState.scrollToPage(readerState.currentPage)
            }
        } else {
            if (lazyListState.firstVisibleItemIndex != readerState.currentPage) {
                lazyListState.scrollToItem(readerState.currentPage)
            }
        }
    }

    // Auto-scroll loop
    LaunchedEffect(readerState.isAutoScrollActive, readerState.autoScrollSpeed, settingsState.swipeByPage) {
        if (readerState.isAutoScrollActive && !settingsState.swipeByPage) {
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
            val textToSpeak = BundledPdfSeeder.getExtractedPageText(
                doc.title,
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

    Scaffold(
        containerColor = Color(0xFF141111),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141111))
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
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
                            .padding(horizontal = 8.dp)
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

                // In-PDF Search Bar
                AnimatedVisibility(visible = readerState.isPdfSearchOpen) {
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
                                placeholder = { Text("Search text in ${doc.pageCount} pages…") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("in_pdf_search_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val matchCount = readerState.pdfSearchMatches.size
                            Text(
                                text = if (matchCount > 0) "${readerState.activeSearchMatchIndex + 1}/$matchCount" else "0/0",
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
        },
        bottomBar = {
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
    ) { innerPadding ->
        var containerHeightPx by remember { mutableFloatStateOf(1200f) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(settingsState.readingTheme.canvasBackground)
                .onSizeChanged { containerHeightPx = it.height.toFloat().coerceAtLeast(1f) }
        ) {

            if (readerState.isReflowMode) {
                ReflowReaderView(
                    doc = doc,
                    currentPage = readerState.currentPage,
                    readingTheme = settingsState.readingTheme,
                    fontSizeSp = readerState.reflowFontSizeSp,
                    onAdjustFontSize = onAdjustReflowFont,
                    onPageChange = onPageChanged,
                    onOpenSaveQuoteDialog = { showAddQuoteDialog = true }
                )
            } else {
                val zoomModifier = if (!readerState.isScrollLocked && readerState.activeTool == AnnotationTool.NONE) {
                    Modifier.pointerInput(readerState.isScrollLocked, readerState.activeTool) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (zoomScale * zoom).coerceIn(1f, 3.5f)
                            zoomScale = newScale
                            panOffset = if (newScale > 1.01f) {
                                val maxOffset = (newScale - 1f) * 500f
                                Offset(
                                    x = (panOffset.x + pan.x).coerceIn(-maxOffset, maxOffset),
                                    y = (panOffset.y + pan.y).coerceIn(-maxOffset, maxOffset)
                                )
                            } else {
                                Offset.Zero
                            }
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
                    if (settingsState.swipeByPage) {
                        if (settingsState.verticalScroll) {
                            VerticalPager(
                                state = pagerState,
                                userScrollEnabled = !readerState.isScrollLocked && readerState.activeTool == AnnotationTool.NONE,
                                modifier = Modifier.fillMaxSize()
                            ) { pageIndex ->
                                PdfSinglePageCanvas(
                                    doc = doc,
                                    pageIndex = pageIndex,
                                    readingTheme = settingsState.readingTheme,
                                    rotationDegrees = readerState.pageRotationDegrees,
                                    activeTool = readerState.activeTool,
                                    activeColorHex = readerState.activeColorHex,
                                    activeStrokeWidth = readerState.activeStrokeWidth,
                                    pageAnnotations = annotations.filter { it.pageIndex == pageIndex },
                                    onCommitStroke = { pts -> onAddStrokeAnnotation(pageIndex, pts) },
                                    onRequestStickyNote = { nx, ny ->
                                        pendingStickyNoteTarget = Triple(pageIndex, nx, ny)
                                    },
                                    onTapExistingStickyNote = { selectedStickyNoteToView = it },
                                    onEraseAnnotation = onDeleteAnnotation
                                )
                            }
                        } else {
                            HorizontalPager(
                                state = pagerState,
                                userScrollEnabled = !readerState.isScrollLocked && readerState.activeTool == AnnotationTool.NONE,
                                modifier = Modifier.fillMaxSize()
                            ) { pageIndex ->
                                PdfSinglePageCanvas(
                                    doc = doc,
                                    pageIndex = pageIndex,
                                    readingTheme = settingsState.readingTheme,
                                    rotationDegrees = readerState.pageRotationDegrees,
                                    activeTool = readerState.activeTool,
                                    activeColorHex = readerState.activeColorHex,
                                    activeStrokeWidth = readerState.activeStrokeWidth,
                                    pageAnnotations = annotations.filter { it.pageIndex == pageIndex },
                                    onCommitStroke = { pts -> onAddStrokeAnnotation(pageIndex, pts) },
                                    onRequestStickyNote = { nx, ny ->
                                        pendingStickyNoteTarget = Triple(pageIndex, nx, ny)
                                    },
                                    onTapExistingStickyNote = { selectedStickyNoteToView = it },
                                    onEraseAnnotation = onDeleteAnnotation
                                )
                            }
                        }
                    } else {
                        if (settingsState.verticalScroll) {
                            LazyColumn(
                                state = lazyListState,
                                userScrollEnabled = !readerState.isScrollLocked && readerState.activeTool == AnnotationTool.NONE,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 6.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("pdf_lazy_column")
                            ) {
                                items(doc.pageCount, key = { "page_$it" }) { pageIndex ->
                                    PdfSinglePageCanvas(
                                        doc = doc,
                                        pageIndex = pageIndex,
                                        readingTheme = settingsState.readingTheme,
                                        rotationDegrees = readerState.pageRotationDegrees,
                                        activeTool = readerState.activeTool,
                                        activeColorHex = readerState.activeColorHex,
                                        activeStrokeWidth = readerState.activeStrokeWidth,
                                        pageAnnotations = annotations.filter { it.pageIndex == pageIndex },
                                        onCommitStroke = { pts -> onAddStrokeAnnotation(pageIndex, pts) },
                                        onRequestStickyNote = { nx, ny ->
                                            pendingStickyNoteTarget = Triple(pageIndex, nx, ny)
                                        },
                                        onTapExistingStickyNote = { selectedStickyNoteToView = it },
                                        onEraseAnnotation = onDeleteAnnotation
                                    )
                                }
                            }
                        } else {
                            LazyRow(
                                state = lazyListState,
                                userScrollEnabled = !readerState.isScrollLocked && readerState.activeTool == AnnotationTool.NONE,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(doc.pageCount, key = { "hpage_$it" }) { pageIndex ->
                                    Box(modifier = Modifier.fillParentMaxWidth()) {
                                        PdfSinglePageCanvas(
                                            doc = doc,
                                            pageIndex = pageIndex,
                                            readingTheme = settingsState.readingTheme,
                                            rotationDegrees = readerState.pageRotationDegrees,
                                            activeTool = readerState.activeTool,
                                            activeColorHex = readerState.activeColorHex,
                                            activeStrokeWidth = readerState.activeStrokeWidth,
                                            pageAnnotations = annotations.filter { it.pageIndex == pageIndex },
                                            onCommitStroke = { pts -> onAddStrokeAnnotation(pageIndex, pts) },
                                            onRequestStickyNote = { nx, ny ->
                                                pendingStickyNoteTarget = Triple(pageIndex, nx, ny)
                                            },
                                            onTapExistingStickyNote = { selectedStickyNoteToView = it },
                                            onEraseAnnotation = onDeleteAnnotation
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Right Interactive Scroll Bar (when Show scroll bar is enabled)
            if (settingsState.showScrollBar && doc.pageCount > 1 && !readerState.isReflowMode) {
                val fraction = (readerState.currentPage.toFloat() / (doc.pageCount - 1).coerceAtLeast(1).toFloat())
                    .coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(24.dp)
                        .padding(vertical = 24.dp, horizontal = 4.dp)
                        .pointerInput(doc.pageCount) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val ratio = (change.position.y / containerHeightPx).coerceIn(0f, 1f)
                                val target = (ratio * (doc.pageCount - 1)).roundToInt()
                                coroutineScope.launch {
                                    onPageChanged(target)
                                }
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = ((containerHeightPx - 140f) * fraction).roundToInt().coerceAtLeast(0)
                                )
                            }
                            .width(6.dp)
                            .height(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD99B93).copy(alpha = 0.85f))
                    )
                }
            }

            // Active Tool Indicator Banner at Top Center
            AnimatedVisibility(
                visible = readerState.activeTool != AnnotationTool.NONE,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
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
                                AnnotationTool.STICKY_NOTE -> "Sticky Note: Tap anywhere on page to pin a note"
                                AnnotationTool.ERASER -> "Eraser: Drag or tap over annotations to erase"
                                else -> "${readerState.activeTool.label} active • Draw directly on PDF"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFECE6E4)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFD99B93),
                            modifier = Modifier.clickable { onSelectAnnotationTool(AnnotationTool.NONE) }
                        )
                    }
                }
            }

            // Floating Left Sidebar Pill (Screenshot 4 + Integrated Quick-Access Annotation Tools)
            FloatingReaderSidebarPill(
                readerState = readerState,
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
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 18.dp)
            )

            // Floating Auto-Scroll Controller Pill
            AnimatedVisibility(
                visible = readerState.isAutoScrollActive,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 20.dp)
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
                            imageVector = Icons.Default.KeyboardDoubleArrowDown,
                            contentDescription = null,
                            tint = Color(0xFFD99B93),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = { onAdjustAutoScrollSpeed(-0.25f) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Slower", tint = Color.White, modifier = Modifier.size(16.dp))
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
                    .padding(end = 16.dp, bottom = if (readerState.isAutoScrollActive) 72.dp else 20.dp)
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
                            imageVector = Icons.Default.Headphones,
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
                            Icon(Icons.Default.Stop, contentDescription = "Stop Read Aloud", tint = Color(0xFFD99B93), modifier = Modifier.size(18.dp))
                        }
                    }
                }
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
        val defaultPassage = remember(doc.title, readerState.currentPage) {
            BundledPdfSeeder.getExtractedPageText(doc.title, readerState.currentPage, doc.pageCount)
                .lines()
                .firstOrNull { it.length > 25 }
                ?: "Highlighted formula & revision passage from Page ${readerState.currentPage + 1}"
        }
        var quoteInput by remember { mutableStateOf(defaultPassage) }
        var noteInput by remember { mutableStateOf("") }

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

@Composable
private fun PdfSinglePageCanvas(
    doc: PdfDocumentEntity,
    pageIndex: Int,
    readingTheme: ReadingTheme,
    rotationDegrees: Int,
    activeTool: AnnotationTool,
    activeColorHex: Long,
    activeStrokeWidth: Float,
    pageAnnotations: List<AnnotationEntity>,
    onCommitStroke: (List<Offset>) -> Unit,
    onRequestStickyNote: (Float, Float) -> Unit,
    onTapExistingStickyNote: (AnnotationEntity) -> Unit,
    onEraseAnnotation: (AnnotationEntity) -> Unit
) {
    var bitmap by remember(doc.filePath, pageIndex) {
        mutableStateOf(PdfRenderEngine.getCachedBitmap(doc.filePath, pageIndex, 1088))
    }

    LaunchedEffect(doc.filePath, pageIndex) {
        if (bitmap == null) {
            bitmap = PdfRenderEngine.renderPage(doc.filePath, pageIndex, 1088)
        }
    }

    val colorFilter = remember(readingTheme) {
        readingTheme.toColorMatrix()?.let { ColorFilter.colorMatrix(it) }
    }

    val currentStrokePoints = remember { mutableStateListOf<Offset>() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(720f / 1020f)
            .background(readingTheme.previewBg)
            .rotate(rotationDegrees.toFloat()),
        contentAlignment = Alignment.Center
    ) {
        val bmp: Bitmap? = bitmap
        if (bmp != null && !bmp.isRecycled) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Page ${pageIndex + 1} of ${doc.title}",
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

        // Vector Annotation & Drawing Layer
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
                // Allow tapping sticky note pins even when in browse mode
                Modifier.pointerInput(pageAnnotations, pageIndex) {
                    detectTapGestures { tapOffset ->
                        val nx = tapOffset.x / size.width.toFloat()
                        val ny = tapOffset.y / size.height.toFloat()
                        pageAnnotations.firstOrNull { ann ->
                            ann.toolType == AnnotationTool.STICKY_NOTE.name &&
                                hypot(ann.anchorX - nx, ann.anchorY - ny) < 0.06f
                        }?.let { onTapExistingStickyNote(it) }
                    }
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

            // Draw saved annotations
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

            // Draw active in-progress stroke
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
        }
    }
}

@Composable
private fun FloatingReaderSidebarPill(
    readerState: ActiveReaderState,
    onToggleScrollLock: () -> Unit,
    onRotatePage: () -> Unit,
    onToggleLandscape: () -> Unit,
    onToggleAnnotationSidebar: () -> Unit,
    onSelectTool: (AnnotationTool) -> Unit,
    onSelectColor: (Long) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var colorIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Expandable Integrated Annotation Tools Pill
        AnimatedVisibility(visible = readerState.isAnnotationSidebarExpanded) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF221E1E).copy(alpha = 0.94f),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .width(54.dp)
                    .border(0.8.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                    .testTag("sidebar_annotation_dock")
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SidebarToolIconButton(
                        icon = Icons.Default.Edit,
                        label = "Pen",
                        selected = readerState.activeTool == AnnotationTool.PEN,
                        activeColor = Color(readerState.activeColorHex),
                        onClick = { onSelectTool(AnnotationTool.PEN) },
                        testTag = "tool_pen_button"
                    )
                    SidebarToolIconButton(
                        icon = Icons.Default.Brush,
                        label = "Marker",
                        selected = readerState.activeTool == AnnotationTool.MARKER,
                        activeColor = Color(readerState.activeColorHex),
                        onClick = { onSelectTool(AnnotationTool.MARKER) },
                        testTag = "tool_marker_button"
                    )
                    SidebarToolIconButton(
                        icon = Icons.Default.Highlight,
                        label = "Highlighter",
                        selected = readerState.activeTool == AnnotationTool.HIGHLIGHTER,
                        activeColor = Color(readerState.activeColorHex),
                        onClick = { onSelectTool(AnnotationTool.HIGHLIGHTER) },
                        testTag = "tool_highlighter_button"
                    )
                    SidebarToolIconButton(
                        icon = Icons.Default.FormatUnderlined,
                        label = "Underline",
                        selected = readerState.activeTool == AnnotationTool.UNDERLINE,
                        activeColor = Color(readerState.activeColorHex),
                        onClick = { onSelectTool(AnnotationTool.UNDERLINE) },
                        testTag = "tool_underline_button"
                    )
                    SidebarToolIconButton(
                        icon = Icons.Default.StickyNote2,
                        label = "Sticky Note",
                        selected = readerState.activeTool == AnnotationTool.STICKY_NOTE,
                        activeColor = Color(readerState.activeColorHex),
                        onClick = { onSelectTool(AnnotationTool.STICKY_NOTE) },
                        testTag = "tool_sticky_note_button"
                    )
                    SidebarToolIconButton(
                        icon = Icons.Default.AutoFixNormal,
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
                            imageVector = Icons.AutoMirrored.Filled.Undo,
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
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo annotation",
                            tint = Color(0xFFBDB6B3),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Core Floating Pill matching Screenshot 4 (Lock, Rotate, Screen Orientation + Annotation Toggle)
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
                        imageVector = if (readerState.isScrollLocked) Icons.Default.Lock else Icons.Default.LockOpen,
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
                        imageVector = Icons.Default.RotateRight,
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
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "Reset zoom & orientation",
                        tint = if (readerState.isLandscapeLocked) Color(0xFFD99B93) else Color(0xFFBDB6B3),
                        modifier = Modifier.size(21.dp)
                    )
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
    onAdjustFontSize: (Float) -> Unit,
    onPageChange: (Int) -> Unit,
    onOpenSaveQuoteDialog: () -> Unit
) {
    val pageText = remember(doc.title, currentPage, doc.pageCount) {
        BundledPdfSeeder.getExtractedPageText(doc.title, currentPage, doc.pageCount)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(readingTheme.previewBg)
            .padding(horizontal = 20.dp, vertical = 14.dp)
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
                    Icon(Icons.Default.Remove, contentDescription = "Smaller text", tint = Color(0xFFECE6E4))
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
                    Icon(Icons.Default.FormatQuote, contentDescription = "Save quote from page", tint = Color(0xFFD99B93))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = pageText,
                fontSize = fontSizeSp.sp,
                lineHeight = (fontSizeSp * 1.55f).sp,
                color = readingTheme.previewText
            )
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
            Spacer(modifier = Modifier.height(72.dp))
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
        color = Color(0xFF1A1717),
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReaderBottomBarAction(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                label = "Contents",
                active = false,
                onClick = onOpenContents,
                testTag = "reader_bottom_contents"
            )

            ReaderBottomBarAction(
                icon = Icons.AutoMirrored.Filled.Article,
                label = "Reflow",
                active = isReflowMode,
                onClick = onToggleReflow,
                testTag = "reader_bottom_reflow"
            )

            // Center Page Counter Pill matching Screenshot 4 ("3 / 14")
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
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    color = Color(0xFFD6CECB),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }

            ReaderBottomBarAction(
                icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                label = "Bookmark",
                active = isBookmarked,
                onClick = onToggleBookmark,
                testTag = "reader_bottom_bookmark"
            )

            ReaderBottomBarAction(
                icon = Icons.Default.Tune,
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
            .padding(horizontal = 10.dp, vertical = 4.dp)
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
            style = MaterialTheme.typography.labelMedium,
            color = if (active) Color(0xFFD99B93) else Color(0xFF9E9693)
        )
    }
}
