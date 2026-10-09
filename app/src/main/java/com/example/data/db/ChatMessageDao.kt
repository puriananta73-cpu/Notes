package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessage>)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)

    @Query("DELETE FROM chat_messages WHERE expiresAt > 0 AND expiresAt <= :currentTime")
    suspend fun deleteExpiredMessages(currentTime: Long)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()

    @Query("UPDATE chat_messages SET reactionsJson = :reactions WHERE id = :id")
    suspend fun updateReactions(id: String, reactions: String)

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getCount(): Int
}
