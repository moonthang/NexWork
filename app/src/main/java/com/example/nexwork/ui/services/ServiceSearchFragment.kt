package com.example.nexwork.ui.services

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexwork.R
import com.example.nexwork.data.model.Service

class ServiceSearchFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var serviceAdapter: ServiceAdapter
    private val viewModel: ServiceViewModel by viewModels()
    private var allServices: List<Service> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_search, container, false)

        searchView = view.findViewById(R.id.search_layout)
        recyclerView = view.findViewById(R.id.rv_categories)

        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        viewModel.getAllServices()

        return view
    }

    private fun setupRecyclerView() {
        serviceAdapter = ServiceAdapter(true) { service ->
            val bundle = Bundle().apply {
                putString("serviceId", service.serviceId)
            }

            val serviceDetailFragment = ServiceDetailFragment().apply {
                arguments = bundle
            }

            // Navegar al fragmento de detalles del servicio
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, serviceDetailFragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = serviceAdapter
        }
        serviceAdapter.submitList(emptyList())
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            // Cuando el usuario hace clic en el botón de búsqueda
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterServices(query)
                return true
            }

            // Cada vez que el usuario escribe algo
            override fun onQueryTextChange(newText: String?): Boolean {
                filterServices(newText)
                return true
            }
        })
    }

    private fun observeViewModel(){
        viewModel.services.observe(viewLifecycleOwner) { services ->
            allServices = services
            filterServices(searchView.query.toString())
        }
    }

    private fun filterServices(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            emptyList()
        } else {
            allServices.filter {
                // Filtra los servicios según el texto de búsqueda
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
        serviceAdapter.submitList(filteredList)
    }
}