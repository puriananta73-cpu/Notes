package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey
    val id: String,
    val senderHandle: String,
    val senderColorHex: Long = 0xFF00F0FF,
    val isMe: Boolean = false,
    val content: String = "",
    val messageType: String = "TEXT", // TEXT, IMAGE, VOICE, DOODLE, VIDEO, STICKER
    val mediaUri: String? = null,
    val mediaDurationSec: Int = 0,
    val waveformSample: String = "0.2,0.5,0.8,0.4,0.9,0.7,0.3,0.6,0.5,0.8,0.4,0.2",
    val reactionsJson: String = "", // e.g. "🔥:2,🤫:1"
    val selfDestructSeconds: Int = 0, // 0 means persistent
    val expiresAt: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
