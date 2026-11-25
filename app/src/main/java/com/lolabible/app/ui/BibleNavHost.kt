package com.lolabible.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.lolabible.app.data.Books
import com.lolabible.app.data.ChapterRef
import com.lolabible.app.ui.chapters.ChaptersScreen
import com.lolabible.app.ui.home.HomeScreen
import com.lolabible.app.ui.reader.ReaderScreen
import com.lolabible.app.ui.reader.ReaderViewModel
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute

@Serializable data class ChaptersRoute(val book: Int)

@Serializable data class ReaderRoute(val book: Int, val chapter: Int)

@Composable
fun BibleNavHost(appViewModel: AppViewModel, navController: NavHostController = rememberNavController()) {
    // The splash screen stays up until settings are loaded, so this is only null for a moment.
    val settings by appViewModel.settings.collectAsStateWithLifecycle()
    val current = settings ?: return

    // On a fresh launch, reopen the chapter Lola was last reading (Back returns to the book list).
    var resumeHandled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!resumeHandled) {
            resumeHandled = true
            current.lastRead?.let { navController.navigate(ReaderRoute(it.book, it.chapter)) }
        }
    }

    val openChapter = { ref: ChapterRef -> navController.navigate(ReaderRoute(ref.book, ref.chapter)) }

    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                settings = current,
                onTranslationSelected = appViewModel::setTranslation,
                onContinueReading = openChapter,
                onBookSelected = { book ->
                    if (book.chapterCount == 1) {
                        openChapter(ChapterRef(book.number, 1))
                    } else {
                        navController.navigate(ChaptersRoute(book.number))
                    }
                },
            )
        }
        composable<ChaptersRoute> { entry ->
            ChaptersScreen(
                book = Books.get(entry.toRoute<ChaptersRoute>().book),
                translation = current.translation,
                onChapterSelected = { chapter ->
                    // Replace the chapter grid so Back from the reader goes to the book list.
                    navController.navigate(ReaderRoute(entry.toRoute<ChaptersRoute>().book, chapter)) {
                        popUpTo<ChaptersRoute> { inclusive = true }
                    }
                },
                onBack = navController::navigateUp,
            )
        }
        composable<ReaderRoute> {
            ReaderScreen(
                viewModel = viewModel(factory = ReaderViewModel.Factory),
                onBack = navController::navigateUp,
            )
        }
    }
}
