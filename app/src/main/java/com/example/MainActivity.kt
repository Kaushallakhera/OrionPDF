package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.PdfDocumentEntity
import com.example.data.PdfRepository
import com.example.ui.AppSettingsAndSideloadDialog
import com.example.ui.ContentsBookmarksQuotesSheet
import com.example.ui.CreateOrMoveFolderDialog
import com.example.ui.DocumentActionBottomSheet
import com.example.ui.FileInfoDialog
import com.example.ui.HomeMoreBottomSheet
import com.example.ui.LibraryScreen
import com.example.ui.ProVersionShowcaseScreen
import com.example.ui.ReaderScreen
import com.example.ui.ReadingSettingsBottomSheet
import com.example.ui.theme.OrionPdfTheme
import com.example.viewmodel.PdfAppViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = PdfRepository(database.pdfDao())

        setContent {
            val appViewModel: PdfAppViewModel = viewModel(
                factory = PdfAppViewModel.Factory(applicationContext, repository)
            )

            // Handle external PDF VIEW intent if launched from file manager
            LaunchedEffect(intent) {
                if (intent?.action == Intent.ACTION_VIEW) {
                    intent.data?.let { uri ->
                        appViewModel.importPdfFromUri(applicationContext, uri, openImmediately = true)
                    }
                }
            }

            OrionPdfRootApp(viewModel = appViewModel)
        }
    }
}

@Composable
fun OrionPdfRootApp(viewModel: PdfAppViewModel) {
    val context = LocalContext.current
    val libraryState by viewModel.libraryState.collectAsStateWithLifecycle()
    val settingsState by viewModel.readerSettings.collectAsStateWithLifecycle()
    val readerState by viewModel.activeReader.collectAsStateWithLifecycle()

    val recentDocuments by viewModel.recentDocuments.collectAsStateWithLifecycle()
    val filteredDocuments by viewModel.filteredDocuments.collectAsStateWithLifecycle()
    val folderGroups by viewModel.folderGroups.collectAsStateWithLifecycle()

    val activeDocBookmarks by viewModel.activeDocumentBookmarks.collectAsStateWithLifecycle()
    val activeDocQuotes by viewModel.activeDocumentQuotes.collectAsStateWithLifecycle()
    val activeDocAnnotations by viewModel.activeDocumentAnnotations.collectAsStateWithLifecycle()
    val allBookmarks by viewModel.allBookmarks.collectAsStateWithLifecycle()
    val allQuotes by viewModel.allQuotes.collectAsStateWithLifecycle()

    var showHomeMoreSheet by remember { mutableStateOf(false) }
    var showProVersionScreen by remember { mutableStateOf(false) }
    var showReadingSettingsSheet by remember { mutableStateOf(false) }
    var showAppSettingsDialog by remember { mutableStateOf(false) }
    var documentForActionSheet by remember { mutableStateOf<PdfDocumentEntity?>(null) }
    var documentForFileInfo by remember { mutableStateOf<PdfDocumentEntity?>(null) }
    var documentForContentsSheet by remember { mutableStateOf<PdfDocumentEntity?>(null) }
    var contentsInitialTab by remember { mutableIntStateOf(0) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var documentToMoveFolder by remember { mutableStateOf<PdfDocumentEntity?>(null) }
    var pendingNotificationDoc by remember { mutableStateOf<PdfDocumentEntity?>(null) }

    // System PDF Picker (Storage Access Framework)
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importPdfFromUri(context, uri, openImmediately = true)
        }
    }

    // Android 13+ POST_NOTIFICATIONS runtime permission for Pin Notification
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        pendingNotificationDoc?.let { doc ->
            viewModel.pinDocumentNotification(context, doc)
            pendingNotificationDoc = null
        }
    }

    // Auto-dismiss status toast
    LaunchedEffect(libraryState.statusMessage) {
        if (libraryState.statusMessage != null) {
            delay(2600L)
            viewModel.showStatusMessage(null)
        }
    }

    OrionPdfTheme(themeMode = settingsState.appThemeMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                showProVersionScreen -> {
                    ProVersionShowcaseScreen(
                        onBack = { showProVersionScreen = false },
                        onRestoreOrContinue = {
                            showProVersionScreen = false
                            viewModel.showStatusMessage("All Pro features are unlocked and active")
                        }
                    )
                }

                readerState.document != null -> {
                    ReaderScreen(
                        readerState = readerState,
                        settingsState = settingsState,
                        bookmarks = activeDocBookmarks,
                        annotations = activeDocAnnotations,
                        onBack = { viewModel.closeDocument() },
                        onPageChanged = { viewModel.onPageChanged(it) },
                        onToggleScrollLock = { viewModel.toggleScrollLock() },
                        onRotatePage = { viewModel.rotatePage90() },
                        onToggleLandscape = { viewModel.toggleLandscapeLock() },
                        onToggleReflow = { viewModel.toggleReflowMode() },
                        onAdjustReflowFont = { viewModel.adjustReflowFontSize(it) },
                        onToggleAnnotationSidebar = { viewModel.toggleAnnotationSidebar() },
                        onSelectAnnotationTool = { viewModel.selectAnnotationTool(it) },
                        onSelectAnnotationColor = { viewModel.selectAnnotationColor(it) },
                        onAddStrokeAnnotation = { page, pts -> viewModel.addStrokeAnnotation(page, pts) },
                        onAddStickyNote = { page, nx, ny, note -> viewModel.addStickyNoteAnnotation(page, nx, ny, note) },
                        onDeleteAnnotation = { viewModel.deleteAnnotation(it) },
                        onUndoAnnotation = { viewModel.undoLastAnnotation() },
                        onRedoAnnotation = { viewModel.redoLastAnnotation() },
                        onToggleBookmark = { viewModel.toggleBookmarkOnCurrentPage() },
                        onSaveQuote = { quote, note, page -> viewModel.saveQuote(quote, note, page) },
                        onOpenContentsSheet = {
                            contentsInitialTab = 0
                            documentForContentsSheet = readerState.document
                        },
                        onOpenReadingSettingsSheet = { showReadingSettingsSheet = true },
                        onOpenDocumentMoreSheet = { documentForActionSheet = it },
                        onTogglePdfSearch = { viewModel.setPdfSearchOpen(it) },
                        onPdfSearchQueryChange = { viewModel.updatePdfSearchQuery(it) },
                        onNavigateSearchMatch = { viewModel.navigatePdfSearchMatch(it) },
                        onStopAutoScroll = { viewModel.setAutoScroll(false) },
                        onAdjustAutoScrollSpeed = { viewModel.adjustAutoScrollSpeed(it) },
                        onStopReadAloud = { viewModel.setReadAloud(false) },
                        onAdjustReadAloudSpeed = { viewModel.adjustReadAloudSpeed(it) }
                    )
                }

                else -> {
                    LibraryScreen(
                        libraryState = libraryState,
                        recentDocuments = recentDocuments,
                        filteredDocuments = filteredDocuments,
                        folderGroups = folderGroups,
                        onSelectTab = { viewModel.selectTab(it) },
                        onSelectFoldersSegment = { viewModel.selectFoldersSegment(it) },
                        onOpenFolderGroup = { viewModel.openFolderGroup(it) },
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onToggleSearchActive = { viewModel.setSearchActive(it) },
                        onSortChange = { viewModel.updateSort(it) },
                        onToggleGridView = { viewModel.toggleGridView() },
                        onStorageFilterChange = { viewModel.setStorageFilter(it) },
                        onOpenDocument = { viewModel.openDocument(it) },
                        onOpenDocumentMenu = { documentForActionSheet = it },
                        onOpenAppMoreSheet = { showHomeMoreSheet = true },
                        onOpenCreateFolderDialog = { showCreateFolderDialog = true }
                    )
                }
            }

            // Home More Menu Sheet (Screenshot 5)
            if (showHomeMoreSheet) {
                HomeMoreBottomSheet(
                    appThemeMode = settingsState.appThemeMode,
                    onDismiss = { showHomeMoreSheet = false },
                    onOpenProVersionScreen = { showProVersionScreen = true },
                    onSelectAppTheme = { viewModel.setAppThemeMode(it) },
                    onPickPdfFromDevice = {
                        pdfPickerLauncher.launch(arrayOf("application/pdf"))
                    },
                    onRescanStorage = { viewModel.rescanStorage() },
                    onOpenSettingsDialog = { showAppSettingsDialog = true },
                    onLeaveRating = { viewModel.showStatusMessage("Thank you for rating Orion PDF Reader 5 ★!") },
                    onSendFeedback = { viewModel.showStatusMessage("Feedback channel ready • All Pro tools active") }
                )
            }

            // Reading Settings Bottom Sheet (Screenshot 3)
            if (showReadingSettingsSheet) {
                ReadingSettingsBottomSheet(
                    settings = settingsState,
                    onDismiss = { showReadingSettingsSheet = false },
                    onBrightnessChange = { viewModel.setBrightness(it) },
                    onUseSystemBrightnessChange = { viewModel.setUseSystemBrightness(it) },
                    onSelectReadingTheme = { viewModel.setReadingTheme(it) },
                    onVerticalScrollChange = { viewModel.setVerticalScroll(it) },
                    onSwipeByPageChange = { viewModel.setSwipeByPage(it) },
                    onShowScrollBarChange = { viewModel.setShowScrollBar(it) }
                )
            }

            // Document Action Bottom Sheet (Screenshot 6)
            documentForActionSheet?.let { doc ->
                DocumentActionBottomSheet(
                    doc = doc,
                    onDismiss = { documentForActionSheet = null },
                    onShare = { viewModel.shareDocument(context, doc) },
                    onOpenWith = { viewModel.openDocumentWithExternalApp(context, doc) },
                    onPrint = { viewModel.printDocument(context, doc) },
                    onShowFileInfo = { documentForFileInfo = doc },
                    onStartReadAloud = {
                        if (readerState.document?.id != doc.id) {
                            viewModel.openDocument(doc)
                        }
                        viewModel.setReadAloud(true)
                    },
                    onStartAutoScroll = {
                        if (readerState.document?.id != doc.id) {
                            viewModel.openDocument(doc)
                        }
                        viewModel.setAutoScroll(true)
                    },
                    onOpenBookmarks = {
                        contentsInitialTab = 1
                        documentForContentsSheet = doc
                    },
                    onOpenQuotes = {
                        contentsInitialTab = 2
                        documentForContentsSheet = doc
                    },
                    onPinNotification = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        ) {
                            pendingNotificationDoc = doc
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.pinDocumentNotification(context, doc)
                        }
                    },
                    onShowInFolder = { viewModel.showDocumentInFolder(doc) },
                    onMoveToFolder = { documentToMoveFolder = doc },
                    onDelete = { viewModel.deleteDocument(doc) }
                )
            }

            // Contents / Bookmarks / Quotes / Annotations Sheet
            documentForContentsSheet?.let { doc ->
                val isCurrentlyOpenDoc = readerState.document?.id == doc.id
                val docBookmarks = if (isCurrentlyOpenDoc) activeDocBookmarks else allBookmarks.filter { it.documentId == doc.id }
                val docQuotes = if (isCurrentlyOpenDoc) activeDocQuotes else allQuotes.filter { it.documentId == doc.id }

                ContentsBookmarksQuotesSheet(
                    doc = doc,
                    initialTab = contentsInitialTab,
                    bookmarks = docBookmarks,
                    quotes = docQuotes,
                    annotations = if (isCurrentlyOpenDoc) activeDocAnnotations else emptyList(),
                    onDismiss = { documentForContentsSheet = null },
                    onJumpToPage = { pageIdx ->
                        if (isCurrentlyOpenDoc) {
                            viewModel.onPageChanged(pageIdx)
                        } else {
                            viewModel.openDocument(doc, initialPage = pageIdx)
                        }
                    },
                    onDeleteBookmark = { viewModel.deleteBookmark(it) },
                    onDeleteQuote = { viewModel.deleteQuote(it) },
                    onDeleteAnnotation = { viewModel.deleteAnnotation(it) }
                )
            }

            // File Info Dialog
            documentForFileInfo?.let { doc ->
                FileInfoDialog(
                    doc = doc,
                    onDismiss = { documentForFileInfo = null }
                )
            }

            // App Settings & APK Sideload Guide Dialog
            if (showAppSettingsDialog) {
                AppSettingsAndSideloadDialog(
                    settings = settingsState,
                    onAutoResumeChange = { viewModel.setAutoResumeLastPdf(it) },
                    onVerticalScrollChange = { viewModel.setVerticalScroll(it) },
                    onShowScrollBarChange = { viewModel.setShowScrollBar(it) },
                    onDismiss = { showAppSettingsDialog = false }
                )
            }

            // Create Custom Folder Dialog
            if (showCreateFolderDialog) {
                CreateOrMoveFolderDialog(
                    title = "Create Custom Folder",
                    existingFolders = emptyList(),
                    isMoveMode = false,
                    onSelectExistingFolder = {},
                    onCreateNewFolder = { viewModel.createCustomFolder(it) },
                    onDismiss = { showCreateFolderDialog = false }
                )
            }

            // Move Document to Folder Dialog
            documentToMoveFolder?.let { doc ->
                CreateOrMoveFolderDialog(
                    title = "Move '${doc.title.take(22)}' to Folder",
                    existingFolders = libraryState.customFolders,
                    isMoveMode = true,
                    onSelectExistingFolder = { folder ->
                        viewModel.moveDocumentToFolder(doc, folder)
                    },
                    onCreateNewFolder = { folder ->
                        viewModel.createCustomFolder(folder)
                    },
                    onDismiss = { documentToMoveFolder = null }
                )
            }

            // Floating Status Toast
            AnimatedVisibility(
                visible = libraryState.statusMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp, start = 24.dp, end = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF2E2625),
                    tonalElevation = 8.dp
                ) {
                    Text(
                        text = libraryState.statusMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFECE6E4),
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
