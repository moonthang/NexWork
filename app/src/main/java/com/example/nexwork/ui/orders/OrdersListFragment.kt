package com.example.nexwork.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexwork.R
import com.example.nexwork.data.model.Order

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
            Order(orderId = "1", titleService = "Limpieza de GYM", clientId = "Sofía Ramírez", time = "10:00 AM - 13:00 PM"),
            Order(orderId = "2", titleService = "Limpieza de HOGAR", clientId = "Juan Pérez", time = "11:00 AM - 14:00 PM"),
            Order(orderId = "3", titleService = "Limpieza de OFICINA", clientId = "María García", time = "12:00 PM - 15:00 PM")
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