package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.EditorialSerif
import com.example.ui.theme.ProCardBackground
import com.example.viewmodel.ReaderSettingsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMoreBottomSheet(
    appThemeMode: AppThemeMode,
    onDismiss: () -> Unit,
    onOpenProVersionScreen: () -> Unit,
    onSelectAppTheme: (AppThemeMode) -> Unit,
    onPickPdfFromDevice: () -> Unit,
    onRescanStorage: () -> Unit,
    onOpenSettingsDialog: () -> Unit,
    onLeaveRating: () -> Unit,
    onSendFeedback: () -> Unit
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Pro Version Banner Card matching Screenshot 5
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ProCardBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        onDismiss()
                        onOpenProVersionScreen()
                    }
                    .testTag("home_pro_banner_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StackedPdfPagesIllustration(modifier = Modifier.size(64.dp))

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pro Version",
                            fontFamily = EditorialSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp,
                            color = Color(0xFFECE6E4)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No ads, unlimited search, quotes and bookmarks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFB5A8A4)
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open Pro Version",
                        tint = Color(0xFFB5A8A4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFA79E9B),
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )

            // 3-Way Segmented Theme Selector (System | Light | Dark) matching Screenshot 5
            Surface(
                shape = CircleShape,
                color = Color(0xFF2A2626),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    AppThemeMode.entries.forEach { mode ->
                        val selected = appThemeMode == mode
                        val icon = when (mode) {
                            AppThemeMode.SYSTEM -> Icons.Default.Brightness6
                            AppThemeMode.LIGHT -> Icons.Default.LightMode
                            AppThemeMode.DARK -> Icons.Default.DarkMode
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    if (selected) Color(0xFFA59C94)
                                    else Color.Transparent
                                )
                                .clickable { onSelectAppTheme(mode) }
                                .testTag("app_theme_${mode.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (selected) Color(0xFF1E1A19) else Color(0xFFA79E9B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) Color(0xFF1E1A19) else Color(0xFFA79E9B)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Open a file from device
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onDismiss()
                        onPickPdfFromDevice()
                    }
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .testTag("menu_open_file_from_device"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.UploadFile,
                    contentDescription = null,
                    tint = Color(0xFFA79E9B),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Open a file from device",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        color = Color(0xFFECE6E4)
                    )
                    Text(
                        text = "Any PDF, via the system file picker",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9C9390)
                    )
                }
            }

            HomeMenuSimpleRow(
                icon = Icons.Default.Refresh,
                label = "Rescan storage",
                onClick = {
                    onDismiss()
                    onRescanStorage()
                },
                testTag = "menu_rescan_storage"
            )

            HomeMenuSimpleRow(
                icon = Icons.Default.Settings,
                label = "Settings",
                onClick = {
                    onDismiss()
                    onOpenSettingsDialog()
                },
                testTag = "menu_settings"
            )

            HorizontalDivider(
                color = Color(0xFF312C2C),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            HomeMenuSimpleRow(
                icon = Icons.Default.RateReview,
                label = "Leave a rating",
                onClick = {
                    onDismiss()
                    onLeaveRating()
                },
                testTag = "menu_leave_rating"
            )

            HomeMenuSimpleRow(
                icon = Icons.Default.Feedback,
                label = "Feedback",
                onClick = {
                    onDismiss()
                    onSendFeedback()
                },
                testTag = "menu_feedback"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Version 1.18.2 • Pro Unlocked",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8C827F),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun HomeMenuSimpleRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFA79E9B),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            color = Color(0xFFECE6E4)
        )
    }
}

/**
 * Full-Screen Pro Version Showcase matching Screenshot 7.
 * All 11 Pro features are unlocked out-of-the-box as confirmed by the user.
 */
@Composable
fun ProVersionShowcaseScreen(
    onBack: () -> Unit,
    onRestoreOrContinue: () -> Unit
) {
    BackHandler(onBack = onBack)

    Scaffold(
        containerColor = Color(0xFF141111),
        bottomBar = {
            Surface(
                color = Color(0xFF1A1616),
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹340.00",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFECE6E4)
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2B2626)
                        ) {
                            Text(
                                text = "one-time • UNLOCKED",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFD99B93),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFD99B93),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onRestoreOrContinue)
                            .testTag("pro_continue_button")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "Continue (All Active)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF3E1411)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF3E1411),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Burgundy Hero Header matching Screenshot 7
            Surface(
                color = Color(0xFF221413),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFFECE6E4)
                            )
                        }
                        TextButton(onClick = onRestoreOrContinue) {
                            Text(
                                text = "Restore",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFFD8CECB)
                            )
                        }
                    }

                    StackedPdfPagesIllustration(modifier = Modifier.size(120.dp))

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Pro Version",
                        fontFamily = EditorialSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color(0xFFECE6E4)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Lifetime access to all premium features\nNo recurring charges",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFA79A96),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Highlighted Top 3 Red-Circle Feature Rows matching Screenshot 7
            ProHighlightedFeatureRow(
                icon = Icons.Default.Block,
                title = "Remove ads",
                subtitle = "Read PDFs without banner ads"
            )
            ProHighlightedFeatureRow(
                icon = Icons.Default.ContentCopy,
                title = "Text selection tools",
                subtitle = "Share, translate, and search selected text"
            )
            ProHighlightedFeatureRow(
                icon = Icons.Default.Search,
                title = "Unlimited PDF search",
                subtitle = "Search inside any PDF without limits"
            )

            HorizontalDivider(
                color = Color(0xFF2B2626),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
            )

            // Remaining 8 Feature Rows matching Screenshot 7
            ProStandardFeatureRow(
                icon = Icons.Default.Headphones,
                title = "Read aloud",
                subtitle = "Listen to your PDF in the device voice"
            )
            ProStandardFeatureRow(
                icon = Icons.Default.KeyboardDoubleArrowDown,
                title = "Auto-scroll",
                subtitle = "Hands-free scrolling at your reading pace"
            )
            ProStandardFeatureRow(
                icon = Icons.Default.Palette,
                title = "Reading themes",
                subtitle = "5 calm page colors for day and night reading"
            )
            ProStandardFeatureRow(
                icon = Icons.Default.FormatQuote,
                title = "Save & manage quotes",
                subtitle = "Highlight passages and build a personal quote library"
            )
            ProStandardFeatureRow(
                icon = Icons.Default.BookmarkBorder,
                title = "Enhanced bookmarks",
                subtitle = "Title your bookmarks and navigate across all your PDFs"
            )
            ProStandardFeatureRow(
                icon = Icons.Outlined.Folder,
                title = "Custom folder organization",
                subtitle = "Move PDFs into your own folders and keep your library tidy"
            )
            ProStandardFeatureRow(
                icon = Icons.Outlined.Notifications,
                title = "Pin reading notifications",
                subtitle = "Keep a continue-reading shortcut pinned in your notifications"
            )
            ProStandardFeatureRow(
                icon = Icons.Default.History,
                title = "Auto-resume last PDF",
                subtitle = "Automatically open where you left off every time you launch the app"
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProHighlightedFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF6B221F)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFF3C6C1),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(18.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                color = Color(0xFFECE6E4)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9C9390)
            )
        }
    }
}

@Composable
private fun ProStandardFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFA79E9B),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                color = Color(0xFFECE6E4)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9C9390)
            )
        }
    }
}

@Composable
private fun StackedPdfPagesIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Left terracotta tilted page
        rotate(degrees = -12f, pivot = Offset(w * 0.35f, h * 0.55f)) {
            drawRoundRect(
                color = Color(0xFF8C483F),
                topLeft = Offset(w * 0.14f, h * 0.18f),
                size = Size(w * 0.38f, h * 0.58f),
                cornerRadius = CornerRadius(8f, 8f)
            )
        }

        // Right warm sand tilted page
        rotate(degrees = 12f, pivot = Offset(w * 0.65f, h * 0.55f)) {
            drawRoundRect(
                color = Color(0xFFBEB8AB),
                topLeft = Offset(w * 0.48f, h * 0.18f),
                size = Size(w * 0.38f, h * 0.58f),
                cornerRadius = CornerRadius(8f, 8f)
            )
        }

        // Center white document page
        drawRoundRect(
            color = Color(0xFFE5E5E5),
            topLeft = Offset(w * 0.28f, h * 0.12f),
            size = Size(w * 0.44f, h * 0.64f),
            cornerRadius = CornerRadius(10f, 10f)
        )

        // Yellow highlight bar inside center page
        drawRoundRect(
            color = Color(0xFFE0CE8D),
            topLeft = Offset(w * 0.34f, h * 0.36f),
            size = Size(w * 0.30f, h * 0.05f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Bottom crimson badge
        drawRoundRect(
            color = Color(0xFFB83B31),
            topLeft = Offset(w * 0.40f, h * 0.58f),
            size = Size(w * 0.24f, h * 0.24f),
            cornerRadius = CornerRadius(12f, 12f)
        )
    }
}

@Composable
fun AppSettingsAndSideloadDialog(
    settings: ReaderSettingsState,
    onAutoResumeChange: (Boolean) -> Unit,
    onVerticalScrollChange: (Boolean) -> Unit,
    onShowScrollBarChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Orion PDF Settings & APK Info") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReadingSettingSwitchRow(
                    label = "Auto-resume last PDF",
                    checked = settings.autoResumeLastPdf,
                    onCheckedChange = onAutoResumeChange,
                    testTag = "settings_auto_resume_switch"
                )
                ReadingSettingSwitchRow(
                    label = "Vertical scroll by default",
                    checked = settings.verticalScroll,
                    onCheckedChange = onVerticalScrollChange,
                    testTag = "settings_vertical_scroll_switch"
                )
                ReadingSettingSwitchRow(
                    label = "Show scroll bar",
                    checked = settings.showScrollBar,
                    onCheckedChange = onShowScrollBarChange,
                    testTag = "settings_scrollbar_switch"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Sideloading APK onto Your Device:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "To install this app on your phone for offline testing, open the AI Studio Settings / Export menu at the top right to download the signed APK directly, or export the project ZIP.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun CreateOrMoveFolderDialog(
    title: String,
    existingFolders: List<String>,
    isMoveMode: Boolean,
    onSelectExistingFolder: (String) -> Unit,
    onCreateNewFolder: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newFolderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("New folder name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isMoveMode && existingFolders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Or pick an existing folder:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    existingFolders.forEach { folder ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectExistingFolder(folder)
                                    onDismiss()
                                }
                        ) {
                            Text(
                                text = folder,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newFolderName.isNotBlank()) {
                        onCreateNewFolder(newFolderName.trim())
                        if (isMoveMode) {
                            onSelectExistingFolder(newFolderName.trim())
                        }
                        onDismiss()
                    }
                }
            ) {
                Text(if (isMoveMode) "Create & Move" else "Create Folder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
