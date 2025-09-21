package com.example.nexwork.ui.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nexwork.R
import com.example.nexwork.data.model.Order

class OrdersAdapter : ListAdapter<Order, OrdersAdapter.OrderViewHolder>(OrdersDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serviceNameTextView: TextView = itemView.findViewById(R.id.service_name)
        private val clientNameTextView: TextView = itemView.findViewById(R.id.client_name)
        private val dateTextView: TextView = itemView.findViewById(R.id.order_date)
        private val timeTextView: TextView = itemView.findViewById(R.id.order_time)
        private val imageView: ImageView = itemView.findViewById(R.id.order_image)
        private val detailsButton: Button = itemView.findViewById(R.id.view_details_button)

        fun bind(order: Order) {
            serviceNameTextView.text = order.serviceName
            clientNameTextView.text = "Cliente: ${order.clientName}"
            dateTextView.text = "Fecha: ${order.date}"
            timeTextView.text = "Horario: ${order.time}"
            // TODO: Load image with Glide or Picasso
        }
    }
}

class OrdersDiffCallback : DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem == newItem
    }
}