package com.example.nexwork.ui.chat

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexwork.R
import com.example.nexwork.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private var items: List<Any>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val VIEW_TYPE_DATE = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is Message -> if (item.senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
            is Date -> VIEW_TYPE_DATE
            else -> throw IllegalArgumentException("Tipo de datos no válido en la posición $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            // Vista para mensajes enviados
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sent_message, parent, false)
                SentMessageViewHolder(view)
            }
            // Vista para mensajes recibidos
            VIEW_TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_received_message, parent, false)
                ReceivedMessageViewHolder(view)
            }
            // Vista para fechas
            VIEW_TYPE_DATE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_separator, parent, false)
                DateViewHolder(view)
            }
            else -> throw IllegalArgumentException("Tipo de vista no válido")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SentMessageViewHolder -> holder.bind(items[position] as Message)
            is ReceivedMessageViewHolder -> holder.bind(items[position] as Message)
            is DateViewHolder -> holder.bind(items[position] as Date)
        }
    }

    override fun getItemCount(): Int = items.size

    // Actualiza los datos del adaptador
    fun updateData(newMessages: List<Message>) {
        val newItems = mutableListOf<Any>()
        var lastDate: Calendar? = null

        for (message in newMessages) {
            val messageDate = Calendar.getInstance().apply { time = message.timestamp }
            if (lastDate == null || !isSameDay(lastDate!!, messageDate)) {
                newItems.add(message.timestamp)
                lastDate = messageDate
            }
            newItems.add(message)
        }
        this.items = newItems
        notifyDataSetChanged()
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    // ViewHolder para mensajes enviados
    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tvMessage)
        private val timestampText: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(message: Message) {
            messageText.text = message.text
            timestampText.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp)
        }
    }

    // ViewHolder para mensajes recibidos
    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tvMessage)
        private val timestampText: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(message: Message) {
            messageText.text = message.text
            timestampText.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp)
        }
    }

    // ViewHolder para separadores de fecha
    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(date: Date) {
            dateText.text = when {
                DateUtils.isToday(date.time) -> "Hoy"
                DateUtils.isToday(date.time + DateUtils.DAY_IN_MILLIS) -> "Ayer"
                else -> SimpleDateFormat("d 'de' MMMM", Locale.getDefault()).format(date)
            }
        }
    }
}