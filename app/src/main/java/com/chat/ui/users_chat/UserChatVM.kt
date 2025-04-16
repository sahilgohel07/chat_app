package com.chat.ui.users_chat

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.chat.data.ChatRepository
import com.chat.data.database.ChatDatabase
import com.chat.data.model.ChatMessage
import com.chat.utils.NetworkStatusTracker
import com.chat.utils.Utils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class UserChatVM(
    context: Application
): AndroidViewModel(context) {
    lateinit var webSocket: WebSocket

    private val _mssgFlow = MutableStateFlow("")
    val mssgFlow: StateFlow<String> = _mssgFlow

    val messages: LiveData<List<ChatMessage>>
    private val repository: ChatRepository

    private var networkStatusTracker: NetworkStatusTracker = NetworkStatusTracker(context)
    var isConnected: Flow<Boolean> = networkStatusTracker.networkStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    init {
        val dao = ChatDatabase.getDatabase(context).chatDao()
        repository = ChatRepository(dao)
        messages = repository.allMessages.asLiveData()
    }

    fun sendMessage(content: String, username: String = "You") {
        val msg = ChatMessage(
            username = username,
            message = "user: $content",
            time = System.currentTimeMillis(),
            isSentByUser = true
        )

        viewModelScope.launch {
            isConnected.collect{
                if (it){
                    if (::webSocket.isInitialized) {
                        webSocket.send("user: $content")
                        repository.sendMessage(msg)
                    }
                } else{
                    repository.sendMessage(msg)
                }
            }
        }

    }

    fun mockReceiveMessage(content: String) {
        val msg = ChatMessage(
            username = "Friend",
            message = content,
            time = System.currentTimeMillis(),
            isSentByUser = false
        )
        viewModelScope.launch {
            repository.sendMessage(msg)
        }
    }

    fun connectToPieSocket(context: Context) {

        val url = "wss://s14463.blr1.piesocket.com/v3/1?api_key=oaxPFUcJjoJvW5eCXkLqLTmB85ofV7cylxZa1BSE&notify_self=1"

        val client = OkHttpClient.Builder()
            .pingInterval(10, TimeUnit.SECONDS) // Keep connection alive
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("PieSocket", "Connected!")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("PieSocket", "Received: $text")
                if (text.isNotEmpty() && !text.startsWith("user:")) {
                    _mssgFlow.value = text.trim()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("PieSocket", "Connection closed")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("PieSocket", "Error: ${t.message}")
            }
        }

        webSocket = client.newWebSocket(request, listener)
    }

}