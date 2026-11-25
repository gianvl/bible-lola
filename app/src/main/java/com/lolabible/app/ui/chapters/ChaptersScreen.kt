package com.lolabible.app.ui.chapters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lolabible.app.data.Book
import com.lolabible.app.data.Books
import com.lolabible.app.data.Translation
import com.lolabible.app.ui.theme.BibleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    book: Book,
    translation: Translation,
    onChapterSelected: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val labels = translation.labels
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book.name(translation)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = labels.back)
                    }
                },
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 76.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items((1..book.chapterCount).toList(), key = { it }) { chapter ->
                FilledTonalButton(
                    onClick = { onChapterSelected(chapter) },
                    modifier = Modifier
                        .height(72.dp)
                        .semantics { contentDescription = "${labels.chapter} $chapter" },
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(chapter.toString(), style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChaptersScreenPreview() {
    BibleTheme {
        ChaptersScreen(book = Books.get(43), translation = Translation.KJV, onChapterSelected = {}, onBack = {})
    }
}
