package com.lolabible.app.data

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileInputStream

/** Runs against the real bundled assets, so it also guards the generated Bible text. */
class BibleRepositoryTest {

    private val assetsRoot = File("src/main/assets")
    private val openedPaths = mutableListOf<String>()
    private val source = AssetSource { path ->
        openedPaths += path
        FileInputStream(File(assetsRoot, path))
    }

    @Test
    fun `reads well-known verses in both translations`() = runTest {
        val repository = BibleRepository(source, StandardTestDispatcher(testScheduler))

        assertEquals(
            "In the beginning God created the heaven and the earth.",
            repository.verses(Translation.KJV, ChapterRef(1, 1)).first(),
        )
        assertEquals(
            "Ang Panginoon ay aking pastor; hindi ako mangangailangan.",
            repository.verses(Translation.ADB, ChapterRef(19, 23)).first(),
        )
        assertTrue(repository.verses(Translation.KJV, ChapterRef(43, 3))[15].startsWith("For God so loved the world"))
    }

    @Test
    fun `loads each book file once while it stays cached`() = runTest {
        val repository = BibleRepository(source, StandardTestDispatcher(testScheduler))

        repository.verses(Translation.KJV, ChapterRef(43, 1))
        repository.verses(Translation.KJV, ChapterRef(43, 2))
        repository.verses(Translation.KJV, ChapterRef(43, 3))

        assertEquals(listOf("bible/kjv/43.json"), openedPaths)
    }

    @Test
    fun `every book in the catalog matches the bundled text`() {
        for (translation in Translation.entries) {
            val bookFiles = File(assetsRoot, "bible/${translation.assetDir}").listFiles().orEmpty()
            assertEquals("${translation.name} book file count", Books.all.size, bookFiles.size)

            for (book in Books.all) {
                val file = File(assetsRoot, "bible/${translation.assetDir}/%02d.json".format(book.number))
                val chapters = Json.decodeFromString<List<List<String>>>(file.readText())
                val where = "${translation.name} ${book.englishName}"
                assertEquals("$where chapter count", book.chapterCount, chapters.size)
                chapters.forEachIndexed { index, verses ->
                    assertTrue("$where ${index + 1} is empty", verses.isNotEmpty())
                    assertTrue("$where ${index + 1} has a blank verse", verses.none { it.isBlank() })
                }
            }
        }
    }

    @Test
    fun `catalog has 39 Old and 27 New Testament books with 1189 chapters`() {
        assertEquals(39, Books.inTestament(Testament.OLD).size)
        assertEquals(27, Books.inTestament(Testament.NEW).size)
        assertEquals(1189, Books.all.sumOf { it.chapterCount })
        Books.all.forEachIndexed { index, book -> assertEquals(index + 1, book.number) }
    }
}
