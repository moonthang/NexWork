package com.example.nexwork.ui.services

import android.app.AlertDialog
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
import com.example.nexwork.data.model.Service

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

        txtTitle.setText(getString(R.string.my_services_title))
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.services_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        serviceAdapter = ServiceAdapter(
            isClientView = false,
            onClick = { service ->
                showOptionsDialog(service)
            },
            onFavoriteClick = {}
        )
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
        observeServiceDeleted()
    }

    // Eliminar servicio
    private fun observeServiceDeleted() {
        viewModel.serviceDeleted.observe(viewLifecycleOwner) { isDeleted ->
            if (isDeleted) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.service_deleted),
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.getAllServices()
                viewModel.onServiceDeletedHandled()
            }
        }
    }

    private fun showOptionsDialog(service: Service) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_item_options, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val btnView = dialogView.findViewById<Button>(R.id.btnOption1)
        val btnEdit = dialogView.findViewById<Button>(R.id.btnOption2)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnOption3)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        tvDialogTitle.text = service.title
        btnView.text = getString(R.string.btn_see)
        btnEdit.text = getString(R.string.btn_edit)
        btnDelete.text = getString(R.string.delete_option)

        btnView.setOnClickListener {
            val bundle = Bundle().apply {
                putString("serviceId", service.serviceId)
            }
            val serviceDetailFragment = ServiceDetailFragment().apply {
                arguments = bundle
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, serviceDetailFragment)
                .addToBackStack(null)
                .commit()
            dialog.dismiss()
        }

        btnEdit.setOnClickListener {
            val bundle = Bundle().apply {
                putString("serviceId", service.serviceId)
                putString("categoryId", service.categoryId)
            }
            val createServiceFragment = CreateServiceFragment().apply {
                arguments = bundle
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, createServiceFragment)
                .addToBackStack(null)
                .commit()
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmationDialog(service)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Eliminar servicio mensaje
    private fun showDeleteConfirmationDialog(service: Service) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.txt_delete_service))
            .setMessage(getString(R.string.txt_delete_service_quest))
            .setPositiveButton(getString(R.string.opt_yes)) { _, _ ->
                viewModel.deleteService(service.serviceId)
            }
            .setNegativeButton(getString(R.string.opt_no), null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.services.observe(viewLifecycleOwner) { services ->
            loadingDialog.dismiss()
            view?.findViewById<View>(R.id.MyServicesFragment)?.visibility = View.VISIBLE
            serviceAdapter.submitList(services)
        }
    }
}