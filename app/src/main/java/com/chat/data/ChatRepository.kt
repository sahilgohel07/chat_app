package com.chat.data

import com.chat.data.database.ChatDao
import com.chat.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val dao: ChatDao) {

    /*val allMessages: Flow<List<ChatMessage>> = dao.getAllMessages()*/

    fun getAllRecordsByUsername(uName:String) : Flow<List<ChatMessage>>{
        return dao.getAllMessages(username = uName)
    }

    suspend fun sendMessage(message: ChatMessage) {
        dao.insertMessage(message)
    }

    suspend fun getAllUnSyncMessages(username:String):List<ChatMessage> {
        return dao.getAllUnSyncedMessage(username)
    }

    suspend fun updateIsSynced(userId:Int) {
        dao.updateChatSyncStatus(userId)
    }

    suspend fun getLatestMessageByUser(username: String): ChatMessage?{
        return dao.getLatestMessageForUser(username)
    }
}
