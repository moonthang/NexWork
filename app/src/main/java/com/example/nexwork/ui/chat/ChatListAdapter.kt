package com.example.nexwork.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexwork.R
import com.example.nexwork.databinding.ItemChatBinding

class ChatListAdapter(
    private var chats: List<ChatWithUser>,
    private val onItemClick: (ChatWithUser) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        // Obtén el chat en la posición actual
        val chat = chats[position]
        holder.bind(chat)
        holder.itemView.setOnClickListener { onItemClick(chat) }
    }

    override fun getItemCount(): Int = chats.size

    fun updateData(newChats: List<ChatWithUser>) {
        // Actualiza los datos y notifica al adaptador
        chats = newChats
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chatWithUser: ChatWithUser) {
            // Configura la vista con los datos del chat
            binding.tvChatName.text = "${chatWithUser.otherUser.firstName} ${chatWithUser.otherUser.lastName}"
            binding.tvLastMessage.text = chatWithUser.chat.lastMessage ?: "No hay mensajes aún."

            // Carga la imagen del usuario
            Glide.with(binding.root.context)
                .load(chatWithUser.otherUser.profileImageUrl)
                .placeholder(R.drawable.circle_background)
                .into(binding.ivProfile)
        }
    }
}