package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfDao {
    @Query("SELECT * FROM pdf_documents ORDER BY title COLLATE NOCASE ASC")
    fun observeAllDocuments(): Flow<List<PdfDocumentEntity>>

    @Query("SELECT * FROM pdf_documents WHERE lastOpenedAt > 0 ORDER BY lastOpenedAt DESC")
    fun observeRecentDocuments(): Flow<List<PdfDocumentEntity>>

    @Query("SELECT * FROM pdf_documents WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE ASC")
    fun observeFavoriteDocuments(): Flow<List<PdfDocumentEntity>>

    @Query("SELECT COUNT(*) FROM pdf_documents")
    suspend fun getDocumentCount(): Int

    @Query("SELECT * FROM pdf_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): PdfDocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: PdfDocumentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(docs: List<PdfDocumentEntity>)

    @Update
    suspend fun updateDocument(doc: PdfDocumentEntity)

    @Query("UPDATE pdf_documents SET currentPage = :pageIndex, lastOpenedAt = :timestamp WHERE id = :docId")
    suspend fun updateReadingProgress(docId: Long, pageIndex: Int, timestamp: Long)

    @Query("UPDATE pdf_documents SET isFavorite = :favorite WHERE id = :docId")
    suspend fun updateFavorite(docId: Long, favorite: Boolean)

    @Query("UPDATE pdf_documents SET folderName = :folderName WHERE id = :docId")
    suspend fun updateDocumentFolder(docId: Long, folderName: String)

    @Query("UPDATE pdf_documents SET isPinnedNotification = :pinned WHERE id = :docId")
    suspend fun updatePinnedStatus(docId: Long, pinned: Boolean)

    @Query("DELETE FROM pdf_documents WHERE id = :docId")
    suspend fun deleteDocument(docId: Long)

    // Bookmarks
    @Query("SELECT * FROM bookmarks WHERE documentId = :docId ORDER BY pageIndex ASC")
    fun observeBookmarksForDoc(docId: Long): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE documentId = :docId AND pageIndex = :pageIndex")
    suspend fun deleteBookmarkAtPage(docId: Long, pageIndex: Int)

    @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
    suspend fun deleteBookmarkById(bookmarkId: Long)

    // Quotes
    @Query("SELECT * FROM quotes WHERE documentId = :docId ORDER BY pageIndex ASC, createdAt DESC")
    fun observeQuotesForDoc(docId: Long): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes ORDER BY createdAt DESC")
    fun observeAllQuotes(): Flow<List<QuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity): Long

    @Query("DELETE FROM quotes WHERE id = :quoteId")
    suspend fun deleteQuoteById(quoteId: Long)

    // Annotations
    @Query("SELECT * FROM annotations WHERE documentId = :docId ORDER BY createdAt ASC")
    fun observeAnnotationsForDoc(docId: Long): Flow<List<AnnotationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: AnnotationEntity): Long

    @Query("DELETE FROM annotations WHERE id = :annotationId")
    suspend fun deleteAnnotationById(annotationId: Long)

    @Query("DELETE FROM annotations WHERE documentId = :docId AND pageIndex = :pageIndex")
    suspend fun clearPageAnnotations(docId: Long, pageIndex: Int)
}
