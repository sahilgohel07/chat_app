package com.chat.ui.home_users

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chat.databinding.UserItemLayoutBinding
import com.chat.models.Users

class UsersListAdapter (
    private val usersList: List<Users>,
    private val context: Context
): RecyclerView.Adapter<UsersListAdapter.ViewHolder>(){

    private lateinit var _binding: UserItemLayoutBinding

    private var listener: OnItemClickListener? = null

    inner class ViewHolder(val binding: UserItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        _binding = UserItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(_binding)
    }

    override fun onBindViewHolder(holder: UsersListAdapter.ViewHolder, position: Int) {
        /*Load image using Glide*/
        Glide.with(context)
            .load(usersList[position].image)
            .into(holder.binding.imageViewUser)

        /*set user name*/
        holder.binding.textViewUserName.text = usersList[position].name

        holder.itemView.setOnClickListener {
            listener?.onItemClick(usersList[position]) // Notify the listener when an item is clicked
        }
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    interface OnItemClickListener {
        fun onItemClick(user: Users) // Pass the clicked item (e.g., User object)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

}