package com.example.pdf

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class DiscoveredPdf(
    val pathOrUri: String,
    val title: String,
    val folderName: String,
    val storageVolume: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val pageCount: Int
)

/**
 * Scans real PDF files stored on the user's Android device using:
 * 1. Direct external storage / SD card traversal when All Files Access (Android 11+) or READ_EXTERNAL_STORAGE (Android 10-) is granted.
 * 2. MediaStore.Files PDF query as a complementary source.
 * 3. Persisted Storage Access Framework (SAF) folder trees selected by the user.
 */
object DevicePdfScanner {

    private val skippedDirNames = setOf(
        "Android",
        ".thumbnails",
        ".trash",
        ".Trash",
        ".cache",
        "cache",
        "DCIM",
        "Pictures",
        "Movies",
        "Music",
        "Ringtones",
        "Notifications",
        "Alarms"
    )

    fun hasAllFilesPermission(context: Context): Boolean {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        }.getOrDefault(false)
    }

    suspend fun scanAllAccessiblePdfs(
        context: Context,
        persistedTreeUris: Set<String>,
        onProgressCount: ((Int) -> Unit)? = null
    ): List<DiscoveredPdf> = withContext(Dispatchers.IO) {
        val discoveredByKey = LinkedHashMap<String, DiscoveredPdf>()
        val hasFullAccess = hasAllFilesPermission(context)

        // 1. Direct volume scan when full storage access is granted
        if (hasFullAccess) {
            val roots = collectStorageRoots(context)
            for ((rootDir, volumeLabel) in roots) {
                scanDirectoryRecursive(
                    context = context,
                    dir = rootDir,
                    volumeLabel = volumeLabel,
                    depth = 0,
                    maxDepth = 8,
                    results = discoveredByKey,
                    onProgressCount = onProgressCount
                )
            }
        }

        // 2. MediaStore.Files fallback / supplement (captures indexed PDFs in Downloads/Documents)
        scanMediaStorePdfs(context, discoveredByKey, onProgressCount)

        // 3. User-picked SAF folder trees (works even without All Files Access)
        for (treeUriStr in persistedTreeUris) {
            runCatching {
                val treeUri = Uri.parse(treeUriStr)
                val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
                val rootFolderName = extractFolderNameFromDocId(rootDocId)
                scanSafTreeRecursive(
                    context = context,
                    treeUri = treeUri,
                    parentDocId = rootDocId,
                    currentFolderName = rootFolderName,
                    depth = 0,
                    maxDepth = 6,
                    results = discoveredByKey,
                    onProgressCount = onProgressCount
                )
            }
        }

        // 4. App's own imported/exported PDF directories
        val appImportedDir = File(context.filesDir, "imported_pdfs")
        if (appImportedDir.exists()) {
            scanDirectoryRecursive(
                context = context,
                dir = appImportedDir,
                volumeLabel = "Internal storage",
                depth = 0,
                maxDepth = 2,
                results = discoveredByKey,
                onProgressCount = onProgressCount
            )
        }
        val appExportedDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "OrionPDF")
        if (appExportedDir.exists()) {
            scanDirectoryRecursive(
                context = context,
                dir = appExportedDir,
                volumeLabel = "Internal storage",
                depth = 0,
                maxDepth = 2,
                results = discoveredByKey,
                onProgressCount = onProgressCount
            )
        }

        discoveredByKey.values.toList()
    }

    private fun collectStorageRoots(context: Context): List<Pair<File, String>> {
        val roots = LinkedHashMap<String, Pair<File, String>>()
        val primary = Environment.getExternalStorageDirectory()
        if (primary != null && primary.exists() && primary.canRead()) {
            roots[primary.absolutePath] = primary to "Internal storage"
        }

        // Detect removable SD cards / secondary volumes via getExternalFilesDirs
        context.getExternalFilesDirs(null).orEmpty().forEach { appExtDir ->
            if (appExtDir != null) {
                val path = appExtDir.absolutePath
                val androidIdx = path.indexOf("/Android/data")
                if (androidIdx > 0) {
                    val volPath = path.substring(0, androidIdx)
                    if (!roots.containsKey(volPath)) {
                        val volFile = File(volPath)
                        if (volFile.exists() && volFile.canRead()) {
                            val label = if (volPath.contains("emulated/0")) {
                                "Internal storage"
                            } else {
                                volFile.name.ifBlank { "SD Card" }
                            }
                            roots[volPath] = volFile to label
                        }
                    }
                }
            }
        }
        return roots.values.toList()
    }

    private fun scanDirectoryRecursive(
        context: Context,
        dir: File,
        volumeLabel: String,
        depth: Int,
        maxDepth: Int,
        results: LinkedHashMap<String, DiscoveredPdf>,
        onProgressCount: ((Int) -> Unit)?
    ) {
        if (depth > maxDepth || !dir.exists() || !dir.isDirectory || !dir.canRead()) return
        val children = dir.listFiles() ?: return

        for (child in children) {
            if (child.isDirectory) {
                val name = child.name
                if (depth == 0 && name in skippedDirNames) continue
                if (name == "Android" || name == ".thumbnails" || name == ".trash") continue
                scanDirectoryRecursive(
                    context = context,
                    dir = child,
                    volumeLabel = volumeLabel,
                    depth = depth + 1,
                    maxDepth = maxDepth,
                    results = results,
                    onProgressCount = onProgressCount
                )
            } else if (child.isFile && child.name.endsWith(".pdf", ignoreCase = true) && child.length() > 0L) {
                val key = child.absolutePath
                if (results.containsKey(key)) continue
                val pages = PdfRenderEngine.inspectPdfPageCountSync(context, key)
                if (pages <= 0) continue

                val folderName = child.parentFile?.name?.ifBlank { "Internal storage" } ?: "Internal storage"
                val title = child.name.removeSuffix(".pdf").removeSuffix(".PDF")
                results[key] = DiscoveredPdf(
                    pathOrUri = key,
                    title = title,
                    folderName = folderName,
                    storageVolume = volumeLabel,
                    sizeBytes = child.length(),
                    lastModified = child.lastModified().takeIf { it > 0L } ?: System.currentTimeMillis(),
                    pageCount = pages
                )
                onProgressCount?.invoke(results.size)
            }
        }
    }

    private fun scanMediaStorePdfs(
        context: Context,
        results: LinkedHashMap<String, DiscoveredPdf>,
        onProgressCount: ((Int) -> Unit)?
    ) {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val projection = mutableListOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection.add(MediaStore.Files.FileColumns.RELATIVE_PATH)
        }

        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("application/pdf", "%.pdf")

        runCatching {
            context.contentResolver.query(
                collection,
                projection.toTypedArray(),
                selection,
                selectionArgs,
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val dataCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                val relPathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH)
                } else -1

                while (cursor.moveToNext()) {
                    val id = if (idCol >= 0) cursor.getLong(idCol) else continue
                    val displayName = if (nameCol >= 0) cursor.getString(nameCol) else null
                    val rawPath = if (dataCol >= 0) cursor.getString(dataCol) else null
                    val size = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                    val modifiedSec = if (dateCol >= 0) cursor.getLong(dateCol) else 0L
                    val relPath = if (relPathCol >= 0) cursor.getString(relPathCol) else null

                    val directFile = rawPath?.let { File(it) }?.takeIf { it.exists() && it.canRead() && it.length() > 0L }
                    val pathOrUri = directFile?.absolutePath
                        ?: Uri.withAppendedPath(collection, id.toString()).toString()

                    if (results.containsKey(pathOrUri)) continue
                    if (rawPath != null && results.containsKey(rawPath)) continue

                    val pages = PdfRenderEngine.inspectPdfPageCountSync(context, pathOrUri)
                    if (pages <= 0) continue

                    val nameStr = displayName ?: directFile?.name ?: "Document_$id.pdf"
                    val title = nameStr.removeSuffix(".pdf").removeSuffix(".PDF")
                    val folderName = when {
                        directFile?.parentFile?.name != null -> directFile.parentFile!!.name
                        !relPath.isNullOrBlank() -> relPath.trim('/').substringAfterLast('/').ifBlank { "Internal storage" }
                        else -> "Download"
                    }
                    val volumeLabel = if (rawPath != null && !rawPath.contains("/emulated/0") && rawPath.startsWith("/storage/")) {
                        rawPath.removePrefix("/storage/").substringBefore('/').ifBlank { "SD Card" }
                    } else {
                        "Internal storage"
                    }

                    results[pathOrUri] = DiscoveredPdf(
                        pathOrUri = pathOrUri,
                        title = title,
                        folderName = folderName,
                        storageVolume = volumeLabel,
                        sizeBytes = if (size > 0L) size else (directFile?.length() ?: 0L),
                        lastModified = if (modifiedSec > 0L) modifiedSec * 1000L else System.currentTimeMillis(),
                        pageCount = pages
                    )
                    onProgressCount?.invoke(results.size)
                }
            }
        }
    }

    private fun scanSafTreeRecursive(
        context: Context,
        treeUri: Uri,
        parentDocId: String,
        currentFolderName: String,
        depth: Int,
        maxDepth: Int,
        results: LinkedHashMap<String, DiscoveredPdf>,
        onProgressCount: ((Int) -> Unit)?
    ) {
        if (depth > maxDepth) return
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )

        runCatching {
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val sizeIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
                val modIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                while (cursor.moveToNext()) {
                    val childDocId = if (idIdx >= 0) cursor.getString(idIdx) else continue
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx) ?: "Document" else "Document"
                    val mime = if (mimeIdx >= 0) cursor.getString(mimeIdx) ?: "" else ""
                    val size = if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) cursor.getLong(sizeIdx) else 0L
                    val modified = if (modIdx >= 0 && !cursor.isNull(modIdx)) cursor.getLong(modIdx) else System.currentTimeMillis()

                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                        scanSafTreeRecursive(
                            context = context,
                            treeUri = treeUri,
                            parentDocId = childDocId,
                            currentFolderName = name,
                            depth = depth + 1,
                            maxDepth = maxDepth,
                            results = results,
                            onProgressCount = onProgressCount
                        )
                    } else if (mime.equals("application/pdf", ignoreCase = true) || name.endsWith(".pdf", ignoreCase = true)) {
                        val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childDocId).toString()
                        if (results.containsKey(docUri)) continue

                        val pages = PdfRenderEngine.inspectPdfPageCountSync(context, docUri)
                        if (pages <= 0) continue

                        val volume = if (childDocId.startsWith("primary:", ignoreCase = true)) {
                            "Internal storage"
                        } else {
                            childDocId.substringBefore(':').ifBlank { "External Folder" }
                        }

                        results[docUri] = DiscoveredPdf(
                            pathOrUri = docUri,
                            title = name.removeSuffix(".pdf").removeSuffix(".PDF"),
                            folderName = currentFolderName,
                            storageVolume = volume,
                            sizeBytes = size,
                            lastModified = modified,
                            pageCount = pages
                        )
                        onProgressCount?.invoke(results.size)
                    }
                }
            }
        }
    }

    private fun extractFolderNameFromDocId(docId: String): String {
        val afterColon = docId.substringAfter(':', docId).trim('/')
        return afterColon.substringAfterLast('/').ifBlank { "Selected Folder" }
    }
}
