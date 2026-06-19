package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ChatConversation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val modelId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val role: String, // "user", "model", or "system"
    val text: String,
    val thought: String? = null, // Will store thoughts streamed by Gemini 2.0/Thinking models
    val thinkingDuration: Long? = null, // Time elapsed in seconds during active thought streaming
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)
