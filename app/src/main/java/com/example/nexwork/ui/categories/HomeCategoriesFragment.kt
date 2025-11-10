package com.example.nexwork.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.nexwork.R
import com.example.nexwork.core.LoadingDialog
import com.example.nexwork.data.model.Category
import com.example.nexwork.databinding.FragmentHomeCategoriesBinding
import com.example.nexwork.ui.services.ServiceSearchFragment

class HomeCategoriesFragment : Fragment(), CategoryAdapter.OnItemClickListener {

    private var _binding: FragmentHomeCategoriesBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var loadingDialog: LoadingDialog
    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        setupHeader()
        loadStaticImages()
        setupCategoriesRecyclerView()
        observeViewModel()
    }

    private fun observeViewModel() {
        categoryViewModel.loading.observe(viewLifecycleOwner) {
            if (it) loadingDialog.show() else loadingDialog.dismiss()
        }
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            categoryAdapter.updateCategories(it)
        }
        categoryViewModel.error.observe(viewLifecycleOwner) {
            it?.let {
                Toast.makeText(requireContext(), "Error al cargar categorías: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupHeader() {
        binding.header.btnNotification.visibility = View.GONE
        binding.header.btnSearch.visibility = View.VISIBLE
        binding.header.btnFilter.visibility = View.GONE
        binding.header.btnOptions.visibility = View.GONE
        binding.header.txtTitle.text = getString(R.string.categories_title)
        binding.header.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.header.btnSearch.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ServiceSearchFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadStaticImages() {
        val imageUrl1 = "https://firebasestorage.googleapis.com/v0/b/nexwork-e4866.firebasestorage.app/o/resources%2FMejorar%20habilidades.png?alt=media&token=3a24c512-e1c0-4ef5-b38e-e488ead39a3a"
        val imageUrl2 = "https://firebasestorage.googleapis.com/v0/b/nexwork-e4866.firebasestorage.app/o/resources%2FCrear%20contenido.png?alt=media&token=84c9c538-9db2-4dcd-9b94-a88274fb35ed"

        // Cargar las imágenes con Glide
        Glide.with(this)
            .load(imageUrl1)
            .into(binding.imageImproveSkills)

        Glide.with(this)
            .load(imageUrl2)
            .into(binding.imageCreateContent)
    }

    // Configuración del RecyclerView de categoríass
    private fun setupCategoriesRecyclerView() {
        categoryAdapter = CategoryAdapter(
            categories = emptyList(),
            listener = this,
            viewType = ViewType.CARD
        )

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = categoryAdapter
        }

        categoryViewModel.fetchCategories()
    }

    // Manejar clics para ver los servicios de la categoria
    override fun onItemClick(category: Category) {
        val fragment = CategoriesDetailFragment.newInstance(category.categoryId, category.name)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}