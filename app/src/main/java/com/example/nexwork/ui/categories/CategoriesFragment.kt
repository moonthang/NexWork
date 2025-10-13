package com.example.nexwork.ui.categories

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexwork.R
import com.example.nexwork.core.LoadingDialog
import com.example.nexwork.databinding.FragmentCategoriesBinding
import com.example.nexwork.core.OptionsDialogFragment
import com.example.nexwork.data.model.Category

class CategoriesFragment : Fragment(), CategoryAdapter.OnItemClickListener, OptionsDialogFragment.OptionsDialogListener {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoriesViewModel by viewModels()
    private lateinit var categoryAdapter: CategoryAdapter
    private var selectedCategory: Category? = null
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        binding.CategoriesFragment.visibility = View.GONE

        loadingDialog = LoadingDialog(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog.show()
        setupHeader()
        setupSearchView()
        setupRecyclerView()
        observeViewModel()

        binding.fabAddCategory.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateCategoryFragment())
                .addToBackStack(null)
                .commit()
        }

        categoryViewModel.loadCategories()
    }

    private fun setupHeader() {
        binding.header.txtTitle.text = getString(R.string.categories_title)
        binding.header.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.header.btnNotification.visibility = View.GONE
        binding.header.btnSearch.visibility = View.GONE
        binding.header.btnFilter.visibility = View.GONE
        binding.header.btnOptions.visibility = View.GONE
    }

    private fun setupSearchView() {
        val searchEditText = binding.searchLayout.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(emptyList(), this)
        binding.rvCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            loadingDialog.dismiss()
            binding.CategoriesFragment.visibility = View.VISIBLE

            categories?.let {
                categoryAdapter.updateCategories(it)
            }
        }
    }

    override fun onItemClick(category: Category) {
        selectedCategory = category
        val dialog = OptionsDialogFragment.newInstance(
            title = category.name,
            option1 = getString(R.string.edit_option),
            option3 = getString(R.string.delete_option)
        )
        dialog.setOptionsDialogListener(this)
        dialog.show(parentFragmentManager, "OptionsDialogFragment")
    }

    override fun onOptionSelected(option: String) {
        when (option) {
            getString(R.string.edit_option) -> {
                // TODO: Implement edit logic
                android.widget.Toast.makeText(requireContext(), "Edit: ${selectedCategory?.name}", android.widget.Toast.LENGTH_SHORT).show()
            }
            getString(R.string.delete_option) -> {
                // TODO: Implement delete logic
                android.widget.Toast.makeText(requireContext(), "Delete: ${selectedCategory?.name}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}