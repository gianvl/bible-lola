package com.lolabible.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lolabible.app.data.Book
import com.lolabible.app.data.Books
import com.lolabible.app.data.ChapterRef
import com.lolabible.app.data.Settings
import com.lolabible.app.data.Testament
import com.lolabible.app.data.Translation
import com.lolabible.app.ui.theme.BibleTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    settings: Settings,
    onTranslationSelected: (Translation) -> Unit,
    onContinueReading: (ChapterRef) -> Unit,
    onBookSelected: (Book) -> Unit,
) {
    val translation = settings.translation
    val labels = translation.labels
    var testament by rememberSaveable {
        mutableStateOf(settings.lastRead?.let { Books.get(it.book).testament } ?: Testament.OLD)
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(labels.appTitle) }) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LanguageToggle(
                selected = translation,
                onSelected = onTranslationSelected,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )

            settings.lastRead?.let { ref ->
                ContinueReadingCard(
                    label = labels.continueReading,
                    title = "${Books.get(ref.book).name(translation)} ${ref.chapter}",
                    onClick = { onContinueReading(ref) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            PrimaryTabRow(selectedTabIndex = testament.ordinal) {
                Testament.entries.forEach { entry ->
                    Tab(
                        selected = testament == entry,
                        onClick = { testament = entry },
                        modifier = Modifier.heightIn(min = 64.dp),
                        text = {
                            Text(
                                text = if (entry == Testament.OLD) labels.oldTestament else labels.newTestament,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(Books.inTestament(testament), key = { it.number }) { book ->
                    BookRow(name = book.name(translation), onClick = { onBookSelected(book) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun LanguageToggle(selected: Translation, onSelected: (Translation) -> Unit, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(modifier) {
        Translation.entries.forEachIndexed { index, translation ->
            SegmentedButton(
                selected = translation == selected,
                onClick = { onSelected(translation) },
                shape = SegmentedButtonDefaults.itemShape(index, Translation.entries.size),
                modifier = Modifier.heightIn(min = 56.dp),
            ) {
                Text(translation.label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ContinueReadingCard(label: String, title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(36.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge)
                Text(title, style = MaterialTheme.typography.titleLarge)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun BookRow(name: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    BibleTheme {
        HomeScreen(
            settings = Settings(translation = Translation.ADB, lastRead = ChapterRef(43, 3)),
            onTranslationSelected = {},
            onContinueReading = {},
            onBookSelected = {},
        )
    }
}
