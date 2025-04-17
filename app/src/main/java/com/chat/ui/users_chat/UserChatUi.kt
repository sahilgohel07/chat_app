package com.chat.ui.users_chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chat.R
import com.chat.databinding.FragmentUserChatUiBinding
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide


class UserChatUi : Fragment() {

    private lateinit var uiBinding: FragmentUserChatUiBinding

    private var image:Int = 0
    private var username: String = ""
    private lateinit var viewModel: UserChatVM

    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        uiBinding = FragmentUserChatUiBinding.inflate(layoutInflater)
        username = arguments?.getString("username") ?: ""
        image = arguments?.getInt("image") ?: 0
        return uiBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[UserChatVM::class.java]

        viewModel.getAllRecords(username)
        viewModel.getAllUnSyncedChatMessages(username)

        adapter = ChatAdapter()

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerChat)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }

        viewModel.messages.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            recycler.scrollToPosition(it.size - 1)
        }

        /*uiBinding.username.text = username*/
        viewModel.connectToPieSocket(requireContext())

        Glide.with(requireContext())
            .load(image)
            .into(uiBinding.imageViewUser)

        uiBinding.textViewUserName.text = username

        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                /*launch {
                    viewModel.mssgFlow.collect{
                        if (!it.isNullOrBlank()) {
                            viewModel.mockReceiveMessage(it)
                        }
                    }
                }*/
                launch {
                    viewModel.isConnected.collect { isConnected ->
                        if (!isConnected) {
                            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        uiBinding.buttonSend.setOnClickListener {
            val msg = uiBinding.editMessage.text.toString().trim()
            if (msg.isNotEmpty()) {
                viewModel.sendMessage(msg, username)
                uiBinding.editMessage.setText("")

                // optional mock receiver
                /*Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.mockReceiveMessage("Reply to: $msg")
                }, 1000)*/
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        val supportActionBar: ActionBar? = (requireActivity() as AppCompatActivity).supportActionBar
        if (supportActionBar != null) supportActionBar.hide()
        requireActivity().findViewById<AppBarLayout>(R.id.appBar).visibility = View.GONE
        super.onResume()
    }

    override fun onDestroy() {
        viewModel.webSocket.close(1000,"App closed")
        viewModel.webSocket.cancel()
        super.onDestroy()
    }
}