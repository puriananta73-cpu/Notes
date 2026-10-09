package com.example.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ChatMessage
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNeonGreen
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberYellow
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isPlayingAudio: Boolean,
    onToggleAudio: () -> Unit,
    onReactionClick: (String) -> Unit,
    onDeleteMessage: () -> Unit
) {
    var showActionMenu by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf(false) }
    val formattedTime = remember(message.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    // Ephemeral self-destruct remaining seconds calculator
    var remainingSeconds by remember(message.expiresAt) {
        mutableLongStateOf(
            if (message.expiresAt > 0) {
                ((message.expiresAt - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
            } else 0L
        )
    }

    LaunchedEffect(message.expiresAt) {
        if (message.expiresAt > 0) {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds = ((message.expiresAt - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
            }
        }
    }

    val bubbleBg = if (message.isMe) {
        Color(0xFF1E293B)
    } else {
        CyberCard
    }

    val borderColor = if (message.isMe) {
        CyberCyan.copy(alpha = 0.35f)
    } else {
        Color(0xFF334155)
    }

    // Alignment Row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start
        ) {
            // Sender Handle & Timestamp Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (message.isMe) "YOU (${message.senderHandle})" else message.senderHandle,
                    color = Color(message.senderColorHex),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedTime,
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )

                if (message.selfDestructSeconds > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${remainingSeconds}s",
                            color = Color(0xFFEF4444),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Message Bubble
            Box {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isMe) 16.dp else 4.dp,
                        bottomEnd = if (message.isMe) 4.dp else 16.dp
                    ),
                    color = bubbleBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .combinedClickable(
                            onClick = { /* normal tap */ },
                            onLongClick = { showActionMenu = true }
                        )
                        .testTag("chat_message_${message.id}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        when (message.messageType) {
                            "IMAGE", "DOODLE" -> {
                                if (!message.mediaUri.isNullOrEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .size(width = 220.dp, height = 180.dp)
                                            .background(Color.Black)
                                            .combinedClickable(onClick = { showFullImage = true })
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(message.mediaUri)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Shared Media",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(width = 220.dp, height = 180.dp)
                                        )
                                    }
                                }
                                if (message.content.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = message.content,
                                        color = Color(0xFFF1F5F9),
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            "VIDEO" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .size(width = 220.dp, height = 140.dp)
                                        .background(Color(0xFF0F172A))
                                        .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircleFilled,
                                        contentDescription = "Play Video",
                                        tint = CyberCyan,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "Intel Video Packet",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 8.dp)
                                    )
                                }
                            }

                            "VOICE" -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    IconButton(
                                        onClick = onToggleAudio,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(CyberCyan)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlayingAudio) "Pause" else "Play",
                                            tint = Color.Black,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Waveform bars
                                    WaveformBars(
                                        waveformStr = message.waveformSample,
                                        isPlaying = isPlayingAudio
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "0:${message.mediaDurationSec.toString().padStart(2, '0')}",
                                        color = CyberCyan,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            "STICKER" -> {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0A0E17),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = message.content,
                                            color = CyberCyan,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            else -> {
                                Text(
                                    text = message.content,
                                    color = Color(0xFFF8FAFC),
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                // Quick Action & Reaction Popup
                DropdownMenu(
                    expanded = showActionMenu,
                    onDismissRequest = { showActionMenu = false },
                    modifier = Modifier.background(CyberCard)
                ) {
                    // Reactions Row inside menu
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val reactionOptions = listOf("🔥", "🤫", "💀", "⚡", "🖤", "👁️")
                        for (emoji in reactionOptions) {
                            Text(
                                text = emoji,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .combinedClickable(onClick = {
                                        showActionMenu = false
                                        onReactionClick(emoji)
                                    })
                                    .padding(4.dp)
                            )
                        }
                    }

                    DropdownMenuItem(
                        text = { Text("Delete Transmission", color = Color(0xFFEF4444)) },
                        onClick = {
                            showActionMenu = false
                            onDeleteMessage()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFEF4444)
                            )
                        }
                    )
                }
            }

            // Reactions badges below message
            if (message.reactionsJson.isNotBlank()) {
                val reactions = parseReactions(message.reactionsJson)
                if (reactions.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        for ((emoji, count) in reactions) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F172A),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .combinedClickable(onClick = { onReactionClick(emoji) })
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = emoji, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = count.toString(),
                                        color = CyberCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Full screen image preview dialog
    if (showFullImage && !message.mediaUri.isNullOrEmpty()) {
        Dialog(onDismissRequest = { showFullImage = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .combinedClickable(onClick = { showFullImage = false })
                    .padding(8.dp)
            ) {
                AsyncImage(
                    model = message.mediaUri,
                    contentDescription = "Full Image View",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun WaveformBars(
    waveformStr: String,
    isPlaying: Boolean
) {
    val samples = remember(waveformStr) {
        val list = waveformStr.split(",").mapNotNull { it.trim().toFloatOrNull() }
        if (list.isEmpty()) listOf(0.3f, 0.7f, 0.5f, 0.9f, 0.4f, 0.6f, 0.8f, 0.3f) else list
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(24.dp)
    ) {
        for (i in samples.indices) {
            val height = (samples[i] * 22f).coerceIn(4f, 24f).dp
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isPlaying) CyberNeonGreen else CyberCyan)
            )
        }
    }
}

private fun parseReactions(json: String): Map<String, Int> {
    if (json.isBlank()) return emptyMap()
    return try {
        json.split(",")
            .mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) parts[0].trim() to (parts[1].trim().toIntOrNull() ?: 1) else null
            }.toMap()
    } catch (_: Exception) {
        emptyMap()
    }
}
