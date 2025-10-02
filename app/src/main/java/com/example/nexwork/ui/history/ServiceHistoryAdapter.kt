package com.example.nexwork.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexwork.R
import com.example.nexwork.data.model.ServiceHistory
import com.example.nexwork.data.model.ServiceStatus
import com.example.nexwork.databinding.ItemServiceHistoryBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ServiceHistoryAdapter(
    private var historyList: List<ServiceHistory>
) : RecyclerView.Adapter<ServiceHistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemServiceHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size

    fun updateData(newHistoryList: List<ServiceHistory>) {
        historyList = newHistoryList
        notifyDataSetChanged()
    }

    class HistoryViewHolder(private val binding: ItemServiceHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())

        fun bind(item: ServiceHistory) {
            binding.tvServiceName.text = item.serviceName
            binding.tvClient.text = itemView.context.getString(R.string.client_format, item.clientName)
            binding.tvProvider.text = itemView.context.getString(R.string.provider_format, item.providerName)
            binding.tvDate.text = itemView.context.getString(R.string.date_format, dateFormat.format(item.date))
            
            val context = itemView.context
            val statusText: String
            val statusColor: Int

            when (item.status) {
                ServiceStatus.PENDIENTE -> {
                    statusText = context.getString(R.string.status_pending)
                    statusColor = ContextCompat.getColor(context, R.color.status_pending)
                }
                ServiceStatus.COMPLETADO -> {
                    statusText = context.getString(R.string.status_completed)
                    statusColor = ContextCompat.getColor(context, R.color.status_completed)
                }
                ServiceStatus.CANCELADO -> {
                    statusText = context.getString(R.string.status_cancelled)
                    statusColor = ContextCompat.getColor(context, R.color.status_cancelled)
                }
            }
            binding.tvStatus.text = context.getString(R.string.status_format, statusText)
            binding.tvStatus.setTextColor(statusColor)

            Glide.with(context)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_launcher_background) // Imagen de placeholder
                .into(binding.ivServiceImage)
        }
    }
}