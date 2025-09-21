package com.example.nexwork.ui.services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nexwork.R
import com.example.nexwork.data.model.Service

class ServiceAdapter(private val onClick: (Service) -> Unit) :
    ListAdapter<Service, ServiceAdapter.ServiceViewHolder>(ServiceDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ServiceViewHolder(itemView: View, val onClick: (Service) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val ratingTextView: TextView = itemView.findViewById(R.id.service_rating)
        private val nameTextView: TextView = itemView.findViewById(R.id.service_name)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.service_description)
        private val priceTextView: TextView = itemView.findViewById(R.id.service_price)
        private val imageView: ImageView = itemView.findViewById(R.id.service_image)
        private var currentService: Service? = null

        init {
            itemView.setOnClickListener {
                currentService?.let {
                    onClick(it)
                }
            }
        }

        fun bind(service: Service) {
            currentService = service
            ratingTextView.text = service.rating.toString()
            nameTextView.text = service.name
            descriptionTextView.text = service.description
            priceTextView.text = "$${service.price}"
            // Aquí se usaría Glide o Picasso para cargar la imagen:
            // Glide.with(itemView.context).load(service.imageUrl).into(imageView)
        }
    }
}

object ServiceDiffCallback : DiffUtil.ItemCallback<Service>() {
    override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean {
        return oldItem == newItem
    }
}