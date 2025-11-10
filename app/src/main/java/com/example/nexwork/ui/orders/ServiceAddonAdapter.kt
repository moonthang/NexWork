package com.example.nexwork.ui.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexwork.R
import com.example.nexwork.data.model.ServiceAddon

class ServiceAddonAdapter(private var addons: List<ServiceAddon>) :
    RecyclerView.Adapter<ServiceAddonAdapter.AddonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_addon, parent, false)
        return AddonViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddonViewHolder, position: Int) {
        val addon = addons[position]
        holder.bind(addon)
    }

    override fun getItemCount(): Int = addons.size

    fun updateAddons(newAddons: List<ServiceAddon>){
        addons = newAddons
        notifyDataSetChanged()
    }

    class AddonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addonTitle: TextView = itemView.findViewById(R.id.addon_title)
        private val addonDescription: TextView = itemView.findViewById(R.id.addon_description)

        fun bind(addon: ServiceAddon) {
            addonTitle.text = addon.addonTitle
            addonDescription.text = addon.addonDescription
        }
    }
}