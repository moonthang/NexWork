package com.example.nexwork.ui.services

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.nexwork.R
import com.example.nexwork.core.LoadingDialog
import com.example.nexwork.data.model.Service
import com.example.nexwork.data.model.ServiceAddon
import com.example.nexwork.data.model.ServicePlan
import com.example.nexwork.data.repository.AuthRepository
import java.util.*
import com.example.nexwork.ui.services.ServiceViewModel.UpsertState

class CreateServiceFragment : Fragment() {
    private lateinit var offerTab: com.google.android.material.button.MaterialButtonToggleGroup
    private lateinit var etOfferTitle: EditText
    private lateinit var etCategory: Spinner
    private lateinit var etOfferDescription: EditText
    private lateinit var etOfferPrice: EditText
    private lateinit var etAddonName: EditText
    private lateinit var etAddonDescription: EditText
    private lateinit var btnAddAnotherAddon: Button
    private lateinit var containerAddons: LinearLayout
    private lateinit var flUploadImages: FrameLayout
    private lateinit var containerImages: LinearLayout
    private lateinit var etUserName: Spinner
    private lateinit var userNameContainer: LinearLayout
    private lateinit var btnCreate: Button
    private lateinit var btnCancel: Button
    private lateinit var rgRequiresVisit: RadioGroup
    private lateinit var loadingDialog: LoadingDialog
    private val viewModel: ServiceViewModel by activityViewModels()
    private val authRepository = AuthRepository()
    private val imageItems =
        mutableListOf<Any?>().apply { addAll(arrayOfNulls<Any>(3)) }
    private var serviceId: String? = null
    private var isEditMode = false
    private var currentPlanIndex = 0
    private val plans = arrayOfNulls<ServicePlan>(3)
    private val addonsList = arrayOf(
        mutableListOf<ServiceAddon>(),
        mutableListOf<ServiceAddon>(),
        mutableListOf<ServiceAddon>()
    )

    private val uploadedImageUrls =
        mutableListOf<String?>().apply { addAll(arrayOfNulls<String>(3)) }
    private val deletedImageUrls = mutableListOf<String>()
    private var currentUbication: Map<String, Double> = emptyMap()

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val firstEmptyIndex = imageItems.indexOfFirst { it == null }
                if (firstEmptyIndex != -1) {
                    imageItems[firstEmptyIndex] = it
                    updateImagesUI()
                } else {
                    Toast.makeText(requireContext(), "Máximo 3 imágenes", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                viewModel.getLatestLocationOnce(true, false) { map ->
                    map?.let { currentUbication = it }
                }
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                viewModel.getLatestLocationOnce(false, true) { map ->
                    map?.let { currentUbication = it }
                }
            }

            else -> Toast.makeText(
                requireContext(),
                "Permiso de ubicación denegado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_create_service, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        serviceId = arguments?.getString("serviceId")
        isEditMode = serviceId != null

        bindViews(view)
        setupCommonUI(view)
        setupListeners()
        observeViewModel()

        if (isEditMode) {
            loadingDialog.show()
            viewModel.loadServiceById(serviceId!!)
        } else {
            serviceId = UUID.randomUUID().toString()
            checkLocationPermissionAndGetLocation()
            loadCurrentPlan()
            updateImagesUI()
        }
    }

    private fun bindViews(view: View) {
        loadingDialog = LoadingDialog(requireContext())
        offerTab = view.findViewById(R.id.offer_tab)
        etOfferTitle = view.findViewById(R.id.et_offer_title)
        etCategory = view.findViewById(R.id.et_category)
        etOfferDescription = view.findViewById(R.id.et_offer_description)
        etOfferPrice = view.findViewById(R.id.et_offer_price)
        etAddonName = view.findViewById(R.id.et_add_on_name)
        etAddonDescription = view.findViewById(R.id.et_add_on_description)
        btnAddAnotherAddon = view.findViewById(R.id.btn_add_another_addon)
        containerAddons = view.findViewById(R.id.container_addons_dynamic)
        flUploadImages = view.findViewById(R.id.fl_upload_images)
        containerImages = view.findViewById(R.id.container_selected_images)
        etUserName = view.findViewById(R.id.et_user_name)
        userNameContainer = view.findViewById(R.id.user_name)
        btnCreate = view.findViewById(R.id.btn_create_service)
        btnCancel = view.findViewById(R.id.btn_cancel)
        rgRequiresVisit = view.findViewById(R.id.rg_requires_visit)
    }

    private fun setupCommonUI(view: View) {
        btnCreate.text = if (isEditMode) {
            getString(R.string.update_service_title)
        } else {
            getString(R.string.create_service_title)
        }
        offerTab.check(R.id.offer_tab_simple)

        authRepository.getCurrentUserId()?.let { userId ->
            authRepository.getUserById(userId) { result ->
                result.onSuccess { user ->
                    userNameContainer.isVisible = user.role == "admin"
                }
            }
        }

        // Cargar categorías
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val categoryNames = mutableListOf("Seleccionar categoría")
            categoryNames.addAll(categories.map { it.second })
            val adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            etCategory.adapter = adapter

            // Si estamos editando, seleccionar la categoría correspondiente
            if (isEditMode) {
                viewModel.service.value?.let { service ->
                    val categoryIndex = categories.indexOfFirst { it.first == service.categoryId }
                    if (categoryIndex != -1) {
                        etCategory.setSelection(categoryIndex + 1)
                    }
                }
            }
        }
        viewModel.getCategories()

        // Cargar proveedores
        viewModel.users.observe(viewLifecycleOwner) { formattedUsers ->
            val userNames = mutableListOf("Seleccionar proveedor")
            userNames.addAll(formattedUsers)
            val adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, userNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            etUserName.adapter = adapter

            // Si estamos editando, seleccionar el proveedor correspondiente
            if (isEditMode) {
                viewModel.service.value?.let { service ->
                    val providerIndex =
                        formattedUsers.indexOfFirst { it.endsWith("(${service.providerId})") }
                    if (providerIndex != -1) {
                        etUserName.setSelection(providerIndex + 1)
                    }
                }
            }
        }
        viewModel.getUsers()

        val header = view.findViewById<View>(R.id.header)
        header.findViewById<TextView>(R.id.txtTitle).text =
            if (isEditMode) getString(R.string.update_service_title) else getString(R.string.create_service_title)

        header.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        header.findViewById<View>(R.id.btnNotification).visibility = View.GONE
        header.findViewById<View>(R.id.btnSearch).visibility = View.GONE
        header.findViewById<View>(R.id.btnFilter).visibility = View.GONE
        header.findViewById<View>(R.id.btnOptions).visibility = View.GONE
    }

    // Cargar los datos del servicio
    private fun populateUiForEdit(service: Service) {
        etOfferTitle.setText(service.title)
        rgRequiresVisit.check(if (service.requiresVisit) R.id.rb_visit_yes else R.id.rb_visit_no)
        currentUbication = service.ubication

        for (i in imageItems.indices) {
            imageItems[i] = service.imageUrl.getOrNull(i)
            uploadedImageUrls[i] = service.imageUrl.getOrNull(i)
        }
        updateImagesUI()

        service.plans.forEach { plan ->
            val index = when (plan.planName) {
                "Simple" -> 0
                "Básico" -> 1
                "Premium" -> 2
                else -> -1
            }
            if (index != -1) {
                plans[index] = plan
            }
        }

        addonsList.forEach { it.clear() }

        service.addons.forEach { addon ->
            val idx = try {
                addon.planIndex.coerceIn(0, addonsList.size - 1)
            } catch (e: Exception) {
                0
            }
            addonsList[idx].add(addon)
        }

        loadCurrentPlan()

        // Cargar la categoría
        viewModel.categories.value?.let { categories ->
            val categoryIndex = categories.indexOfFirst { it.first == service.categoryId }
            if (categoryIndex != -1) {
                etCategory.setSelection(categoryIndex + 1)
            }
        }

        // Cargar el proveedor
        viewModel.users.value?.let { formattedUsers ->
            val providerIndex =
                formattedUsers.indexOfFirst { it.endsWith("(${service.providerId})") }
            if (providerIndex != -1) {
                etUserName.setSelection(providerIndex + 1)
            }
        }

        loadingDialog.dismiss()
    }

    // Funciones de ubicación
    private fun checkLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.getLatestLocationOnce(true, false) { map ->
                map?.let { currentUbication = it }
            }
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun setupListeners() {
        flUploadImages.setOnClickListener { imagePicker.launch("image/*") }

        btnAddAnotherAddon.setOnClickListener {
            if (containerAddons.childCount < 5) {
                val addonView =
                    layoutInflater.inflate(R.layout.item_addon_input, containerAddons, false)
                containerAddons.addView(addonView)
            } else {
                Toast.makeText(requireContext(), "Máximo 5 addons", Toast.LENGTH_SHORT).show()
            }
        }

        offerTab.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                saveCurrentPlan()
                saveAddons()
                currentPlanIndex = when (checkedId) {
                    R.id.offer_tab_simple -> 0
                    R.id.offer_tab_basic -> 1
                    R.id.offer_tab_premium -> 2
                    else -> 0
                }
                loadCurrentPlan()
            }
        }

        btnCreate.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener

            saveCurrentPlan()
            saveAddons()

            val hasFine = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            viewModel.getLatestLocationOnce(hasFine, hasCoarse) { map ->
                map?.let { currentUbication = it }

                loadingDialog.show()
                btnCreate.isEnabled = false

                val imagesToUpload = imageItems.mapIndexedNotNull { index, item ->
                    if (item is Uri) index to item else null
                }

                val providerId: String = if (userNameContainer.isVisible) {
                    val selectedUserFormatted = etUserName.selectedItem.toString()
                    selectedUserFormatted.substringAfterLast('(').substringBeforeLast(')')
                } else {
                    authRepository.getCurrentUserId()!!
                }

                val categoryPosition = etCategory.selectedItemPosition
                val selectedCategoryId =
                    viewModel.categories.value?.getOrNull(categoryPosition - 1)?.first ?: ""

                val createdAtValue = if (isEditMode) viewModel.service.value?.createdAt
                    ?: System.currentTimeMillis() else System.currentTimeMillis()

                val input = ServiceViewModel.ServiceUpsertInput(
                    serviceId = serviceId ?: "",
                    providerId = providerId,
                    title = etOfferTitle.text.toString().trim(),
                    categoryId = selectedCategoryId,
                    description = "",
                    existingImageUrls = uploadedImageUrls,
                    newImages = imagesToUpload,
                    deletedImageUrls = deletedImageUrls.toList(),
                    createdAt = createdAtValue,
                    plans = plans.filterNotNull().toList(),
                    addons = addonsList.flatMap { it },
                    requiresVisit = rgRequiresVisit.checkedRadioButtonId == R.id.rb_visit_yes,
                    ubication = currentUbication
                )

                viewModel.upsertService(input)
            }
        }

        btnCancel.setOnClickListener {
            try {
                if (isAdded && !isDetached) {
                    findNavController().navigateUp()
                } else {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            } catch (e: Exception) {
                try {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                } catch (e2: Exception) {
                    requireActivity().finish()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.service.observe(viewLifecycleOwner) { service ->
            // Si estamos editando, cargar los datos del servicio
            if (service != null && isEditMode && service.serviceId == serviceId) {
                populateUiForEdit(service)
            }
        }

        // Observar el estado de la operación de actualización
        viewModel.upsertState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UpsertState.Loading -> {
                    loadingDialog.show()
                }

                is UpsertState.Uploading -> {
                    loadingDialog.show()
                }

                is UpsertState.Success -> {
                    loadingDialog.dismiss()
                    val message =
                        "Servicio ${if (isEditMode) "actualizado" else "creado"} correctamente."
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    btnCreate.isEnabled = true
                    try {
                        val handled = findNavController().navigateUp()
                        if (!handled) requireActivity().onBackPressedDispatcher.onBackPressed()
                    } catch (e: Exception) {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }

                is UpsertState.Error -> {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Error al guardar servicio: ${state.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    btnCreate.isEnabled = true
                }

                null -> {
                }
            }
        }
    }

    // Funciones de validación
    private fun validateInputs(): Boolean {
        if (etOfferTitle.text.isBlank()) {
            Toast.makeText(requireContext(), "Por favor, completa el título", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (etCategory.selectedItemPosition == 0) {
            Toast.makeText(
                requireContext(),
                "Por favor, selecciona una categoría",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (imageItems.all { it == null }) {
            Toast.makeText(requireContext(), "Debes subir al menos una imagen", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (userNameContainer.isVisible && etUserName.selectedItemPosition == 0) {
            Toast.makeText(
                requireContext(),
                "Por favor, selecciona un proveedor.",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun saveCurrentPlan() {
        val priceText = etOfferPrice.text.toString()
        val priceValue = priceText.toDoubleOrNull() ?: 0.0

        val plan = ServicePlan(
            planName = when (currentPlanIndex) {
                0 -> "Simple"
                1 -> "Básico"
                else -> "Premium"
            },
            planDescription = etOfferDescription.text.toString(),
            price = if (priceText.isEmpty()) 0.0 else priceValue,
            features = emptyList()
        )
        plans[currentPlanIndex] = plan
    }

    private fun saveAddons() {
        addonsList[currentPlanIndex].clear()

        val fixedName = etAddonName.text.toString()
        val fixedDesc = etAddonDescription.text.toString()
        if (fixedName.isNotEmpty() || fixedDesc.isNotEmpty()) {
            addonsList[currentPlanIndex].add(ServiceAddon(fixedName, fixedDesc, currentPlanIndex))
        }

        for (i in 0 until containerAddons.childCount) {
            val child = containerAddons.getChildAt(i)
            val nameField = child.findViewById<EditText>(R.id.et_addon_name_dynamic)
            val descField = child.findViewById<EditText>(R.id.et_addon_description_dynamic)
            if (!nameField.text.isNullOrEmpty() || !descField.text.isNullOrEmpty()) {
                addonsList[currentPlanIndex].add(
                    ServiceAddon(
                        nameField.text.toString(),
                        descField.text.toString(),
                        currentPlanIndex
                    )
                )
            }
        }
    }

    private fun loadCurrentPlan() {
        val plan = plans[currentPlanIndex]
        etOfferPrice.setText(
            if (plan?.price != null && plan.price != 0.0) plan.price.toString() else ""
        )
        etOfferDescription.setText(plan?.planDescription ?: "")

        if (addonsList[currentPlanIndex].isNotEmpty()) {
            val firstAddon = addonsList[currentPlanIndex][0]
            etAddonName.setText(firstAddon.addonTitle)
            etAddonDescription.setText(firstAddon.addonDescription)
        } else {
            etAddonName.setText("")
            etAddonDescription.setText("")
        }

        containerAddons.removeAllViews()
        addonsList[currentPlanIndex].drop(1).forEach { addon ->
            val addonView =
                layoutInflater.inflate(R.layout.item_addon_input, containerAddons, false)
            val nameField = addonView.findViewById<EditText>(R.id.et_addon_name_dynamic)
            val descField = addonView.findViewById<EditText>(R.id.et_addon_description_dynamic)
            nameField.setText(addon.addonTitle)
            descField.setText(addon.addonDescription)
            containerAddons.addView(addonView)
        }
    }

    private fun updateImagesUI() {
        containerImages.removeAllViews()
        containerImages.isVisible = imageItems.any { it != null }

        // Actualizar la vista de las imágenes
        imageItems.forEachIndexed { index, item ->
            if (item != null) {
                val imageViewLayout =
                    layoutInflater.inflate(R.layout.item_deselected_image, containerImages, false)
                val iv = imageViewLayout.findViewById<ImageView>(R.id.iv_selected_image_1)
                val btnRemove = imageViewLayout.findViewById<ImageButton>(R.id.btn_remove_image)

                when (item) {
                    is Uri -> iv.setImageURI(item)
                    is String -> Glide.with(this).load(item).into(iv)
                }

                btnRemove.setOnClickListener {
                    val removedItem = item
                    if (removedItem is String && removedItem.isNotEmpty()) {
                        if (!deletedImageUrls.contains(removedItem)) {
                            deletedImageUrls.add(removedItem)
                        }
                    }
                    imageItems[index] = null
                    uploadedImageUrls[index] = null
                    updateImagesUI()
                }
                containerImages.addView(imageViewLayout)
            }
        }

        flUploadImages.isVisible = imageItems.any { it == null }
    }
}