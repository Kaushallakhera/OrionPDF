package com.example.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.BookmarkEntity
import com.example.data.PdfDocumentEntity
import com.example.data.PdfRepository
import com.example.data.QuoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

data class PdfOutlineChapter(
    val title: String,
    val pageIndex: Int,
    val subtitle: String
)

object BundledPdfSeeder {

    private val class10Chapters = listOf(
        "Chapter 1: Real Numbers",
        "Chapter 2: Polynomials",
        "Chapter 3: Pair of Linear Equations in Two Variables",
        "Chapter 4: Quadratic Equations",
        "Chapter 5: Arithmetic Progressions",
        "Chapter 6: Triangles & Similarity",
        "Chapter 7: Coordinate Geometry",
        "Chapter 8: Introduction to Trigonometry",
        "Chapter 9: Some Applications of Trigonometry",
        "Chapter 10: Circles & Tangent Theorems",
        "Chapter 11: Areas Related to Circles",
        "Chapter 12: Surface Areas and Volumes",
        "Chapter 13: Statistics (Mean, Median, Mode)",
        "Chapter 14: Probability & Board Formula Vault"
    )

    suspend fun ensureSeeded(context: Context, repository: PdfRepository) = withContext(Dispatchers.IO) {
        if (repository.getDocumentCount() > 0) return@withContext

        val pdfDir = File(context.filesDir, "seeded_pdfs").apply { mkdirs() }
        val now = System.currentTimeMillis()

        // 1. Class_10 Maths Cheat-Sheet (14 pages, currently at page index 2 -> Page 3 of 14, 21%)
        val mathsFile = File(pdfDir, "Class_10_Maths_Cheat_Sheet.pdf")
        if (!mathsFile.exists()) {
            generateClass10MathsPdf(mathsFile, 14)
        }
        val mathsDocId = repository.insertDocument(
            PdfDocumentEntity(
                title = "Class_10 Maths Cheat-Sheet Complete Formula Vault",
                filePath = mathsFile.absolutePath,
                folderName = ".DPP Class 10",
                storageVolume = "Internal storage",
                author = "Kaushal Maths Academy",
                sizeBytes = 1_840_000L,
                pageCount = 14,
                currentPage = 2, // Page 3 of 14 (21%)
                lastOpenedAt = now - 4 * 60 * 1000L, // 4 min. ago
                createdAt = dateMillis(2026, Calendar.MARCH, 15),
                isFavorite = true
            )
        )

        // 2. NCERT Class 10 Ganit Mathematics (second item in Continue Reading carousel)
        val ganitFile = File(pdfDir, "NCERT_Ganit_Mathematics_X.pdf")
        if (!ganitFile.exists()) {
            generateAcademicPdf(
                file = ganitFile,
                title = "गणित Mathematics Class X",
                subtitle = "Student Support Material & Solved Exemplar Problems",
                accentColor = Color.rgb(32, 92, 148),
                pageCount = 12
            )
        }
        repository.insertDocument(
            PdfDocumentEntity(
                title = "NCERT Ganit Mathematics Student Support Material",
                filePath = ganitFile.absolutePath,
                folderName = ".DPP Class 10",
                storageVolume = "Internal storage",
                author = "Kendriya Vidyalaya Sangathan",
                sizeBytes = 4_210_000L,
                pageCount = 12,
                currentPage = 5, // Page 6 of 12 (50%)
                lastOpenedAt = now - 28 * 60 * 1000L,
                createdAt = dateMillis(2026, Calendar.JANUARY, 20),
                isFavorite = true
            )
        )

        // 3. REET Syllabus L2 2024-25 (Feb 9, 2025 · 2.6 MB · Download)
        val reetFile = File(pdfDir, "REET_Syllabus_L2_2024_25.pdf")
        if (!reetFile.exists()) {
            generateAcademicPdf(
                file = reetFile,
                title = "REET Syllabus L2 2024-25",
                subtitle = "Rajasthan Eligibility Examination for Teachers (Level-II Class 6 to 8)",
                accentColor = Color.rgb(44, 62, 80),
                pageCount = 8
            )
        }
        repository.insertDocument(
            PdfDocumentEntity(
                title = "REET Syllabus L2 2024-25",
                filePath = reetFile.absolutePath,
                folderName = "Download",
                storageVolume = "Internal storage",
                author = "Board of Secondary Education Rajasthan",
                sizeBytes = 2_726_297L, // 2.6 MB
                pageCount = 8,
                currentPage = 0,
                lastOpenedAt = 0L,
                createdAt = dateMillis(2025, Calendar.FEBRUARY, 9),
                isFavorite = false
            )
        )

        // 4. SI RPSC Card (Apr 2, 2026 · 0.96 MB · Download)
        val siCard1File = File(pdfDir, "SI_RPSC_Card_Download.pdf")
        if (!siCard1File.exists()) {
            generateAdmitCardPdf(
                file = siCard1File,
                title = "SI RPSC Admit & Exam Identity Card",
                candidateRef = "RPSC-SI-2026-88412",
                pageCount = 4
            )
        }
        repository.insertDocument(
            PdfDocumentEntity(
                title = "SI RPSC Card",
                filePath = siCard1File.absolutePath,
                folderName = "Download",
                storageVolume = "Internal storage",
                author = "Rajasthan Public Service Commission",
                sizeBytes = 1_006_632L, // 0.96 MB
                pageCount = 4,
                currentPage = 0,
                lastOpenedAt = 0L,
                createdAt = dateMillis(2026, Calendar.APRIL, 2),
                isFavorite = false
            )
        )

        // 5. SI RPSC Card (Apr 4, 2026 · 0.92 MB · Print)
        val siCard2File = File(pdfDir, "SI_RPSC_Card_Print.pdf")
        if (!siCard2File.exists()) {
            generateAdmitCardPdf(
                file = siCard2File,
                title = "SI RPSC Admit Card (Print Verified Copy)",
                candidateRef = "RPSC-SI-2026-88412-P",
                pageCount = 4
            )
        }
        repository.insertDocument(
            PdfDocumentEntity(
                title = "SI RPSC Card",
                filePath = siCard2File.absolutePath,
                folderName = "Print",
                storageVolume = "Internal storage",
                author = "Rajasthan Public Service Commission",
                sizeBytes = 964_689L, // 0.92 MB
                pageCount = 4,
                currentPage = 0,
                lastOpenedAt = 0L,
                createdAt = dateMillis(2026, Calendar.APRIL, 4),
                isFavorite = false
            )
        )

        // 6. (6) Spoken English Success Darpan (Aug 9, 2024 · 3.3 MB · Study Notes)
        val spokenFile = File(pdfDir, "Spoken_English_Success_Darpan.pdf")
        if (!spokenFile.exists()) {
            generateAcademicPdf(
                file = spokenFile,
                title = "(6) Spoken English Success Darpan",
                subtitle = "Complete Grammar, Idioms, Phrasal Verbs & Daily Conversation Guide",
                accentColor = Color.rgb(168, 42, 42),
                pageCount = 10
            )
        }
        repository.insertDocument(
            PdfDocumentEntity(
                title = "(6) Spoken English Success Darpan",
                filePath = spokenFile.absolutePath,
                folderName = "Study Notes",
                storageVolume = "Internal storage",
                author = "Success Darpan Publications",
                sizeBytes = 3_460_300L, // 3.3 MB
                pageCount = 10,
                currentPage = 1,
                lastOpenedAt = now - 3 * 3600 * 1000L,
                createdAt = dateMillis(2024, Calendar.AUGUST, 9),
                isFavorite = true
            )
        )

        // 7. (S@roj)-Book of History (Hindi) (Oct 5, 2025 · 5.1 MB · Telegram Videos)
        val historyFile = File(pdfDir, "Saroj_Book_of_History_Hindi.pdf")
        if (!historyFile.exists()) {
            generateAcademicPdf(
                file = historyFile,
                title = "(S@roj)-Book of Indian History (Hindi)",
                subtitle = "SUCCESS IN PRELIMS (SIP+) — Ancient, Medieval & Modern History Notes",
                accentColor = Color.rgb(156, 39, 48),
                pageCount = 10
            )
        }
        repository.insertDocument(
            PdfDocumentEntity(
                title = "(S@roj)-Book of History (Hindi)",
                filePath = historyFile.absolutePath,
                folderName = "Telegram Videos",
                storageVolume = "Internal storage",
                author = "Saroj Civil Services",
                sizeBytes = 5_347_737L, // 5.1 MB
                pageCount = 10,
                currentPage = 0,
                lastOpenedAt = 0L,
                createdAt = dateMillis(2025, Calendar.OCTOBER, 5),
                isFavorite = false
            )
        )

        // 8. Additional documents to populate the exact Folders from Screenshot 1
        val folderSeeds = listOf(
            Triple(".Alarms", "B437-1C18", "Morning Revision Formula Schedule"),
            Triple(".Chats", "B437-1C18", "Study Group Doubt Solutions PDF"),
            Triple(".Kaushal Document", "Internal storage", "Kaushal Academic Certificates & Marksheet"),
            Triple(".Lokesh Documents", "Internal storage", "Lokesh Engineering Semester Notes"),
            Triple(".Mummy Document", "Internal storage", "Household Medical & Insurance Records"),
            Triple(".other", "B437-1C18", "General Reference Tables & Constants"),
            Triple(".Payal Document", "Internal storage", "Payal B.Ed Lesson Plan Portfolio")
        )

        folderSeeds.forEachIndexed { idx, (folder, volume, docTitle) ->
            val f = File(pdfDir, "Folder_Doc_${idx + 1}.pdf")
            if (!f.exists()) {
                generateAcademicPdf(
                    file = f,
                    title = docTitle,
                    subtitle = "Archived in $folder ($volume) — Ready for Instant Reading & Markup",
                    accentColor = Color.rgb(65, 90, 119),
                    pageCount = 5
                )
            }
            repository.insertDocument(
                PdfDocumentEntity(
                    title = docTitle,
                    filePath = f.absolutePath,
                    folderName = folder,
                    storageVolume = volume,
                    author = folder.removePrefix("."),
                    sizeBytes = (640_000L + idx * 210_000L),
                    pageCount = 5,
                    currentPage = 0,
                    lastOpenedAt = 0L,
                    createdAt = dateMillis(2025, Calendar.JUNE, 10 + idx),
                    isFavorite = false
                )
            )
        }

        // Seed initial Bookmarks & Quotes on Class_10 Maths Cheat-Sheet
        repository.addBookmark(
            BookmarkEntity(
                documentId = mathsDocId,
                documentTitle = "Class_10 Maths Cheat-Sheet Complete Formula Vault",
                pageIndex = 2,
                title = "Ch 3: Linear Equations Ratio Vault & Shortcuts"
            )
        )
        repository.addBookmark(
            BookmarkEntity(
                documentId = mathsDocId,
                documentTitle = "Class_10 Maths Cheat-Sheet Complete Formula Vault",
                pageIndex = 7,
                title = "Ch 8: Trigonometry Identities Table"
            )
        )
        repository.addQuote(
            QuoteEntity(
                documentId = mathsDocId,
                documentTitle = "Class_10 Maths Cheat-Sheet Complete Formula Vault",
                pageIndex = 2,
                quoteText = "INTERCHANGED COEFFICIENTS TRICK (49x + 51y = 499 & 51x + 49y = 501): Never multiply by 49 and 51! Step 1: Add both -> x + y = 10. Step 2: Subtract both -> x - y = -1. Solve in 5 sec!",
                note = "Must revise before board exam MCQ section"
            )
        )
        repository.addQuote(
            QuoteEntity(
                documentId = mathsDocId,
                documentTitle = "Class_10 Maths Cheat-Sheet Complete Formula Vault",
                pageIndex = 2,
                quoteText = "STEP-MARKING SECRET: In graphical triangle questions ('Shade triangle formed with X-axis'), writing the 3 vertex coordinates clearly and shading the region carries 1 full mark out of 3.",
                note = "Guaranteed 3-mark graph question tip"
            )
        )
    }

    private fun dateMillis(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, day, 10, 30, 0)
        return cal.timeInMillis
    }

    /**
     * Generates the 14-page Class 10 Maths Cheat-Sheet PDF where Page 3 (index 2)
     * replicates the exact layout in Screenshot 4.
     */
    private fun generateClass10MathsPdf(file: File, pageCount: Int) {
        val pdf = PdfDocument()
        val width = 720
        val height = 1020

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        for (pageIndex in 0 until pageCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(width, height, pageIndex + 1).create()
            val page = pdf.startPage(pageInfo)
            val canvas = page.canvas

            // Page paper background
            bgPaint.color = Color.rgb(245, 246, 248)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            if (pageIndex == 2) {
                drawClass10Page3Exact(canvas, width, height)
            } else {
                val chapterTitle = class10Chapters.getOrElse(pageIndex) { "Chapter ${pageIndex + 1}: Revision Sheet" }
                drawClass10ChapterPage(canvas, width, height, pageIndex, pageCount, chapterTitle, bgPaint, borderPaint, textPaint)
            }

            pdf.finishPage(page)
        }

        FileOutputStream(file).use { out ->
            pdf.writeTo(out)
        }
        pdf.close()
    }

    private fun drawClass10Page3Exact(canvas: Canvas, width: Int, height: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }

        // Top tag pill: CBSE CLASS 10 WEIGHTAGE: 4-5 MARKS
        paint.color = Color.rgb(246, 234, 212)
        canvas.drawRoundRect(RectF(36f, 28f, 238f, 48f), 6f, 6f, paint)
        paint.color = Color.rgb(180, 95, 30)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 10.5f
        canvas.drawText("CBSE CLASS 10 WEIGHTAGE: 4-5 MARKS", 44f, 42f, paint)

        // Main Title
        paint.color = Color.rgb(20, 26, 38)
        paint.textSize = 19f
        canvas.drawText("Chapter 3: Pair of Linear Equations in Two Variables", 36f, 70f, paint)

        paint.color = Color.rgb(75, 85, 102)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("1-Page High-Yield Formula, Shortcut & Mistake Revision Sheet (Rationalised Syllabus)", 36f, 88f, paint)

        // QR Code pattern at top right
        drawQrPattern(canvas, 505f, 24f, 68f)

        // MICRO-STORE EDITION Page 3 of 14 badge
        paint.color = Color.rgb(235, 242, 252)
        canvas.drawRoundRect(RectF(582f, 52f, 688f, 88f), 8f, 8f, paint)
        stroke.color = Color.rgb(140, 175, 230)
        canvas.drawRoundRect(RectF(582f, 52f, 688f, 88f), 8f, 8f, stroke)
        paint.color = Color.rgb(28, 78, 168)
        paint.textSize = 9.5f
        canvas.drawText("MICRO-STORE EDITION", 590f, 68f, paint)
        paint.textSize = 11f
        canvas.drawText("Page 3 of 14", 606f, 82f, paint)

        // Divider line
        stroke.color = Color.rgb(205, 212, 222)
        canvas.drawLine(36f, 98f, 688f, 98f, stroke)

        // Card 1: 1. ANATOMY OF LINEAR PAIR (Left column)
        val c1 = RectF(36f, 108f, 354f, 388f)
        paint.color = Color.WHITE
        canvas.drawRoundRect(c1, 10f, 10f, paint)
        stroke.color = Color.rgb(210, 216, 226)
        canvas.drawRoundRect(c1, 10f, 10f, stroke)

        paint.color = Color.rgb(22, 28, 42)
        paint.textSize = 12f
        canvas.drawText("1. ANATOMY OF LINEAR PAIR", 48f, 128f, paint)
        paint.color = Color.rgb(120, 130, 145)
        paint.textSize = 9f
        canvas.drawText("CORE RULE", 292f, 128f, paint)

        // Standard General Form sub-box
        val fBox = RectF(46f, 144f, 344f, 198f)
        paint.color = Color.rgb(239, 244, 252)
        canvas.drawRoundRect(fBox, 6f, 6f, paint)
        stroke.color = Color.rgb(160, 188, 235)
        canvas.drawRoundRect(fBox, 6f, 6f, stroke)
        paint.color = Color.rgb(30, 70, 145)
        paint.textSize = 9.5f
        canvas.drawText("STANDARD GENERAL FORM", 54f, 160f, paint)
        paint.color = Color.rgb(15, 23, 38)
        paint.textSize = 13f
        canvas.drawText("a₁x + b₁y + c₁ = 0  and  a₂x + b₂y + c₂ = 0", 54f, 184f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 10.5f
        paint.color = Color.rgb(35, 42, 54)
        canvas.drawText("• Geometrical Meaning: Every solution (x, y) is a point lying", 48f, 222f, paint)
        canvas.drawText("  on the straight line.", 48f, 238f, paint)
        canvas.drawText("• Axis Equations: Equation of X-axis is y = 0;", 48f, 260f, paint)
        canvas.drawText("  Equation of Y-axis is x = 0.", 48f, 276f, paint)
        canvas.drawText("• Parallel to Axes: x = k is a line parallel to Y-axis;", 48f, 298f, paint)
        canvas.drawText("  y = k is parallel to X-axis.", 48f, 314f, paint)
        canvas.drawText("• Syllabus Alert: Only Graphical, Substitution & Elimination", 48f, 336f, paint)
        canvas.drawText("  methods are in syllabus (Cross-Multiplication DELETED).", 48f, 352f, paint)

        // Card 2: 2. MASTER RATIO & CONSISTENCY VAULT (Right column)
        val c2 = RectF(366f, 108f, 688f, 388f)
        paint.color = Color.WHITE
        canvas.drawRoundRect(c2, 10f, 10f, paint)
        stroke.color = Color.rgb(210, 216, 226)
        canvas.drawRoundRect(c2, 10f, 10f, stroke)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = Color.rgb(22, 28, 42)
        paint.textSize = 12f
        canvas.drawText("2. MASTER RATIO & CONSISTENCY VAULT", 378f, 128f, paint)

        // Ratio table
        val tableRect = RectF(376f, 140f, 678f, 302f)
        paint.color = Color.rgb(244, 246, 250)
        canvas.drawRoundRect(tableRect, 6f, 6f, paint)
        stroke.color = Color.rgb(200, 208, 220)
        canvas.drawRoundRect(tableRect, 6f, 6f, stroke)

        paint.textSize = 9.5f
        paint.color = Color.rgb(40, 48, 60)
        canvas.drawText("Ratio Condition", 384f, 156f, paint)
        canvas.drawText("Graph Lines", 468f, 156f, paint)
        canvas.drawText("Solutions", 538f, 156f, paint)
        canvas.drawText("Consistency", 612f, 156f, paint)
        canvas.drawLine(376f, 164f, 678f, 164f, stroke)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("a₁/a₂ ≠ b₁/b₂", 384f, 190f, paint)
        canvas.drawText("Intersecting", 468f, 190f, paint)
        canvas.drawText("Unique (1)", 538f, 190f, paint)
        canvas.drawText("Consistent", 612f, 190f, paint)
        canvas.drawLine(376f, 206f, 678f, 206f, stroke)

        canvas.drawText("a₁/a₂=b₁/b₂=c₁/c₂", 382f, 236f, paint)
        canvas.drawText("Coincident", 468f, 236f, paint)
        canvas.drawText("Infinitely Many", 534f, 236f, paint)
        canvas.drawText("Dependent", 612f, 236f, paint)
        canvas.drawLine(376f, 254f, 678f, 254f, stroke)

        canvas.drawText("a₁/a₂=b₁/b₂≠c₁/c₂", 382f, 282f, paint)
        canvas.drawText("Parallel", 468f, 282f, paint)
        canvas.drawText("No Solution", 538f, 282f, paint)
        canvas.drawText("Inconsistent", 612f, 282f, paint)

        // Two-digit number formula box inside Card 2
        val twoDigitBox = RectF(376f, 322f, 678f, 376f)
        paint.color = Color.rgb(239, 244, 252)
        canvas.drawRoundRect(twoDigitBox, 6f, 6f, paint)
        stroke.color = Color.rgb(160, 188, 235)
        canvas.drawRoundRect(twoDigitBox, 6f, 6f, stroke)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = Color.rgb(20, 30, 48)
        paint.textSize = 10.5f
        canvas.drawText("Original No. = 10x + y  |  Reversed No. = 10y + x", 386f, 344f, paint)
        canvas.drawText("(where x = tens digit, y = units digit)", 386f, 362f, paint)

        // Card 3: 3. TOP 3 BOARD EXAM MISTAKES (Left middle)
        val c3 = RectF(36f, 402f, 354f, 646f)
        paint.color = Color.WHITE
        canvas.drawRoundRect(c3, 10f, 10f, paint)
        stroke.color = Color.rgb(210, 216, 226)
        canvas.drawRoundRect(c3, 10f, 10f, stroke)

        paint.color = Color.rgb(22, 28, 42)
        paint.textSize = 12f
        canvas.drawText("3. TOP 3 BOARD EXAM MISTAKES", 48f, 424f, paint)

        val m1 = RectF(46f, 436f, 344f, 496f)
        paint.color = Color.rgb(253, 242, 242)
        canvas.drawRoundRect(m1, 6f, 6f, paint)
        paint.color = Color.rgb(185, 38, 38)
        paint.textSize = 10f
        canvas.drawText("✗ Comparing c₁/c₂ when one c is on LHS and one on RHS", 54f, 456f, paint)
        paint.color = Color.rgb(22, 125, 58)
        canvas.drawText("✓ DO: Bring BOTH constants to LHS (= 0) before c₁/c₂", 54f, 478f, paint)

        val m2 = RectF(46f, 506f, 344f, 566f)
        paint.color = Color.rgb(253, 242, 242)
        canvas.drawRoundRect(m2, 6f, 6f, paint)
        paint.color = Color.rgb(185, 38, 38)
        canvas.drawText("✗ Writing Two-Digit Number as xy instead of 10x + y", 54f, 526f, paint)
        paint.color = Color.rgb(22, 125, 58)
        canvas.drawText("✓ DO: In algebra, a 2-digit number is ALWAYS 10x + y.", 54f, 548f, paint)

        val m3 = RectF(46f, 576f, 344f, 636f)
        paint.color = Color.rgb(253, 242, 242)
        canvas.drawRoundRect(m3, 6f, 6f, paint)
        paint.color = Color.rgb(185, 38, 38)
        canvas.drawText("✗ Ignoring the 2nd Case in |x - y| = 2 Digit Problems", 54f, 596f, paint)
        paint.color = Color.rgb(22, 125, 58)
        canvas.drawText("✓ DO: 'Digits differ by 2' gives x - y = 2 AND y - x = 2.", 54f, 618f, paint)

        // Card 4: 4. TOPPER 5-SECOND MCQ SHORTCUTS (Right middle)
        val c4 = RectF(366f, 402f, 688f, 646f)
        paint.color = Color.WHITE
        canvas.drawRoundRect(c4, 10f, 10f, paint)
        stroke.color = Color.rgb(210, 216, 226)
        canvas.drawRoundRect(c4, 10f, 10f, stroke)

        paint.color = Color.rgb(22, 28, 42)
        paint.textSize = 12f
        canvas.drawText("4. TOPPER 5-SECOND MCQ SHORTCUTS", 378f, 424f, paint)

        paint.color = Color.rgb(90, 100, 115)
        paint.textSize = 9.5f
        canvas.drawText("INTERCHANGED COEFFICIENTS TRICK (49x + 51y = 499 & 51x + 49y = 501)", 378f, 446f, paint)

        val sBox1 = RectF(376f, 456f, 678f, 532f)
        paint.color = Color.rgb(239, 244, 252)
        canvas.drawRoundRect(sBox1, 6f, 6f, paint)
        stroke.color = Color.rgb(160, 188, 235)
        canvas.drawRoundRect(sBox1, 6f, 6f, stroke)
        paint.color = Color.rgb(20, 32, 52)
        paint.textSize = 10.5f
        canvas.drawText("Never multiply by 49 and 51! Step 1: Add both -> 100(x+y)=1000", 384f, 478f, paint)
        canvas.drawText("-> x + y = 10. Step 2: Subtract both -> -2(x - y) = -2 -> x - y = 1.", 384f, 498f, paint)
        canvas.drawText("Solve in 5 sec! (x = 5.5, y = 4.5)", 384f, 518f, paint)

        val sBox2 = RectF(376f, 558f, 678f, 632f)
        paint.color = Color.rgb(239, 244, 252)
        canvas.drawRoundRect(sBox2, 6f, 6f, paint)
        canvas.drawRoundRect(sBox2, 6f, 6f, stroke)
        canvas.drawText("• (10x + y) + (10y + x) = 11(x + y)  (Always divisible by 11)", 384f, 586f, paint)
        canvas.drawText("• (10x + y) - (10y + x) = 9(x - y)   (Always divisible by 9)", 384f, 610f, paint)

        // Card 5: 5. MUST-SOLVE BOARD QUESTIONS + STEP-MARKING SECRET (Bottom wide card)
        val c5 = RectF(36f, 662f, 688f, 916f)
        paint.color = Color.WHITE
        canvas.drawRoundRect(c5, 10f, 10f, paint)
        stroke.color = Color.rgb(210, 216, 226)
        canvas.drawRoundRect(c5, 10f, 10f, stroke)

        paint.color = Color.rgb(22, 28, 42)
        paint.textSize = 12f
        canvas.drawText("5. MUST-SOLVE BOARD QUESTIONS + STEP-MARKING SECRET", 48f, 686f, paint)

        // 4 Question boxes inside Card 5
        val q1 = RectF(48f, 700f, 356f, 768f)
        val q2 = RectF(368f, 700f, 676f, 768f)
        val q3 = RectF(48f, 778f, 356f, 846f)
        val q4 = RectF(368f, 778f, 676f, 846f)
        paint.color = Color.rgb(244, 246, 250)
        listOf(q1, q2, q3, q4).forEach { canvas.drawRoundRect(it, 6f, 6f, paint) }

        paint.textSize = 10f
        paint.color = Color.rgb(20, 26, 38)
        canvas.drawText("Q1: Sum of digits of a 2-digit no. is 9. 9x original = 2x reversed.", 56f, 720f, paint)
        paint.color = Color.rgb(28, 78, 168)
        canvas.drawText("Ans: x + y = 9 & 9(10x+y) = 2(10y+x) -> 8x = y -> x=1, y=8 -> 18.", 56f, 746f, paint)

        paint.color = Color.rgb(20, 26, 38)
        canvas.drawText("Q2: Ratio of incomes is 9:7 and expenditures 4:3. Each saves ₹2000.", 376f, 720f, paint)
        paint.color = Color.rgb(28, 78, 168)
        canvas.drawText("Ans: 9x - 4y = 2000 & 7x - 3y = 2000 -> x = 2000 -> ₹18,000 & ₹14,000.", 376f, 746f, paint)

        paint.color = Color.rgb(20, 26, 38)
        canvas.drawText("Q3: Find k for which kx + 3y - (k - 3) = 0 & 12x + ky - k = 0 has ∞ sol.", 56f, 798f, paint)
        paint.color = Color.rgb(28, 78, 168)
        canvas.drawText("Ans: k/12 = 3/k = (k-3)/k -> k² = 36 -> k = ±6. Second pair gives k = 6!", 56f, 824f, paint)

        paint.color = Color.rgb(20, 26, 38)
        canvas.drawText("Q4: Lending library has fixed charge for 3 days & per-day charge after.", 376f, 798f, paint)
        paint.color = Color.rgb(28, 78, 168)
        canvas.drawText("Ans: x + 4y = 27 & x + 2y = 21 -> y = ₹3/day, Fixed charge x = ₹15.", 376f, 824f, paint)

        // Yellow/Amber Step-Marking Secret Banner
        val secretBox = RectF(48f, 856f, 676f, 904f)
        paint.color = Color.rgb(254, 248, 232)
        canvas.drawRoundRect(secretBox, 6f, 6f, paint)
        stroke.color = Color.rgb(225, 180, 95)
        canvas.drawRoundRect(secretBox, 6f, 6f, stroke)
        paint.color = Color.rgb(135, 78, 18)
        canvas.drawText("STEP-MARKING SECRET: In graphical triangle questions ('Shade triangle formed with X-axis'),", 58f, 876f, paint)
        canvas.drawText("writing the 3 vertex coordinates clearly and shading the region carries 1 full mark out of 3.", 58f, 894f, paint)

        // Footer banner matching screenshot
        val footerBox = RectF(72f, 940f, 652f, 984f)
        paint.color = Color.rgb(236, 239, 244)
        canvas.drawRoundRect(footerBox, 10f, 10f, paint)
        stroke.color = Color.rgb(195, 202, 214)
        canvas.drawRoundRect(footerBox, 10f, 10f, stroke)
        paint.color = Color.rgb(30, 36, 48)
        paint.textSize = 11f
        canvas.drawText("All Study Materials, Premium DPPs & Community Channels in One Place", 92f, 960f, paint)
        paint.color = Color.rgb(100, 110, 125)
        paint.textSize = 9.5f
        canvas.drawText("Access complete chapter PDFs, question papers, test links & discussion groups", 92f, 975f, paint)

        val btnBox = RectF(536f, 948f, 638f, 976f)
        paint.color = Color.rgb(29, 78, 166)
        canvas.drawRoundRect(btnBox, 14f, 14f, paint)
        paint.color = Color.WHITE
        paint.textSize = 10f
        canvas.drawText("CLICK HERE", 558f, 966f, paint)
    }

    private fun drawQrPattern(canvas: Canvas, x: Float, y: Float, size: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.WHITE
        canvas.drawRect(x, y, x + size, y + size, p)
        p.color = Color.BLACK
        val cell = size / 9f
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val isCorner = (r < 3 && c < 3) || (r < 3 && c >= 6) || (r >= 6 && c < 3)
                val isPattern = (r + c) % 2 == 0 || (r * c) % 3 == 1
                if (isCorner || isPattern) {
                    canvas.drawRect(
                        x + c * cell + 1f,
                        y + r * cell + 1f,
                        x + (c + 1) * cell - 1f,
                        y + (r + 1) * cell - 1f,
                        p
                    )
                }
            }
        }
    }

    private fun drawClass10ChapterPage(
        canvas: Canvas,
        width: Int,
        height: Int,
        pageIndex: Int,
        pageCount: Int,
        chapterTitle: String,
        bgPaint: Paint,
        borderPaint: Paint,
        textPaint: Paint
    ) {
        // Header pill
        bgPaint.color = Color.rgb(246, 234, 212)
        canvas.drawRoundRect(RectF(36f, 32f, 260f, 54f), 6f, 6f, bgPaint)
        textPaint.color = Color.rgb(180, 95, 30)
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.textSize = 11f
        canvas.drawText("CBSE CLASS 10 REVISION VAULT", 46f, 47f, textPaint)

        // Page badge
        bgPaint.color = Color.rgb(235, 242, 252)
        canvas.drawRoundRect(RectF(568f, 32f, 684f, 68f), 8f, 8f, bgPaint)
        textPaint.color = Color.rgb(28, 78, 168)
        textPaint.textSize = 11.5f
        canvas.drawText("Page ${pageIndex + 1} of $pageCount", 588f, 55f, textPaint)

        // Chapter Title
        textPaint.color = Color.rgb(20, 26, 38)
        textPaint.textSize = 21f
        canvas.drawText(chapterTitle, 36f, 84f, textPaint)

        textPaint.color = Color.rgb(90, 100, 118)
        textPaint.textSize = 12f
        canvas.drawText("1-Page High-Yield Formula, Shortcut & Mistake Revision Sheet (Rationalised Syllabus)", 36f, 106f, textPaint)

        // Draw 4 structured formula cards
        val cards = listOf(
            RectF(36f, 128f, 352f, 440f) to "1. CORE CONCEPTS & DEFINITIONS",
            RectF(368f, 128f, 684f, 440f) to "2. KEY FORMULAS & THEOREMS",
            RectF(36f, 460f, 352f, 760f) to "3. COMMON BOARD EXAM MISTAKES",
            RectF(368f, 460f, 684f, 760f) to "4. SOLVED NCERT EXEMPLAR SHORTCUTS"
        )

        cards.forEachIndexed { idx, (rect, heading) ->
            bgPaint.color = Color.WHITE
            canvas.drawRoundRect(rect, 10f, 10f, bgPaint)
            borderPaint.color = Color.rgb(210, 216, 226)
            canvas.drawRoundRect(rect, 10f, 10f, borderPaint)

            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textPaint.color = Color.rgb(25, 35, 52)
            textPaint.textSize = 12.5f
            canvas.drawText(heading, rect.left + 14f, rect.top + 26f, textPaint)

            val highlightBox = RectF(rect.left + 12f, rect.top + 42f, rect.right - 12f, rect.top + 106f)
            bgPaint.color = if (idx == 2) Color.rgb(253, 242, 242) else Color.rgb(239, 244, 252)
            canvas.drawRoundRect(highlightBox, 6f, 6f, bgPaint)

            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textPaint.color = if (idx == 2) Color.rgb(175, 38, 38) else Color.rgb(24, 68, 152)
            textPaint.textSize = 11.5f
            canvas.drawText("Key Rule #${idx + 1} for $chapterTitle", highlightBox.left + 10f, highlightBox.top + 24f, textPaint)

            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textPaint.color = Color.rgb(42, 50, 64)
            textPaint.textSize = 11f
            canvas.drawText("Always write the standard formula before substituting", highlightBox.left + 10f, highlightBox.top + 48f, textPaint)

            for (line in 0 until 6) {
                val y = rect.top + 136f + line * 28f
                canvas.drawText("• Point ${line + 1}: Step-by-step derivation & board marking tip", rect.left + 14f, y, textPaint)
            }
        }

        // Bottom summary box
        val bottomBox = RectF(36f, 780f, 684f, 916f)
        bgPaint.color = Color.rgb(254, 248, 232)
        canvas.drawRoundRect(bottomBox, 10f, 10f, bgPaint)
        borderPaint.color = Color.rgb(225, 180, 95)
        canvas.drawRoundRect(bottomBox, 10f, 10f, borderPaint)

        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.color = Color.rgb(135, 78, 18)
        textPaint.textSize = 12f
        canvas.drawText("5. GUARANTEED BOARD EXAM STEP-MARKING CHECKLIST", 52f, 808f, textPaint)
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.textSize = 11f
        canvas.drawText("✓ Box your final answer with units.  ✓ State theorem reason in brackets.", 52f, 836f, textPaint)
        canvas.drawText("✓ Verify roots/solutions by substituting back into the original equation.", 52f, 862f, textPaint)
        canvas.drawText("✓ Use the floating sidebar tools on the left to highlight or add sticky notes!", 52f, 888f, textPaint)
    }

    private fun generateAcademicPdf(
        file: File,
        title: String,
        subtitle: String,
        accentColor: Int,
        pageCount: Int
    ) {
        val pdf = PdfDocument()
        val width = 720
        val height = 1020
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }

        for (pageIndex in 0 until pageCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(width, height, pageIndex + 1).create()
            val page = pdf.startPage(pageInfo)
            val canvas = page.canvas

            paint.color = Color.rgb(250, 249, 246)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            // Top header bar
            paint.color = accentColor
            canvas.drawRect(0f, 0f, width.toFloat(), 110f, paint)

            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.textSize = 22f
            canvas.drawText(title.take(42), 40f, 54f, paint)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 12.5f
            canvas.drawText("$subtitle  •  Page ${pageIndex + 1} of $pageCount", 40f, 84f, paint)

            // Content sections
            for (sec in 0 until 3) {
                val top = 140f + sec * 265f
                val card = RectF(40f, top, 680f, top + 240f)
                paint.color = Color.WHITE
                canvas.drawRoundRect(card, 10f, 10f, paint)
                stroke.color = Color.rgb(215, 218, 224)
                canvas.drawRoundRect(card, 10f, 10f, stroke)

                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                paint.color = accentColor
                paint.textSize = 14f
                canvas.drawText("Section ${pageIndex + 1}.${sec + 1} — Key Syllabus Topic & Reference Notes", 60f, top + 34f, paint)

                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                paint.color = Color.rgb(45, 52, 64)
                paint.textSize = 12f
                val lines = listOf(
                    "1. Detailed analytical summary and high-yield exam pointers for $title.",
                    "2. Essential definitions, structured comparison tables, and previous year questions.",
                    "3. Important dates, statutory guidelines, and step-by-step problem solving methodology.",
                    "4. Highlight key terms or attach a sticky note using the quick-access left sidebar.",
                    "5. Switch to Reflow mode or Read Aloud at any time from the viewer toolbar."
                )
                lines.forEachIndexed { idx, line ->
                    canvas.drawText(line, 60f, top + 72f + idx * 32f, paint)
                }
            }

            pdf.finishPage(page)
        }

        FileOutputStream(file).use { out ->
            pdf.writeTo(out)
        }
        pdf.close()
    }

    private fun generateAdmitCardPdf(
        file: File,
        title: String,
        candidateRef: String,
        pageCount: Int
    ) {
        val pdf = PdfDocument()
        val width = 720
        val height = 1020
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
            color = Color.rgb(60, 70, 85)
        }

        for (pageIndex in 0 until pageCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(width, height, pageIndex + 1).create()
            val page = pdf.startPage(pageInfo)
            val canvas = page.canvas

            paint.color = Color.WHITE
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            // Outer frame
            canvas.drawRect(32f, 32f, 688f, 980f, stroke)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.color = Color.rgb(20, 30, 48)
            paint.textSize = 20f
            canvas.drawText(title, 52f, 72f, paint)

            paint.textSize = 12f
            paint.color = Color.rgb(90, 100, 115)
            canvas.drawText("Reference ID: $candidateRef  |  Page ${pageIndex + 1} of $pageCount", 52f, 98f, paint)
            canvas.drawLine(32f, 114f, 688f, 114f, stroke)

            // Candidate details table
            val rows = listOf(
                "Candidate Name" to "KAUSHAL KUMAR LAKHERA",
                "Examination Name" to "Sub-Inspector Combined Competitive Exam",
                "Roll Number" to "2026048912",
                "Examination Center" to "Govt. Maharaja College Campus, Jaipur (Center Code 041)",
                "Reporting Time" to "08:30 AM IST (Gate Closes at 09:00 AM IST)",
                "Subject / Shift" to "Paper I & Paper II (General Hindi & General Knowledge)"
            )

            rows.forEachIndexed { i, (label, value) ->
                val yTop = 136f + i * 54f
                canvas.drawRect(52f, yTop, 668f, yTop + 46f, stroke)
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                paint.color = Color.rgb(40, 50, 65)
                canvas.drawText(label, 66f, yTop + 28f, paint)
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                canvas.drawText(value, 260f, yTop + 28f, paint)
            }

            // Instructions block
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.textSize = 14f
            canvas.drawText("Mandatory Candidate Instructions:", 52f, 510f, paint)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 12f
            val inst = listOf(
                "1. Bring this printed Admit Card along with an original Photo ID proof.",
                "2. Blue/Black ballpoint pen with transparent barrel is permitted.",
                "3. Verify your roll number, room allocation, and biometric attendance slip.",
                "4. Keep a digital copy pinned in Orion PDF Reader for instant verification."
            )
            inst.forEachIndexed { idx, line ->
                canvas.drawText(line, 52f, 544f + idx * 34f, paint)
            }

            drawQrPattern(canvas, 560f, 720f, 96f)

            pdf.finishPage(page)
        }

        FileOutputStream(file).use { out ->
            pdf.writeTo(out)
        }
        pdf.close()
    }

    /**
     * Provides structured text per page for Reflow Mode, In-PDF Search, and Read Aloud (TTS).
     */
    fun getExtractedPageText(docTitle: String, pageIndex: Int, pageCount: Int): String {
        if (docTitle.contains("Maths Cheat-Sheet", ignoreCase = true)) {
            if (pageIndex == 2) {
                return """
                    CBSE CLASS 10 WEIGHTAGE: 4-5 MARKS
                    Chapter 3: Pair of Linear Equations in Two Variables
                    1-Page High-Yield Formula, Shortcut & Mistake Revision Sheet (Rationalised Syllabus) — Page 3 of $pageCount
                    
                    1. ANATOMY OF LINEAR PAIR (CORE RULE)
                    Standard General Form: a₁x + b₁y + c₁ = 0 and a₂x + b₂y + c₂ = 0
                    • Geometrical Meaning: Every solution (x, y) is a point lying on the straight line.
                    • Axis Equations: Equation of X-axis is y = 0; Equation of Y-axis is x = 0.
                    • Parallel to Axes: x = k is a line parallel to Y-axis; y = k is parallel to X-axis.
                    • Syllabus Alert: Only Graphical, Substitution & Elimination methods are in syllabus (Cross-Multiplication is DELETED).
                    
                    2. MASTER RATIO & CONSISTENCY VAULT
                    • Intersecting Lines: a₁/a₂ ≠ b₁/b₂ → Unique Solution (Exactly 1) → Consistent
                    • Coincident Lines: a₁/a₂ = b₁/b₂ = c₁/c₂ → Infinitely Many Solutions → Consistent (Dependent)
                    • Parallel Lines: a₁/a₂ = b₁/b₂ ≠ c₁/c₂ → No Solution → Inconsistent
                    • Two-Digit Number Formula: Original No. = 10x + y | Reversed No. = 10y + x (where x = tens, y = units).
                    
                    3. TOP 3 BOARD EXAM MISTAKES TO AVOID
                    ✗ Mistake 1: Comparing c₁/c₂ when one constant is on LHS and one on RHS.
                    ✓ DO: Bring BOTH constants to the LHS (= 0) before taking c₁/c₂.
                    ✗ Mistake 2: Writing a Two-Digit Number as xy instead of 10x + y.
                    ✓ DO: In algebra, xy means x × y. A 2-digit number with digits x, y is ALWAYS 10x + y.
                    ✗ Mistake 3: Ignoring the 2nd Case in |x - y| = 2 Digit Problems.
                    ✓ DO: "Digits differ by 2" gives TWO cases: x - y = 2 AND y - x = 2 (e.g., 42 and 24).
                    
                    4. TOPPER 5-SECOND MCQ SHORTCUTS
                    Interchanged Coefficients Trick (49x + 51y = 499 & 51x + 49y = 501):
                    Never multiply by 49 and 51! Step 1: Add both → 100(x + y) = 1000 → x + y = 10. Step 2: Subtract both → -2(x - y) = -2 → x - y = 1. Solve in 5 seconds!
                    • (10x + y) + (10y + x) = 11(x + y) (Always divisible by 11)
                    • (10x + y) - (10y + x) = 9(x - y) (Always divisible by 9)
                    
                    5. MUST-SOLVE BOARD QUESTIONS + STEP-MARKING SECRET
                    • Q1: Sum of digits of a 2-digit no. is 9. Nine times this number is twice the reversed number.
                      Ans: x + y = 9 & 9(10x + y) = 2(10y + x) → 88x = 11y → y = 8x → x = 1, y = 8 → No. is 18.
                    • Q2: Ratio of incomes is 9:7 and expenditures is 4:3. Each saves ₹2000. Find incomes.
                      Ans: 9x - 4y = 2000 & 7x - 3y = 2000 → x = 2000 → Incomes: ₹18,000 & ₹14,000.
                    • Q3: Find k for which kx + 3y - (k - 3) = 0 and 12x + ky - k = 0 have infinitely many solutions.
                      Ans: k/12 = 3/k = (k - 3)/k → k² = 36 → k = ±6. Second pair gives k = 6 only!
                    • Q4: Lending library has fixed charge for first 3 days & per-day charge thereafter (7 days = ₹27, 5 days = ₹21).
                      Ans: x + 4y = 27 & x + 2y = 21 → y = ₹3/day, Fixed charge x = ₹15.
                    
                    💡 STEP-MARKING SECRET: In graphical triangle questions ("Shade triangle formed with X-axis"), writing the 3 vertex coordinates clearly and shading the region carries 1 full mark out of 3.
                """.trimIndent()
            }
            val ch = class10Chapters.getOrElse(pageIndex) { "Chapter ${pageIndex + 1}" }
            return """
                CBSE CLASS 10 MATHEMATICS REVISION SHEET — Page ${pageIndex + 1} of $pageCount
                $ch
                
                1. CORE CONCEPTS & DEFINITIONS
                • Comprehensive summary of $ch aligned with the rationalised NCERT syllabus.
                • Always state the standard algebraic or geometric condition before substituting numerical values.
                
                2. KEY FORMULAS & THEOREMS
                • Formula Vault for $ch: Memorize standard forms, discriminant/ratio conditions, and coordinate identities.
                • Write units clearly in mensuration, coordinate geometry, and word problems.
                
                3. COMMON BOARD EXAM MISTAKES TO AVOID
                • Do not skip intermediate steps—CBSE step-marking awards 0.5 to 1 mark for each logical deduction.
                • Double-check negative signs when transposing terms across the equality sign.
                
                4. SOLVED NCERT EXEMPLAR SHORTCUTS
                • Use elimination and ratio comparison to solve 1-mark MCQs in under 10 seconds.
                • Verify your final answer by plugging values back into the original statement.
            """.trimIndent()
        }

        return """
            $docTitle — Page ${pageIndex + 1} of $pageCount
            
            Section ${pageIndex + 1}.1 — Key Syllabus & Reference Notes
            Detailed analytical summary and high-yield exam pointers for $docTitle. Includes essential definitions, comparison tables, and solved examples.
            
            Section ${pageIndex + 1}.2 — Core Methodology & Practice Guidelines
            Step-by-step problem solving methodology and revision checklist. Use the floating sidebar on the left to draw freehand notes, highlight passages, underline key formulas, or drop a sticky note pin anywhere on the page.
            
            Section ${pageIndex + 1}.3 — Quick Review Summary
            Switch between 8 eye-comfort reading themes (Light, Sepia, Dark, Mist, Sage, Sand, Dusk, Candle) or use Read Aloud and Auto-scroll for hands-free study.
        """.trimIndent()
    }

    fun getDocumentOutline(docTitle: String, pageCount: Int): List<PdfOutlineChapter> {
        if (docTitle.contains("Maths Cheat-Sheet", ignoreCase = true)) {
            return class10Chapters.take(pageCount).mapIndexed { index, title ->
                PdfOutlineChapter(
                    title = title,
                    pageIndex = index,
                    subtitle = "1-Page Formula & Shortcut Vault • Page ${index + 1}"
                )
            }
        }
        return (0 until pageCount).map { idx ->
            PdfOutlineChapter(
                title = "Section ${idx + 1}: ${docTitle.take(28)}",
                pageIndex = idx,
                subtitle = "Page ${idx + 1} of $pageCount"
            )
        }
    }
}
