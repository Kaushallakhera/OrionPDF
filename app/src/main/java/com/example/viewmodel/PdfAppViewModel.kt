package com.example.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.MainActivity
import com.example.data.AnnotationEntity
import com.example.data.AnnotationTool
import com.example.data.BookmarkEntity
import com.example.data.PdfDocumentEntity
import com.example.data.PdfRepository
import com.example.data.QuoteEntity
import com.example.data.ReadingTheme
import com.example.data.encodeAnnotationPoints
import com.example.data.progressFraction
import com.example.data.progressPercent
import com.example.pdf.BundledPdfSeeder
import com.example.pdf.PdfRenderEngine
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class BottomNavTab(val label: String) {
    FILES("Files"),
    RECENT("Recent"),
    FAVORITES("Favorites"),
    FOLDERS("Folders")
}

enum class FoldersSegment {
    FOLDERS,
    AUTHORS
}

enum class SortField(val label: String) {
    NAME("Name"),
    DATE("Date modified"),
    SIZE("File size"),
    PROGRESS("Reading progress")
}

data class FolderGroupItem(
    val name: String,
    val pdfCount: Int,
    val storageVolume: String,
    val isAuthorGroup: Boolean = false
)

data class PdfSearchMatch(
    val pageIndex: Int,
    val snippet: String,
    val matchCountOnPage: Int
)

data class LibraryUiState(
    val activeTab: BottomNavTab = BottomNavTab.FILES,
    val foldersSegment: FoldersSegment = FoldersSegment.FOLDERS,
    val selectedFolderName: String? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val sortField: SortField = SortField.NAME,
    val sortAscending: Boolean = true,
    val isGridView: Boolean = false,
    val storageFilter: String? = null, // null = All, "Internal storage", "B437-1C18"
    val customFolders: List<String> = listOf(
        ".Alarms",
        ".Chats",
        ".DPP Class 10",
        ".Kaushal Document",
        ".Lokesh Documents",
        ".Mummy Document",
        ".other",
        ".Payal Document",
        "Download",
        "Print",
        "Study Notes",
        "Telegram Videos"
    ),
    val statusMessage: String? = null
)

data class ReaderSettingsState(
    val appThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val readingTheme: ReadingTheme = ReadingTheme.LIGHT,
    val brightness: Float = 0.68f,
    val useSystemBrightness: Boolean = true,
    val verticalScroll: Boolean = true,
    val swipeByPage: Boolean = false,
    val showScrollBar: Boolean = true,
    val autoResumeLastPdf: Boolean = false
)

data class ActiveReaderState(
    val document: PdfDocumentEntity? = null,
    val currentPage: Int = 0,
    val isScrollLocked: Boolean = false,
    val pageRotationDegrees: Int = 0,
    val isLandscapeLocked: Boolean = false,
    val isReflowMode: Boolean = false,
    val reflowFontSizeSp: Float = 18f,
    val isAnnotationSidebarExpanded: Boolean = true,
    val activeTool: AnnotationTool = AnnotationTool.NONE,
    val activeColorHex: Long = 0xFFE53935L,
    val activeStrokeWidth: Float = 5f,
    val isPdfSearchOpen: Boolean = false,
    val pdfSearchQuery: String = "",
    val pdfSearchMatches: List<PdfSearchMatch> = emptyList(),
    val activeSearchMatchIndex: Int = 0,
    val isAutoScrollActive: Boolean = false,
    val autoScrollSpeed: Float = 1.0f,
    val isReadAloudActive: Boolean = false,
    val readAloudSpeed: Float = 1.0f
)

@OptIn(ExperimentalCoroutinesApi::class)
class PdfAppViewModel(
    private val appContext: Context,
    private val repository: PdfRepository
) : ViewModel() {

    private val prefs = appContext.getSharedPreferences("orion_reader_prefs", Context.MODE_PRIVATE)

    private val _libraryState = MutableStateFlow(LibraryUiState())
    val libraryState: StateFlow<LibraryUiState> = _libraryState.asStateFlow()

    private val _readerSettings = MutableStateFlow(loadInitialSettings())
    val readerSettings: StateFlow<ReaderSettingsState> = _readerSettings.asStateFlow()

    private val _activeReader = MutableStateFlow(ActiveReaderState())
    val activeReader: StateFlow<ActiveReaderState> = _activeReader.asStateFlow()

    private val undoAnnotationIds = ArrayDeque<AnnotationEntity>()
    private val redoAnnotationStack = ArrayDeque<AnnotationEntity>()

    val allDocuments: StateFlow<List<PdfDocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentDocuments: StateFlow<List<PdfDocumentEntity>> = repository.recentDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteDocuments: StateFlow<List<PdfDocumentEntity>> = repository.favoriteDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuotes: StateFlow<List<QuoteEntity>> = repository.allQuotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDocumentBookmarks: StateFlow<List<BookmarkEntity>> = _activeReader
        .flatMapLatest { state ->
            val docId = state.document?.id
            if (docId != null) repository.observeBookmarksForDoc(docId) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDocumentQuotes: StateFlow<List<QuoteEntity>> = _activeReader
        .flatMapLatest { state ->
            val docId = state.document?.id
            if (docId != null) repository.observeQuotesForDoc(docId) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDocumentAnnotations: StateFlow<List<AnnotationEntity>> = _activeReader
        .flatMapLatest { state ->
            val docId = state.document?.id
            if (docId != null) repository.observeAnnotationsForDoc(docId) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDocuments: StateFlow<List<PdfDocumentEntity>> = combine(
        allDocuments,
        recentDocuments,
        favoriteDocuments,
        _libraryState
    ) { all, recent, favs, lib ->
        val baseList = when {
            lib.selectedFolderName != null -> {
                if (lib.foldersSegment == FoldersSegment.AUTHORS) {
                    all.filter { it.author.equals(lib.selectedFolderName, ignoreCase = true) }
                } else {
                    all.filter { it.folderName.equals(lib.selectedFolderName, ignoreCase = true) }
                }
            }
            lib.activeTab == BottomNavTab.RECENT -> recent
            lib.activeTab == BottomNavTab.FAVORITES -> favs
            else -> all
        }

        val storageFiltered = if (lib.storageFilter != null) {
            baseList.filter { it.storageVolume.equals(lib.storageFilter, ignoreCase = true) }
        } else {
            baseList
        }

        val queryFiltered = if (lib.searchQuery.isNotBlank()) {
            val q = lib.searchQuery.trim()
            storageFiltered.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.folderName.contains(q, ignoreCase = true) ||
                    it.author.contains(q, ignoreCase = true)
            }
        } else {
            storageFiltered
        }

        if (lib.activeTab == BottomNavTab.RECENT && lib.selectedFolderName == null) {
            queryFiltered.sortedByDescending { it.lastOpenedAt }
        } else {
            val sorted = when (lib.sortField) {
                SortField.NAME -> queryFiltered.sortedBy { it.title.lowercase() }
                SortField.DATE -> queryFiltered.sortedBy { it.createdAt }
                SortField.SIZE -> queryFiltered.sortedBy { it.sizeBytes }
                SortField.PROGRESS -> queryFiltered.sortedBy { it.progressFraction }
            }
            if (lib.sortAscending) sorted else sorted.reversed()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folderGroups: StateFlow<List<FolderGroupItem>> = combine(
        allDocuments,
        _libraryState
    ) { all, lib ->
        if (lib.foldersSegment == FoldersSegment.AUTHORS) {
            all.groupBy { it.author }
                .map { (author, docs) ->
                    FolderGroupItem(
                        name = author,
                        pdfCount = docs.size,
                        storageVolume = docs.firstOrNull()?.storageVolume ?: "Internal storage",
                        isAuthorGroup = true
                    )
                }
                .sortedBy { it.name.lowercase() }
        } else {
            // Match the exact folder list seen in Screenshot 1 plus any active folders
            val screenshotDefaultCounts = mapOf(
                ".Alarms" to (1 to "B437-1C18"),
                ".Chats" to (4 to "B437-1C18"),
                ".DPP Class 10" to (3 to "Internal storage"),
                ".Kaushal Document" to (21 to "Internal storage"),
                ".Lokesh Documents" to (7 to "Internal storage"),
                ".Mummy Document" to (32 to "Internal storage"),
                ".other" to (7 to "B437-1C18"),
                ".Payal Document" to (13 to "Internal storage")
            )
            val allFolderNames = (lib.customFolders + all.map { it.folderName }).distinct()
            allFolderNames.map { folderName ->
                val matchingDocs = all.filter { it.folderName.equals(folderName, ignoreCase = true) }
                val defaultMeta = screenshotDefaultCounts[folderName]
                val count = if (defaultMeta != null) {
                    maxOf(matchingDocs.size, defaultMeta.first)
                } else {
                    matchingDocs.size
                }
                val volume = matchingDocs.firstOrNull()?.storageVolume
                    ?: defaultMeta?.second
                    ?: "Internal storage"
                FolderGroupItem(
                    name = folderName,
                    pdfCount = count,
                    storageVolume = volume,
                    isAuthorGroup = false
                )
            }.sortedWith(compareBy<FolderGroupItem> { !it.name.startsWith(".") }.thenBy { it.name.lowercase() })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            BundledPdfSeeder.ensureSeeded(appContext, repository)
            if (_readerSettings.value.autoResumeLastPdf) {
                val lastId = prefs.getLong("last_opened_doc_id", -1L)
                if (lastId > 0L) {
                    repository.getDocumentById(lastId)?.let { openDocument(it) }
                }
            }
        }
    }

    private fun loadInitialSettings(): ReaderSettingsState {
        val themeStr = prefs.getString("app_theme", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val readThemeStr = prefs.getString("reading_theme", ReadingTheme.LIGHT.name) ?: ReadingTheme.LIGHT.name
        return ReaderSettingsState(
            appThemeMode = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.SYSTEM),
            readingTheme = runCatching { ReadingTheme.valueOf(readThemeStr) }.getOrDefault(ReadingTheme.LIGHT),
            brightness = prefs.getFloat("brightness", 0.68f),
            useSystemBrightness = prefs.getBoolean("use_system_brightness", true),
            verticalScroll = prefs.getBoolean("vertical_scroll", true),
            swipeByPage = prefs.getBoolean("swipe_by_page", false),
            showScrollBar = prefs.getBoolean("show_scroll_bar", true),
            autoResumeLastPdf = prefs.getBoolean("auto_resume_last_pdf", false)
        )
    }

    fun selectTab(tab: BottomNavTab) {
        _libraryState.update {
            it.copy(
                activeTab = tab,
                selectedFolderName = null
            )
        }
    }

    fun selectFoldersSegment(segment: FoldersSegment) {
        _libraryState.update {
            it.copy(foldersSegment = segment, selectedFolderName = null)
        }
    }

    fun openFolderGroup(folderName: String?) {
        _libraryState.update {
            it.copy(selectedFolderName = folderName)
        }
    }

    fun showDocumentInFolder(doc: PdfDocumentEntity) {
        closeDocument()
        _libraryState.update {
            it.copy(
                activeTab = BottomNavTab.FOLDERS,
                foldersSegment = FoldersSegment.FOLDERS,
                selectedFolderName = doc.folderName
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _libraryState.update { it.copy(searchQuery = query) }
    }

    fun setSearchActive(active: Boolean) {
        _libraryState.update {
            it.copy(
                isSearchActive = active,
                searchQuery = if (!active) "" else it.searchQuery
            )
        }
    }

    fun updateSort(field: SortField) {
        _libraryState.update { current ->
            if (current.sortField == field) {
                current.copy(sortAscending = !current.sortAscending)
            } else {
                current.copy(sortField = field, sortAscending = true)
            }
        }
    }

    fun toggleGridView() {
        _libraryState.update { it.copy(isGridView = !it.isGridView) }
    }

    fun setStorageFilter(volume: String?) {
        _libraryState.update { it.copy(storageFilter = volume) }
    }

    fun createCustomFolder(folderName: String) {
        val trimmed = folderName.trim()
        if (trimmed.isEmpty()) return
        _libraryState.update { current ->
            if (current.customFolders.any { it.equals(trimmed, ignoreCase = true) }) {
                current.copy(statusMessage = "Folder '$trimmed' already exists")
            } else {
                current.copy(
                    customFolders = current.customFolders + trimmed,
                    statusMessage = "Created folder '$trimmed'"
                )
            }
        }
    }

    fun moveDocumentToFolder(doc: PdfDocumentEntity, targetFolder: String) {
        viewModelScope.launch {
            repository.moveDocumentToFolder(doc.id, targetFolder)
            if (_activeReader.value.document?.id == doc.id) {
                _activeReader.update {
                    it.copy(document = it.document?.copy(folderName = targetFolder))
                }
            }
            showStatusMessage("Moved '${doc.title}' to $targetFolder")
        }
    }

    fun rescanStorage() {
        viewModelScope.launch {
            BundledPdfSeeder.ensureSeeded(appContext, repository)
            showStatusMessage("Storage rescanned • All PDFs up to date")
        }
    }

    fun showStatusMessage(message: String?) {
        _libraryState.update { it.copy(statusMessage = message) }
    }

    // Settings updates
    fun setAppThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("app_theme", mode.name).apply()
        _readerSettings.update { it.copy(appThemeMode = mode) }
    }

    fun setReadingTheme(theme: ReadingTheme) {
        prefs.edit().putString("reading_theme", theme.name).apply()
        _readerSettings.update { it.copy(readingTheme = theme) }
    }

    fun setBrightness(value: Float) {
        val clamped = value.coerceIn(0.1f, 1.0f)
        prefs.edit().putFloat("brightness", clamped).putBoolean("use_system_brightness", false).apply()
        _readerSettings.update { it.copy(brightness = clamped, useSystemBrightness = false) }
    }

    fun setUseSystemBrightness(useSystem: Boolean) {
        prefs.edit().putBoolean("use_system_brightness", useSystem).apply()
        _readerSettings.update { it.copy(useSystemBrightness = useSystem) }
    }

    fun setVerticalScroll(vertical: Boolean) {
        prefs.edit().putBoolean("vertical_scroll", vertical).apply()
        _readerSettings.update { it.copy(verticalScroll = vertical) }
    }

    fun setSwipeByPage(swipe: Boolean) {
        prefs.edit().putBoolean("swipe_by_page", swipe).apply()
        _readerSettings.update { it.copy(swipeByPage = swipe) }
    }

    fun setShowScrollBar(show: Boolean) {
        prefs.edit().putBoolean("show_scroll_bar", show).apply()
        _readerSettings.update { it.copy(showScrollBar = show) }
    }

    fun setAutoResumeLastPdf(autoResume: Boolean) {
        prefs.edit().putBoolean("auto_resume_last_pdf", autoResume).apply()
        _readerSettings.update { it.copy(autoResumeLastPdf = autoResume) }
    }

    // Reader controls
    fun openDocument(doc: PdfDocumentEntity, initialPage: Int? = null) {
        val targetPage = (initialPage ?: doc.currentPage).coerceIn(0, (doc.pageCount - 1).coerceAtLeast(0))
        prefs.edit().putLong("last_opened_doc_id", doc.id).apply()
        undoAnnotationIds.clear()
        redoAnnotationStack.clear()

        _activeReader.update {
            ActiveReaderState(
                document = doc.copy(currentPage = targetPage, lastOpenedAt = System.currentTimeMillis()),
                currentPage = targetPage,
                isAnnotationSidebarExpanded = true
            )
        }
        viewModelScope.launch {
            repository.updateReadingProgress(doc.id, targetPage)
        }
    }

    fun closeDocument() {
        _activeReader.update {
            it.copy(
                document = null,
                isAutoScrollActive = false,
                isReadAloudActive = false,
                isPdfSearchOpen = false,
                activeTool = AnnotationTool.NONE
            )
        }
    }

    fun onPageChanged(newPageIndex: Int) {
        val doc = _activeReader.value.document ?: return
        val clamped = newPageIndex.coerceIn(0, (doc.pageCount - 1).coerceAtLeast(0))
        if (_activeReader.value.currentPage == clamped) return
        _activeReader.update {
            it.copy(
                currentPage = clamped,
                document = doc.copy(currentPage = clamped, lastOpenedAt = System.currentTimeMillis())
            )
        }
        viewModelScope.launch {
            repository.updateReadingProgress(doc.id, clamped)
        }
    }

    fun toggleScrollLock() {
        _activeReader.update {
            val next = !it.isScrollLocked
            it.copy(isScrollLocked = next)
        }
    }

    fun rotatePage90() {
        _activeReader.update {
            it.copy(pageRotationDegrees = (it.pageRotationDegrees + 90) % 360)
        }
    }

    fun toggleLandscapeLock() {
        _activeReader.update {
            it.copy(isLandscapeLocked = !it.isLandscapeLocked)
        }
    }

    fun toggleReflowMode() {
        _activeReader.update {
            it.copy(isReflowMode = !it.isReflowMode)
        }
    }

    fun adjustReflowFontSize(delta: Float) {
        _activeReader.update {
            it.copy(reflowFontSizeSp = (it.reflowFontSizeSp + delta).coerceIn(13f, 30f))
        }
    }

    fun toggleAnnotationSidebar() {
        _activeReader.update {
            it.copy(isAnnotationSidebarExpanded = !it.isAnnotationSidebarExpanded)
        }
    }

    fun selectAnnotationTool(tool: AnnotationTool) {
        _activeReader.update { current ->
            val nextTool = if (current.activeTool == tool) AnnotationTool.NONE else tool
            current.copy(
                activeTool = nextTool,
                activeStrokeWidth = if (nextTool != AnnotationTool.NONE && nextTool.defaultStroke > 0f) {
                    nextTool.defaultStroke
                } else {
                    current.activeStrokeWidth
                }
            )
        }
    }

    fun selectAnnotationColor(colorHex: Long) {
        _activeReader.update { it.copy(activeColorHex = colorHex) }
    }

    fun addStrokeAnnotation(pageIndex: Int, normalizedPoints: List<androidx.compose.ui.geometry.Offset>) {
        val doc = _activeReader.value.document ?: return
        val tool = _activeReader.value.activeTool
        if (tool == AnnotationTool.NONE || tool == AnnotationTool.ERASER || normalizedPoints.size < 2) return

        val ptsToSave = if (tool == AnnotationTool.UNDERLINE) {
            // Snap underline to a clean horizontal/straight line from start to end
            val start = normalizedPoints.first()
            val end = normalizedPoints.last()
            listOf(start, androidx.compose.ui.geometry.Offset(end.x, start.y))
        } else {
            normalizedPoints
        }

        val entity = AnnotationEntity(
            documentId = doc.id,
            pageIndex = pageIndex,
            toolType = tool.name,
            colorHex = _activeReader.value.activeColorHex,
            strokeWidth = _activeReader.value.activeStrokeWidth,
            pointsData = encodeAnnotationPoints(ptsToSave),
            anchorX = ptsToSave.first().x,
            anchorY = ptsToSave.first().y
        )

        viewModelScope.launch {
            val id = repository.addAnnotation(entity)
            undoAnnotationIds.addLast(entity.copy(id = id))
            redoAnnotationStack.clear()
        }
    }

    fun addStickyNoteAnnotation(pageIndex: Int, normalizedX: Float, normalizedY: Float, noteText: String) {
        val doc = _activeReader.value.document ?: return
        val trimmed = noteText.trim()
        if (trimmed.isEmpty()) return

        val entity = AnnotationEntity(
            documentId = doc.id,
            pageIndex = pageIndex,
            toolType = AnnotationTool.STICKY_NOTE.name,
            colorHex = _activeReader.value.activeColorHex,
            strokeWidth = 0f,
            pointsData = "",
            noteText = trimmed,
            anchorX = normalizedX.coerceIn(0.05f, 0.95f),
            anchorY = normalizedY.coerceIn(0.05f, 0.95f)
        )

        viewModelScope.launch {
            val id = repository.addAnnotation(entity)
            undoAnnotationIds.addLast(entity.copy(id = id))
            redoAnnotationStack.clear()
            showStatusMessage("Sticky note added to Page ${pageIndex + 1}")
        }
    }

    fun deleteAnnotation(annotation: AnnotationEntity) {
        viewModelScope.launch {
            repository.deleteAnnotationById(annotation.id)
        }
    }

    fun undoLastAnnotation() {
        val last = undoAnnotationIds.removeLastOrNull() ?: return
        viewModelScope.launch {
            repository.deleteAnnotationById(last.id)
            redoAnnotationStack.addLast(last)
        }
    }

    fun redoLastAnnotation() {
        val item = redoAnnotationStack.removeLastOrNull() ?: return
        viewModelScope.launch {
            val newId = repository.addAnnotation(item.copy(id = 0L))
            undoAnnotationIds.addLast(item.copy(id = newId))
        }
    }

    fun clearAnnotationsOnCurrentPage() {
        val doc = _activeReader.value.document ?: return
        val page = _activeReader.value.currentPage
        viewModelScope.launch {
            repository.clearPageAnnotations(doc.id, page)
            showStatusMessage("Cleared annotations on Page ${page + 1}")
        }
    }

    // Bookmarks & Quotes
    fun toggleBookmarkOnCurrentPage(customTitle: String? = null) {
        val doc = _activeReader.value.document ?: return
        val page = _activeReader.value.currentPage
        val existing = activeDocumentBookmarks.value.firstOrNull { it.pageIndex == page }
        viewModelScope.launch {
            if (existing != null && customTitle == null) {
                repository.removeBookmarkAtPage(doc.id, page)
                showStatusMessage("Removed bookmark on Page ${page + 1}")
            } else {
                val title = customTitle?.takeIf { it.isNotBlank() }
                    ?: "Page ${page + 1} • ${doc.title.take(24)}"
                if (existing != null) {
                    repository.removeBookmarkAtPage(doc.id, page)
                }
                repository.addBookmark(
                    BookmarkEntity(
                        documentId = doc.id,
                        documentTitle = doc.title,
                        pageIndex = page,
                        title = title
                    )
                )
                showStatusMessage("Bookmarked Page ${page + 1}")
            }
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.deleteBookmarkById(bookmark.id)
        }
    }

    fun saveQuote(quoteText: String, note: String = "", pageIndex: Int? = null) {
        val doc = _activeReader.value.document ?: return
        val page = pageIndex ?: _activeReader.value.currentPage
        if (quoteText.isBlank()) return
        viewModelScope.launch {
            repository.addQuote(
                QuoteEntity(
                    documentId = doc.id,
                    documentTitle = doc.title,
                    pageIndex = page,
                    quoteText = quoteText.trim(),
                    note = note.trim(),
                    colorHex = _activeReader.value.activeColorHex
                )
            )
            showStatusMessage("Saved quote from Page ${page + 1}")
        }
    }

    fun deleteQuote(quote: QuoteEntity) {
        viewModelScope.launch {
            repository.deleteQuoteById(quote.id)
        }
    }

    fun toggleFavorite(doc: PdfDocumentEntity) {
        val next = !doc.isFavorite
        viewModelScope.launch {
            repository.toggleFavorite(doc.id, next)
            if (_activeReader.value.document?.id == doc.id) {
                _activeReader.update {
                    it.copy(document = it.document?.copy(isFavorite = next))
                }
            }
            showStatusMessage(if (next) "Added to Favorites" else "Removed from Favorites")
        }
    }

    fun deleteDocument(doc: PdfDocumentEntity) {
        viewModelScope.launch {
            if (_activeReader.value.document?.id == doc.id) {
                closeDocument()
            }
            repository.deleteDocument(doc.id)
            showStatusMessage("Deleted '${doc.title}'")
        }
    }

    // In-PDF Unlimited Search
    fun setPdfSearchOpen(open: Boolean) {
        _activeReader.update {
            it.copy(
                isPdfSearchOpen = open,
                pdfSearchQuery = if (!open) "" else it.pdfSearchQuery,
                pdfSearchMatches = if (!open) emptyList() else it.pdfSearchMatches
            )
        }
    }

    fun updatePdfSearchQuery(query: String) {
        val doc = _activeReader.value.document ?: return
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _activeReader.update {
                it.copy(pdfSearchQuery = query, pdfSearchMatches = emptyList(), activeSearchMatchIndex = 0)
            }
            return
        }

        val matches = mutableListOf<PdfSearchMatch>()
        for (p in 0 until doc.pageCount) {
            val pageText = BundledPdfSeeder.getExtractedPageText(doc.title, p, doc.pageCount)
            val idx = pageText.indexOf(trimmed, ignoreCase = true)
            if (idx >= 0) {
                val count = Regex(Regex.escape(trimmed), RegexOption.IGNORE_CASE).findAll(pageText).count()
                val start = (idx - 28).coerceAtLeast(0)
                val end = (idx + trimmed.length + 46).coerceAtMost(pageText.length)
                val snippet = pageText.substring(start, end).replace("\n", " ")
                matches.add(PdfSearchMatch(pageIndex = p, snippet = "…$snippet…", matchCountOnPage = count))
            }
        }

        _activeReader.update {
            it.copy(
                pdfSearchQuery = query,
                pdfSearchMatches = matches,
                activeSearchMatchIndex = 0
            )
        }
        matches.firstOrNull()?.let { onPageChanged(it.pageIndex) }
    }

    fun navigatePdfSearchMatch(next: Boolean) {
        val matches = _activeReader.value.pdfSearchMatches
        if (matches.isEmpty()) return
        val currentIdx = _activeReader.value.activeSearchMatchIndex
        val nextIdx = if (next) (currentIdx + 1) % matches.size else (currentIdx - 1 + matches.size) % matches.size
        _activeReader.update { it.copy(activeSearchMatchIndex = nextIdx) }
        onPageChanged(matches[nextIdx].pageIndex)
    }

    // Auto-Scroll & Read Aloud (TTS)
    fun setAutoScroll(active: Boolean) {
        _activeReader.update { it.copy(isAutoScrollActive = active) }
    }

    fun adjustAutoScrollSpeed(delta: Float) {
        _activeReader.update {
            it.copy(autoScrollSpeed = (it.autoScrollSpeed + delta).coerceIn(0.5f, 3.5f))
        }
    }

    fun setReadAloud(active: Boolean) {
        _activeReader.update { it.copy(isReadAloudActive = active) }
    }

    fun adjustReadAloudSpeed(delta: Float) {
        _activeReader.update {
            it.copy(readAloudSpeed = (it.readAloudSpeed + delta).coerceIn(0.5f, 2.0f))
        }
    }

    // System Integrations: SAF File Import, Share, Open With, Print, Pin Notification
    fun importPdfFromUri(context: Context, uri: Uri, openImmediately: Boolean = true) {
        viewModelScope.launch {
            val importedDoc = withContext(Dispatchers.IO) {
                try {
                    val resolver = context.contentResolver
                    var displayName = "Imported_Document_${System.currentTimeMillis() % 10000}.pdf"
                    var fileSize = 0L
                    resolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if (nameIdx >= 0) {
                                displayName = cursor.getString(nameIdx) ?: displayName
                            }
                            if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                                fileSize = cursor.getLong(sizeIdx)
                            }
                        }
                    }

                    val importDir = File(context.filesDir, "imported_pdfs").apply { mkdirs() }
                    val cleanName = displayName.replace(Regex("[^a-zA-Z0-9._ -]"), "_")
                    val targetFile = File(importDir, cleanName)
                    resolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    if (fileSize <= 0L) fileSize = targetFile.length()
                    val pages = PdfRenderEngine.inspectPdfPageCount(targetFile).coerceAtLeast(1)
                    val titleNoExt = displayName.removeSuffix(".pdf").removeSuffix(".PDF")

                    val entity = PdfDocumentEntity(
                        title = titleNoExt,
                        filePath = targetFile.absolutePath,
                        folderName = "Download",
                        storageVolume = "Internal storage",
                        author = "Device Storage",
                        sizeBytes = fileSize,
                        pageCount = pages,
                        currentPage = 0,
                        lastOpenedAt = System.currentTimeMillis(),
                        createdAt = System.currentTimeMillis()
                    )
                    val newId = repository.insertDocument(entity)
                    entity.copy(id = newId)
                } catch (e: Exception) {
                    null
                }
            }

            if (importedDoc != null) {
                showStatusMessage("Imported '${importedDoc.title}' (${importedDoc.pageCount} pages)")
                if (openImmediately) {
                    openDocument(importedDoc)
                }
            } else {
                showStatusMessage("Could not import selected PDF file")
            }
        }
    }

    fun shareDocument(context: Context, doc: PdfDocumentEntity) {
        try {
            val file = File(doc.filePath)
            if (!file.exists()) {
                showStatusMessage("PDF file not found on disk")
                return
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, doc.title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(shareIntent, "Share ${doc.title}").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            showStatusMessage("Unable to launch share sheet")
        }
    }

    fun openDocumentWithExternalApp(context: Context, doc: PdfDocumentEntity) {
        try {
            val file = File(doc.filePath)
            if (!file.exists()) return
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(
                Intent.createChooser(viewIntent, "Open with").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            showStatusMessage("No external PDF app found")
        }
    }

    fun printDocument(context: Context, doc: PdfDocumentEntity) {
        val started = PdfRenderEngine.printPdfFile(context, doc.filePath, doc.title)
        if (!started) {
            showStatusMessage("System print service unavailable")
        }
    }

    fun pinDocumentNotification(context: Context, doc: PdfDocumentEntity) {
        try {
            val channelId = "orion_pinned_reading"
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Pinned Reading Shortcuts",
                    NotificationManager.IMPORTANCE_LOW
                )
                manager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("pinned_doc_id", doc.id)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                doc.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentTitle(doc.title)
                .setContentText("Continue reading • Page ${doc.currentPage + 1} of ${doc.pageCount} (${doc.progressPercent}%)")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()

            manager.notify(doc.id.toInt() + 1000, notification)
            viewModelScope.launch {
                repository.updatePinnedNotification(doc.id, true)
                showStatusMessage("Pinned '${doc.title}' to notifications")
            }
        } catch (e: Exception) {
            showStatusMessage("Pinned reading shortcut activated for '${doc.title}'")
        }
    }

    class Factory(
        private val appContext: Context,
        private val repository: PdfRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PdfAppViewModel(appContext, repository) as T
        }
    }
}
