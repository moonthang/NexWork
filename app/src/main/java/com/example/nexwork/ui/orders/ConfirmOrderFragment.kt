package com.example.nexwork.ui.orders

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexwork.R
import com.example.nexwork.data.model.Order
import com.example.nexwork.data.model.Service
import com.example.nexwork.data.model.ServicePlan
import com.example.nexwork.data.repository.AuthRepository
import com.example.nexwork.ui.categories.CategoryViewModel
import com.example.nexwork.ui.services.ServiceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ConfirmOrderFragment : Fragment(), OrdersAdapter.OnItemClickListener {

    private val serviceViewModel: ServiceViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val orderViewModel: OrderViewModel by viewModels()
    private val authRepository = AuthRepository()
    private var serviceId: String? = null
    private var planIndex: Int = 0
    private var currentService: Service? = null
    private var currentPlan: ServicePlan? = null
    private var clientLocation: Map<String, Double>? = null
    private lateinit var serviceTitle: TextView
    private lateinit var serviceDetails: TextView
    private lateinit var serviceDate: TextView
    private lateinit var dateService: EditText
    private lateinit var hoursService: EditText
    private lateinit var textPlan: TextView
    private lateinit var textPricePlan: TextView
    private lateinit var btnContinue: Button
    private lateinit var addonsRecyclerView: RecyclerView
    private lateinit var serviceAddonAdapter: ServiceAddonAdapter
    private lateinit var ordersAdapter: OrdersAdapter
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getLocation()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            serviceId = it.getString("serviceId")
            planIndex = it.getInt("planIndex")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_confirm_order, container, false)
        serviceTitle = view.findViewById(R.id.service_title)
        serviceDetails = view.findViewById(R.id.service_details)
        serviceDate = view.findViewById(R.id.service_date)
        dateService = view.findViewById(R.id.date_service)
        hoursService = view.findViewById(R.id.hours_service)
        textPlan = view.findViewById(R.id.text_plan)
        textPricePlan = view.findViewById(R.id.text_price_plan)
        btnContinue = view.findViewById(R.id.btn_continue)
        addonsRecyclerView = view.findViewById(R.id.addons_recycler_view)

        dateService.setOnClickListener { showDatePickerDialog() }
        hoursService.setOnClickListener { showTimePickerDialog() }
        btnContinue.setOnClickListener { onContinueClicked() }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        btnOptions.visibility = View.VISIBLE

        txtTitle.setText(getString(R.string.confirm_order_title))
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        ordersAdapter = OrdersAdapter(this, true)
        serviceAddonAdapter = ServiceAddonAdapter(emptyList())
        addonsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        addonsRecyclerView.adapter = serviceAddonAdapter

        serviceId?.let { serviceViewModel.loadServiceById(it) }

        serviceViewModel.service.observe(viewLifecycleOwner) { service ->
            currentService = service
            currentPlan = service?.plans?.getOrNull(planIndex)
            service?.categoryId?.let { categoryId -> categoryViewModel.getCategoryById(categoryId) }
            updateUI()
        }

        categoryViewModel.category.observe(viewLifecycleOwner) { category ->
            serviceDetails.text = category.name
        }

        orderViewModel.createOrderResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Order created successfully", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }.onFailure {
                Toast.makeText(requireContext(), "Error creating order: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI() {
        currentService?.let {
            serviceTitle.text = it.title
            val selectedAddons = it.addons.filter { addon -> addon.planIndex == planIndex }
            serviceAddonAdapter.updateAddons(selectedAddons)
        }
        currentPlan?.let {
            textPlan.text = "Plan ${it.planName}"
            val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
            format.maximumFractionDigits = 0
            textPricePlan.text = format.format(it.price)
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateService.setText(dateFormat.format(selectedDate.time))
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val endTime = String.format("%02d:%02d", selectedHour + 1, selectedMinute)
                hoursService.setText(String.format("%02d:%02d - %s", selectedHour, selectedMinute, endTime))
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    private fun onContinueClicked() {
        if (currentService?.requiresVisit == true) {
            requestLocationPermission()
        } else {
            saveOrder()
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLocation()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getLocation() {
        serviceViewModel.getLatestLocationOnce(true, true) { location ->
            clientLocation = location
            saveOrder()
        }
    }

    private fun saveOrder() {
        val userId = authRepository.getCurrentUserId()
        if (currentService != null && currentPlan != null && userId != null) {
            val order = Order(
                serviceId = currentService!!.serviceId,
                titleService = currentService!!.title,
                categoryId = currentService!!.categoryId,
                titleCategory = serviceDetails.text.toString(),
                providerId = currentService!!.providerId,
                clientId = userId,
                time = hoursService.text.toString(),
                imageUrl = currentService!!.imageUrl.firstOrNull() ?: "",
                price = currentPlan!!.price,
                ubicationClient = clientLocation ?: emptyMap(),
                providerName = "",
                clientName = "",
                status = ""
            )
            orderViewModel.createOrder(order)
        } else {
            Toast.makeText(requireContext(), "Error creating order", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemClick(order: Order) {
        //
    }
}