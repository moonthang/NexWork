package com.example.nexwork.ui.profile.favorites

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexwork.R
import com.example.nexwork.ui.services.ServiceAdapter
import com.example.nexwork.ui.services.ServiceDetailFragment

class FavoritesListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var serviceAdapter: ServiceAdapter
    private val viewModel: FavoritesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites_list, container, false)
        recyclerView = view.findViewById(R.id.favorites_recycler_view)

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

        txtTitle.setText(getString(R.string.my_favorites_title))
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setupRecyclerView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.favoriteServices.observe(viewLifecycleOwner, Observer {
            serviceAdapter.submitList(it)
        })
        viewModel.fetchFavoriteServices()
    }

    private fun setupRecyclerView() {
        serviceAdapter = ServiceAdapter(true, {
            val bundle = Bundle().apply {
                putString("serviceId", it.serviceId)
            }
            val serviceDetailFragment = ServiceDetailFragment().apply {
                arguments = bundle
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, serviceDetailFragment)
                .addToBackStack(null)
                .commit()
        }, {
            viewModel.removeFavorite(it)
        })
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = serviceAdapter
        }
    }
}