package com.example.ui.notes

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateMuted
import com.example.ui.viewmodel.NotesViewModel

@Composable
fun NoteEditorScreen(
    noteId: Long?,
    viewModel: NotesViewModel,
    onBack: () -> Unit,
    onSecretUnlock: () -> Unit
) {
    BackHandler { onBack() }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal") }
    var isPinned by remember { mutableStateOf(false) }
    var isTriggerDetected by remember { mutableStateOf(false) }

    // Load note if editing existing
    LaunchedEffect(noteId) {
        if (noteId != null && noteId > 0) {
            // Find note in notes
            viewModel.filteredNotes.value.find { it.id == noteId }?.let { existing ->
                title = existing.title
                content = existing.content
                category = existing.category
                isPinned = existing.isPinned
            }
        }
    }

    // Real-time detection of secret trigger #APAP
    LaunchedEffect(title) {
        if (title.trim() == "#APAP") {
            isTriggerDetected = true
        } else {
            isTriggerDetected = false
        }
    }

    val categories = listOf("Personal", "Work", "Ideas", "Todos")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("note_editor_screen"),
        containerColor = PaperWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SlateDark
                        )
                    }
                    Column {
                        Text(
                            text = "Workspace / $category / Doc",
                            color = SlateMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "☁️ Auto-saved to cloud",
                            color = Color(0xFF10B981),
                            fontSize = 10.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isPinned = !isPinned },
                        modifier = Modifier.testTag("editor_pin_button")
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (isPinned) Color(0xFFD97706) else SlateMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = {
                            viewModel.saveNote(
                                id = noteId ?: 0L,
                                title = title,
                                content = content,
                                category = category,
                                isPinned = isPinned,
                                onSecretUnlock = onSecretUnlock,
                                onSaved = onBack
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTriggerDetected) Color(0xFF0F172A) else SlateDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("save_note_button")
                    ) {
                        if (isTriggerDetected) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Unlock",
                                color = Color(0xFF00F0FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Secret trigger prompt banner when #APAP is entered
            AnimatedVisibility(
                visible = isTriggerDetected,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clickable { onSecretUnlock() }
                        .testTag("secret_trigger_detected_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00F0FF))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "TRIGGER #APAP DETECTED",
                                color = Color(0xFF00F0FF),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "TAP TO ENTER SECURE CHAT →",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Category Selector Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = category.equals(cat, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) SlateDark else Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) SlateDark else PaperBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { category = cat }
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else SlateDark,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Note Title Input
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                ),
                cursorBrush = SolidColor(SlateDark),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        if (title.isEmpty()) {
                            Text(
                                text = "Note Title",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_input")
            )

            // Content body editor with formatting bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        content = if (content.isEmpty()) "- " else "$content\n- "
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = "Add bullet list",
                        tint = SlateMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${content.length} characters",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            BasicTextField(
                value = content,
                onValueChange = { content = it },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = SlateDark,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(SlateDark),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        if (content.isEmpty()) {
                            Text(
                                text = "Start typing your note here...",
                                fontSize = 16.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 24.sp
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("note_content_input")
            )
        }
    }
}
