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

class CategoryAdapter(
    private var categories: List<Category>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged() // Para una lista simple esto está bien.
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_category_name)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tv_category_description)
        private val idTextView: TextView = itemView.findViewById(R.id.tv_category_id)
        private val imageView: ImageView = itemView.findViewById(R.id.iv_category_image)

        fun bind(category: Category) {
            nameTextView.text = category.name
            descriptionTextView.text = category.description
            idTextView.text = itemView.context.getString(R.string.category_id_format, category.id)

            // Usamos Glide para cargar la imagen (asegúrate de tener la dependencia)
            Glide.with(itemView.context)
                .load(category.imageUrl)
                .placeholder(R.drawable.ic_launcher_background) // Imagen de placeholder
                .into(imageView)
        }
    }
}