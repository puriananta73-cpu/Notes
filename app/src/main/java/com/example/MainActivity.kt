package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.db.AppDatabase
import com.example.data.repository.ChatRepository
import com.example.data.repository.NotesRepository
import com.example.ui.chat.SecretChatScreen
import com.example.ui.notes.NoteEditorScreen
import com.example.ui.notes.NotesHomeScreen
import com.example.ui.notes.NotesSettingsDialog
import com.example.ui.notes.PinLockScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NotesViewModel
import com.example.ui.viewmodel.SecretChatViewModel
import com.example.ui.web.DecoyGoogleSearchScreen
import com.example.ui.web.WebBrowserChrome

sealed class ScreenState {
    object NotesHome : ScreenState()
    data class NoteEditor(val noteId: Long?) : ScreenState()
    object SecretChat : ScreenState()
    object DecoyGoogle : ScreenState()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val notesRepository = NotesRepository(database.noteDao())
        val chatRepository = ChatRepository(database.chatMessageDao())

        setContent {
            val notesViewModel: NotesViewModel = remember {
                NotesViewModel(notesRepository)
            }
            val chatViewModel: SecretChatViewModel = remember {
                SecretChatViewModel(application, chatRepository)
            }

            MyApplicationTheme {
                MainAppContent(
                    notesViewModel = notesViewModel,
                    chatViewModel = chatViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    notesViewModel: NotesViewModel,
    chatViewModel: SecretChatViewModel
) {
    val isAppLocked by notesViewModel.isAppLocked.collectAsState()
    var currentScreen by remember { mutableStateOf<ScreenState>(ScreenState.NotesHome) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val currentUrl = when (currentScreen) {
        is ScreenState.NotesHome -> "https://quicknote.io/notes"
        is ScreenState.NoteEditor -> "https://quicknote.io/notes/editor"
        is ScreenState.SecretChat -> "https://shadowcom.onion/0xAPAP"
        is ScreenState.DecoyGoogle -> "https://www.google.com/search?q=best+notes+apps"
    }

    val isSecret = currentScreen is ScreenState.SecretChat

    Surface(modifier = Modifier.fillMaxSize()) {
        if (isAppLocked) {
            PinLockScreen(
                viewModel = notesViewModel,
                onSecretUnlock = {
                    notesViewModel.unlockApp("1234")
                    currentScreen = ScreenState.SecretChat
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Persistent Web Browser Chrome (Address bar, tabs, window controls)
                WebBrowserChrome(
                    currentUrl = currentUrl,
                    onUrlSubmit = { url ->
                        val clean = url.trim()
                        if (clean.endsWith("#APAP") || clean.contains("shadowcom") || clean == "#APAP") {
                            currentScreen = ScreenState.SecretChat
                        } else if (clean.contains("google.com")) {
                            currentScreen = ScreenState.DecoyGoogle
                        } else {
                            currentScreen = ScreenState.NotesHome
                        }
                    },
                    onReload = {
                        // Reload acts as stealth reset
                        if (isSecret) {
                            currentScreen = ScreenState.NotesHome
                        }
                    },
                    onBack = {
                        when (currentScreen) {
                            is ScreenState.NoteEditor -> currentScreen = ScreenState.NotesHome
                            is ScreenState.SecretChat -> currentScreen = ScreenState.NotesHome
                            is ScreenState.DecoyGoogle -> currentScreen = ScreenState.NotesHome
                            else -> {}
                        }
                    },
                    isSecretMode = isSecret,
                    onPanicToGoogle = {
                        currentScreen = ScreenState.DecoyGoogle
                    }
                )

                // Website Body Content
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                    },
                    label = "web_screen_transition",
                    modifier = Modifier.weight(1f)
                ) { screen ->
                    when (screen) {
                        is ScreenState.NotesHome -> {
                            NotesHomeScreen(
                                viewModel = notesViewModel,
                                onNoteClick = { noteId ->
                                    currentScreen = ScreenState.NoteEditor(noteId)
                                },
                                onNewNoteClick = {
                                    currentScreen = ScreenState.NoteEditor(null)
                                },
                                onSecretUnlock = {
                                    currentScreen = ScreenState.SecretChat
                                },
                                onOpenSettings = {
                                    showSettingsDialog = true
                                }
                            )
                        }

                        is ScreenState.NoteEditor -> {
                            NoteEditorScreen(
                                noteId = screen.noteId,
                                viewModel = notesViewModel,
                                onBack = {
                                    currentScreen = ScreenState.NotesHome
                                },
                                onSecretUnlock = {
                                    currentScreen = ScreenState.SecretChat
                                }
                            )
                        }

                        is ScreenState.SecretChat -> {
                            SecretChatScreen(
                                viewModel = chatViewModel,
                                onPanicClose = {
                                    // Instantly navigate browser back to innocent QuickNote web
                                    currentScreen = ScreenState.NotesHome
                                }
                            )
                        }

                        is ScreenState.DecoyGoogle -> {
                            DecoyGoogleSearchScreen(
                                onReturnToNotes = {
                                    currentScreen = ScreenState.NotesHome
                                }
                            )
                        }
                    }
                }
            }

            if (showSettingsDialog) {
                NotesSettingsDialog(
                    viewModel = notesViewModel,
                    onDismiss = { showSettingsDialog = false },
                    onSecretUnlock = {
                        showSettingsDialog = false
                        currentScreen = ScreenState.SecretChat
                    }
                )
            }
        }
    }
}
