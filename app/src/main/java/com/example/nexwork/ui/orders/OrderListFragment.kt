package com.example.nexwork.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexwork.R

class OrderListFragment : Fragment() {

    private lateinit var pedidoType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pedidoType = it.getString(ARG_PEDIDO_TYPE) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pedidos_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = PedidosAdapter()
        recyclerView.adapter = adapter

        val dummyOrders = listOf(
            Order("1", "Limpieza de GYM", "Sofía Ramírez", "15 de Diciembre, 2025", "10:00 AM - 13:00 PM", ""),
            Order("2", "Limpieza de HOGAR", "Juan Pérez", "16 de Diciembre, 2025", "11:00 AM - 14:00 PM", ""),
            Order("3", "Limpieza de OFICINA", "María García", "17 de Diciembre, 2025", "12:00 PM - 15:00 PM", "")
        )

        adapter.submitList(dummyOrders)
    }

    companion object {
        private const val ARG_PEDIDO_TYPE = "pedido_type"

        @JvmStatic
        fun newInstance(pedidoType: String) =
            OrderListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PEDIDO_TYPE, pedidoType)
                }
            }
    }
}