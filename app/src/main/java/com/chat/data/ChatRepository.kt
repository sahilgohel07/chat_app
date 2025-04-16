package com.chat.data

import com.chat.data.database.ChatDao
import com.chat.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val dao: ChatDao) {

    val allMessages: Flow<List<ChatMessage>> = dao.getAllMessages()

    suspend fun sendMessage(message: ChatMessage) {
        dao.insertMessage(message)
    }
}
