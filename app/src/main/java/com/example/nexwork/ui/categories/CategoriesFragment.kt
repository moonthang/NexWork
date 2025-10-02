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
import com.example.nexwork.Home
import com.example.nexwork.R
import com.example.nexwork.databinding.FragmentCategoriesBinding
import kotlin.jvm.java

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModels()
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupHeader()
        setupRecyclerView()
        observeViewModel()
        
        categoryViewModel.fetchCategories()
    }

    private fun setupHeader() {

        binding.header.txtTitle.text = getString(R.string.categories_title)
        binding.header.btnBack.setOnClickListener {
            val intent = Intent(requireActivity(), Home::class.java)
            startActivity(intent)
            requireActivity().finish()

        }
    }

    private fun setupSearchView() {
        val searchEditText = binding.searchLayout.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(emptyList())
        binding.rvCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            categories?.let {
                categoryAdapter.updateCategories(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}