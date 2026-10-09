package com.example.data.repository

import com.example.data.db.ChatMessageDao
import com.example.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class ChatRepository(private val chatMessageDao: ChatMessageDao) {
    val allMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()

    suspend fun sendMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(message)
    }

    suspend fun deleteMessage(id: String) {
        chatMessageDao.deleteMessageById(id)
    }

    suspend fun purgeAll() {
        chatMessageDao.deleteAllMessages()
    }

    suspend fun deleteExpired(now: Long = System.currentTimeMillis()) {
        chatMessageDao.deleteExpiredMessages(now)
    }

    suspend fun toggleReaction(messageId: String, emoji: String) {
        val currentMessages = chatMessageDao.getAllMessages().firstOrNull() ?: return
        val target = currentMessages.find { it.id == messageId } ?: return
        
        // Format: "🔥:2,🤫:1"
        val map = parseReactions(target.reactionsJson)
        val currentCount = map[emoji] ?: 0
        val newMap = map.toMutableMap()
        if (currentCount > 0) {
            newMap[emoji] = currentCount + 1
        } else {
            newMap[emoji] = 1
        }
        val serialized = newMap.entries.joinToString(",") { "${it.key}:${it.value}" }
        chatMessageDao.updateReactions(messageId, serialized)
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

    suspend fun checkAndSeedInitialPeerMessages() {
        if (chatMessageDao.getCount() == 0) {
            val now = System.currentTimeMillis()
            val initial = listOf(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderHandle = "Ghost_x3p",
                    senderColorHex = 0xFF00F0FF,
                    isMe = false,
                    content = "Room initialized. All traffic routed through zero-knowledge relays. Welcome to #APAP.",
                    messageType = "TEXT",
                    timestamp = now - 1000 * 60 * 18,
                    reactionsJson = "🤫:3,⚡:2"
                ),
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderHandle = "Cipher_91f",
                    senderColorHex = 0xFF10B981,
                    isMe = false,
                    content = "Remember: Panic button instantly returns to innocent QuickNote view. Trigger #APAP in any note title to return.",
                    messageType = "TEXT",
                    timestamp = now - 1000 * 60 * 12,
                    reactionsJson = "🔥:4"
                ),
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderHandle = "NeonDrifter",
                    senderColorHex = 0xFFFF0055,
                    isMe = false,
                    content = "Encrypted voice channel is ready. Tap the call icon in the top bar to connect to the group comms.",
                    messageType = "TEXT",
                    timestamp = now - 1000 * 60 * 5,
                    reactionsJson = "💀:1,⚡:3"
                ),
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderHandle = "Phantom_8b",
                    senderColorHex = 0xFF8B5CF6,
                    isMe = false,
                    content = "Voice note test transmission below:",
                    messageType = "TEXT",
                    timestamp = now - 1000 * 60 * 2
                ),
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderHandle = "Phantom_8b",
                    senderColorHex = 0xFF8B5CF6,
                    isMe = false,
                    content = "Encrypted audio packet [0:06]",
                    messageType = "VOICE",
                    mediaDurationSec = 6,
                    waveformSample = "0.2,0.6,0.9,0.5,0.8,0.7,0.4,0.9,0.3,0.5,0.8,0.2",
                    timestamp = now - 1000 * 60 * 2,
                    reactionsJson = "🔥:2"
                )
            )
            chatMessageDao.insertMessages(initial)
        }
    }
}
