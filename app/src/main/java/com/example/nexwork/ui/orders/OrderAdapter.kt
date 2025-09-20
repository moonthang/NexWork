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

class PedidosAdapter : ListAdapter<Order, PedidosAdapter.PedidoViewHolder>(PedidosDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = getItem(position)
        holder.bind(pedido)
    }

    class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serviceNameTextView: TextView = itemView.findViewById(R.id.servicio_nombre)
        private val clientNameTextView: TextView = itemView.findViewById(R.id.cliente_nombre)
        private val dateTextView: TextView = itemView.findViewById(R.id.pedido_fecha)
        private val timeTextView: TextView = itemView.findViewById(R.id.pedido_horario)
        private val imageView: ImageView = itemView.findViewById(R.id.pedido_imagen)
        private val detailsButton: Button = itemView.findViewById(R.id.ver_detalles_button)

        fun bind(order: Order) {
            serviceNameTextView.text = order.serviceName
            clientNameTextView.text = "Cliente: ${order.clientName}"
            dateTextView.text = "Fecha: ${order.date}"
            timeTextView.text = "Horario: ${order.time}"
            // TODO imagenes (Depronto es necesario la instlacion de dependencias))
        }
    }
}

class PedidosDiffCallback : DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem == newItem
    }
}