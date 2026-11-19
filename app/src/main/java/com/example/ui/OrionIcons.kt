package com.example.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Lightweight Material Symbols vector icon set replacing the ~14 MB material-icons-extended
 * dependency so the compiled APK stays lean (~3 MB) for fast downloads and instant emulator install.
 */
object OrionIcons {

    private fun vector(name: String, pathData: String, autoMirror: Boolean = false): ImageVector {
        val nodes = PathParser().parsePathString(pathData).toNodes()
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
            autoMirror = autoMirror
        ).addPath(
            pathData = nodes,
            fill = SolidColor(Color.Black)
        ).build()
    }

    val Article: ImageVector by lazy {
        vector("Article", "M19,3H5C3.9,3 3,3.9 3,5v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V5C21,3.9 20.1,3 19,3zM14,17H7v-2h7V17zM17,13H7v-2h10V13zM17,9H7V7h10V9z", autoMirror = true)
    }
    val FormatListBulleted: ImageVector by lazy {
        vector("FormatListBulleted", "M4,10.5c-0.83,0 -1.5,0.67 -1.5,1.5s0.67,1.5 1.5,1.5 1.5,-0.67 1.5,-1.5 -0.67,-1.5 -1.5,-1.5zM4,4.5c-0.83,0 -1.5,0.67 -1.5,1.5S3.17,7.5 4,7.5 5.5,6.83 5.5,6 4.83,4.5 4,4.5zM4,16.5c-0.83,0 -1.5,0.68 -1.5,1.5s0.68,1.5 1.5,1.5 1.5,-0.68 1.5,-1.5 -0.67,-1.5 -1.5,-1.5zM7,19h14v-2H7v2zM7,13h14v-2H7v2zM7,5v2h14V5H7z", autoMirror = true)
    }
    val OpenInNew: ImageVector by lazy {
        vector("OpenInNew", "M19,19H5V5h7V3H5C3.89,3 3,3.9 3,5v14c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2v-7h-2v7zM14,3v2h3.59l-9.83,9.83 1.41,1.41L19,6.41V10h2V3h-7z", autoMirror = true)
    }
    val Redo: ImageVector by lazy {
        vector("Redo", "M18.4,10.6C16.55,8.99 14.15,8 11.5,8c-4.65,0 -8.58,3.03 -9.96,7.22L3.9,16c1.05,-3.19 4.05,-5.5 7.6,-5.5 1.95,0 3.73,0.72 5.12,1.88L13,16h9V7l-3.6,3.6z", autoMirror = true)
    }
    val Sort: ImageVector by lazy {
        vector("Sort", "M3,18h6v-2H3v2zM3,6v2h18V6H3zM3,13h12v-2H3v2z", autoMirror = true)
    }
    val Undo: ImageVector by lazy {
        vector("Undo", "M12.5,8c-2.65,0 -5.05,0.99 -6.9,2.6L2,7v9h9l-3.62,-3.62c1.39,-1.16 3.16,-1.88 5.12,-1.88 3.54,0 6.55,2.31 7.6,5.5l2.37,-0.78C21.08,11.03 17.15,8 12.5,8z", autoMirror = true)
    }
    val ArrowDownward: ImageVector by lazy {
        vector("ArrowDownward", "M20,12l-1.41,-1.41L13,16.17V4h-2v12.17l-5.58,-5.59L4,12l8,8 8,-8z")
    }
    val ArrowUpward: ImageVector by lazy {
        vector("ArrowUpward", "M4,12l1.41,1.41L11,7.83V20h2V7.83l5.58,5.59L20,12l-8,-8 -8,8z")
    }
    val AutoFixNormal: ImageVector by lazy {
        vector("AutoFixNormal", "M16.24,3.56l4.95,4.94c0.78,0.79 0.78,2.05 0,2.84L12,20.53c-0.78,0.78 -2.05,0.78 -2.83,0L2.81,14.17c-0.78,-0.78 -0.78,-2.05 0,-2.83l10.6,-10.6c0.79,-0.78 2.05,-0.78 2.83,0zM4.22,12.76l6.36,6.36 3.54,-3.54 -6.36,-6.36 -3.54,3.54z")
    }
    val Block: ImageVector by lazy {
        vector("Block", "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM4,12c0,-4.42 3.58,-8 8,-8 1.85,0 3.55,0.63 4.9,1.69L5.69,16.9C4.63,15.55 4,13.85 4,12zM12,20c-1.85,0 -3.55,-0.63 -4.9,-1.69L18.31,7.1C19.37,8.45 20,10.15 20,12c0,4.42 -3.58,8 -8,8z")
    }
    val Bookmark: ImageVector by lazy {
        vector("Bookmark", "M17,3H7c-1.1,0 -1.99,0.9 -1.99,2L5,21l7,-3 7,3V5c0,-1.1 -0.9,-2 -2,-2z")
    }
    val BookmarkBorder: ImageVector by lazy {
        vector("BookmarkBorder", "M17,3H7c-1.1,0 -1.99,0.9 -1.99,2L5,21l7,-3 7,3V5c0,-1.1 -0.9,-2 -2,-2zM17,18l-5,-2.18L7,18V5h10v13z")
    }
    val Brightness6: ImageVector by lazy {
        vector("Brightness6", "M20,15.31L23.31,12 20,8.69V4h-4.69L12,0.69 8.69,4H4v4.69L0.69,12 4,15.31V20h4.69L12,23.31 15.31,20H20v-4.69zM12,18V6c3.31,0 6,2.69 6,6s-2.69,6 -6,6z")
    }
    val BrightnessHigh: ImageVector by lazy {
        vector("BrightnessHigh", "M20,8.69V4h-4.69L12,0.69 8.69,4H4v4.69L0.69,12 4,15.31V20h4.69L12,23.31 15.31,20H20v-4.69L23.31,12 20,8.69zM12,18c-3.31,0 -6,-2.69 -6,-6s2.69,-6 6,-6 6,2.69 6,6 -2.69,6 -6,6zM12,8c-2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4 -1.79,-4 -4,-4z")
    }
    val BrightnessLow: ImageVector by lazy {
        vector("BrightnessLow", "M20,15.31L23.31,12 20,8.69V4h-4.69L12,0.69 8.69,4H4v4.69L0.69,12 4,15.31V20h4.69L12,23.31 15.31,20H20v-4.69zM12,18c-3.31,0 -6,-2.69 -6,-6s2.69,-6 6,-6 6,2.69 6,6 -2.69,6 -6,6z")
    }
    val Brush: ImageVector by lazy {
        vector("Brush", "M7,14c-1.66,0 -3,1.34 -3,3 0,1.31 -1.16,2 -2,2 0.92,1.22 2.49,2 4,2 2.21,0 4,-1.79 4,-4 0,-1.66 -1.34,-3 -3,-3zM20.71,4.63l-1.34,-1.34c-0.39,-0.39 -1.02,-0.39 -1.41,0L9,12.25 11.75,15l8.96,-8.96c0.39,-0.39 0.39,-1.02 0,-1.41z")
    }
    val ContentCopy: ImageVector by lazy {
        vector("ContentCopy", "M16,1H4c-1.1,0 -2,0.9 -2,2v14h2V3h12V1zM19,5H8c-1.1,0 -2,0.9 -2,2v14c0,1.1 0.9,2 2,2h11c1.1,0 2,-0.9 2,-2V7c0,-1.1 -0.9,-2 -2,-2zM19,21H8V7h11v14z")
    }
    val CreateNewFolder: ImageVector by lazy {
        vector("CreateNewFolder", "M20,6h-8l-2,-2H4C2.89,4 2.01,4.89 2.01,6L2,18c0,1.11 0.89,2 2,2h16c1.11,0 2,-0.89 2,-2V8c0,-1.11 -0.89,-2 -2,-2zM19,14h-3v3h-2v-3h-3v-2h3V9h2v3h3v2z")
    }
    val DarkMode: ImageVector by lazy {
        vector("DarkMode", "M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9 9,-4.03 9,-9c0,-0.46 -0.04,-0.92 -0.1,-1.36 -0.98,1.37 -2.58,2.26 -4.4,2.26 -2.98,0 -5.4,-2.42 -5.4,-5.4 0,-1.81 0.89,-3.42 2.26,-4.4C12.92,3.04 12.46,3 12,3z")
    }
    val DeleteOutline: ImageVector by lazy {
        vector("DeleteOutline", "M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM8,9h8v10H8V9zM15.5,4l-1,-1h-5l-1,1H5v2h14V4z")
    }
    val Description: ImageVector by lazy {
        vector("Description", "M14,2H6c-1.1,0 -1.99,0.9 -1.99,2L4,20c0,1.1 0.89,2 1.99,2H18c1.1,0 2,-0.9 2,-2V8l-6,-6zM16,18H8v-2h8v2zM16,14H8v-2h8v2zM13,9V3.5L18.5,9H13z")
    }
    val DescriptionOutlined: ImageVector by lazy {
        vector("DescriptionOutlined", "M8,16h8v2H8zM8,12h8v2H8zM14,2H6C4.9,2 4,2.9 4,4v16c0,1.1 0.89,2 1.99,2H18c1.1,0 2,-0.9 2,-2V8L14,2zM18,20H6V4h7v5h5V20z")
    }
    val DriveFileMove: ImageVector by lazy {
        vector("DriveFileMove", "M20,6h-8l-2,-2H4C2.9,4 2,4.9 2,6v12c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2V8C22,6.9 21.1,6 20,6zM14,18v-3h-4v-4h4V8l5,5L14,18z")
    }
    val Feedback: ImageVector by lazy {
        vector("Feedback", "M20,2H4C2.9,2 2.01,2.9 2.01,4L2,22l4,-4h14c1.1,0 2,-0.9 2,-2V4C22,2.9 21.1,2 20,2zM13,14h-2v-2h2V14zM13,10h-2V6h2V10z")
    }
    val Folder: ImageVector by lazy {
        vector("Folder", "M10,4H4c-1.1,0 -1.99,0.9 -1.99,2L2,18c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2V8c0,-1.1 -0.9,-2 -2,-2h-8l-2,-2z")
    }
    val FolderOutlined: ImageVector by lazy {
        vector("FolderOutlined", "M9.17,6l2,2H20v10H4V6H9.17M10,4H4C2.9,4 2.01,4.9 2.01,6L2,18c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2V8c0,-1.1 -0.9,-2 -2,-2h-8L10,4z")
    }
    val FolderOpen: ImageVector by lazy {
        vector("FolderOpen", "M20,6h-8l-2,-2H4C2.9,4 2.01,4.9 2.01,6L2,18c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2V8C22,6.9 21.1,6 20,6zM20,18H4V8h16V18z")
    }
    val FormatQuote: ImageVector by lazy {
        vector("FormatQuote", "M6,17h3l2,-4V7H5v6h3zM14,17h3l2,-4V7h-6v6h3z")
    }
    val FormatUnderlined: ImageVector by lazy {
        vector("FormatUnderlined", "M12,17c3.31,0 6,-2.69 6,-6V3h-2.5v8c0,1.93 -1.57,3.5 -3.5,3.5S8.5,12.93 8.5,11V3H6v8C6,14.31 8.69,17 12,17zM5,19v2h14v-2H5z")
    }
    val GridView: ImageVector by lazy {
        vector("GridView", "M3,3v8h8V3H3zM9,9H5V5h4V9zM3,13v8h8v-8H3zM9,19H5v-4h4V19zM13,3v8h8V3H13zM19,9h-4V5h4V9zM13,13v8h8v-8H13zM19,19h-4v-4h4V19z")
    }
    val Headphones: ImageVector by lazy {
        vector("Headphones", "M12,3c-4.97,0 -9,4.03 -9,9v7c0,1.1 0.9,2 2,2h4v-8H5v-1c0,-3.87 3.13,-7 7,-7s7,3.13 7,7v1h-4v8h4c1.1,0 2,-0.9 2,-2v-7C21,7.03 16.97,3 12,3z")
    }
    val Highlight: ImageVector by lazy {
        vector("Highlight", "M6,14l3,3v5h6v-5l3,-3V9H6V14zM11,2h2v3h-2V2zM3.5,5.88l1.41,-1.41 2.12,2.12L5.62,8 3.5,5.88zM16.96,6.59l2.12,-2.12 1.41,1.41L18.38,8 16.96,6.59z")
    }
    val History: ImageVector by lazy {
        vector("History", "M13,3c-4.97,0 -9,4.03 -9,9H1l3.89,3.89 0.07,0.14L9,12H6c0,-3.87 3.13,-7 7,-7s7,3.13 7,7 -3.13,7 -7,7c-1.93,0 -3.68,-0.79 -4.94,-2.06l-1.42,1.42C8.27,19.99 10.51,21 13,21c4.97,0 9,-4.03 9,-9s-4.03,-9 -9,-9zM12,8v5l4.28,2.54 0.72,-1.21 -3.5,-2.08V8H12z")
    }
    val KeyboardDoubleArrowDown: ImageVector by lazy {
        vector("KeyboardDoubleArrowDown", "M18,6.41L16.59,5 12,9.58 7.41,5 6,6.41l6,6zM18,13l-1.41,-1.41L12,16.17 7.41,11.59 6,13l6,6z")
    }
    val LightMode: ImageVector by lazy {
        vector("LightMode", "M12,7c-2.76,0 -5,2.24 -5,5s2.24,5 5,5 5,-2.24 5,-5 -2.24,-5 -5,-5zM2,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1H2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM20,13h2c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1h-2c-0.55,0 -1,0.45 -1,1s0.45,1 1,1zM11,2v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1V2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM11,20v2c0,0.55 0.45,1 1,1s1,-0.45 1,-1v-2c0,-0.55 -0.45,-1 -1,-1s-1,0.45 -1,1zM5.99,4.58c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0s0.39,-1.03 0,-1.41L5.99,4.58zM18.36,16.95c-0.39,-0.39 -1.03,-0.39 -1.41,0 -0.39,0.39 -0.39,1.03 0,1.41l1.06,1.06c0.39,0.39 1.03,0.39 1.41,0 0.39,-0.39 0.39,-1.03 0,-1.41l-1.06,-1.06zM19.42,5.99c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06zM7.05,18.36c0.39,-0.39 0.39,-1.03 0,-1.41 -0.39,-0.39 -1.03,-0.39 -1.41,0l-1.06,1.06c-0.39,0.39 -0.39,1.03 0,1.41s1.03,0.39 1.41,0l1.06,-1.06z")
    }
    val LockOpen: ImageVector by lazy {
        vector("LockOpen", "M12,17c1.1,0 2,-0.9 2,-2s-0.9,-2 -2,-2 -2,0.9 -2,2 0.9,2 2,2zM18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6h1.9c0,-1.71 1.39,-3.1 3.1,-3.1 1.71,0 3.1,1.39 3.1,3.1v2H6c-1.1,0 -2,0.9 -2,2v10c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V10c0,-1.1 -0.9,-2 -2,-2zM18,20H6V10h12V20z")
    }
    val Palette: ImageVector by lazy {
        vector("Palette", "M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9c0.83,0 1.5,-0.67 1.5,-1.5 0,-0.39 -0.15,-0.74 -0.39,-1.01 -0.23,-0.26 -0.38,-0.61 -0.38,-0.99 0,-0.83 0.67,-1.5 1.5,-1.5H16c2.76,0 5,-2.24 5,-5 0,-4.42 -4.03,-8 -9,-8zM6.5,12c-0.83,0 -1.5,-0.67 -1.5,-1.5S5.67,9 6.5,9 8,9.67 8,10.5 7.33,12 6.5,12zM9.5,8C8.67,8 8,7.33 8,6.5S8.67,5 9.5,5 11,5.67 11,6.5 10.33,8 9.5,8zM14.5,8C13.67,8 13,7.33 13,6.5S13.67,5 14.5,5 16,5.67 16,6.5 15.33,8 14.5,8zM17.5,12c-0.83,0 -1.5,-0.67 -1.5,-1.5S16.67,9 17.5,9 19,9.67 19,10.5 18.33,12 17.5,12z")
    }
    val Print: ImageVector by lazy {
        vector("Print", "M19,8H5c-1.66,0 -3,1.34 -3,3v6h4v4h12v-4h4v-6c0,-1.66 -1.34,-3 -3,-3zM16,19H8v-5h8V19zM19,12c-0.55,0 -1,-0.45 -1,-1s0.45,-1 1,-1 1,0.45 1,1 -0.45,1 -1,1zM18,3H6v4h12V3z")
    }
    val RateReview: ImageVector by lazy {
        vector("RateReview", "M20,2H4C2.9,2 2.01,2.9 2.01,4L2,22l4,-4h14c1.1,0 2,-0.9 2,-2V4C22,2.9 21.1,2 20,2zM6,14v-2.47l6.88,-6.88c0.2,-0.2 0.51,-0.2 0.71,0l1.77,1.77c0.2,0.2 0.2,0.51 0,0.71L8.47,14H6zM18,14h-7.5l2,-2H18V14z")
    }
    val Remove: ImageVector by lazy {
        vector("Remove", "M19,13H5v-2h14v2z")
    }
    val RotateRight: ImageVector by lazy {
        vector("RotateRight", "M15.55,5.55L11,1v3.07C7.06,4.56 4,7.92 4,12s3.05,7.44 7,7.93v-2.02c-2.84,-0.48 -5,-2.94 -5,-5.91s2.16,-5.43 5,-5.91V10L15.55,5.55zM19.93,11c-0.17,-1.39 -0.72,-2.73 -1.62,-3.89l-1.42,1.42c0.54,0.75 0.88,1.6 1.02,2.47H19.93zM13,17.9v2.02c1.39,-0.17 2.74,-0.71 3.9,-1.61l-1.44,-1.44C14.7,17.42 13.86,17.76 13,17.9zM16.89,15.46l1.42,1.42c0.9,-1.16 1.45,-2.5 1.62,-3.89h-2.02C17.77,13.86 17.43,14.71 16.89,15.46z")
    }
    val SaveAlt: ImageVector by lazy {
        vector("SaveAlt", "M19,12v7H5v-7H3v7c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2v-7H19zM13,12.67l2.59,-2.58L17,11.5l-5,5 -5,-5 1.41,-1.41L11,12.67V3h2V12.67z")
    }
    val ScreenRotation: ImageVector by lazy {
        vector("ScreenRotation", "M16.48,2.52c3.27,1.55 5.61,4.72 5.97,8.48h1.5C23.44,4.84 18.29,0 12,0l-0.66,0.03 3.81,3.81 1.33,-1.32zM10.23,1.75c-0.59,-0.59 -1.54,-0.59 -2.12,0L1.75,8.11c-0.59,0.59 -0.59,1.54 0,2.12l12.02,12.02c0.59,0.59 1.54,0.59 2.12,0l6.36,-6.36c0.59,-0.59 0.59,-1.54 0,-2.12L10.23,1.75zM14.83,21.19L2.81,9.17l6.36,-6.36 12.02,12.02 -6.36,6.36zM7.52,21.48C4.25,19.94 1.91,16.76 1.55,13H0.05C0.56,19.16 5.71,24 12,24l0.66,-0.03 -3.81,-3.81 -1.33,1.32z")
    }
    val Security: ImageVector by lazy {
        vector("Security", "M12,1L3,5v6c0,5.55 3.84,10.74 9,12 5.16,-1.26 9,-6.45 9,-12V5L12,1zM12,11.99h7c-0.53,4.12 -3.28,7.79 -7,8.94V12H5V6.3l7,-3.11V11.99z")
    }
    val StarBorder: ImageVector by lazy {
        vector("StarBorder", "M22,9.24l-7.19,-0.62L12,2 9.19,8.63 2,9.24l5.46,4.73L5.82,21 12,17.27 18.18,21l-1.63,-7.03L22,9.24zM12,15.4l-3.76,2.27 1,-4.28 -3.32,-2.88 4.38,-0.38L12,6.1l1.71,4.04 4.38,0.38 -3.32,2.88 1,4.28L12,15.4z")
    }
    val StickyNote2: ImageVector by lazy {
        vector("StickyNote2", "M19,3H5C3.9,3 3,3.9 3,5v14c0,1.1 0.9,2 2,2h10l6,-6V5C21,3.9 20.1,3 19,3zM7,8h10v2H7V8zM12,14H7v-2h5V14zM14,19.5V14h5.5L14,19.5z")
    }
    val Stop: ImageVector by lazy {
        vector("Stop", "M6,6h12v12H6V6z")
    }
    val Tune: ImageVector by lazy {
        vector("Tune", "M3,17v2h6v-2H3zM3,5v2h10V5H3zM13,21v-2h8v-2h-8v-2h-2v6H13zM7,9v2H3v2h4v2h2V9H7zM21,13v-2H11v2H21zM15,9h2V7h4V5h-4V3h-2V9z")
    }
    val UploadFile: ImageVector by lazy {
        vector("UploadFile", "M14,2H6C4.9,2 4.01,2.9 4.01,4L4,20c0,1.1 0.89,2 1.99,2H18c1.1,0 2,-0.9 2,-2V8L14,2zM18,20H6V4h7v5h5V20zM8,15.01l1.41,1.41L11,14.84V19h2v-4.16l1.59,1.59L16,15.01 12.01,11 8,15.01z")
    }
    val ViewList: ImageVector by lazy {
        vector("ViewList", "M3,14h4v-4H3V14zM3,19h4v-4H3V19zM3,9h4V5H3V9zM8,14h13v-4H8V14zM8,19h13v-4H8V19zM8,5v4h13V5H8V5z")
    }
}
