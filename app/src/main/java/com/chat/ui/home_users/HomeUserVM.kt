package com.chat.ui.home_users

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.R
import com.chat.models.Users
import com.chat.utils.Utils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class HomeUserVM:ViewModel() {

    private var usersItems: MutableList<Users> = mutableListOf(Users())

    fun storeListOfUsers() {
        usersItems.clear()

        usersItems.addAll(
            listOf(
                Users(image = R.drawable.support_user, name = "Support"),
               /* Users(image = R.drawable.sales_user, name = "Sales"),
                Users(image = R.drawable.faq, name = "FAQ"),*/
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