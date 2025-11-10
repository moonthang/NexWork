package com.example.nexwork.ui.categories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexwork.R
import com.example.nexwork.data.model.Category

enum class ViewType {
    LIST, CARD
}

class CategoryAdapter(
    private var categories: List<Category>,
    private val listener: OnItemClickListener,
    private val viewType: ViewType
) : RecyclerView.Adapter<CategoryAdapter.BaseViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(category: Category)
    }

    // ViewHolders comunes para ambas vistas
    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(category: Category)
    }

    // Vista de lista
    class ListViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_category_name)
        private val idTextView: TextView = itemView.findViewById(R.id.tv_category_id)
        private val imageView: ImageView = itemView.findViewById(R.id.iv_category_image)

        override fun bind(category: Category) {
            nameTextView.text = category.name
            idTextView.text =
                itemView.context.getString(R.string.category_id_format, category.categoryId)

            Glide.with(itemView.context)
                .load(category.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(imageView)
        }
    }

    // Vista tipo card
    class CardViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.card_title)
        private val imageView: ImageView = itemView.findViewById(R.id.card_image)

        override fun bind(category: Category) {
            nameTextView.text = category.name

            Glide.with(itemView.context)
                .load(category.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(imageView)
        }
    }

    // Obtener el tipo de vista
    override fun getItemViewType(position: Int): Int {
        // 0 para lista, 1 para card
        return viewType.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewTypeInt: Int): BaseViewHolder {
        // Convierte el entero del tipo de vista
        val viewType = ViewType.entries[viewTypeInt]

        return when (viewType) {
            ViewType.LIST -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_category_list, parent, false)
                ListViewHolder(view)
            }

            ViewType.CARD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_category_card, parent, false)
                CardViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
        holder.itemView.setOnClickListener {
            listener.onItemClick(category)
        }
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}