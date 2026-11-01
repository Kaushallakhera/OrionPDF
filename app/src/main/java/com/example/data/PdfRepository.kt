package com.example.data

import kotlinx.coroutines.flow.Flow

class PdfRepository(private val dao: PdfDao) {
    val allDocuments: Flow<List<PdfDocumentEntity>> = dao.observeAllDocuments()
    val recentDocuments: Flow<List<PdfDocumentEntity>> = dao.observeRecentDocuments()
    val favoriteDocuments: Flow<List<PdfDocumentEntity>> = dao.observeFavoriteDocuments()
    val allBookmarks: Flow<List<BookmarkEntity>> = dao.observeAllBookmarks()
    val allQuotes: Flow<List<QuoteEntity>> = dao.observeAllQuotes()

    fun observeBookmarksForDoc(docId: Long): Flow<List<BookmarkEntity>> = dao.observeBookmarksForDoc(docId)
    fun observeQuotesForDoc(docId: Long): Flow<List<QuoteEntity>> = dao.observeQuotesForDoc(docId)
    fun observeAnnotationsForDoc(docId: Long): Flow<List<AnnotationEntity>> = dao.observeAnnotationsForDoc(docId)

    suspend fun getDocumentCount(): Int = dao.getDocumentCount()
    suspend fun getDocumentById(id: Long): PdfDocumentEntity? = dao.getDocumentById(id)

    suspend fun insertDocument(doc: PdfDocumentEntity): Long = dao.insertDocument(doc)
    suspend fun insertDocuments(docs: List<PdfDocumentEntity>) = dao.insertDocuments(docs)
    suspend fun updateDocument(doc: PdfDocumentEntity) = dao.updateDocument(doc)

    suspend fun updateReadingProgress(docId: Long, pageIndex: Int, timestamp: Long = System.currentTimeMillis()) {
        dao.updateReadingProgress(docId, pageIndex, timestamp)
    }

    suspend fun toggleFavorite(docId: Long, isFavorite: Boolean) {
        dao.updateFavorite(docId, isFavorite)
    }

    suspend fun moveDocumentToFolder(docId: Long, folderName: String) {
        dao.updateDocumentFolder(docId, folderName)
    }

    suspend fun updatePinnedNotification(docId: Long, pinned: Boolean) {
        dao.updatePinnedStatus(docId, pinned)
    }

    suspend fun deleteDocument(docId: Long) {
        dao.deleteDocument(docId)
    }

    suspend fun addBookmark(bookmark: BookmarkEntity): Long = dao.insertBookmark(bookmark)
    suspend fun removeBookmarkAtPage(docId: Long, pageIndex: Int) = dao.deleteBookmarkAtPage(docId, pageIndex)
    suspend fun deleteBookmarkById(id: Long) = dao.deleteBookmarkById(id)

    suspend fun addQuote(quote: QuoteEntity): Long = dao.insertQuote(quote)
    suspend fun deleteQuoteById(id: Long) = dao.deleteQuoteById(id)

    suspend fun addAnnotation(annotation: AnnotationEntity): Long = dao.insertAnnotation(annotation)
    suspend fun deleteAnnotationById(id: Long) = dao.deleteAnnotationById(id)
    suspend fun clearPageAnnotations(docId: Long, pageIndex: Int) = dao.clearPageAnnotations(docId, pageIndex)
}
