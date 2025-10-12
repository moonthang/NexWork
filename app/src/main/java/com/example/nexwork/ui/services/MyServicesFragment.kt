package com.example.nexwork.ui.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexwork.R
import com.example.nexwork.core.LoadingDialog

class MyServicesFragment : Fragment() {

    private val viewModel: ServiceViewModel by viewModels()
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_services, container, false)

        val contentLayout = view.findViewById<View>(R.id.MyServicesFragment)
        contentLayout?.visibility = View.GONE

        loadingDialog = LoadingDialog(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog.show()
        val basic_header = view.findViewById<View>(R.id.header)
        val btnNotification = basic_header.findViewById<ImageView>(R.id.btnNotification)
        val btnSearch = basic_header.findViewById<ImageView>(R.id.btnSearch)
        val btnFilter = basic_header.findViewById<ImageView>(R.id.btnFilter)
        val btnOptions = basic_header.findViewById<ImageView>(R.id.btnOptions)
        val txtTitle = basic_header.findViewById<TextView>(R.id.txtTitle)
        val btnBack = basic_header.findViewById<ImageView>(R.id.btnBack)

        btnNotification.visibility = View.GONE
        btnSearch.visibility = View.GONE
        btnFilter.visibility = View.GONE
        btnOptions.visibility = View.GONE

        txtTitle.setText(getString(R.string.account_title))
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.services_recycler_view)
        // Muestra la lista de servicios verticalmente
        recyclerView.layoutManager = LinearLayoutManager(context)
        serviceAdapter = ServiceAdapter { service ->
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
            loadingDialog.dismiss()
            view?.findViewById<View>(R.id.MyServicesFragment)?.visibility = View.VISIBLE

            // Actualiza la lista de servicios en el adaptador
            serviceAdapter.submitList(services)
        }
    }
}