package com.lolabible.app.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream

/** Opens a bundled file by its path relative to the assets root. */
fun interface AssetSource {
    fun open(path: String): InputStream
}

/**
 * Reads Bible text from the bundled assets. Each book is one JSON file
 * (`bible/<translation>/<NN>.json`): an array of chapters, each an array of verses.
 * Recently opened books are kept in memory so moving between chapters is instant.
 */
class BibleRepository(
    private val assets: AssetSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val mutex = Mutex()
    private val cache = object : LinkedHashMap<Pair<Translation, Int>, List<List<String>>>(
        CACHE_SIZE, 0.75f, /* accessOrder = */ true,
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Pair<Translation, Int>, List<List<String>>>) =
            size > CACHE_SIZE
    }

    suspend fun verses(translation: Translation, ref: ChapterRef): List<String> =
        book(translation, ref.book)[ref.chapter - 1]

    private suspend fun book(translation: Translation, number: Int): List<List<String>> {
        val key = translation to number
        return mutex.withLock {
            cache.getOrPut(key) { withContext(ioDispatcher) { load(translation, number) } }
        }
    }

    private fun load(translation: Translation, number: Int): List<List<String>> {
        val path = "bible/${translation.assetDir}/%02d.json".format(number)
        val text = assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
        return Json.decodeFromString(text)
    }

    private companion object {
        const val CACHE_SIZE = 4
    }
}
