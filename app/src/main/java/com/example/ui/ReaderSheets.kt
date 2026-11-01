package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AnnotationEntity
import com.example.data.BookmarkEntity
import com.example.data.PdfDocumentEntity
import com.example.data.QuoteEntity
import com.example.data.ReadingTheme
import com.example.pdf.BundledPdfSeeder
import com.example.ui.theme.BurgundyBadgeBg
import com.example.ui.theme.BurgundyBadgeText
import com.example.viewmodel.ReaderSettingsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingSettingsBottomSheet(
    settings: ReaderSettingsState,
    onDismiss: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onUseSystemBrightnessChange: (Boolean) -> Unit,
    onSelectReadingTheme: (ReadingTheme) -> Unit,
    onVerticalScrollChange: (Boolean) -> Unit,
    onSwipeByPageChange: (Boolean) -> Unit,
    onShowScrollBarChange: (Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1F1C1C),
        contentColor = Color(0xFFECE6E4),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Reading settings",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp),
                color = Color(0xFFECE6E4)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Brightness Slider Row matching Screenshot 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BrightnessLow,
                    contentDescription = "Low brightness",
                    tint = Color(0xFFA29A97),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Slider(
                    value = settings.brightness,
                    onValueChange = onBrightnessChange,
                    valueRange = 0.15f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFD99B93),
                        activeTrackColor = Color(0xFFD99B93),
                        inactiveTrackColor = Color(0xFF332E2E)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("brightness_slider")
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.BrightnessHigh,
                    contentDescription = "High brightness",
                    tint = Color(0xFFA29A97),
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ReadingSettingSwitchRow(
                label = "Use system brightness",
                checked = settings.useSystemBrightness,
                onCheckedChange = onUseSystemBrightnessChange,
                testTag = "switch_system_brightness"
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 8 Reading Themes in a 2x4 Grid matching Screenshot 3
            val firstRowThemes = listOf(
                ReadingTheme.LIGHT,
                ReadingTheme.SEPIA,
                ReadingTheme.DARK,
                ReadingTheme.MIST
            )
            val secondRowThemes = listOf(
                ReadingTheme.SAGE,
                ReadingTheme.SAND,
                ReadingTheme.DUSK,
                ReadingTheme.CANDLE
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                firstRowThemes.forEach { theme ->
                    ReadingThemeSwatchCard(
                        theme = theme,
                        selected = settings.readingTheme == theme,
                        onClick = { onSelectReadingTheme(theme) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                secondRowThemes.forEach { theme ->
                    ReadingThemeSwatchCard(
                        theme = theme,
                        selected = settings.readingTheme == theme,
                        onClick = { onSelectReadingTheme(theme) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            ReadingSettingSwitchRow(
                label = "Vertical scroll",
                checked = settings.verticalScroll,
                onCheckedChange = onVerticalScrollChange,
                testTag = "switch_vertical_scroll"
            )

            Spacer(modifier = Modifier.height(10.dp))

            ReadingSettingSwitchRow(
                label = "Swipe by page",
                checked = settings.swipeByPage,
                onCheckedChange = onSwipeByPageChange,
                testTag = "switch_swipe_by_page"
            )

            Spacer(modifier = Modifier.height(10.dp))

            ReadingSettingSwitchRow(
                label = "Show scroll bar",
                checked = settings.showScrollBar,
                onCheckedChange = onShowScrollBarChange,
                testTag = "switch_show_scroll_bar"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReadingThemeSwatchCard(
    theme: ReadingTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("reading_theme_${theme.name.lowercase()}")
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            // Outer selection ring when active
            val cardShape = RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
                    .height(66.dp)
                    .then(
                        if (selected) {
                            Modifier
                                .border(2.5.dp, Color(0xFFD99B93), RoundedCornerShape(21.dp))
                                .padding(4.dp)
                        } else {
                            Modifier
                        }
                    )
                    .clip(cardShape)
                    .background(theme.previewBg)
                    .border(
                        width = 1.dp,
                        color = if (theme == ReadingTheme.DARK || theme == ReadingTheme.CANDLE) {
                            Color.White.copy(alpha = 0.14f)
                        } else {
                            Color.Transparent
                        },
                        shape = cardShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aa",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = theme.previewText
                )
            }

            // Selected checkmark circle OR "Pro" badge at top center matching Screenshot 3
            if (selected) {
                Box(
                    modifier = Modifier
                        .offset(y = 12.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD99B93))
                        .border(1.5.dp, Color(0xFF421714), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color(0xFF421714),
                        modifier = Modifier.size(13.dp)
                    )
                }
            } else if (theme.isProFeature) {
                Surface(
                    shape = CircleShape,
                    color = BurgundyBadgeBg,
                    modifier = Modifier.offset(y = 0.dp)
                ) {
                    Text(
                        text = "Pro",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = BurgundyBadgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = theme.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) Color(0xFFECE6E4) else Color(0xFFA29A97)
        )
    }
}

@Composable
fun ReadingSettingSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.5.sp),
            color = Color(0xFFDAD2CF),
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            thumbContent = if (checked) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFFD99B93),
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF5C1E1B),
                checkedTrackColor = Color(0xFFD99B93),
                uncheckedThumbColor = Color(0xFF938B88),
                uncheckedTrackColor = Color(0xFF332E2E),
                uncheckedBorderColor = Color(0xFF8C8481)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentActionBottomSheet(
    doc: PdfDocumentEntity,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onOpenWith: () -> Unit,
    onPrint: () -> Unit,
    onShowFileInfo: () -> Unit,
    onStartReadAloud: () -> Unit,
    onStartAutoScroll: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenQuotes: () -> Unit,
    onPinNotification: () -> Unit,
    onShowInFolder: () -> Unit,
    onMoveToFolder: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1F1C1C),
        contentColor = Color(0xFFECE6E4),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = doc.title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                color = Color(0xFFECE6E4),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Quick Action Cards matching Screenshot 6 (Share, Open with, Print, File info)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TopQuickActionTile(
                    icon = Icons.Default.Share,
                    label = "Share",
                    onClick = {
                        onDismiss()
                        onShare()
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "doc_action_share"
                )
                TopQuickActionTile(
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    label = "Open with",
                    onClick = {
                        onDismiss()
                        onOpenWith()
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "doc_action_open_with"
                )
                TopQuickActionTile(
                    icon = Icons.Default.Print,
                    label = "Print",
                    onClick = {
                        onDismiss()
                        onPrint()
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "doc_action_print"
                )
                TopQuickActionTile(
                    icon = Icons.Default.Info,
                    label = "File info",
                    onClick = {
                        onDismiss()
                        onShowFileInfo()
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "doc_action_file_info"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SheetMenuRow(
                icon = Icons.Default.Headphones,
                label = "Read aloud",
                showProBadge = true,
                onClick = {
                    onDismiss()
                    onStartReadAloud()
                },
                testTag = "doc_action_read_aloud"
            )

            SheetMenuRow(
                icon = Icons.Default.KeyboardDoubleArrowDown,
                label = "Auto-scroll",
                showProBadge = true,
                onClick = {
                    onDismiss()
                    onStartAutoScroll()
                },
                testTag = "doc_action_auto_scroll"
            )

            HorizontalDivider(
                color = Color(0xFF312C2C),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            SheetMenuRow(
                icon = Icons.Default.BookmarkBorder,
                label = "Bookmarks",
                onClick = {
                    onDismiss()
                    onOpenBookmarks()
                },
                testTag = "doc_action_bookmarks"
            )

            SheetMenuRow(
                icon = Icons.Default.FormatQuote,
                label = "Quotes",
                onClick = {
                    onDismiss()
                    onOpenQuotes()
                },
                testTag = "doc_action_quotes"
            )

            HorizontalDivider(
                color = Color(0xFF312C2C),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            SheetMenuRow(
                icon = Icons.Outlined.Notifications,
                label = "Pin notification",
                onClick = {
                    onDismiss()
                    onPinNotification()
                },
                testTag = "doc_action_pin_notification"
            )

            SheetMenuRow(
                icon = Icons.Outlined.Folder,
                label = "Show in folder",
                onClick = {
                    onDismiss()
                    onShowInFolder()
                },
                testTag = "doc_action_show_in_folder"
            )

            SheetMenuRow(
                icon = Icons.Default.DriveFileMove,
                label = "Move to custom folder",
                onClick = {
                    onDismiss()
                    onMoveToFolder()
                },
                testTag = "doc_action_move_folder"
            )

            HorizontalDivider(
                color = Color(0xFF312C2C),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            SheetMenuRow(
                icon = Icons.Default.DeleteOutline,
                label = "Delete",
                onClick = {
                    onDismiss()
                    onDelete()
                },
                testTag = "doc_action_delete"
            )
        }
    }
}

@Composable
private fun TopQuickActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF282424),
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFD6CECB),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFD6CECB),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SheetMenuRow(
    icon: ImageVector,
    label: String,
    showProBadge: Boolean = false,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFA79F9C),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            color = Color(0xFFDCD5D2),
            modifier = Modifier.weight(1f)
        )
        if (showProBadge) {
            Surface(
                shape = CircleShape,
                color = BurgundyBadgeBg
            ) {
                Text(
                    text = "Pro",
                    style = MaterialTheme.typography.labelMedium,
                    color = BurgundyBadgeText,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentsBookmarksQuotesSheet(
    doc: PdfDocumentEntity,
    initialTab: Int = 0,
    bookmarks: List<BookmarkEntity>,
    quotes: List<QuoteEntity>,
    annotations: List<AnnotationEntity>,
    onDismiss: () -> Unit,
    onJumpToPage: (Int) -> Unit,
    onDeleteBookmark: (BookmarkEntity) -> Unit,
    onDeleteQuote: (QuoteEntity) -> Unit,
    onDeleteAnnotation: (AnnotationEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val chapters = remember(doc.title, doc.pageCount) {
        BundledPdfSeeder.getDocumentOutline(doc.title, doc.pageCount)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1F1C1C),
        contentColor = Color(0xFFECE6E4),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 380.dp, max = 560.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1F1C1C),
                contentColor = Color(0xFFD99B93)
            ) {
                listOf("Contents", "Bookmarks (${bookmarks.size})", "Quotes (${quotes.size})", "Markup (${annotations.size})").forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> {
                        items(chapters) { chapter ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF282424),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onJumpToPage(chapter.pageIndex)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = chapter.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFFECE6E4)
                                        )
                                        Text(
                                            text = chapter.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFA29A97)
                                        )
                                    }
                                    Text(
                                        text = "p. ${chapter.pageIndex + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFFD99B93)
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        if (bookmarks.isEmpty()) {
                            item {
                                Text(
                                    text = "No bookmarks yet. Tap Bookmark in the bottom bar while reading.",
                                    color = Color(0xFFA29A97),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(bookmarks, key = { it.id }) { bm ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF282424),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onJumpToPage(bm.pageIndex)
                                            onDismiss()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(bm.title, style = MaterialTheme.typography.titleMedium, color = Color(0xFFECE6E4))
                                            Text("Page ${bm.pageIndex + 1}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD99B93))
                                        }
                                        IconButton(onClick = { onDeleteBookmark(bm) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete bookmark", tint = Color(0xFFA29A97))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        if (quotes.isEmpty()) {
                            item {
                                Text(
                                    text = "No saved quotes yet. Switch to Reflow mode to highlight and save passages.",
                                    color = Color(0xFFA29A97),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(quotes, key = { it.id }) { qt ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF282424),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onJumpToPage(qt.pageIndex)
                                            onDismiss()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "“${qt.quoteText}”",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFFECE6E4)
                                            )
                                            if (qt.note.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Note: ${qt.note}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFFD99B93)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Page ${qt.pageIndex + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFA29A97)
                                            )
                                        }
                                        IconButton(onClick = { onDeleteQuote(qt) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete quote", tint = Color(0xFFA29A97))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        if (annotations.isEmpty()) {
                            item {
                                Text(
                                    text = "No sidebar annotations on this PDF yet. Use the Pen, Highlighter, or Sticky Note tools on the left sidebar!",
                                    color = Color(0xFFA29A97),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(annotations, key = { it.id }) { ann ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF282424),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onJumpToPage(ann.pageIndex)
                                            onDismiss()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(Color(ann.colorHex))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (ann.noteText.isNotBlank()) "Sticky Note: ${ann.noteText}" else "${ann.toolType} Markup",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = Color(0xFFECE6E4)
                                            )
                                            Text(
                                                text = "Page ${ann.pageIndex + 1}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFA29A97)
                                            )
                                        }
                                        IconButton(onClick = { onDeleteAnnotation(ann) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete markup", tint = Color(0xFFA29A97))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileInfoDialog(
    doc: PdfDocumentEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("File Information") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Title: ${doc.title}", style = MaterialTheme.typography.bodyMedium)
                Text("Author: ${doc.author}", style = MaterialTheme.typography.bodyMedium)
                Text("Folder: ${doc.folderName} (${doc.storageVolume})", style = MaterialTheme.typography.bodyMedium)
                Text("Pages: ${doc.pageCount} pages (Current: Page ${doc.currentPage + 1})", style = MaterialTheme.typography.bodyMedium)
                Text("Size: ${formatFileSize(doc.sizeBytes)}", style = MaterialTheme.typography.bodyMedium)
                Text("Modified: ${formatShortDate(doc.createdAt)}", style = MaterialTheme.typography.bodyMedium)
                Text("Path: ${doc.filePath}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
