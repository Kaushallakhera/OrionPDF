package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PdfDocumentEntity
import com.example.data.progressFraction
import com.example.data.progressPercent
import com.example.viewmodel.BottomNavTab
import com.example.viewmodel.FolderGroupItem
import com.example.viewmodel.FoldersSegment
import com.example.viewmodel.LibraryUiState
import com.example.viewmodel.SortField

@Composable
fun LibraryScreen(
    libraryState: LibraryUiState,
    recentDocuments: List<PdfDocumentEntity>,
    filteredDocuments: List<PdfDocumentEntity>,
    folderGroups: List<FolderGroupItem>,
    availableVolumes: List<String>,
    onSelectTab: (BottomNavTab) -> Unit,
    onSelectFoldersSegment: (FoldersSegment) -> Unit,
    onOpenFolderGroup: (String?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearchActive: (Boolean) -> Unit,
    onSortChange: (SortField) -> Unit,
    onToggleGridView: () -> Unit,
    onStorageFilterChange: (String?) -> Unit,
    onOpenDocument: (PdfDocumentEntity) -> Unit,
    onOpenDocumentMenu: (PdfDocumentEntity) -> Unit,
    onOpenAppMoreSheet: () -> Unit,
    onOpenCreateFolderDialog: () -> Unit,
    onRequestStoragePermission: () -> Unit,
    onPickPdfFromDevice: () -> Unit,
    onPickFolderFromDevice: () -> Unit,
    onRescanStorage: () -> Unit
) {
    if (libraryState.selectedFolderName != null) {
        BackHandler {
            onOpenFolderGroup(null)
        }
    } else if (libraryState.isSearchActive) {
        BackHandler {
            onToggleSearchActive(false)
        }
    } else if (libraryState.activeTab != BottomNavTab.FILES) {
        BackHandler {
            onSelectTab(BottomNavTab.FILES)
        }
    }

    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (libraryState.selectedFolderName != null) {
                        IconButton(
                            onClick = { onOpenFolderGroup(null) },
                            modifier = Modifier.testTag("folder_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to folders",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onOpenAppMoreSheet)
                                .testTag("orion_pdf_app_icon_badge")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = OrionIcons.Description,
                                    contentDescription = "Orion PDF Reader Icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Text(
                        text = libraryState.selectedFolderName ?: libraryState.activeTab.label,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (libraryState.activeTab == BottomNavTab.FOLDERS && libraryState.selectedFolderName == null) {
                        IconButton(
                            onClick = onOpenCreateFolderDialog,
                            modifier = Modifier.testTag("create_folder_button")
                        ) {
                            Icon(
                                imageVector = OrionIcons.CreateNewFolder,
                                contentDescription = "Create custom folder",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    IconButton(
                        onClick = { onToggleSearchActive(!libraryState.isSearchActive) },
                        modifier = Modifier.testTag("library_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search PDFs",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(
                        onClick = onOpenAppMoreSheet,
                        modifier = Modifier.testTag("app_more_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                AnimatedVisibility(visible = libraryState.isSearchActive) {
                    OutlinedTextField(
                        value = libraryState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search PDFs by title, folder, or author…") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (libraryState.searchQuery.isNotEmpty()) {
                                        onSearchQueryChange("")
                                    } else {
                                        onToggleSearchActive(false)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .testTag("library_search_input")
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onPickPdfFromDevice,
                icon = { Icon(OrionIcons.FolderOpen, contentDescription = null) },
                text = { Text("Open PDF") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_open_pdf")
            )
        },
        bottomBar = {
            OrionBottomNavigationBar(
                activeTab = libraryState.activeTab,
                onSelectTab = onSelectTab
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (libraryState.isScanning) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (libraryState.scannedCount > 0) {
                                "Scanning device storage… Found ${libraryState.scannedCount} PDFs"
                            } else {
                                "Scanning device folders for PDF files…"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (libraryState.activeTab == BottomNavTab.FOLDERS && libraryState.selectedFolderName == null) {
                FoldersTabContent(
                    foldersSegment = libraryState.foldersSegment,
                    folderGroups = folderGroups,
                    storageFilter = libraryState.storageFilter,
                    availableVolumes = availableVolumes,
                    hasStoragePermission = libraryState.hasStoragePermission,
                    onSelectSegment = onSelectFoldersSegment,
                    onOpenFolder = { onOpenFolderGroup(it.name) },
                    onStorageFilterChange = onStorageFilterChange,
                    onRequestStoragePermission = onRequestStoragePermission,
                    onPickFolderFromDevice = onPickFolderFromDevice
                )
            } else {
                FilesListAndCarouselContent(
                    activeTab = libraryState.activeTab,
                    hasStoragePermission = libraryState.hasStoragePermission,
                    isScanning = libraryState.isScanning,
                    showContinueReading = libraryState.activeTab == BottomNavTab.FILES &&
                        libraryState.selectedFolderName == null &&
                        libraryState.searchQuery.isBlank() &&
                        recentDocuments.isNotEmpty(),
                    recentDocuments = recentDocuments,
                    documents = filteredDocuments,
                    sortField = libraryState.sortField,
                    sortAscending = libraryState.sortAscending,
                    isGridView = libraryState.isGridView,
                    storageFilter = libraryState.storageFilter,
                    availableVolumes = availableVolumes,
                    showSortMenu = showSortMenu,
                    onShowSortMenuChange = { showSortMenu = it },
                    showFilterMenu = showFilterMenu,
                    onShowFilterMenuChange = { showFilterMenu = it },
                    onSortChange = onSortChange,
                    onToggleGridView = onToggleGridView,
                    onStorageFilterChange = onStorageFilterChange,
                    onOpenDocument = onOpenDocument,
                    onOpenDocumentMenu = onOpenDocumentMenu,
                    onRequestStoragePermission = onRequestStoragePermission,
                    onPickPdfFromDevice = onPickPdfFromDevice,
                    onPickFolderFromDevice = onPickFolderFromDevice,
                    onRescanStorage = onRescanStorage
                )
            }
        }
    }
}

@Composable
private fun FilesListAndCarouselContent(
    activeTab: BottomNavTab,
    hasStoragePermission: Boolean,
    isScanning: Boolean,
    showContinueReading: Boolean,
    recentDocuments: List<PdfDocumentEntity>,
    documents: List<PdfDocumentEntity>,
    sortField: SortField,
    sortAscending: Boolean,
    isGridView: Boolean,
    storageFilter: String?,
    availableVolumes: List<String>,
    showSortMenu: Boolean,
    onShowSortMenuChange: (Boolean) -> Unit,
    showFilterMenu: Boolean,
    onShowFilterMenuChange: (Boolean) -> Unit,
    onSortChange: (SortField) -> Unit,
    onToggleGridView: () -> Unit,
    onStorageFilterChange: (String?) -> Unit,
    onOpenDocument: (PdfDocumentEntity) -> Unit,
    onOpenDocumentMenu: (PdfDocumentEntity) -> Unit,
    onRequestStoragePermission: () -> Unit,
    onPickPdfFromDevice: () -> Unit,
    onPickFolderFromDevice: () -> Unit,
    onRescanStorage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (isGridView) {
            if (!hasStoragePermission && activeTab == BottomNavTab.FILES) {
                StoragePermissionOnboardingCard(
                    compact = documents.isNotEmpty(),
                    onGrantPermission = onRequestStoragePermission,
                    onPickPdf = onPickPdfFromDevice,
                    onPickFolder = onPickFolderFromDevice
                )
            }
            if (showContinueReading) {
                ContinueReadingSection(
                    recentDocuments = recentDocuments,
                    onOpenDocument = onOpenDocument
                )
            }
            SortAndFilterHeaderRow(
                sortField = sortField,
                sortAscending = sortAscending,
                isGridView = isGridView,
                storageFilter = storageFilter,
                availableVolumes = availableVolumes,
                showSortMenu = showSortMenu,
                onShowSortMenuChange = onShowSortMenuChange,
                showFilterMenu = showFilterMenu,
                onShowFilterMenuChange = onShowFilterMenuChange,
                onSortChange = onSortChange,
                onToggleGridView = onToggleGridView,
                onStorageFilterChange = onStorageFilterChange
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 92.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(documents, key = { it.id }) { doc ->
                    PdfGridItemCard(
                        doc = doc,
                        onClick = { onOpenDocument(doc) },
                        onMoreClick = { onOpenDocumentMenu(doc) }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 92.dp)
            ) {
                if (!hasStoragePermission && activeTab == BottomNavTab.FILES) {
                    item(key = "storage_permission_card") {
                        StoragePermissionOnboardingCard(
                            compact = documents.isNotEmpty(),
                            onGrantPermission = onRequestStoragePermission,
                            onPickPdf = onPickPdfFromDevice,
                            onPickFolder = onPickFolderFromDevice
                        )
                    }
                }

                if (showContinueReading) {
                    item(key = "continue_reading_header") {
                        ContinueReadingSection(
                            recentDocuments = recentDocuments,
                            onOpenDocument = onOpenDocument
                        )
                    }
                }

                if (documents.isNotEmpty()) {
                    item(key = "sort_filter_row") {
                        SortAndFilterHeaderRow(
                            sortField = sortField,
                            sortAscending = sortAscending,
                            isGridView = isGridView,
                            storageFilter = storageFilter,
                            availableVolumes = availableVolumes,
                            showSortMenu = showSortMenu,
                            onShowSortMenuChange = onShowSortMenuChange,
                            showFilterMenu = showFilterMenu,
                            onShowFilterMenuChange = onShowFilterMenuChange,
                            onSortChange = onSortChange,
                            onToggleGridView = onToggleGridView,
                            onStorageFilterChange = onStorageFilterChange
                        )
                    }
                }

                if (documents.isEmpty() && !isScanning) {
                    item(key = "empty_state") {
                        LibraryEmptyStateCard(
                            activeTab = activeTab,
                            hasStoragePermission = hasStoragePermission,
                            onGrantPermission = onRequestStoragePermission,
                            onPickPdf = onPickPdfFromDevice,
                            onPickFolder = onPickFolderFromDevice,
                            onRescan = onRescanStorage
                        )
                    }
                } else {
                    items(documents, key = { it.id }) { doc ->
                        PdfListRowItem(
                            doc = doc,
                            onClick = { onOpenDocument(doc) },
                            onMoreClick = { onOpenDocumentMenu(doc) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoragePermissionOnboardingCard(
    compact: Boolean,
    onGrantPermission: () -> Unit,
    onPickPdf: () -> Unit,
    onPickFolder: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("storage_permission_onboarding_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = OrionIcons.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Access PDFs on Your Phone",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (compact) {
                    "Allow All Files Access to automatically index PDFs across Download, Documents, WhatsApp, and SD card folders."
                } else {
                    "Grant storage permission to automatically scan all PDFs on your phone, or pick individual files and folders directly from your File Manager."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onGrantPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("grant_storage_permission_button")
                ) {
                    Text("Allow Access", maxLines = 1)
                }
                FilledTonalButton(
                    onClick = onPickFolder,
                    modifier = Modifier.testTag("pick_saf_folder_button")
                ) {
                    Text("Add Folder", maxLines = 1)
                }
            }
            if (!compact) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onPickPdf,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_pick_pdf_button")
                ) {
                    Icon(OrionIcons.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open PDF from File Manager")
                }
            }
        }
    }
}

@Composable
private fun LibraryEmptyStateCard(
    activeTab: BottomNavTab,
    hasStoragePermission: Boolean,
    onGrantPermission: () -> Unit,
    onPickPdf: () -> Unit,
    onPickFolder: () -> Unit,
    onRescan: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 48.dp)
    ) {
        Icon(
            imageVector = when (activeTab) {
                BottomNavTab.RECENT -> OrionIcons.History
                BottomNavTab.FAVORITES -> OrionIcons.StarBorder
                else -> OrionIcons.DescriptionOutlined
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(52.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = when (activeTab) {
                BottomNavTab.RECENT -> "No recently opened PDFs"
                BottomNavTab.FAVORITES -> "No favorite PDFs yet"
                else -> if (hasStoragePermission) "No PDF files found on device" else "Your PDF library is ready"
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = when (activeTab) {
                BottomNavTab.RECENT -> "Documents you open from your phone or File Manager will appear here with reading progress."
                BottomNavTab.FAVORITES -> "Tap the three-dot menu on any PDF and choose Add to Favorites."
                else -> if (hasStoragePermission) {
                    "Tap Rescan Storage or pick any PDF from your File Manager to start reading and annotating."
                } else {
                    "Grant storage permission above to auto-scan your phone, or tap Open PDF below."
                }
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (hasStoragePermission) {
                FilledTonalButton(onClick = onRescan) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rescan")
                }
            } else {
                Button(onClick = onGrantPermission) {
                    Text("Scan Phone")
                }
            }
            OutlinedButton(onClick = onPickPdf) {
                Text("Open PDF")
            }
            if (activeTab == BottomNavTab.FILES) {
                OutlinedButton(onClick = onPickFolder) {
                    Text("Folder")
                }
            }
        }
    }
}

@Composable
private fun ContinueReadingSection(
    recentDocuments: List<PdfDocumentEntity>,
    onOpenDocument: (PdfDocumentEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Continue reading",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 12.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recentDocuments.take(6), key = { "recent_${it.id}" }) { doc ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .width(316.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenDocument(doc) }
                        .testTag("continue_reading_card_${doc.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PdfThumbnailView(
                            filePath = doc.filePath,
                            pageIndex = doc.currentPage,
                            targetWidthPx = 220,
                            cornerRadius = 8.dp,
                            modifier = Modifier
                                .width(58.dp)
                                .height(78.dp)
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = doc.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Page ${doc.currentPage + 1} of ${doc.pageCount} · ${formatRelativeTime(doc.lastOpenedAt)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(doc.progressFraction.coerceIn(0.05f, 1f))
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = "${doc.progressPercent}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun SortAndFilterHeaderRow(
    sortField: SortField,
    sortAscending: Boolean,
    isGridView: Boolean,
    storageFilter: String?,
    availableVolumes: List<String>,
    showSortMenu: Boolean,
    onShowSortMenuChange: (Boolean) -> Unit,
    showFilterMenu: Boolean,
    onShowFilterMenuChange: (Boolean) -> Unit,
    onSortChange: (SortField) -> Unit,
    onToggleGridView: () -> Unit,
    onStorageFilterChange: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onShowSortMenuChange(true) }
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .testTag("sort_selector_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = OrionIcons.Sort,
                    contentDescription = "Sort by",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = sortField.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (sortAscending) OrionIcons.ArrowUpward else OrionIcons.ArrowDownward,
                    contentDescription = if (sortAscending) "Ascending" else "Descending",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { onShowSortMenuChange(false) }
            ) {
                SortField.entries.forEach { field ->
                    DropdownMenuItem(
                        text = { Text(field.label) },
                        onClick = {
                            onSortChange(field)
                            onShowSortMenuChange(false)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onToggleGridView,
            modifier = Modifier.testTag("toggle_grid_view_button")
        ) {
            Icon(
                imageVector = if (isGridView) OrionIcons.ViewList else OrionIcons.GridView,
                contentDescription = "Toggle grid or list view",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box {
            IconButton(
                onClick = { onShowFilterMenuChange(true) },
                modifier = Modifier.testTag("storage_filter_button")
            ) {
                Icon(
                    imageVector = OrionIcons.Tune,
                    contentDescription = "Filter by storage",
                    tint = if (storageFilter != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(
                expanded = showFilterMenu,
                onDismissRequest = { onShowFilterMenuChange(false) }
            ) {
                DropdownMenuItem(
                    text = { Text("All Storage Locations") },
                    onClick = {
                        onStorageFilterChange(null)
                        onShowFilterMenuChange(false)
                    }
                )
                val volumes = availableVolumes.ifEmpty { listOf("Internal storage") }
                volumes.forEach { volume ->
                    DropdownMenuItem(
                        text = { Text(volume) },
                        onClick = {
                            onStorageFilterChange(volume)
                            onShowFilterMenuChange(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PdfListRowItem(
    doc: PdfDocumentEntity,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp)
            .testTag("pdf_list_item_${doc.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PdfThumbnailView(
            filePath = doc.filePath,
            pageIndex = 0,
            targetWidthPx = 192,
            cornerRadius = 6.dp,
            modifier = Modifier
                .width(52.dp)
                .height(68.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = doc.title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.5.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatShortDate(doc.createdAt)} · ${formatFileSize(doc.sizeBytes)} · ${doc.folderName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.testTag("pdf_item_more_${doc.id}")
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Document actions",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PdfGridItemCard(
    doc: PdfDocumentEntity,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            PdfThumbnailView(
                filePath = doc.filePath,
                pageIndex = 0,
                targetWidthPx = 320,
                cornerRadius = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatFileSize(doc.sizeBytes)} · ${doc.folderName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Actions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FoldersTabContent(
    foldersSegment: FoldersSegment,
    folderGroups: List<FolderGroupItem>,
    storageFilter: String?,
    availableVolumes: List<String>,
    hasStoragePermission: Boolean,
    onSelectSegment: (FoldersSegment) -> Unit,
    onOpenFolder: (FolderGroupItem) -> Unit,
    onStorageFilterChange: (String?) -> Unit,
    onRequestStoragePermission: () -> Unit,
    onPickFolderFromDevice: () -> Unit
) {
    var showStorageMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    val isFolders = foldersSegment == FoldersSegment.FOLDERS
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                if (isFolders) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.surfaceContainer
                            )
                            .clickable { onSelectSegment(FoldersSegment.FOLDERS) }
                            .testTag("segment_folders"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = OrionIcons.FolderOutlined,
                                contentDescription = null,
                                tint = if (isFolders) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Folders",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isFolders) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val isAuthors = foldersSegment == FoldersSegment.AUTHORS
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                if (isAuthors) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.surfaceContainer
                            )
                            .clickable { onSelectSegment(FoldersSegment.AUTHORS) }
                            .testTag("segment_authors"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isAuthors) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Authors",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isAuthors) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box {
                IconButton(
                    onClick = { showStorageMenu = true },
                    modifier = Modifier.testTag("folders_filter_button")
                ) {
                    Icon(
                        imageVector = OrionIcons.Tune,
                        contentDescription = "Filter folders",
                        tint = if (storageFilter != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showStorageMenu,
                    onDismissRequest = { showStorageMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Storage Volumes") },
                        onClick = {
                            onStorageFilterChange(null)
                            showStorageMenu = false
                        }
                    )
                    val volumes = availableVolumes.ifEmpty { listOf("Internal storage") }
                    volumes.forEach { volume ->
                        DropdownMenuItem(
                            text = { Text(volume) },
                            onClick = {
                                onStorageFilterChange(volume)
                                showStorageMenu = false
                            }
                        )
                    }
                }
            }
        }

        val displayedGroups = if (storageFilter != null) {
            folderGroups.filter { it.storageVolume.equals(storageFilter, ignoreCase = true) }
        } else {
            folderGroups
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 92.dp)
        ) {
            if (displayedGroups.isEmpty()) {
                item(key = "empty_folders") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 48.dp)
                    ) {
                        Icon(
                            imageVector = OrionIcons.FolderOutlined,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No PDF folders indexed yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add a folder from your phone storage or allow All Files Access to discover Download, Documents, and SD card folders automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (!hasStoragePermission) {
                                Button(onClick = onRequestStoragePermission) {
                                    Text("Allow Access")
                                }
                            }
                            FilledTonalButton(onClick = onPickFolderFromDevice) {
                                Text("Add Folder")
                            }
                        }
                    }
                }
            } else {
                items(displayedGroups, key = { it.name }) { group ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenFolder(group) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("folder_row_${group.name}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (group.isAuthorGroup) Icons.Default.Person else OrionIcons.FolderOutlined,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val countLabel = if (group.pdfCount == 1) "1 PDF" else "${group.pdfCount} PDFs"
                            Text(
                                text = "$countLabel · ${group.storageVolume}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open ${group.name}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrionBottomNavigationBar(
    activeTab: BottomNavTab,
    onSelectTab: (BottomNavTab) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavTab.entries.forEach { tab ->
                val selected = activeTab == tab
                val icon = when (tab) {
                    BottomNavTab.FILES -> if (selected) OrionIcons.Description else OrionIcons.DescriptionOutlined
                    BottomNavTab.RECENT -> OrionIcons.History
                    BottomNavTab.FAVORITES -> if (selected) Icons.Default.Star else OrionIcons.StarBorder
                    BottomNavTab.FOLDERS -> if (selected) OrionIcons.Folder else OrionIcons.FolderOutlined
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 4.dp)
                        .testTag("bottom_tab_${tab.name.lowercase()}")
                ) {
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.surfaceContainerHighest
                                else MaterialTheme.colorScheme.surfaceContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.label,
                            tint = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
