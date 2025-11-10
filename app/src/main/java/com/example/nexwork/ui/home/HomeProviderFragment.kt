package com.example.nexwork.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexwork.R
import com.example.nexwork.databinding.FragmentHomeProviderBinding
import com.example.nexwork.ui.services.MyServicesFragment
import com.example.nexwork.ui.services.ServiceAdapter
import com.google.android.material.card.MaterialCardView

class HomeProviderFragment : Fragment() {

    private var _binding: FragmentHomeProviderBinding? = null
    private val binding get() = _binding!!

    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeProviderBinding.inflate(inflater, container, false)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardPedidosPendientes = binding.root.findViewById<View>(R.id.card_pedidos_pendientes)
        cardPedidosPendientes.findViewById<TextView>(R.id.tv_title).text = "Pedidos\nPendientes"
        cardPedidosPendientes.findViewById<TextView>(R.id.tv_value).text = "4"

        val cardIngresosGenerados = binding.root.findViewById<MaterialCardView>(R.id.card_ingresos_generados)
        cardIngresosGenerados.findViewById<TextView>(R.id.tv_title).text = "Ingresos\nGenerados"
        cardIngresosGenerados.findViewById<TextView>(R.id.tv_value).text = "$1,250"
        cardIngresosGenerados.setStrokeColor(Color.parseColor("#00FF00"))
        cardIngresosGenerados.strokeWidth = resources.getDimensionPixelSize(R.dimen.card_stroke_width)

        val cardMensajesNuevos = binding.root.findViewById<View>(R.id.card_mensajes_nuevos)
        cardMensajesNuevos.findViewById<TextView>(R.id.tv_title).text = "Mensajes Nuevos"
        cardMensajesNuevos.findViewById<TextView>(R.id.tv_value).text = "2"

        val seeMyServices = view.findViewById<Button>(R.id.btn_see_my_services)
        seeMyServices.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MyServicesFragment())
                .addToBackStack(null)
                .commit()
        }

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

        txtTitle.setText(getString(R.string.home_provider_title))
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setupRecyclerView()
        observeViewModel()
        homeViewModel.fetchLastThreeServices()
    }

    private fun setupRecyclerView() {
        serviceAdapter = ServiceAdapter(isClientView = false, onClick = {}, onFavoriteClick = {})
        binding.rvMyServices.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = serviceAdapter
        }
    }

    private fun observeViewModel() {
        homeViewModel.services.observe(viewLifecycleOwner) {
            serviceAdapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}