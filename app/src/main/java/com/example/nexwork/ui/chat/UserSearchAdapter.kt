package com.example.nexwork.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexwork.R
import com.example.nexwork.data.model.User
import com.example.nexwork.databinding.ItemChatBinding

class UserSearchAdapter(
    private var users: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UserSearchAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        // Obtén el usuario en la posición actual del adaptador
        val user = users[position]
        holder.bind(user)
        holder.itemView.setOnClickListener { onItemClick(user) }
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    inner class UserViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvChatName.text = "${user.firstName} ${user.lastName}"
            binding.tvLastMessage.text = user.email

            // Carga la imagen del usuario
            Glide.with(binding.root.context)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.circle_background)
                .into(binding.ivProfile)
        }
    }
}