package com.lolabible.app.data

enum class Testament { OLD, NEW }

data class Book(
    /** 1-based canonical number: 1 = Genesis ... 66 = Revelation. */
    val number: Int,
    val englishName: String,
    val tagalogName: String,
    val chapterCount: Int,
) {
    val testament: Testament
        get() = if (number <= Books.OLD_TESTAMENT_SIZE) Testament.OLD else Testament.NEW

    fun name(translation: Translation): String = when (translation) {
        Translation.KJV -> englishName
        Translation.ADB -> tagalogName
    }
}

/** A position in the Bible. Both fields are 1-based. */
data class ChapterRef(val book: Int, val chapter: Int) {
    init {
        require(book in 1..Books.all.size) { "Unknown book $book" }
        require(chapter in 1..Books.get(book).chapterCount) { "Book $book has no chapter $chapter" }
    }

    /** The following chapter, continuing into the next book; null after Revelation 22. */
    fun next(): ChapterRef? = when {
        chapter < Books.get(book).chapterCount -> ChapterRef(book, chapter + 1)
        book < Books.all.size -> ChapterRef(book + 1, 1)
        else -> null
    }

    /** The preceding chapter, going back into the previous book; null before Genesis 1. */
    fun previous(): ChapterRef? = when {
        chapter > 1 -> ChapterRef(book, chapter - 1)
        book > 1 -> ChapterRef(book - 1, Books.get(book - 1).chapterCount)
        else -> null
    }
}

object Books {
    const val OLD_TESTAMENT_SIZE = 39

    val all: List<Book> = listOf(
        Book(1, "Genesis", "Genesis", 50),
        Book(2, "Exodus", "Exodo", 40),
        Book(3, "Leviticus", "Levitico", 27),
        Book(4, "Numbers", "Mga Bilang", 36),
        Book(5, "Deuteronomy", "Deuteronomio", 34),
        Book(6, "Joshua", "Josue", 24),
        Book(7, "Judges", "Mga Hukom", 21),
        Book(8, "Ruth", "Ruth", 4),
        Book(9, "1 Samuel", "1 Samuel", 31),
        Book(10, "2 Samuel", "2 Samuel", 24),
        Book(11, "1 Kings", "1 Mga Hari", 22),
        Book(12, "2 Kings", "2 Mga Hari", 25),
        Book(13, "1 Chronicles", "1 Mga Cronica", 29),
        Book(14, "2 Chronicles", "2 Mga Cronica", 36),
        Book(15, "Ezra", "Ezra", 10),
        Book(16, "Nehemiah", "Nehemias", 13),
        Book(17, "Esther", "Esther", 10),
        Book(18, "Job", "Job", 42),
        Book(19, "Psalms", "Mga Awit", 150),
        Book(20, "Proverbs", "Mga Kawikaan", 31),
        Book(21, "Ecclesiastes", "Eclesiastes", 12),
        Book(22, "Song of Solomon", "Awit ni Solomon", 8),
        Book(23, "Isaiah", "Isaias", 66),
        Book(24, "Jeremiah", "Jeremias", 52),
        Book(25, "Lamentations", "Mga Panaghoy", 5),
        Book(26, "Ezekiel", "Ezekiel", 48),
        Book(27, "Daniel", "Daniel", 12),
        Book(28, "Hosea", "Oseas", 14),
        Book(29, "Joel", "Joel", 3),
        Book(30, "Amos", "Amos", 9),
        Book(31, "Obadiah", "Abdias", 1),
        Book(32, "Jonah", "Jonas", 4),
        Book(33, "Micah", "Mikas", 7),
        Book(34, "Nahum", "Nahum", 3),
        Book(35, "Habakkuk", "Habacuc", 3),
        Book(36, "Zephaniah", "Sofonias", 3),
        Book(37, "Haggai", "Hageo", 2),
        Book(38, "Zechariah", "Zacarias", 14),
        Book(39, "Malachi", "Malakias", 4),
        Book(40, "Matthew", "Mateo", 28),
        Book(41, "Mark", "Marcos", 16),
        Book(42, "Luke", "Lucas", 24),
        Book(43, "John", "Juan", 21),
        Book(44, "Acts", "Mga Gawa", 28),
        Book(45, "Romans", "Mga Taga-Roma", 16),
        Book(46, "1 Corinthians", "1 Mga Taga-Corinto", 16),
        Book(47, "2 Corinthians", "2 Mga Taga-Corinto", 13),
        Book(48, "Galatians", "Mga Taga-Galacia", 6),
        Book(49, "Ephesians", "Mga Taga-Efeso", 6),
        Book(50, "Philippians", "Mga Taga-Filipos", 4),
        Book(51, "Colossians", "Mga Taga-Colosas", 4),
        Book(52, "1 Thessalonians", "1 Mga Taga-Tesalonica", 5),
        Book(53, "2 Thessalonians", "2 Mga Taga-Tesalonica", 3),
        Book(54, "1 Timothy", "1 Timoteo", 6),
        Book(55, "2 Timothy", "2 Timoteo", 4),
        Book(56, "Titus", "Tito", 3),
        Book(57, "Philemon", "Filemon", 1),
        Book(58, "Hebrews", "Mga Hebreo", 13),
        Book(59, "James", "Santiago", 5),
        Book(60, "1 Peter", "1 Pedro", 5),
        Book(61, "2 Peter", "2 Pedro", 3),
        Book(62, "1 John", "1 Juan", 5),
        Book(63, "2 John", "2 Juan", 1),
        Book(64, "3 John", "3 Juan", 1),
        Book(65, "Jude", "Judas", 1),
        Book(66, "Revelation", "Apocalipsis", 22),
    )

    fun get(number: Int): Book = all[number - 1]

    fun inTestament(testament: Testament): List<Book> = all.filter { it.testament == testament }
}
