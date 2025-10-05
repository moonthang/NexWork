package com.example.nexwork.ui.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexwork.R

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

        val headerView = view.findViewById<View>(R.id.header)
        val titleTextView = headerView.findViewById<TextView>(R.id.txtTitle)
        titleTextView.text = getString(R.string.my_services_title)

        val recyclerView = view.findViewById<RecyclerView>(R.id.services_recycler_view)
        // Muestra la lista de servicios verticalmente
        recyclerView.layoutManager = LinearLayoutManager(context)
        serviceAdapter = ServiceAdapter { service ->
            Toast.makeText(context, "Clicked on ${service.title}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = serviceAdapter


        val fab = view.findViewById<Button>(R.id.add_service_fab)
        fab.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateServiceFragment())
                .addToBackStack(null)
                .commit()
        }

        observeViewModel()
        viewModel.getAllServices()
    }

    // Observa los cambios en la lista de servicios
    private fun observeViewModel() {
        viewModel.services.observe(viewLifecycleOwner) { services ->
            // Actualiza la lista de servicios en el adaptador
            serviceAdapter.submitList(services)
        }
    }
}