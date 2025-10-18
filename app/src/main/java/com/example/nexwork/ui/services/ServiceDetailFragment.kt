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
import com.example.nexwork.data.model.Service
import com.example.nexwork.data.model.ServiceAddon
import com.example.nexwork.data.model.ServicePlan
import com.example.nexwork.ui.categories.CategoryViewModel
import java.text.NumberFormat
import java.util.Locale
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.nexwork.core.LoadingDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ServiceDetailFragment : Fragment() {

    private val serviceViewModel: ServiceViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private lateinit var providerName: TextView
    private lateinit var serviceDescription: TextView
    private lateinit var providerProfession: TextView
    private lateinit var providerProfileImage: ImageView
    private lateinit var providerUbication: TextView
    private lateinit var addonsContainer: LinearLayout
    private lateinit var offerTab: MaterialButtonToggleGroup
    private lateinit var btnContinueService: Button
    private lateinit var tabIndicator: TabLayout
    private lateinit var btnSimple: MaterialButton
    private lateinit var btnBasic: MaterialButton
    private lateinit var btnPremium: MaterialButton
    private lateinit var loadingDialog: LoadingDialog
    private var currentService: Service? = null
    private lateinit var similarServicesRecyclerView: RecyclerView
    private lateinit var similarServicesAdapter: ServiceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_detail, container, false)
        val contentLayout = view.findViewById<View>(R.id.ServiceDetailFragment)
        contentLayout.visibility = View.GONE

        loadingDialog = LoadingDialog(requireContext())

        providerName = view.findViewById(R.id.name_provider)
        serviceDescription = view.findViewById(R.id.service_description)
        //providerProfession = view.findViewById(R.id.profession_provider)
        //providerUbication = view.findViewById(R.id.ubication_provider)
        providerProfileImage = view.findViewById(R.id.profileImage)
        tabIndicator = view.findViewById(R.id.tab_indicator)
        addonsContainer = view.findViewById(R.id.addons_service)
        offerTab = view.findViewById(R.id.offer_tab)
        btnSimple = view.findViewById(R.id.offer_tab_simple)
        btnBasic = view.findViewById(R.id.offer_tab_basic)
        btnPremium = view.findViewById(R.id.offer_tab_premium)
        btnContinueService = view.findViewById(R.id.btn_continue_service)
        similarServicesRecyclerView = view.findViewById(R.id.services_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog.show()

        setupSimilarServicesRecyclerView()

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

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val serviceId = arguments?.getString("serviceId")

        if (serviceId != null) {
            serviceViewModel.loadServiceById(serviceId)
        } else {
            Toast.makeText(context, "Error: Service ID no encontrado", Toast.LENGTH_LONG).show()
            loadingDialog.dismiss()
        }

        observeServiceDetail(txtTitle)
        observeProvider()
        observeSimilarServices()
    }

    private fun setupSimilarServicesRecyclerView() {
        similarServicesAdapter = ServiceAdapter(true) { service ->
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
        }
        similarServicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = similarServicesAdapter
        }
    }

    private fun observeSimilarServices() {
        categoryViewModel.servicesByCategory.observe(viewLifecycleOwner) { services ->
            similarServicesAdapter.submitList(services)
        }
    }

    private fun observeServiceDetail(headerTitle: TextView) {
        serviceViewModel.service.observe(viewLifecycleOwner) { service ->
            if (service != null) {
                currentService = service

                // Cargar servicios similares con el Id de categoria
                categoryViewModel.getServicesByCategoryId(service.categoryId, service.serviceId)
                setupImageCarousel(service.imageUrl)
                headerTitle.text = service.title
                displayAddons(service.addons)
                setupPlanSelection(service.plans)
                view?.findViewById<View>(R.id.ServiceDetailFragment)?.visibility = View.VISIBLE
                loadingDialog.dismiss()
            } else if (serviceViewModel.error.value != null) {
                Toast.makeText(context, serviceViewModel.error.value, Toast.LENGTH_LONG).show()
                loadingDialog.dismiss()
            }
        }
    }

    // Datos proveedor
    private fun observeProvider() {
        serviceViewModel.provider.observe(viewLifecycleOwner) { provider ->
            if (provider != null) {
                providerName.text = "${provider.firstName} ${provider.lastName}"
                Glide.with(this).load(provider.profileImageUrl).into(providerProfileImage)
            }
        }
    }

    // Configuracion carrusel
    private fun setupImageCarousel(imageUrls: List<String>) {
        val viewPager = view?.findViewById<ViewPager2>(R.id.image_carousel)

        if (viewPager != null && imageUrls.isNotEmpty()) {
            val adapter = ImageCarouselAdapter(imageUrls, requireContext())
            viewPager.adapter = adapter

            // Conecta el TabLayout con el ViewPager
            TabLayoutMediator(tabIndicator, viewPager) { tab, position ->
            }.attach()

        } else if (viewPager != null) {
            viewPager.visibility = View.GONE
            tabIndicator.visibility = View.GONE
        }
    }

    private fun setupPlanSelection(plans: List<ServicePlan>) {

        if (plans.isEmpty()) {
            offerTab.visibility = View.GONE
            serviceDescription.text = currentService?.description
            btnContinueService.text = getString(R.string.btn_continue_service)
            return
        }
        offerTab.visibility = View.VISIBLE

        val planMap = plans.associateBy { it.planName.lowercase(Locale.ROOT) }

        val buttonPlanMap = mapOf(
            R.id.offer_tab_simple to "simple",
            R.id.offer_tab_basic to "básico",
            R.id.offer_tab_premium to "premium"
        )

        // Mostrar los botones correspondientes
        buttonPlanMap.forEach { (buttonId, planNameKey) ->
            val button = view?.findViewById<MaterialButton>(buttonId)
            val plan = planMap[planNameKey]
            if (button != null) {
                if (plan != null) {
                    button.visibility = View.VISIBLE
                } else {
                    button.visibility = View.GONE
                }
            }
        }

        offerTab.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val selectedPlanNameKey = when(checkedId) {
                    R.id.offer_tab_simple -> "simple"
                    R.id.offer_tab_basic -> "básico"
                    R.id.offer_tab_premium -> "premium"
                    else -> null
                }

                val selectedPlan = selectedPlanNameKey?.let { planMap[it] }

                selectedPlan?.let { showPlanDetails(it) }
            }
        }

        val defaultPlan = planMap["simple"] ?: plans.firstOrNull()
        if(defaultPlan != null) {
            showPlanDetails(defaultPlan)
            if (defaultPlan.planName.lowercase(Locale.ROOT) == "simple") {
                btnSimple.isChecked = true
            } else if (defaultPlan.planName.lowercase(Locale.ROOT) == "básico") {
                btnBasic.isChecked = true
            } else if (defaultPlan.planName.lowercase(Locale.ROOT) == "premium") {
                btnPremium.isChecked = true
            }
        }
    }

    private fun showPlanDetails(plan: ServicePlan) {
        val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        format.maximumFractionDigits = 0
        val priceText = format.format(plan.price)

        btnContinueService.text = "Continuar ($priceText)"
        serviceDescription.text = plan.planDescription

        // Mostrar los addons correspondientes a cada plan
        val planName = plan.planName.lowercase(Locale.ROOT)
        val addonIndex = when(planName) {
            "simple" -> 0
            "básico" -> 1
            "premium" -> 2
            else -> -1
        }

        val selectedAddons = if (addonIndex != -1 && currentService?.addons?.size ?: 0 > addonIndex) {
            listOf(currentService!!.addons[addonIndex])
        } else {
            emptyList()
        }

        displayAddons(selectedAddons)
    }

    private fun displayAddons(addons: List<ServiceAddon>) {
        addonsContainer.removeAllViews()
        val inflater = LayoutInflater.from(context)

        if (addons.isEmpty()) {
            addonsContainer.visibility = View.GONE
            return
        }

        addonsContainer.visibility = View.VISIBLE

        for (addon in addons) {
            val addonView = inflater.inflate(R.layout.item_service_addon, addonsContainer, false)
            addonView.findViewById<TextView>(R.id.addon_title).text = addon.addonTitle
            addonView.findViewById<TextView>(R.id.addon_description).text = addon.addonDescription

            addonsContainer.addView(addonView)
        }
    }
}