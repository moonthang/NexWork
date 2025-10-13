package com.example.nexwork.ui.services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexwork.R
import com.example.nexwork.data.model.Service
import java.text.NumberFormat
import java.util.Locale

class ServiceAdapter(private val onClick: (Service) -> Unit) :
    ListAdapter<Service, ServiceAdapter.ServiceViewHolder>(ServiceDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        // Infla el layout para crear la lista de servicios
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        // Vincula los datos con la vista
        holder.bind(getItem(position))
    }

    class ServiceViewHolder(itemView: View, val onClick: (Service) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val favoriteLayout: LinearLayout = itemView.findViewById(R.id.favorite_layout)
        private val ratingLayout: LinearLayout = itemView.findViewById(R.id.rating_layout)
        private val nameTextView: TextView = itemView.findViewById(R.id.service_name)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.service_description)
        private val priceTextView: TextView = itemView.findViewById(R.id.service_price)
        private val imageView: ImageView = itemView.findViewById(R.id.service_image)
        private var currentService: Service? = null

        // Configura el click listener
        init {
            itemView.setOnClickListener {
                currentService?.let {
                    onClick(it)
                }
            }
        }

        // Vincula los datos con la vista
        fun bind(service: Service) {
            currentService = service
            nameTextView.text = service.title
            ratingLayout.visibility = View.GONE
            favoriteLayout.visibility = View.GONE

            // Obtener el primer plan de la lista 'Sencillo'
            val simplePlan = service.plans.getOrNull(0)

            if (simplePlan != null) {
                descriptionTextView.text = simplePlan.planDescription
                val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                format.maximumFractionDigits = 0
                priceTextView.text = format.format(simplePlan.price)
            } else {
                // Si no hay plan 'Sencillo', mostrar la descripción principal
                descriptionTextView.text = service.description
                priceTextView.text = ""
            }

            // Cargar la imagen
            if (service.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(service.imageUrl[0])
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imageView)
            } else {
                // Si no hay imagen, muestra una imagen generica
                 imageView.setImageResource(R.drawable.ic_launcher_background)
            }
        }
    }
}

// Optimizador la lista de servicios
object ServiceDiffCallback : DiffUtil.ItemCallback<Service>() {
    // Compara si los objetos Service son iguales por ID
    override fun areItemsTheSame(oldItem: Service, newItem: Service): Boolean {
        return oldItem.serviceId == newItem.serviceId
    }

    // Compara si los contenidos de los objetos Service son iguales
    override fun areContentsTheSame(oldItem: Service, newItem: Service): Boolean {
        return oldItem == newItem
    }
}