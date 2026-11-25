package com.lolabible.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChapterRefTest {

    @Test
    fun `next stays within a book`() {
        assertEquals(ChapterRef(43, 4), ChapterRef(43, 3).next())
    }

    @Test
    fun `next crosses into the following book`() {
        assertEquals(ChapterRef(2, 1), ChapterRef(1, 50).next())
        assertEquals(ChapterRef(40, 1), ChapterRef(39, 4).next())
    }

    @Test
    fun `previous crosses back to the last chapter of the preceding book`() {
        assertEquals(ChapterRef(1, 50), ChapterRef(2, 1).previous())
        assertEquals(ChapterRef(64, 1), ChapterRef(65, 1).previous())
    }

    @Test
    fun `there is nothing before Genesis 1 or after Revelation 22`() {
        assertNull(ChapterRef(1, 1).previous())
        assertNull(ChapterRef(66, 22).next())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects a chapter the book does not have`() {
        ChapterRef(2, 41)
    }

    @Test
    fun `walking forward visits every chapter exactly once`() {
        var ref: ChapterRef? = ChapterRef(1, 1)
        var count = 0
        while (ref != null) {
            count++
            ref = ref.next()
        }
        assertEquals(1189, count)
    }
}
