package com.lolabible.app.ui.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lolabible.app.data.ChapterRef
import com.lolabible.app.data.Translation
import com.lolabible.app.speech.Speaker
import com.lolabible.app.ui.theme.BibleTheme

@Composable
fun ReaderScreen(viewModel: ReaderViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val noVoiceMessage by rememberUpdatedState(state?.translation?.labels?.noVoice)

    LaunchedEffect(viewModel) {
        viewModel.speechEvents.collect { event ->
            when (event) {
                Speaker.Event.VoiceUnavailable -> noVoiceMessage?.let { snackbarHostState.showSnackbar(it) }
            }
        }
    }

    val current = state
    if (current == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    ReaderContent(
        state = current,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onPrevious = viewModel::goToPrevious,
        onNext = viewModel::goToNext,
        onSmallerText = viewModel::makeTextSmaller,
        onLargerText = viewModel::makeTextLarger,
        onToggleReadAloud = viewModel::toggleReadAloud,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderContent(
    state: ReaderUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSmallerText: () -> Unit,
    onLargerText: () -> Unit,
    onToggleReadAloud: () -> Unit,
) {
    val labels = state.translation.labels
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = labels.back)
                    }
                },
                actions = {
                    IconButton(onClick = onSmallerText, enabled = state.canShrinkText) {
                        Icon(Icons.Filled.TextDecrease, contentDescription = labels.smallerText)
                    }
                    IconButton(onClick = onLargerText, enabled = state.canGrowText) {
                        Icon(Icons.Filled.TextIncrease, contentDescription = labels.largerText)
                    }
                    FilledTonalIconToggleButton(checked = state.isSpeaking, onCheckedChange = { onToggleReadAloud() }) {
                        Icon(
                            if (state.isSpeaking) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (state.isSpeaking) labels.stopReading else labels.readAloud,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                },
            )
        },
        bottomBar = {
            ChapterNavigation(
                previousLabel = labels.previous,
                nextLabel = labels.next,
                hasPrevious = state.hasPrevious,
                hasNext = state.hasNext,
                onPrevious = onPrevious,
                onNext = onNext,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        // A fresh scroll position for every chapter, so a new chapter always starts at verse 1.
        key(state.ref, state.translation) {
            VerseList(
                verses = state.verses,
                textSize = state.textSize,
                contentPadding = padding,
            )
        }
    }
}

@Composable
private fun VerseList(verses: List<String>, textSize: Float, contentPadding: PaddingValues) {
    val listState = rememberLazyListState()
    val verseStyle = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = textSize.sp,
        lineHeight = (textSize * 1.55f).sp,
        color = MaterialTheme.colorScheme.onBackground,
    )
    val numberStyle = SpanStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = (textSize * 0.7f).sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        itemsIndexed(verses) { index, verse ->
            Text(
                text = buildAnnotatedString {
                    withStyle(numberStyle) { append("${index + 1}  ") }
                    append(verse)
                },
                style = verseStyle,
            )
        }
    }
}

@Composable
private fun ChapterNavigation(
    previousLabel: String,
    nextLabel: String,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = hasPrevious,
                modifier = Modifier.weight(1f).heightIn(min = 60.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, modifier = Modifier.size(28.dp))
                Text(previousLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Button(
                onClick = onNext,
                enabled = hasNext,
                modifier = Modifier.weight(1f).heightIn(min = 60.dp),
            ) {
                Text(nextLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReaderPreview() {
    BibleTheme {
        ReaderContent(
            state = ReaderUiState(
                ref = ChapterRef(19, 23),
                translation = Translation.KJV,
                textSize = 22f,
                verses = listOf(
                    "The LORD is my shepherd; I shall not want.",
                    "He maketh me to lie down in green pastures: he leadeth me beside the still waters.",
                ),
                isSpeaking = false,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {}, onPrevious = {}, onNext = {}, onSmallerText = {}, onLargerText = {}, onToggleReadAloud = {},
        )
    }
}
