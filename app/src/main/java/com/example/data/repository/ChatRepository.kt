package com.example.data.repository

import com.example.data.database.ChatConversation
import com.example.data.database.ChatMessage
import com.example.data.database.ChatDao
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {
    val allConversations: Flow<List<ChatConversation>> = chatDao.getAllConversations()

    fun getMessagesForConversation(conversationId: Long): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForConversation(conversationId)
    }

    suspend fun getMessagesForConversationDirect(conversationId: Long): List<ChatMessage> {
        return chatDao.getMessagesForConversationDirect(conversationId)
    }

    suspend fun insertConversation(conversation: ChatConversation): Long {
        return chatDao.insertConversation(conversation)
    }

    suspend fun insertMessage(message: ChatMessage): Long {
        return chatDao.insertMessage(message)
    }

    suspend fun deleteConversation(conversationId: Long) {
        chatDao.deleteMessagesForConversation(conversationId)
        chatDao.deleteConversation(conversationId)
    }

    suspend fun updateConversationTitle(conversationId: Long, title: String) {
        chatDao.updateConversationTitle(conversationId, title)
    }
}
