package com.example

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.test.core.app.ApplicationProvider
import com.example.data.AnnotationEntity
import com.example.data.ReadingTheme
import com.example.data.encodeAnnotationPoints
import com.example.data.parseNormalizedPoints
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Orion PDF Reader", appName)
    }

    @Test
    fun `encode and decode normalized annotation points`() {
        val points = listOf(Offset(0.15f, 0.25f), Offset(0.65f, 0.75f))
        val encoded = encodeAnnotationPoints(points)
        val entity = AnnotationEntity(
            documentId = 1L,
            pageIndex = 2,
            toolType = "PEN",
            colorHex = 0xFFE53935L,
            strokeWidth = 4.5f,
            pointsData = encoded
        )
        val parsed = entity.parseNormalizedPoints()
        assertEquals(2, parsed.size)
        assertEquals(0.15f, parsed[0].x, 0.001f)
        assertEquals(0.75f, parsed[1].y, 0.001f)
    }

    @Test
    fun `reading themes provide expected matrices`() {
        assertEquals(8, ReadingTheme.entries.size)
        assertNotNull(ReadingTheme.DARK.toColorMatrix())
        assertNotNull(ReadingTheme.CANDLE.toColorMatrix())
    }
}
