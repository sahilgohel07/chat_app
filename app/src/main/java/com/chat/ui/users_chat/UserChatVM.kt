package com.chat.ui.users_chat

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.chat.data.ChatRepository
import com.chat.data.database.ChatDatabase
import com.chat.data.model.ChatMessage
import com.chat.utils.NetworkStatusTracker
import com.chat.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class UserChatVM(
    val context: Application
): AndroidViewModel(context) {
    lateinit var webSocket: WebSocket

    private val _mssgFlow = MutableStateFlow("")
    val mssgFlow: StateFlow<String> = _mssgFlow

    lateinit var messages: LiveData<List<ChatMessage>>
    val dao = ChatDatabase.getDatabase(context).chatDao()
    private val repository: ChatRepository = ChatRepository(dao)

    private var networkStatusTracker: NetworkStatusTracker = NetworkStatusTracker(context)
    var isConnected: Flow<Boolean> = networkStatusTracker.networkStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    private lateinit var nUsername: String

    private lateinit var listOfUnSyncChat:List<ChatMessage>

    var userActive: Boolean = false

    /*init {
        val dao = ChatDatabase.getDatabase(context).chatDao()
        repository = ChatRepository(dao)
        messages = repository.getAllRecordsByUsername("Support").asLiveData()
    }*/

    fun getAllRecords(username: String) {
        nUsername = username
        userActive = true
        Log.d("UserChatVM","The GetAllRecords: $username")
        messages = repository.getAllRecordsByUsername(nUsername).asLiveData()
    }

    fun sendMessage(content: String, username: String) {
        viewModelScope.launch {
            if(Utils.isInternetAvailable(context)){

                    val msg = ChatMessage(
                        username = username,
                        message = content,
                        time = System.currentTimeMillis(),
                        userFlag = "You",
                        isSentByUser = true,
                        isSynced = true,
                        isRead = userActive
                    )
                    if (::webSocket.isInitialized) {
                        webSocket.send("$username: $content")
                        repository.sendMessage(msg)
                    }

            } else{
                val msg = ChatMessage(
                    username = username,
                    message = content,
                    time = System.currentTimeMillis(),
                    userFlag = "You",
                    isSentByUser = true,
                    isSynced = false,
                    isRead = userActive
                )
                repository.sendMessage(msg)
            }
            /*isConnected.collect{

            }*/
        }

    }

    private fun mockReceiveMessage(content: String, username: String) {
        val msg = ChatMessage(
            username = username,
            message = content,
            time = System.currentTimeMillis(),
            userFlag = "Friend",
            isSentByUser = false,
            isSynced = true,
            isRead = userActive
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
                val parts = text.split(":", limit = 2)
                val userInfo = parts[0].trim()

                val user = userInfo.split(" ").first()

                if (text.isNotEmpty() && text.startsWith("$user Bot:")) {
                    storeMessageResponse(text)
                } /*else {
                    storeOnlyMessage(text)
                }*/
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

    fun storeMessageResponse(text: String) {
        val parts = text.split(":", limit = 2)

        Log.d("PieSocket", "storeMessageResponse: $text")

        if (parts.size == 2) {
            val userInfo = parts[0].trim()
            val message = parts[1].trim()
            /*_mssgFlow.value = message.trim()*/
            val user = userInfo.split(" ").first()
            mockReceiveMessage(message.trim(), user)
            println("Before Colon: $userInfo")
            println("After Colon: $message")
        }
    }


    /*fun storeOnlyMessage(message: String) {
        if (listOfUnSyncChat.isNotEmpty()) {
            for (i in listOfUnSyncChat) {
                if (nMessage == i.message) {
                    viewModelScope.launch {
                        repository.updateIsSynced(i.id)
                    }
                }
            }
        }
    }*/

    fun getAllUnSyncedChatMessages(username: String) {
        CoroutineScope(Dispatchers.IO).launch {
            listOfUnSyncChat = repository.getAllUnSyncMessages(username = username)
            sendUnSyncedMessages()
        }
    }

    /*When internet comes then call this method for send message to socket*/
    private fun sendUnSyncedMessages() {
        if (listOfUnSyncChat.isNotEmpty()){
            listOfUnSyncChat.forEach {
                /*sendMessage(it.message, it.username)*/
                webSocket.send("${it.username}: ${it.message}")
                viewModelScope.launch {
                    repository.updateIsSynced(it.id)
                }
            }
        }
    }
}