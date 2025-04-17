package com.chat.ui.home_users

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.chat.R
import com.chat.data.ChatRepository
import com.chat.data.database.ChatDatabase
import com.chat.data.model.ChatMessage
import com.chat.models.Users
import com.chat.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class HomeUserVM(
    val context: Application
): AndroidViewModel(context) {

    private var usersItems: MutableList<Users> = mutableListOf(Users())
    val dao = ChatDatabase.getDatabase(context).chatDao()
    private val repository: ChatRepository = ChatRepository(dao)

    private var _userSupportMessage: MutableLiveData<String> = MutableLiveData()
    val userSupportMessage: LiveData<String> = _userSupportMessage

    private var _userSalesMessage: MutableLiveData<String> = MutableLiveData()
    val userSalesMessage: LiveData<String> = _userSalesMessage

    private var _userFaqMessage: MutableLiveData<String> = MutableLiveData()
    val userFaqMessage: LiveData<String> = _userFaqMessage

    fun getLatestMessage() {
        CoroutineScope(Dispatchers.IO).launch {
            _userSupportMessage.postValue(repository.getLatestMessageByUser("Support")?.message)
            _userSalesMessage.postValue(repository.getLatestMessageByUser("Sales")?.message)
            _userFaqMessage.postValue(repository.getLatestMessageByUser("FAQ")?.message)
        }
    }

    fun storeListOfUsers() {
        usersItems.clear()

        usersItems.addAll(
            listOf(
                Users(image = R.drawable.support_user, name = "Support"),
                Users(image = R.drawable.sales_user, name = "Sales"),
                Users(image = R.drawable.faq, name = "FAQ"),
            )
        )
    }

    fun getListOfUsers(): List<Users> {
        return usersItems
    }

    private var countdownJob: Job? = null
    var shouldRun = false
    private val _networkFlow = MutableStateFlow("")
    val networkFlow: StateFlow<String> = _networkFlow
    var isConnected = true

    /*private fun startCountdown(context: Context) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (shouldRun) {
                isUpdate(context)
                delay(1000L)
            }
        }
    }

    private fun isUpdate(context: Context) {
        isConnected = Utils.isInternetAvailable(context)
        if (isConnected){
            shouldRun = false
            _networkFlow.value = "Connected"
        } else {
            shouldRun = true
        }
    }

    fun stopGenerating() {
        shouldRun = false
        countdownJob?.cancel()
    }*/
}