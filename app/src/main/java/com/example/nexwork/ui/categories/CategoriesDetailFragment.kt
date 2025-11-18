package com.example.nexwork.ui.categories

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexwork.R
import com.example.nexwork.core.LoadingDialog
import com.example.nexwork.databinding.FragmentCategoriesDetailBinding
import com.example.nexwork.ui.services.ServiceAdapter
import com.example.nexwork.ui.services.ServiceDetailFragment
import com.example.nexwork.ui.services.ServiceSearchFragment
import com.example.nexwork.ui.services.ServiceViewModel

// constantes con los datos de la categoría
private const val CATEGORY_ID = "category_id"
private const val CATEGORY_NAME = "category_name"

class CategoriesDetailFragment : Fragment() {

    private var categoryId: String? = null
    private var name: String? = null
    private var _binding: FragmentCategoriesDetailBinding? = null
    private val binding get() = _binding!!
    private val serviceViewModel: ServiceViewModel by viewModels()
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Recupera los datos de la categoría
            categoryId = it.getString(CATEGORY_ID)
            name = it.getString(CATEGORY_NAME)
        }
        serviceViewModel.getAllServices()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesDetailBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireContext())

        binding.CategoriesDetailFragment.visibility = View.GONE

        return binding.root
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
        btnSearch.visibility = View.VISIBLE
        btnFilter.visibility = View.GONE
        btnOptions.visibility = View.GONE

        txtTitle.text = name

        btnSearch.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ServiceSearchFragment())
                .addToBackStack(null)
                .commit()
        }

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Se llama el adaptador para la vista decliente, y manejo del clic en un servicio
        serviceAdapter = ServiceAdapter(
            isClientView = true,
            onClick = { service ->
                val fragment = ServiceDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString("serviceId", service.serviceId)
                    }
                }
                // Navega al fragmento de detalles del servicio
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onFavoriteClick = { service ->
                serviceViewModel.toggleFavorite(service)
            }
        )

        // Configuración del RecyclerView
        binding.rvServices.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = serviceAdapter
        }

        // Observa los cambios
        serviceViewModel.services.observe(viewLifecycleOwner) { services ->
            val filteredServices = services.filter { it.categoryId == categoryId }
            // Envia los datos al adaptador
            serviceAdapter.submitList(filteredServices)

            loadingDialog.dismiss()
            binding.CategoriesDetailFragment.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(categoryId: String, name: String) =
            CategoriesDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(CATEGORY_ID, categoryId)
                    putString(CATEGORY_NAME, name)
                }
            }
    }
}