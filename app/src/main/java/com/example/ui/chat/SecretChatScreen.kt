package com.example.ui.chat

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNeonGreen
import com.example.ui.theme.CyberPanicRed
import com.example.ui.theme.CyberYellow
import com.example.ui.viewmodel.SecretChatViewModel
import kotlinx.coroutines.launch

@Composable
fun SecretChatScreen(
    viewModel: SecretChatViewModel,
    onPanicClose: () -> Unit
) {
    // Intercept back button to perform Panic Disguise
    BackHandler { onPanicClose() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages by viewModel.allMessages.collectAsState()
    val myHandle by viewModel.myHandle.collectAsState()
    val selfDestructSeconds by viewModel.selfDestructSeconds.collectAsState()
    val isScreenshotBlocked by viewModel.isScreenshotBlocked.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDuration by viewModel.recordingDurationSec.collectAsState()
    val playingAudioId by viewModel.playingAudioMessageId.collectAsState()
    val isInVoiceCall by viewModel.isInVoiceCall.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showDoodleDialog by remember { mutableStateOf(false) }
    var showStickerSheet by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }

    // Screenshot protection flag management
    DisposableEffect(isScreenshotBlocked) {
        val window = (context as? Activity)?.window
        if (isScreenshotBlocked) {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.sendImageMessage(uri.toString())
        }
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("secret_chat_screen"),
        containerColor = CyberBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Neon Stealth Header Bar
            Surface(
                color = CyberCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Room Status Info
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CyberNeonGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SHADOWCOM Web Client // 0xAPAP",
                                color = CyberCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "WSS:// RELAY • 5 NODES • $myHandle",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Re-roll Handle",
                                tint = CyberCyan,
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable { viewModel.reRollHandle() }
                            )
                        }
                    }

                    // Action Controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Voice Call Button
                        IconButton(
                            onClick = { viewModel.startVoiceCall() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = 0.15f))
                                .testTag("start_voice_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Group Voice Call",
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Panic Disguise Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyberPanicRed.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberPanicRed),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPanicClose() }
                                .testTag("panic_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = CyberPanicRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PANIC",
                                    color = CyberPanicRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // Options Menu
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(CyberCard)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Self-Destruct: ${if (selfDestructSeconds == 0) "Off" else "${selfDestructSeconds}s"}",
                                            color = CyberYellow
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showTimerDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.HourglassBottom,
                                            contentDescription = null,
                                            tint = CyberYellow
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (isScreenshotBlocked) "Screenshot Block: ON" else "Screenshot Block: OFF",
                                            color = CyberCyan
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        viewModel.toggleScreenshotBlocked(!isScreenshotBlocked)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = CyberCyan
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Re-roll Handle", color = Color.White) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.reRollHandle()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Purge All Messages", color = CyberPanicRed) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.purgeRoom()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DeleteSweep,
                                            contentDescription = null,
                                            tint = CyberPanicRed
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Active Self-Destruct Banner if set
            if (selfDestructSeconds > 0) {
                Surface(
                    color = CyberYellow.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberYellow.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = CyberYellow,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EPHEMERAL MODE ACTIVE: Messages self-destruct after ${selfDestructSeconds}s",
                            color = CyberYellow,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Messages Stream
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatMessageItem(
                        message = msg,
                        isPlayingAudio = playingAudioId == msg.id,
                        onToggleAudio = { viewModel.toggleAudioPlayback(msg.id, msg.mediaUri) },
                        onReactionClick = { emoji -> viewModel.toggleReaction(msg.id, emoji) },
                        onDeleteMessage = { viewModel.deleteMessage(msg.id) }
                    )
                }
            }

            // Audio recording status strip
            AnimatedVisibility(visible = isRecording) {
                Surface(
                    color = Color(0xFFEF4444).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RECORDING ENCRYPTED AUDIO • 0:${recordingDuration.toString().padStart(2, '0')}",
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "TAP SEND OR RELEASE",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                color = CyberCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachment (+) Button
                    Box {
                        IconButton(
                            onClick = { showAttachmentMenu = true },
                            modifier = Modifier.testTag("chat_attachment_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Attachment",
                                tint = CyberCyan
                            )
                        }

                        DropdownMenu(
                            expanded = showAttachmentMenu,
                            onDismissRequest = { showAttachmentMenu = false },
                            modifier = Modifier.background(CyberCard)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Send Photo", color = Color.White) },
                                onClick = {
                                    showAttachmentMenu = false
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = CyberCyan
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Draw Schematic", color = Color.White) },
                                onClick = {
                                    showAttachmentMenu = false
                                    showDoodleDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Brush,
                                        contentDescription = null,
                                        tint = CyberNeonGreen
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Intel Stickers", color = Color.White) },
                                onClick = {
                                    showAttachmentMenu = false
                                    showStickerSheet = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = CyberYellow
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Send Intel Video", color = Color.White) },
                                onClick = {
                                    showAttachmentMenu = false
                                    viewModel.sendVideoMessage("simulated_video_${System.currentTimeMillis()}")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = Color(0xFFFF0055)
                                    )
                                }
                            )
                        }
                    }

                    // Text Input Field
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                "Transmit encrypted payload...",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF080B10),
                            unfocusedContainerColor = Color(0xFF080B10),
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = CyberCyan
                        ),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Voice Message or Send Button
                    if (textInput.isBlank()) {
                        // Mic Button (Hold to Record, Release to Send)
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) Color(0xFFEF4444) else CyberCard)
                                .border(
                                    1.dp,
                                    if (isRecording) Color(0xFFEF4444) else Color(0xFF334155),
                                    CircleShape
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            viewModel.startVoiceRecording()
                                            tryAwaitRelease()
                                            viewModel.stopAndSendVoiceRecording(cancelled = false)
                                        }
                                    )
                                }
                                .testTag("voice_record_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Hold to record voice message",
                                tint = if (isRecording) Color.White else CyberCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        // Send Text Button
                        IconButton(
                            onClick = {
                                viewModel.sendTextMessage(textInput)
                                textInput = ""
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(CyberCyan)
                                .testTag("send_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Message",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Voice Call Dialog
    if (isInVoiceCall) {
        GroupCallDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.endVoiceCall() }
        )
    }

    // Doodle Drawing Dialog
    if (showDoodleDialog) {
        DoodleDialog(
            onDismiss = { showDoodleDialog = false },
            onSendDoodle = { path -> viewModel.sendDoodleMessage(path) }
        )
    }

    // Sticker Picker Sheet
    if (showStickerSheet) {
        StickerPickerSheet(
            onDismiss = { showStickerSheet = false },
            onSelectSticker = { name, emoji -> viewModel.sendSticker(name, emoji) }
        )
    }

    // Self Destruct Timer Selector Dialog
    if (showTimerDialog) {
        SelfDestructTimerDialog(
            currentSeconds = selfDestructSeconds,
            onSelect = { sec ->
                viewModel.setSelfDestructSeconds(sec)
                showTimerDialog = false
            },
            onDismiss = { showTimerDialog = false }
        )
    }
}

@Composable
fun SelfDestructTimerDialog(
    currentSeconds: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CyberCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberYellow.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "EPHEMERAL TIMER",
                    color = CyberYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Messages will incinerate automatically after expiration.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                val options = listOf(
                    0 to "Off (Persistent)",
                    10 to "10 Seconds",
                    30 to "30 Seconds",
                    60 to "1 Minute",
                    86400 to "24 Hours"
                )

                for ((sec, label) in options) {
                    val isSelected = currentSeconds == sec
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) CyberYellow.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { onSelect(sec) }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) CyberYellow else Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                        if (isSelected) {
                            Text(
                                text = "ACTIVE",
                                color = CyberYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
