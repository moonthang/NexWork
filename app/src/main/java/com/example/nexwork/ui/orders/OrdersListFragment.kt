package com.example.nexwork.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexwork.R

class OrdersListFragment : Fragment() {

    private lateinit var orderType: String

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
        return inflater.inflate(R.layout.fragment_orders_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = OrdersAdapter()
        recyclerView.adapter = adapter

        val dummyOrders = listOf(
            Order("1", "Limpieza de GYM", "Sofía Ramírez", "15 de Diciembre, 2025", "10:00 AM - 13:00 PM", ""),
            Order("2", "Limpieza de HOGAR", "Juan Pérez", "16 de Diciembre, 2025", "11:00 AM - 14:00 PM", ""),
            Order("3", "Limpieza de OFICINA", "María García", "17 de Diciembre, 2025", "12:00 PM - 15:00 PM", "")
        )

        adapter.submitList(dummyOrders)
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