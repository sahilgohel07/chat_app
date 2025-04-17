package com.chat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val message: String,
    val time: Long,
    val isSentByUser: Boolean, // true = sent, false = received
    val userFlag: String,
    val isSynced: Boolean,
    val isRead: Boolean
)
