package com.example.nexwork.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexwork.R
import com.example.nexwork.core.OptionsDialogFragment
import com.example.nexwork.data.model.Order
import com.example.nexwork.databinding.FragmentOrdersListBinding

class OrdersListFragment : Fragment(), OrdersAdapter.OnItemClickListener, OptionsDialogFragment.OptionsDialogListener {


    private var _binding: FragmentOrdersListBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderType: String
    private lateinit var ordersAdapter: OrdersAdapter
    private var selectedOrder: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            orderType = it.getString(ARG_ORDER_TYPE) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding  = FragmentOrdersListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.header.txtTitle.text = getString(R.string.text_manage_orders)
        binding.header.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.header.btnNotification.visibility = View.GONE
        binding.header.btnSearch.visibility = View.GONE
        binding.header.btnFilter.visibility = View.GONE
        binding.header.btnOptions.visibility = View.GONE

        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        ordersAdapter = OrdersAdapter(this)
        binding.recyclerView.adapter = ordersAdapter

        val dummyOrders = listOf(Order(orderId = "1", titleService = "Limpieza de GYM", clientId = "Sofía Ramírez", time = "10:00 AM - 13:00 PM"),
            Order(orderId = "2", titleService = "Limpieza de HOGAR", clientId = "Juan Pérez", time = "11:00 AM - 14:00 PM"),
            Order(orderId = "3", titleService = "Limpieza de OFICINA", clientId = "María García", time = "12:00 PM - 15:00 PM"),
            Order("1", "Limpieza de GYM", "Sofía Ramírez", "Miguel Burgos","Activo","15 de Diciembre, 2025", "10:00 AM - 13:00 PM", ""),
            Order("2", "Limpieza de HOGAR", "Juan Pérez","Alberto Usquen","Activo", "16 de Diciembre, 2025", "11:00 AM - 14:00 PM", ""),
            Order("3", "Limpieza de OFICINA", "María García", "Sofia Lisarazo","Finalizado","17 de Diciembre, 2025", "12:00 PM - 15:00 PM", "")
        )

        ordersAdapter.submitList(dummyOrders)
    }

    override fun onItemClick(order: Order) {
        selectedOrder = order
        val dialog = OptionsDialogFragment.newInstance(
            title = order.titleService,
            option1 = getString(R.string.view_details_option),
            option2 = getString(R.string.edit_option),
            option3 = getString(R.string.delete_option)
        )
        dialog.setOptionsDialogListener(this)
        dialog.show(parentFragmentManager, "OrdersOptionsDialogFragment")
    }

    override fun onOptionSelected(option: String) {
        val order = selectedOrder ?: return
        when (option) {
            getString(R.string.view_details_option) -> {
                Toast.makeText(requireContext(), "Ver detalles de la orden: ${order.serviceId}", Toast.LENGTH_SHORT).show()
                // Aquí podrías navegar a un fragmento con detalles de la orden
            }
            getString(R.string.edit_option) -> {
                Toast.makeText(requireContext(), "Editar orden: ${order.serviceId}", Toast.LENGTH_SHORT).show()
                // Aquí podrías navegar a un fragmento de edición de la orden
            }
            getString(R.string.delete_option) -> {
                Toast.makeText(requireContext(), "Eliminar orden: ${order.serviceId}", Toast.LENGTH_SHORT).show()
                // Aquí podrías implementar la lógica para eliminar la orden
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ORDER_TYPE = "order_type"

        @JvmStatic
        fun newInstance(orderType: String) =
            OrdersListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ORDER_TYPE, orderType)
                }
            }
    }
}