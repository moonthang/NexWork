package com.example.nexwork.ui.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexwork.R
import com.example.nexwork.data.model.Order
import java.text.SimpleDateFormat
import java.util.Locale

class OrdersAdapter(private val listener: OnItemClickListener, private val isConfirmOrder: Boolean) : ListAdapter<Order, OrdersAdapter.OrderViewHolder>(OrdersDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serviceNameTextView: TextView = itemView.findViewById(R.id.service_name)
        private val clientNameTextView: TextView = itemView.findViewById(R.id.client_name)
        private val providerNameTextView: TextView = itemView.findViewById(R.id.provider_name)
        private val dateTextView: TextView = itemView.findViewById(R.id.order_date)
        private val timeTextView: TextView = itemView.findViewById(R.id.order_time)
        private val statusTextView: TextView = itemView.findViewById(R.id.status)
        private val imageView: ImageView = itemView.findViewById(R.id.order_image)

        fun bind(order: Order) {
            serviceNameTextView.text = order.titleService
            clientNameTextView.text = "Cliente: ${order.clientName}"
            providerNameTextView.text = "Proveedor: ${order.providerName}"
            statusTextView.text = "Estatus: ${order.status}"
            dateTextView.text = "Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(order.date)}"
            timeTextView.text = "Horario: ${order.time}"

            if (order.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(order.imageUrl)
                    .into(imageView)
            }

            if(isConfirmOrder){
                providerNameTextView.visibility = View.GONE
                statusTextView.visibility = View.GONE
            }

            itemView.setOnClickListener { listener.onItemClick(order) }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(order: Order)
    }
}

class OrdersDiffCallback : DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem.orderId == newItem.orderId
    }

    override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem == newItem
    }
}