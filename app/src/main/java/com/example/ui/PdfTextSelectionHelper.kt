package com.example.ui

import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pdf.NormalizedRect
import com.example.pdf.PdfPageTextLayout
import com.example.pdf.PdfTextExtractor
import kotlin.math.max
import kotlin.math.min

/**
 * Active on-page PDF text selection state.
 * Holds the exact word range on [pageIndex] so blue highlight rects, draggable teardrop handles,
 * and Android's native floating ActionMode toolbar stay synchronized in real time.
 */
data class PdfPageSelectionState(
    val pageIndex: Int,
    val layout: PdfPageTextLayout,
    val startWordIdx: Int,
    val endWordIdx: Int,
    val isDraggingHandle: Boolean = false
) {
    val loIdx: Int get() = min(startWordIdx, endWordIdx).coerceIn(0, (layout.words.lastIndex).coerceAtLeast(0))
    val hiIdx: Int get() = max(startWordIdx, endWordIdx).coerceIn(0, (layout.words.lastIndex).coerceAtLeast(0))

    val selectedRects: List<NormalizedRect>
        get() = PdfTextExtractor.buildSelectionRects(layout, loIdx, hiIdx)

    val selectedText: String
        get() = PdfTextExtractor.buildSelectedText(layout, loIdx, hiIdx)

    val startHandleAnchor: NormalizedRect?
        get() = layout.words.getOrNull(loIdx)?.rect

    val endHandleAnchor: NormalizedRect?
        get() = layout.words.getOrNull(hiIdx)?.rect
}

private const val MENU_ID_COPY = Menu.FIRST + 1
private const val MENU_ID_SELECT_ALL = Menu.FIRST + 2
private const val MENU_ID_SHARE = Menu.FIRST + 3
private const val MENU_ID_HIGHLIGHT = Menu.FIRST + 4
private const val MENU_ID_QUOTE = Menu.FIRST + 5
private const val MENU_ID_WEB_SEARCH = Menu.FIRST + 6
private const val MENU_ID_PROCESS_TEXT_BASE = Menu.FIRST + 100

/**
 * Launches the native Android floating text selection toolbar (ActionMode.TYPE_FLOATING)
 * positioned directly around [anchorRectInView] (in local View pixel coordinates), exposing
 * standard Android actions: Copy, Select all, Share, Highlight, Quote, Web Search, and
 * installed Android PROCESS_TEXT apps (Translate, Dictionary, etc.).
 */
fun launchNativeSelectionActionMode(
    view: View,
    context: Context,
    anchorRectInView: Rect,
    getSelectedText: () -> String,
    onCopy: () -> Unit,
    onSelectAll: () -> Unit,
    onShare: () -> Unit,
    onHighlight: () -> Unit,
    onQuote: () -> Unit,
    onWebSearch: () -> Unit,
    onDismissSelection: () -> Unit
): ActionMode? {
    val processTextIntents = queryProcessTextActivities(context)
    val callback = object : ActionMode.Callback2() {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.add(0, MENU_ID_COPY, 0, "Copy")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            menu.add(0, MENU_ID_SHARE, 1, "Share")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            menu.add(0, MENU_ID_SELECT_ALL, 2, "Select all")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            menu.add(0, MENU_ID_HIGHLIGHT, 3, "Highlight")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            menu.add(0, MENU_ID_QUOTE, 4, "Save Quote")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            menu.add(0, MENU_ID_WEB_SEARCH, 5, "Web search")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

            // Populate system PROCESS_TEXT actions (Google Translate, Dictionary, etc.)
            processTextIntents.forEachIndexed { idx, (label, _) ->
                menu.add(0, MENU_ID_PROCESS_TEXT_BASE + idx, 10 + idx, label)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                MENU_ID_COPY -> {
                    onCopy()
                    mode.finish()
                    true
                }
                MENU_ID_SELECT_ALL -> {
                    onSelectAll()
                    true
                }
                MENU_ID_SHARE -> {
                    onShare()
                    mode.finish()
                    true
                }
                MENU_ID_HIGHLIGHT -> {
                    onHighlight()
                    mode.finish()
                    true
                }
                MENU_ID_QUOTE -> {
                    onQuote()
                    mode.finish()
                    true
                }
                MENU_ID_WEB_SEARCH -> {
                    onWebSearch()
                    mode.finish()
                    true
                }
                else -> {
                    val procIdx = item.itemId - MENU_ID_PROCESS_TEXT_BASE
                    val entry = processTextIntents.getOrNull(procIdx)
                    if (entry != null) {
                        val selected = getSelectedText()
                        if (selected.isNotBlank()) {
                            runCatching {
                                val intent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
                                    type = "text/plain"
                                    component = entry.second
                                    putExtra(Intent.EXTRA_PROCESS_TEXT, selected)
                                    putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        }
                        mode.finish()
                        true
                    } else {
                        false
                    }
                }
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            onDismissSelection()
        }

        override fun onGetContentRect(mode: ActionMode, view: View, outRect: Rect) {
            outRect.set(anchorRectInView)
        }
    }

    return runCatching {
        view.startActionMode(callback, ActionMode.TYPE_FLOATING)
    }.getOrNull()
}

private fun queryProcessTextActivities(context: Context): List<Pair<CharSequence, ComponentName>> {
    return try {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_PROCESS_TEXT).apply { type = "text/plain" }
        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }
        resolved
            .filter { it.activityInfo != null && it.activityInfo.packageName != context.packageName }
            .take(4)
            .map { info ->
                val label = info.loadLabel(pm)
                val comp = ComponentName(info.activityInfo.packageName, info.activityInfo.name)
                label to comp
            }
    } catch (_: Exception) {
        emptyList()
    }
}

fun copyTextToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}

fun shareSelectedPlainText(context: Context, title: String, text: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share text").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

fun searchSelectedTextOnWeb(context: Context, text: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, text.take(200))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

/**
 * Compact fallback floating pill toolbar shown ONLY if the OEM device returns null from
 * View.startActionMode(ActionMode.TYPE_FLOATING).
 */
@Composable
fun FallbackFloatingSelectionToolbar(
    onCopy: () -> Unit,
    onSelectAll: () -> Unit,
    onHighlight: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF232020),
        shadowElevation = 10.dp,
        tonalElevation = 6.dp,
        modifier = modifier.testTag("fallback_selection_toolbar")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionToolbarChip(label = "Copy", onClick = onCopy, testTag = "selection_action_copy")
            SelectionToolbarChip(label = "Select all", onClick = onSelectAll, testTag = "selection_action_select_all")
            SelectionToolbarChip(label = "Highlight", onClick = onHighlight, testTag = "selection_action_highlight")
            SelectionToolbarChip(label = "Share", onClick = onShare, testTag = "selection_action_share")
        }
    }
}

@Composable
private fun SelectionToolbarChip(
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge.copy(
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium
        ),
        color = Color(0xFFECE6E4),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(testTag)
    )
}
