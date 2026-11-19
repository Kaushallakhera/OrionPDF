package com.example.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
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
import com.example.pdf.DevicePdfScanner
import com.example.pdf.NormalizedRect
import com.example.pdf.PdfRenderEngine
import com.example.pdf.PdfTextExtractor
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
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
    val rects: List<NormalizedRect> = emptyList(),
    val matchCountOnPage: Int = 1
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
    val storageFilter: String? = null,
    val hasStoragePermission: Boolean = false,
    val isScanning: Boolean = false,
    val scannedCount: Int = 0,
    val persistedTreeCount: Int = 0,
    val customFolders: List<String> = emptyList(),
    val statusMessage: String? = null
)

data class ReaderSettingsState(
    val appThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val readingTheme: ReadingTheme = ReadingTheme.LIGHT,
    val brightness: Float = 0.78f,
    val useSystemBrightness: Boolean = true,
    val horizontalScroll: Boolean = false,
    val bookMode: Boolean = false,
    val showScrollBar: Boolean = true,
    val autoResumeLastPdf: Boolean = false,
    val autoScanStorageOnLaunch: Boolean = false
)

data class ActiveReaderState(
    val document: PdfDocumentEntity? = null,
    val currentPage: Int = 0,
    val isScrollLocked: Boolean = false,
    val pageRotationDegrees: Int = 0,
    val isLandscapeLocked: Boolean = false,
    val isReflowMode: Boolean = false,
    val reflowFontSizeSp: Float = 18f,
    val isAnnotationSidebarExpanded: Boolean = false,
    val activeTool: AnnotationTool = AnnotationTool.NONE,
    val activeColorHex: Long = 0xFFE53935L,
    val activeStrokeWidth: Float = 5f,
    val isPdfSearchOpen: Boolean = false,
    val pdfSearchQuery: String = "",
    val pdfSearchMatches: List<PdfSearchMatch> = emptyList(),
    val activeSearchMatchIndex: Int = 0,
    val isSearchingPdf: Boolean = false,
    val isAutoScrollActive: Boolean = false,
    val autoScrollSpeed: Float = 1.0f,
    val isReadAloudActive: Boolean = false,
    val readAloudSpeed: Float = 1.0f,
    val isExportingAnnotatedPdf: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class PdfAppViewModel(
    private val appContext: Context,
    private val repository: PdfRepository
) : ViewModel() {

    private val prefs = appContext.getSharedPreferences("orion_reader_prefs", Context.MODE_PRIVATE)
    private var scanJob: Job? = null
    private var pdfSearchJob: Job? = null

    private val _libraryState = MutableStateFlow(
        LibraryUiState(
            hasStoragePermission = DevicePdfScanner.hasAllFilesPermission(appContext),
            persistedTreeCount = getPersistedTreeUris().size,
            customFolders = getSavedCustomFolders()
        )
    )
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

    val availableVolumes: StateFlow<List<String>> = allDocuments
        .combine(_libraryState) { docs, _ ->
            docs.map { it.storageVolume }.distinct().sorted()
        }
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
        val volumeFiltered = if (lib.storageFilter != null) {
            all.filter { it.storageVolume.equals(lib.storageFilter, ignoreCase = true) }
        } else {
            all
        }

        if (lib.foldersSegment == FoldersSegment.AUTHORS) {
            volumeFiltered.groupBy { it.author }
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
            val realFolderNames = (volumeFiltered.map { it.folderName } + lib.customFolders).distinct()
            realFolderNames.mapNotNull { folderName ->
                val matchingDocs = volumeFiltered.filter { it.folderName.equals(folderName, ignoreCase = true) }
                val isUserCustom = lib.customFolders.any { it.equals(folderName, ignoreCase = true) }
                if (matchingDocs.isEmpty() && !isUserCustom) return@mapNotNull null
                FolderGroupItem(
                    name = folderName,
                    pdfCount = matchingDocs.size,
                    storageVolume = matchingDocs.firstOrNull()?.storageVolume ?: "Internal storage",
                    isAuthorGroup = false
                )
            }.sortedBy { it.name.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Remove any leftover legacy bundled sample PDFs from earlier versions
            purgeLegacySampleDocuments()
            refreshPermissionStatus()
            if (_readerSettings.value.autoScanStorageOnLaunch) {
                refreshPermissionAndScan(showToastWhenDone = false)
            }
            if (_readerSettings.value.autoResumeLastPdf) {
                val lastId = prefs.getLong("last_opened_doc_id", -1L)
                if (lastId > 0L) {
                    repository.getDocumentById(lastId)?.let { doc ->
                        if (isDocumentAccessible(doc.filePath)) {
                            openDocument(doc)
                        }
                    }
                }
            }
        }
    }

    private suspend fun purgeLegacySampleDocuments() = withContext(Dispatchers.IO) {
        val sampleDir = File(appContext.filesDir, "sample_pdfs")
        val existing = repository.getAllDocumentsOnce()
        existing.forEach { doc ->
            if (doc.filePath.contains("/sample_pdfs/")) {
                repository.deleteDocument(doc.id)
            }
        }
        if (sampleDir.exists()) {
            runCatching { sampleDir.deleteRecursively() }
        }
    }

    private fun isDocumentAccessible(pathOrUri: String): Boolean {
        return try {
            if (pathOrUri.startsWith("content://")) {
                appContext.contentResolver.openFileDescriptor(Uri.parse(pathOrUri), "r")?.use { true } ?: false
            } else {
                val f = File(pathOrUri)
                f.exists() && f.canRead() && f.length() > 0L
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun getPersistedTreeUris(): Set<String> {
        return prefs.getStringSet("persisted_saf_tree_uris", emptySet())?.toSet() ?: emptySet()
    }

    private fun savePersistedTreeUris(uris: Set<String>) {
        prefs.edit().putStringSet("persisted_saf_tree_uris", uris).apply()
        _libraryState.update { it.copy(persistedTreeCount = uris.size) }
    }

    private fun getSavedCustomFolders(): List<String> {
        return prefs.getStringSet("custom_folder_names", emptySet())?.sorted() ?: emptyList()
    }

    fun refreshPermissionStatus() {
        val granted = DevicePdfScanner.hasAllFilesPermission(appContext)
        _libraryState.update {
            it.copy(
                hasStoragePermission = granted,
                persistedTreeCount = getPersistedTreeUris().size
            )
        }
    }

    fun refreshPermissionAndScan(showToastWhenDone: Boolean = true) {
        val granted = DevicePdfScanner.hasAllFilesPermission(appContext)
        val treeUris = getPersistedTreeUris()
        _libraryState.update {
            it.copy(
                hasStoragePermission = granted,
                persistedTreeCount = treeUris.size
            )
        }

        if (scanJob?.isActive == true) return
        scanJob = viewModelScope.launch {
            _libraryState.update { it.copy(isScanning = true, scannedCount = 0) }
            try {
                val discovered = DevicePdfScanner.scanAllAccessiblePdfs(
                    context = appContext,
                    persistedTreeUris = treeUris,
                    onProgressCount = { count ->
                        _libraryState.update { it.copy(scannedCount = count) }
                    }
                )

                val existingDocs = withContext(Dispatchers.IO) { repository.getAllDocumentsOnce() }
                val existingByPath = existingDocs.associateBy { it.filePath }
                val discoveredPaths = discovered.map { it.pathOrUri }.toHashSet()

                var addedCount = 0
                withContext(Dispatchers.IO) {
                    for (item in discovered) {
                        val existing = existingByPath[item.pathOrUri]
                        if (existing == null) {
                            repository.insertDocument(
                                PdfDocumentEntity(
                                    title = item.title,
                                    filePath = item.pathOrUri,
                                    folderName = item.folderName,
                                    storageVolume = item.storageVolume,
                                    author = item.folderName,
                                    sizeBytes = item.sizeBytes,
                                    pageCount = item.pageCount,
                                    currentPage = 0,
                                    lastOpenedAt = 0L,
                                    createdAt = item.lastModified
                                )
                            )
                            addedCount++
                        } else if (existing.pageCount != item.pageCount || existing.sizeBytes != item.sizeBytes) {
                            repository.updateDocument(
                                existing.copy(
                                    title = item.title,
                                    sizeBytes = item.sizeBytes,
                                    pageCount = item.pageCount,
                                    createdAt = item.lastModified
                                )
                            )
                        }
                    }

                    // Clean up entries whose backing files were deleted outside the app
                    for (oldDoc in existingDocs) {
                        if (!discoveredPaths.contains(oldDoc.filePath) && !isDocumentAccessible(oldDoc.filePath)) {
                            repository.deleteDocument(oldDoc.id)
                        }
                    }
                }

                if (showToastWhenDone) {
                    val total = repository.getDocumentCount()
                    showStatusMessage(
                        when {
                            addedCount > 0 -> "Found $addedCount new PDF${if (addedCount == 1) "" else "s"} ($total total)"
                            total > 0 -> "Storage rescanned • $total PDF${if (total == 1) "" else "s"} ready"
                            else -> "No PDFs found yet • Grant storage access or pick a file"
                        }
                    )
                }
            } catch (_: Exception) {
                if (showToastWhenDone) {
                    showStatusMessage("Could not complete storage scan")
                }
            } finally {
                _libraryState.update { it.copy(isScanning = false) }
            }
        }
    }

    fun addSafFolderTree(context: Context, treeUri: Uri) {
        viewModelScope.launch {
            runCatching {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(treeUri, flags)
            }.onFailure {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }

            val updated = getPersistedTreeUris() + treeUri.toString()
            savePersistedTreeUris(updated)
            showStatusMessage("Folder added • Scanning for PDFs…")
            refreshPermissionAndScan(showToastWhenDone = true)
        }
    }

    fun openDocumentById(docId: Long) {
        viewModelScope.launch {
            repository.getDocumentById(docId)?.let { openDocument(it) }
        }
    }

    private fun loadInitialSettings(): ReaderSettingsState {
        val themeStr = prefs.getString("app_theme", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val readThemeStr = prefs.getString("reading_theme", ReadingTheme.LIGHT.name) ?: ReadingTheme.LIGHT.name
        return ReaderSettingsState(
            appThemeMode = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.SYSTEM),
            readingTheme = runCatching { ReadingTheme.valueOf(readThemeStr) }.getOrDefault(ReadingTheme.LIGHT),
            brightness = prefs.getFloat("brightness", 0.78f),
            useSystemBrightness = prefs.getBoolean("use_system_brightness", true),
            horizontalScroll = prefs.getBoolean("horizontal_scroll", false),
            bookMode = prefs.getBoolean("book_mode", false),
            showScrollBar = prefs.getBoolean("show_scroll_bar", true),
            autoResumeLastPdf = prefs.getBoolean("auto_resume_last_pdf", false),
            autoScanStorageOnLaunch = prefs.getBoolean("auto_scan_storage_on_launch", false)
        )
    }

    fun selectTab(tab: BottomNavTab) {
        _libraryState.update {
            it.copy(activeTab = tab, selectedFolderName = null)
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
                val updated = (current.customFolders + trimmed).sorted()
                prefs.edit().putStringSet("custom_folder_names", updated.toSet()).apply()
                current.copy(
                    customFolders = updated,
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
        refreshPermissionAndScan(showToastWhenDone = true)
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

    fun setHorizontalScroll(horizontal: Boolean) {
        prefs.edit().putBoolean("horizontal_scroll", horizontal).apply()
        _readerSettings.update { it.copy(horizontalScroll = horizontal) }
    }

    fun setBookMode(bookMode: Boolean) {
        prefs.edit().putBoolean("book_mode", bookMode).apply()
        _readerSettings.update { it.copy(bookMode = bookMode) }
    }

    fun setShowScrollBar(show: Boolean) {
        prefs.edit().putBoolean("show_scroll_bar", show).apply()
        _readerSettings.update { it.copy(showScrollBar = show) }
    }

    fun setAutoResumeLastPdf(autoResume: Boolean) {
        prefs.edit().putBoolean("auto_resume_last_pdf", autoResume).apply()
        _readerSettings.update { it.copy(autoResumeLastPdf = autoResume) }
    }

    fun setAutoScanStorageOnLaunch(enabled: Boolean) {
        prefs.edit().putBoolean("auto_scan_storage_on_launch", enabled).apply()
        _readerSettings.update { it.copy(autoScanStorageOnLaunch = enabled) }
        if (enabled && _libraryState.value.hasStoragePermission) {
            refreshPermissionAndScan(showToastWhenDone = true)
        }
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
                isAnnotationSidebarExpanded = false
            )
        }
        viewModelScope.launch {
            repository.updateReadingProgress(doc.id, targetPage)
        }
    }

    fun closeDocument() {
        pdfSearchJob?.cancel()
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
        _activeReader.update { it.copy(isScrollLocked = !it.isScrollLocked) }
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
        _activeReader.update { current ->
            val nextExpanded = !current.isAnnotationSidebarExpanded
            current.copy(
                isAnnotationSidebarExpanded = nextExpanded,
                activeTool = if (!nextExpanded) AnnotationTool.NONE else current.activeTool
            )
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

    fun addHighlightRectsAnnotation(pageIndex: Int, rects: List<NormalizedRect>) {
        val doc = _activeReader.value.document ?: return
        if (rects.isEmpty()) return
        viewModelScope.launch {
            for (r in rects) {
                val midY = ((r.top + r.bottom) * 0.5f).coerceIn(0.01f, 0.99f)
                val pts = listOf(
                    androidx.compose.ui.geometry.Offset(r.left.coerceIn(0.01f, 0.99f), midY),
                    androidx.compose.ui.geometry.Offset(r.right.coerceIn(0.01f, 0.99f), midY)
                )
                val entity = AnnotationEntity(
                    documentId = doc.id,
                    pageIndex = pageIndex,
                    toolType = AnnotationTool.HIGHLIGHTER.name,
                    colorHex = 0xFFFDD835L,
                    strokeWidth = 14f,
                    pointsData = encodeAnnotationPoints(pts),
                    anchorX = pts.first().x,
                    anchorY = pts.first().y
                )
                val id = repository.addAnnotation(entity)
                undoAnnotationIds.addLast(entity.copy(id = id))
            }
            redoAnnotationStack.clear()
            showStatusMessage("Highlighted selection on Page ${pageIndex + 1}")
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

    // Export flattened Annotated PDF copy
    fun exportAnnotatedPdf(
        context: Context,
        doc: PdfDocumentEntity,
        destinationUri: Uri? = null
    ) {
        if (_activeReader.value.isExportingAnnotatedPdf) return
        viewModelScope.launch {
            _activeReader.update { it.copy(isExportingAnnotatedPdf = true) }
            showStatusMessage("Exporting annotated PDF…")
            val annotations = withContext(Dispatchers.IO) {
                repository.getAnnotationsForDocOnce(doc.id)
            }
            val result = PdfRenderEngine.exportAnnotatedPdf(
                context = context,
                sourcePathOrUri = doc.filePath,
                docTitle = doc.title,
                annotations = annotations,
                destinationUri = destinationUri
            )
            _activeReader.update { it.copy(isExportingAnnotatedPdf = false) }
            result.onSuccess { savedLocation ->
                if (!savedLocation.startsWith("content://")) {
                    val savedFile = File(savedLocation)
                    if (savedFile.exists()) {
                        val pages = PdfRenderEngine.inspectPdfPageCount(context, savedFile.absolutePath).coerceAtLeast(1)
                        repository.insertDocument(
                            PdfDocumentEntity(
                                title = savedFile.nameWithoutExtension,
                                filePath = savedFile.absolutePath,
                                folderName = savedFile.parentFile?.name ?: "OrionPDF",
                                storageVolume = "Internal storage",
                                author = "Annotated Copy",
                                sizeBytes = savedFile.length(),
                                pageCount = pages,
                                currentPage = 0,
                                lastOpenedAt = System.currentTimeMillis(),
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    }
                    showStatusMessage("Saved annotated copy: ${savedFile.name}")
                } else {
                    showStatusMessage("Annotated PDF exported successfully")
                }
            }.onFailure { err ->
                showStatusMessage("Export failed: ${err.message ?: "Unable to write file"}")
            }
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

    fun deleteDocument(context: Context, doc: PdfDocumentEntity, deleteFromStorage: Boolean) {
        viewModelScope.launch {
            if (_activeReader.value.document?.id == doc.id) {
                closeDocument()
            }
            PdfRenderEngine.closeRenderer(doc.filePath)

            var diskDeleted = false
            if (deleteFromStorage) {
                diskDeleted = withContext(Dispatchers.IO) {
                    try {
                        if (doc.filePath.startsWith("content://")) {
                            val uri = Uri.parse(doc.filePath)
                            val deletedByDoc = runCatching {
                                DocumentsContract.deleteDocument(context.contentResolver, uri)
                            }.getOrDefault(false)
                            if (deletedByDoc) true
                            else context.contentResolver.delete(uri, null, null) > 0
                        } else {
                            val f = File(doc.filePath)
                            !f.exists() || f.delete()
                        }
                    } catch (_: Exception) {
                        false
                    }
                }
            }

            repository.deleteDocument(doc.id)
            showStatusMessage(
                when {
                    deleteFromStorage && diskDeleted -> "Deleted '${doc.title}' from device"
                    deleteFromStorage -> "Removed from library (OS protected original file)"
                    else -> "Removed '${doc.title}' from Orion library"
                }
            )
        }
    }

    // Real In-PDF Text Search powered by PdfTextExtractor
    fun setPdfSearchOpen(open: Boolean) {
        if (!open) pdfSearchJob?.cancel()
        _activeReader.update {
            it.copy(
                isPdfSearchOpen = open,
                pdfSearchQuery = if (!open) "" else it.pdfSearchQuery,
                pdfSearchMatches = if (!open) emptyList() else it.pdfSearchMatches,
                isSearchingPdf = false
            )
        }
    }

    fun updatePdfSearchQuery(query: String) {
        val doc = _activeReader.value.document ?: return
        _activeReader.update { it.copy(pdfSearchQuery = query) }
        pdfSearchJob?.cancel()

        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _activeReader.update {
                it.copy(pdfSearchMatches = emptyList(), activeSearchMatchIndex = 0, isSearchingPdf = false)
            }
            return
        }

        pdfSearchJob = viewModelScope.launch {
            _activeReader.update { it.copy(isSearchingPdf = true) }
            val matches = withContext(Dispatchers.IO) {
                PdfTextExtractor.searchDocumentWithBounds(
                    context = appContext,
                    filePathOrUri = doc.filePath,
                    query = trimmed,
                    expectedPageCount = doc.pageCount
                ).map { hit ->
                    PdfSearchMatch(
                        pageIndex = hit.pageIndex,
                        snippet = hit.snippet,
                        rects = hit.rects,
                        matchCountOnPage = 1
                    )
                }
            }
            _activeReader.update {
                it.copy(
                    pdfSearchMatches = matches,
                    activeSearchMatchIndex = 0,
                    isSearchingPdf = false
                )
            }
            matches.firstOrNull()?.let { onPageChanged(it.pageIndex) }
        }
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

    // System Integrations: SAF File Import, File Manager ACTION_VIEW / ACTION_SEND, Share, Open With, Print, Pin Notification
    fun importPdfFromUri(context: Context, uri: Uri, openImmediately: Boolean = true) {
        viewModelScope.launch {
            val importedDoc = withContext(Dispatchers.IO) {
                try {
                    // Attempt persistable permission so we can read real device file directly
                    val hasPersistable = runCatching {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        true
                    }.getOrDefault(false)

                    // Direct file:// URI support
                    if (uri.scheme.equals("file", ignoreCase = true)) {
                        val directPath = uri.path
                        if (!directPath.isNullOrBlank()) {
                            val f = File(directPath)
                            if (f.exists() && f.canRead()) {
                                repository.getDocumentByPath(f.absolutePath)?.let { return@withContext it }
                                val pages = PdfRenderEngine.inspectPdfPageCount(context, f.absolutePath)
                                if (pages > 0) {
                                    val entity = PdfDocumentEntity(
                                        title = f.nameWithoutExtension,
                                        filePath = f.absolutePath,
                                        folderName = f.parentFile?.name ?: "Internal storage",
                                        storageVolume = "Internal storage",
                                        author = f.parentFile?.name ?: "Device Storage",
                                        sizeBytes = f.length(),
                                        pageCount = pages,
                                        currentPage = 0,
                                        lastOpenedAt = System.currentTimeMillis(),
                                        createdAt = f.lastModified().takeIf { it > 0L } ?: System.currentTimeMillis()
                                    )
                                    val id = repository.insertDocument(entity)
                                    return@withContext entity.copy(id = id)
                                }
                            }
                        }
                    }

                    val resolver = context.contentResolver
                    var displayName = "Document_${System.currentTimeMillis() % 10000}.pdf"
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

                    val uriStr = uri.toString()
                    if (hasPersistable) {
                        repository.getDocumentByPath(uriStr)?.let { return@withContext it }
                        val directPages = PdfRenderEngine.inspectPdfPageCount(context, uriStr)
                        if (directPages > 0) {
                            val titleNoExt = displayName.removeSuffix(".pdf").removeSuffix(".PDF")
                            val entity = PdfDocumentEntity(
                                title = titleNoExt,
                                filePath = uriStr,
                                folderName = "Opened Files",
                                storageVolume = "Internal storage",
                                author = "Device Storage",
                                sizeBytes = fileSize,
                                pageCount = directPages,
                                currentPage = 0,
                                lastOpenedAt = System.currentTimeMillis(),
                                createdAt = System.currentTimeMillis()
                            )
                            val newId = repository.insertDocument(entity)
                            return@withContext entity.copy(id = newId)
                        }
                    }

                    // For temporary one-time content:// URIs (e.g. from WhatsApp / Gmail / File Manager), cache locally
                    val importDir = File(context.filesDir, "imported_pdfs").apply { mkdirs() }
                    val cleanName = displayName.replace(Regex("[^a-zA-Z0-9._ -]"), "_").let {
                        if (it.endsWith(".pdf", ignoreCase = true)) it else "$it.pdf"
                    }
                    val targetFile = File(importDir, cleanName)
                    resolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    } ?: return@withContext null

                    if (fileSize <= 0L) fileSize = targetFile.length()
                    val pages = PdfRenderEngine.inspectPdfPageCount(context, targetFile.absolutePath)
                    if (pages <= 0) {
                        targetFile.delete()
                        return@withContext null
                    }

                    repository.getDocumentByPath(targetFile.absolutePath)?.let { existing ->
                        val updated = existing.copy(
                            sizeBytes = fileSize,
                            pageCount = pages,
                            lastOpenedAt = System.currentTimeMillis()
                        )
                        repository.updateDocument(updated)
                        return@withContext updated
                    }

                    val titleNoExt = displayName.removeSuffix(".pdf").removeSuffix(".PDF")
                    val entity = PdfDocumentEntity(
                        title = titleNoExt,
                        filePath = targetFile.absolutePath,
                        folderName = "Opened Files",
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
                } catch (_: Exception) {
                    null
                }
            }

            if (importedDoc != null) {
                showStatusMessage("Opened '${importedDoc.title}' (${importedDoc.pageCount} pages)")
                if (openImmediately) {
                    openDocument(importedDoc)
                }
            } else {
                showStatusMessage("Could not open selected PDF file")
            }
        }
    }

    private fun resolveShareableUri(context: Context, doc: PdfDocumentEntity): Uri? {
        return try {
            if (doc.filePath.startsWith("content://")) {
                // Copy to cache so external apps always receive reliable FileProvider read grants
                val cacheDir = File(context.cacheDir, "shared_pdfs").apply { mkdirs() }
                val safeName = doc.title.replace(Regex("[^a-zA-Z0-9._ -]"), "_").ifBlank { "document" } + ".pdf"
                val outFile = File(cacheDir, safeName)
                context.contentResolver.openInputStream(Uri.parse(doc.filePath))?.use { input ->
                    FileOutputStream(outFile).use { output -> input.copyTo(output) }
                }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
            } else {
                val file = File(doc.filePath)
                if (!file.exists()) return null
                runCatching {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                }.getOrElse {
                    // Fallback to cache copy for unusual SD card mount paths
                    val cacheDir = File(context.cacheDir, "shared_pdfs").apply { mkdirs() }
                    val outFile = File(cacheDir, file.name)
                    file.copyTo(outFile, overwrite = true)
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun shareDocument(context: Context, doc: PdfDocumentEntity) {
        viewModelScope.launch {
            val uri = withContext(Dispatchers.IO) { resolveShareableUri(context, doc) }
            if (uri == null) {
                showStatusMessage("PDF file not accessible for sharing")
                return@launch
            }
            try {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, doc.title)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(shareIntent, "Share ${doc.title}").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (_: Exception) {
                showStatusMessage("Unable to launch share sheet")
            }
        }
    }

    fun openDocumentWithExternalApp(context: Context, doc: PdfDocumentEntity) {
        viewModelScope.launch {
            val uri = withContext(Dispatchers.IO) { resolveShareableUri(context, doc) }
            if (uri == null) {
                showStatusMessage("PDF file not accessible")
                return@launch
            }
            try {
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(
                    Intent.createChooser(viewIntent, "Open with").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (_: Exception) {
                showStatusMessage("No external PDF app found")
            }
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
        } catch (_: Exception) {
            showStatusMessage("Notification permission needed to pin shortcut")
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
