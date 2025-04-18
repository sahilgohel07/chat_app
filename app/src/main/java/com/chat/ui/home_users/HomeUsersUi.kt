package com.chat.ui.home_users

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chat.R
import com.chat.databinding.FragmentHomeUsersUiBinding
import com.chat.models.Users
import com.chat.ui.users_chat.UserChatUi
import com.chat.ui.users_chat.UserChatVM
import com.chat.utils.Utils
import com.google.android.material.appbar.AppBarLayout

class HomeUsersUi : Fragment(), UsersListAdapter.OnItemClickListener {

    private lateinit var uiBinding: FragmentHomeUsersUiBinding
    /*private val viewModel: HomeUserVM by viewModels()*/
    private lateinit var viewModel: HomeUserVM
    private lateinit var adapter: UsersListAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        uiBinding = FragmentHomeUsersUiBinding.inflate(layoutInflater)
        return uiBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[HomeUserVM::class.java]
        viewModel.storeListOfUsers()
        viewModel.getLatestMessage()

        adapter = UsersListAdapter(viewModel.getListOfUsers(),requireContext())

        viewModel.userSupportMessage.observe(viewLifecycleOwner){
            if (it!=null){
                adapter.provideSupportMessage(it.trim())
            }
        }

        viewModel.userSalesMessage.observe(viewLifecycleOwner){
            if (it!=null){
                adapter.provideSalesMessage(it.trim())
            }
        }

        viewModel.userFaqMessage.observe(viewLifecycleOwner){
            if (it!=null){
                adapter.provideFaqMessage(it.trim())
            }
        }

        uiBinding.recyclerViewUsers.layoutManager = LinearLayoutManager(requireContext())
        adapter.setOnItemClickListener(this)
        uiBinding.recyclerViewUsers.adapter = adapter


        super.onViewCreated(view, savedInstanceState)
    }

    override fun onItemClick(user: Users) {
        if (Utils.isInternetAvailable(requireContext())) {
            val newUi = UserChatUi()
            val bundle = Bundle().apply {
                putInt("image", user.image!!)
                putString("username", user.name)
            }
            newUi.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fView, newUi)
                .addToBackStack(null)
                .commit()
        }else{
            Toast.makeText(requireContext(),"You're offline. Connect to the internet to continue.",Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        requireActivity().findViewById<AppBarLayout>(R.id.appBar).visibility = View.VISIBLE
        super.onResume()
    }
}