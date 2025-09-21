package com.example.nexwork.ui.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexwork.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MyServicesFragment : Fragment() {

    private val viewModel: ServiceViewModel by viewModels()
    private lateinit var serviceAdapter: ServiceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar Header
        val headerView = view.findViewById<View>(R.id.header)
        val titleTextView = headerView.findViewById<TextView>(R.id.txtTitle)
        titleTextView.text = getString(R.string.my_services_title)

        // Configurar RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.services_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        serviceAdapter = ServiceAdapter { service ->
            // TODO: Navegar a la pantalla de detalle del servicio
            Toast.makeText(context, "Clicked on ${service.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = serviceAdapter


        val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.add_service_fab)
        fab.setOnClickListener {
            // TODO: Navegar a la pantalla de creación de un nuevo servicio
        }
        
        observeViewModel()
        viewModel.fetchServices()
    }

    private fun observeViewModel() {
        viewModel.services.observe(viewLifecycleOwner) { services ->
            // Implementar logica cuando esto este ready para el party
            if (services.isNullOrEmpty()) {
                val dummyServices = listOf(
                    com.example.nexwork.data.model.Service("1", "Limpieza de GYM", "Descripción de prueba...", 120000.0, 4.8f, ""),
                    com.example.nexwork.data.model.Service("2", "Limpieza de HOGAR", "Descripción de prueba...", 150000.0, 4.9f, ""),
                    com.example.nexwork.data.model.Service("3", "Limpieza de OFICINA", "Descripción de prueba...", 200000.0, 4.7f, "")
                )
                serviceAdapter.submitList(dummyServices)
            } else {
                serviceAdapter.submitList(services)
            }
        }
    }
}