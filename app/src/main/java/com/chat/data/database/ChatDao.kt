package com.chat.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chat.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    /*@Query("SELECT * FROM chat_messages ORDER BY time ASC")*/
    @Query("SELECT * FROM chat_messages WHERE username = :username ORDER BY time ASC")
    fun getAllMessages(username:String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE isSynced = 0 AND username = :username")
    fun getAllUnSyncedMessage(username: String): List<ChatMessage>

    @Query("UPDATE chat_messages SET isSynced = 1 WHERE id = :userId")
    suspend fun updateChatSyncStatus(userId: Int)

    @Query("SELECT * FROM chat_messages WHERE username = :username ORDER BY time DESC LIMIT 1")
    suspend fun getLatestMessageForUser(username: String): ChatMessage?
}
