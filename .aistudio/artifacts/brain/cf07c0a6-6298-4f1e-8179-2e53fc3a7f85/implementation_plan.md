# Orion PDF — Ultra-Fast Mobile PDF Viewer & Annotator

Orion PDF is a high-performance, low-memory Android PDF reader and annotation workspace modeled after the provided reference screenshots. It combines hardware-accelerated page rendering, 8 eye-comfort reading themes, and a quick-access floating sidebar with integrated freehand drawing, highlighting, underline, and sticky-note callouts—all unlocked out of the box.

## User Review & Critical Decisions

> [!IMPORTANT]
> All clarifications from our initial review have been locked into this plan:
> - **Confirmed Decision 1 (All Pro Features Unlocked)**: Every feature marked "Pro" in the reference screenshots—including all 8 reading color themes (`Light`, `Sepia`, `Dark`, `Mist`, `Sage`, `Sand`, `Dusk`, `Candle`), Text-to-Speech Read Aloud, Auto-Scroll, Unlimited PDF Search, Quotes Manager, Enhanced Bookmarks, Custom Folder Organization, Pin Reading Notification, and Auto-Resume—is unlocked by default without ads or paywalls. A "Pro Unlocked" status overview sheet is also accessible from the app menu so you can inspect all capabilities.
> - **Confirmed Decision 2 (Integrated Sidebar Annotation Suite)**: The floating left-side pill inside the PDF viewer includes both quick viewport controls (Touch Lock, Page Rotate, Orientation Lock, Expandable Annotation Dock) and direct one-tap access to **Freehand Pen & Marker**, **Text Highlighter & Underline**, **Sticky Notes & Text Callouts**, **Eraser**, **Color/Stroke Picker**, and **Undo/Redo**.
> - **Confirmed Decision 3 (Pre-Bundled PDFs + System File & Folder Picker + Local Room Storage)**: On first launch, the app automatically generates and indexes realistic multi-page PDF documents (including *Class 10 Maths Cheat-Sheet (14 pages)*, *REET Syllabus L2 2024-25*, *SI RPSC Card*, and *Spoken English Study Notes*) so the `Continue reading` carousel, `Files`, `Recent`, `Favorites`, and `Folders` tabs work immediately, while supporting importing any device PDF or directory via the Android Storage Access Framework (`OpenDocument` / `OpenDocumentTree`).
> - **Confirmed Decision 4 (Sideloadable APK Generation)**: In addition to running live in the streaming preview, the project is configured with standard debug/release signing so you can export the installable APK directly from the AI Studio **Settings / Export APK** menu or build via Gradle (`assembleDebug`) for sideloading onto your physical phone.

---

## 1. Overview & Core Concept

- **What It Does**: Provides a distraction-free, ultra-fast PDF reading and markup experience with instant library organization (`Files`, `Recent`, `Favorites`, `Folders` / `Authors`), a `Continue reading` progress carousel, customizable color filter themes, text reflow, full-text search, voice read-aloud, hands-free auto-scroll, and real-time vector annotations saved per document page.
- **Target Audience / Persona**: Students, exam aspirants, researchers, and mobile readers who work with dense study sheets, textbooks, and reference PDFs daily and need zero-lag scrolling alongside immediate markup tools.
- **Key Value**: Eliminates memory bloat and UI clutter by pairing an LRU bitmap tile cache (`android.graphics.pdf.PdfRenderer`) with a collapsible floating sidebar that keeps both viewport controls and annotation tools one thumb-tap away.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Home Library & Continue Reading (`Files` Tab)**:
   - Top app bar displays the current section title (`Files`, `Recent`, `Favorites`, or `Folders`), global PDF & quote search button, and the app-level three-dot menu.
   - **Continue Reading Carousel**: Horizontal cards at the top of `Files` show a live rendered first-page thumbnail, document title, current page & relative timestamp (e.g., `Page 3 of 14 · 4 min. ago`), warm terracotta progress bar, and completion percentage (`21%`).
   - **Sort, View Mode & Filter Bar**: Toggle sort order (`Name`, `Date Modified`, `Size`, `Progress` ascending/descending), switch between **List View** and **Grid View**, or filter by storage folder/status.
   - **Document List & Per-File Actions**: Each row displays a crisp page preview thumbnail, title, date · file size · folder badge, and a three-dot menu opening the Document Action Sheet (`Share`, `Open with`, `Print`, `File info`, `Read aloud`, `Auto-scroll`, `Bookmarks`, `Quotes`, `Pin notification`, `Show in folder`, `Favorite`, `Move to Folder`, `Delete`).
2. **Folders & Authors Organization (`Folders` Tab)**:
   - Segmented pill switcher at the top toggles between **Folders** (grouped by directory or user-created custom folders with PDF count and storage badge) and **Authors / Categories**.
   - Tapping any folder drills down into its filtered PDF list with back navigation and ability to assign/move PDFs across custom folders.
3. **App Settings & More Menu Sheet**:
   - Accessible from the top-right overflow icon on the home screen.
   - Includes the **Pro Version (All Features Active)** showcase card, segmented **App Theme** selector (`System`, `Light`, `Dark`), **Open a file from device** (system PDF picker), **Rescan storage** (re-indexes bundled and imported documents), **Settings** (including *Auto-resume last PDF on launch*), **Leave a rating**, and **Feedback**.
4. **Ultra-Fast PDF Viewer & Floating Annotation Sidebar**:
   - **Top Viewer Bar**: Back button, document title, **In-PDF Search** (searches extracted page text with next/previous match jumps and highlight badges), and **Document More Menu** bottom sheet.
   - **Floating Left Sidebar Pill**:
     - Mirrors the vertical dark translucent pill on the left edge of the canvas with **Touch/Scroll Lock**, **Rotate Page (90° steps)**, **Screen Orientation Toggle**, and an **Annotation Tools Toggle** button.
     - Expanding the annotation dock reveals quick-access tools directly in the sidebar:
       - **Pen** (fine ink) & **Marker** (chisel/semi-transparent ink)
       - **Highlighter** & **Underline**
       - **Sticky Note / Text Callout** (tap anywhere on a page to place a collapsible note pin or text box)
       - **Eraser**, **Color Swatches & Stroke Width**, and **Undo / Redo**
   - **Bottom Reader Bar**:
     - `Contents`: Opens a modal sheet with **Chapters / Outline**, **Page Thumbnails Grid**, **Bookmarks**, and **Saved Quotes & Annotations**.
     - `Reflow`: Switches between native rendered PDF pages and mobile-optimized **Text Reflow Mode** with adjustable font size for effortless small-screen reading.
     - `Page Pill (e.g., 3 / 14)`: Tapping opens a quick Jump-to-Page scrubber/dialog.
     - `Bookmark`: One-tap toggle to bookmark/unbookmark the current page (with optional custom title).
     - `Display`: Opens the **Reading Settings** bottom sheet.
5. **Reading Settings Bottom Sheet (`Display`)**:
   - **Brightness Slider** with sun icons + **Use system brightness** switch (dynamically adjusts window brightness or applies a dim overlay).
   - **8 Reading Color Themes**: `Light`, `Sepia`, `Dark`, `Mist`, `Sage`, `Sand`, `Dusk`, and `Candle`—rendered with hardware `ColorFilter` / `ColorMatrix` transformations so PDF pages and text adapt instantaneously without re-allocating bitmaps.
   - **Reading Mode Toggles**: `Vertical scroll` (continuous vertical vs. horizontal), `Swipe by page` (pager snapping vs. free fling), and `Show scroll bar` (interactive fast-scroll thumb).

### Visual Identity & Theme

- **Aesthetic Direction**: Warm Obsidian Editorial — faithful to the screenshots' deep espresso-charcoal surfaces paired with warm rose-terracotta active accents and soft cream typography for zero eye strain in low light.
- **Color Palette & Mood**:
  - Background Surface (`#121010`), Elevated Card / Sheet Surface (`#1E1B1B`), Pill / Control Surface (`#2A2525`)
  - Primary Accent (`#D99B93` warm dusty rose/terracotta for active switches, sliders, progress bars, and selected theme borders), Deep Accent Container (`#5C2B29`)
  - Primary Text (`#ECE6E4`), Secondary Metadata Text (`#9E9491`)
  - Full support for `System`, `Light` (`#FAF7F5` warm paper), and `Dark` (`#121010`) app themes.
- **Typography & Hierarchy**:
  - Locally bundled Google Fonts via `font-util`: **Fraunces** / **Playfair Display** for editorial headings (such as the Pro showcase header) paired with **Plus Jakarta Sans** for crisp UI labels, metadata, and reflow reading text, and **JetBrains Mono** for page counters and file metrics.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Low-Memory Bitmap Rendering Pipeline (`PdfRenderer` + LRU Cache + Mutex)**
  - *Chosen Approach*: Use Android's native `android.graphics.pdf.PdfRenderer` synchronized via a single coroutine `Mutex` on `Dispatchers.IO`, paired with an in-memory `LruCache<String, Bitmap>` capped at 20% of available heap and `RGB_565` / adaptive resolution scaling.
  - *Why*: `PdfRenderer` is extremely fast and requires zero external native binary bloat, while strict LRU eviction and single-page sequential rendering guarantee smooth 60–120fps LazyColumn/Pager scrolling even on low-RAM devices.
  - *Alternatives Considered*: Third-party WebView PDF wrappers were rejected due to high memory overhead, sluggish pinch-zoom, and poor offline annotation layering.
- **Decision 2: ColorMatrix-Driven Reading Themes (8 Modes)**
  - *Chosen Approach*: Apply GPU-accelerated `ColorMatrix` transformations (`Light`, `Sepia`, `Dark`, `Mist`, `Sage`, `Sand`, `Dusk`, `Candle`) directly in the Compose `Image`/`Canvas` draw phase.
  - *Why*: Switching between Dark, Sepia, Sage, or Candle happens in 0ms without invalidating or re-rendering cached PDF page bitmaps.
- **Decision 3: Normalized Vector Annotation Layer (`0f..1f` Page Coordinates)**
  - *Chosen Approach*: Store all pen strokes, highlighter paths, underlines, and sticky note callouts in Room using normalized page coordinates (`0.0` to `1.0` relative to page width and height).
  - *Why*: Guarantees annotations stay pixel-aligned across any zoom level, screen rotation, tablet/phone window size, or reflow toggle.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌───────────────────────────────────────────────────────────────────────────┐
│                         UI LAYER (Jetpack Compose)                        │
├───────────────────────────────────┬───────────────────────────────────────┤
│        LIBRARY & HOME SHELL       │          PDF VIEWER & STUDIO          │
│ ┌───────────────────────────────┐ │ ┌───────────────────────────────────┐ │
│ │ TopBar + Search + AppMenuSheet│ │ │ ViewerTopBar (In-PDF Search/Menu) │ │
│ └──────────────┬────────────────┘ │ └─────────────────┬─────────────────┘ │
│                ▼                  │                   ▼                   │
│ ┌───────────────────────────────┐ │ ┌───────────────────────────────────┐ │
│ │ BottomNav Tabs:               │ │ │ Zoomable PDF Canvas (Lazy/Pager)  │ │
│ │ • Files (Continue Reading +   │ │ │ + ColorMatrix Theme Filter        │ │
│ │   List/Grid + Sort/Filter)    │ │ │ + Vector Annotation Canvas Layer  │ │
│ │ • Recent                      │ │ └─────────────────┬─────────────────┘ │
│ │ • Favorites                   │ │                   │                   │
│ │ • Folders / Authors Switcher  │ │ ┌─────────────────┴─────────────────┐ │
│ └──────────────┬────────────────┘ │ │ Floating Left Sidebar Pill:       │ │
│                │                  │ │ Lock | Rotate | Orient | Annotate │ │
│ ┌──────────────┴────────────────┐ │ │ (Pen, Marker, Highlight, Note)    │ │
│ │ Document Action Bottom Sheet  │ │ └─────────────────┬─────────────────┘ │
│ │ (Share, Print, Info, Delete)  │ │                   ▼                   │
│ └───────────────────────────────┘ │ ┌───────────────────────────────────┐ │
│                                   │ │ Bottom Bar: Contents | Reflow |   │ │
│                                   │ │ Page (3/14) | Bookmark | Display  │ │
│                                   │ └───────────────────────────────────┘ │
└─────────────────┬─────────────────┴───────────────────┬───────────────────┘
                  │         StateFlow / UI Events       │
                  ▼                                     ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                     VIEWMODEL & DOMAIN SERVICES LAYER                     │
│ • PdfLibraryViewModel (Search, Sort, Filter, Folders, SAF File Import)    │
│ • PdfReaderViewModel (Zoom/Pan, Page Sync, Undo/Redo Stack, Search, TTS)  │
│ • BundledPdfSeeder (Generates rich multi-page sample PDFs on first run)   │
│ • PdfRenderEngine (Coroutine Mutex + LruCache<PageKey, Bitmap> + TextMap) │
└───────────────────────────────────┬───────────────────────────────────────┘
                                    │
                                    ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                     DATA & PERSISTENCE LAYER (Room SQLite)                │
│ • PdfDocumentEntity (id, uri, title, author, folderName, size, pageCount, │
│   currentPage, lastOpenedAt, isFavorite, isPinned)                        │
│ • BookmarkEntity (id, docId, pageIndex, title, createdAt)                 │
│ • QuoteEntity (id, docId, pageIndex, text, note, highlightColor, created) │
│ • AnnotationEntity (id, docId, pageIndex, toolType, colorHex, strokeWidth,│
│   serializedPointsJson, noteText, normalizedX, normalizedY)               │
└───────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State

- **`PdfDocumentEntity`**: Tracks every bundled or user-imported PDF, its file path/URI, page count, file size, folder assignment, author tag, favorite status, current reading page (`0..pageCount-1`), and `lastReadTimestamp` for the `Continue reading` carousel and `Recent` tab.
- **`AnnotationEntity`**: Persists page-level markups (`PEN`, `MARKER`, `HIGHLIGHTER`, `UNDERLINE`, `STICKY_NOTE`, `CALLOUT`) with normalized point arrays, color hex, stroke thickness, and optional note text.
- **`BookmarkEntity` & `QuoteEntity`**: Stores titled page bookmarks and saved text passages/quotes across all PDFs so users can browse them either per-document or globally.
- **`ReaderPreferences` State**: Persists app theme (`System`/`Light`/`Dark`), reading color theme (`Light`, `Sepia`, `Dark`, `Mist`, `Sage`, `Sand`, `Dusk`, `Candle`), brightness level & system-brightness toggle, scroll direction (`Vertical` vs. `Horizontal`), `Swipe by page` snapping, `Show scroll bar`, and `Auto-resume last PDF`.

### Interactive Component & State Mapping

- **First-Launch Sample PDF Seeding**: On initial startup, `BundledPdfSeeder` uses Android's `PdfDocument` canvas API to generate high-resolution, multi-page educational and reference PDFs (including the 14-page *Class 10 Maths Cheat-Sheet* shown in the screenshots with formulas, tables, and chapter headings) into local app storage so every screen, search query, text reflow, and thumbnail matches the screenshots immediately.
- **Sidebar Annotation Workflow**:
  - Tapping the **Lock** icon disables/enables canvas pan/zoom gestures to prevent accidental shifts while reading or drawing.
  - Tapping **Rotate** rotates the active page view in 90° increments; **Orientation** toggles portrait/landscape layout behavior.
  - Tapping the **Annotate** button expands the sidebar tool palette (`Pen`, `Marker`, `Highlighter`, `Underline`, `Sticky Note`, `Eraser`, `Color`, `Undo`). When an ink tool is active, touch drag gestures on the PDF page capture normalized stroke coordinates and commit them to Room; when `Sticky Note` is active, tapping a point on the page opens a callout editor and anchors a tappable note marker at that location.
- **Read Aloud (TTS) & Auto-Scroll**:
  - Activating **Read aloud** from the document menu initializes Android `TextToSpeech` and reads the extracted text of the current and subsequent pages with a floating mini controller (Play/Pause, Speed, Stop).
  - Activating **Auto-scroll** starts a smooth frame-synced coroutine scroller with an on-screen speed pill (`0.5x` – `3.0x` and pause/resume).
- **System Print, Share & Pin Notification**:
  - `Share` & `Open with` use `FileProvider` intents; `Print` connects to Android's native `PrintManager` with a custom `PrintDocumentAdapter`; `Pin notification` posts a persistent notification shortcut (`NotificationCompat`) that jumps straight back into the active PDF.
