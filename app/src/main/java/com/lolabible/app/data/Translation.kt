package com.lolabible.app.data

import java.util.Locale

/**
 * A Bible translation bundled with the app. The UI language follows the chosen translation,
 * so Lola never has to think about two separate language settings.
 */
enum class Translation(
    /** Folder under `assets/bible/` holding this translation's books. */
    val assetDir: String,
    /** Label shown on the language toggle, written in its own language. */
    val label: String,
    /** Voices to try for read-aloud, most preferred first. */
    val speechLocales: List<Locale>,
    val labels: Labels,
) {
    KJV(
        assetDir = "kjv",
        label = "English",
        speechLocales = listOf(Locale.US, Locale.UK, Locale.ENGLISH),
        labels = Labels(
            appTitle = "Holy Bible",
            oldTestament = "Old Testament",
            newTestament = "New Testament",
            continueReading = "Continue reading",
            chapter = "Chapter",
            previous = "Previous",
            next = "Next",
            back = "Back",
            readAloud = "Read aloud",
            stopReading = "Stop reading",
            smallerText = "Smaller text",
            largerText = "Larger text",
            noVoice = "This phone has no English voice installed for reading aloud.",
        ),
    ),
    ADB(
        assetDir = "adb",
        label = "Tagalog",
        speechLocales = listOf(Locale.forLanguageTag("fil-PH"), Locale.forLanguageTag("tl-PH")),
        labels = Labels(
            appTitle = "Banal na Biblia",
            oldTestament = "Lumang Tipan",
            newTestament = "Bagong Tipan",
            continueReading = "Ituloy ang pagbasa",
            chapter = "Kabanata",
            previous = "Nakaraan",
            next = "Susunod",
            back = "Bumalik",
            readAloud = "Basahin nang malakas",
            stopReading = "Ihinto ang pagbasa",
            smallerText = "Paliitin ang letra",
            largerText = "Palakihin ang letra",
            noVoice = "Walang boses na Tagalog sa teleponong ito para sa pagbasa nang malakas.",
        ),
    ),
}

/** User-facing text for one language. */
data class Labels(
    val appTitle: String,
    val oldTestament: String,
    val newTestament: String,
    val continueReading: String,
    val chapter: String,
    val previous: String,
    val next: String,
    val back: String,
    val readAloud: String,
    val stopReading: String,
    val smallerText: String,
    val largerText: String,
    val noVoice: String,
)
