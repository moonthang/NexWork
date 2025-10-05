package com.example.nexwork.ui.services

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.nexwork.R
import com.example.nexwork.data.model.Service
import com.example.nexwork.data.model.ServiceAddon
import com.example.nexwork.data.model.ServicePlan
import com.example.nexwork.data.repository.AuthRepository
import java.util.*
import com.example.nexwork.core.LoadingDialog

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
    private lateinit var loadingDialog: LoadingDialog
    private val viewModel: ServiceViewModel by activityViewModels()
    private val authRepository = AuthRepository()
    private val selectedImages = mutableListOf<Uri>()
    private var currentPlanIndex = 0
    private val plans = arrayOfNulls<ServicePlan>(3)
    private val addonsList = arrayOf(
        mutableListOf<ServiceAddon>(),
        mutableListOf<ServiceAddon>(),
        mutableListOf<ServiceAddon>()
    )

    private val serviceId: String = UUID.randomUUID().toString()
    private val uploadedImageUrls = mutableListOf<String>()
    private var imagesToUploadCount = 0
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                if (selectedImages.size < 3) {
                    selectedImages.add(it)
                    updateImagesUI()
                } else {
                    Toast.makeText(requireContext(), "Máximo 3 imágenes", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_create_service, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadingDialog = LoadingDialog(requireContext())
        offerTab = view.findViewById(R.id.offer_tab)
        offerTab.check(R.id.offer_tab_simple)
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

        // Verificar si el usuario es administrador
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

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            etCategory.adapter = adapter
        }
        viewModel.getCategories()

        // Cargar proveedores
        viewModel.users.observe(viewLifecycleOwner) { formattedUsers ->
            val userNames = mutableListOf("Seleccionar proveedor")
            userNames.addAll(formattedUsers)

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                userNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            etUserName.adapter = adapter
        }
        viewModel.getUsers()

        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)

        txtTitle.text = getString(R.string.create_service_title)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupListeners()
        observeViewModel()
        loadCurrentPlan()
    }

    private fun setupListeners() {
        flUploadImages.setOnClickListener { imagePicker.launch("image/*") }

        // Botón para agregar otro addon
        btnAddAnotherAddon.setOnClickListener {
            if (containerAddons.childCount < 5) {
                val addonView = layoutInflater.inflate(R.layout.item_addon_input, containerAddons, false)
                containerAddons.addView(addonView)
            } else {
                Toast.makeText(requireContext(), "Máximo 5 addons", Toast.LENGTH_SHORT).show()
            }
        }

        // Control de cambio de pestaña
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

        // Crear servicio
        btnCreate.setOnClickListener {
            val title = etOfferTitle.text.toString().trim()
            val categoryPosition = etCategory.selectedItemPosition
            val description = etOfferDescription.text.toString().trim()
            val priceText = etOfferPrice.text.toString().trim()
            val userPosition = etUserName.selectedItemPosition

            if (title.isEmpty() || categoryPosition == 0 || description.isEmpty() || priceText.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos principales.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userNameContainer.isVisible && userPosition == 0) {
                Toast.makeText(requireContext(), "Por favor, selecciona un proveedor.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImages.isEmpty()) {
                Toast.makeText(requireContext(), "Debes subir al menos una imagen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveCurrentPlan()
            saveAddons()

            uploadedImageUrls.clear()
            imagesToUploadCount = selectedImages.size
            loadingDialog.show()

            selectedImages.forEachIndexed { index, uri ->
                viewModel.uploadServiceImage(serviceId, uri, index)
            }

            btnCreate.isEnabled = false
        }

        btnCancel.setOnClickListener { requireActivity().onBackPressed() }
    }

    private fun observeViewModel() {
        // Observar el estado de la URL de la imagen
        viewModel.uploadImageUrl.observe(viewLifecycleOwner) { result ->
            if (result.isSuccess) {
                val url = result.getOrThrow()
                uploadedImageUrls.add(url)

                // Si se han subido todas las imágenes, crear el servicio
                if (uploadedImageUrls.size == imagesToUploadCount) {
                    createServiceFinal()
                }
            } else {
                loadingDialog.dismiss()
                Toast.makeText(requireContext(), "Error al subir una imagen", Toast.LENGTH_LONG).show()
                btnCreate.isEnabled = true
                uploadedImageUrls.clear()
            }
        }
    }

    // Crear servicio y guardar en Firebase
    private fun createServiceFinal() {
        // Identificar el ID proveedor para obtener su ID
        val providerId: String
        if (userNameContainer.isVisible) {
            val selectedUserFormatted = etUserName.selectedItem.toString()
            providerId = selectedUserFormatted.substringAfterLast('(').substringBeforeLast(')')
        } else {
            providerId = authRepository.getCurrentUserId() ?: run {
                btnCreate.isEnabled = true
                loadingDialog.dismiss()
                return
            }
        }

        // Identificar el ID de categoría
        val categoryPosition = etCategory.selectedItemPosition
        val selectedCategoryId = viewModel.categories.value
            ?.getOrNull(categoryPosition - 1)
            ?.first ?: ""

        // Crea el servicio
        val service = Service(
            serviceId = serviceId,
            providerId = providerId,
            title = etOfferTitle.text.toString().trim(),
            categoryId = selectedCategoryId,
            description = etOfferDescription.text.toString().trim(),
            imageUrl = uploadedImageUrls.toList(),
            createdAt = System.currentTimeMillis(),
            plans = plans.filterNotNull().toList(),
            addons = addonsList.flatMap { it }
        )

        // Guarda el servicio en Firebase
        viewModel.createService(service)
        loadingDialog.dismiss()
        Toast.makeText(requireContext(), "Servicio creado correctamente.", Toast.LENGTH_LONG).show()
        requireActivity().onBackPressed()
        btnCreate.isEnabled = true
    }

    // Guardar el plan visible
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

    // Guardar los addons
    private fun saveAddons() {
        addonsList[currentPlanIndex].clear()

        val fixedName = etAddonName.text.toString()
        val fixedDesc = etAddonDescription.text.toString()
        if (fixedName.isNotEmpty() || fixedDesc.isNotEmpty()) {
            addonsList[currentPlanIndex].add(ServiceAddon(fixedName, fixedDesc))
        }

        for (i in 0 until containerAddons.childCount) {
            val child = containerAddons.getChildAt(i)
            val nameField = child.findViewById<EditText>(R.id.et_addon_name_dynamic)
            val descField = child.findViewById<EditText>(R.id.et_addon_description_dynamic)
            if (!nameField.text.isNullOrEmpty() || !descField.text.isNullOrEmpty()) {
                addonsList[currentPlanIndex].add(
                    ServiceAddon(nameField.text.toString(), descField.text.toString())
                )
            }
        }
    }

    // Guardar y mostrar datos del plan visible
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
            val addonView = layoutInflater.inflate(R.layout.item_addon_input, containerAddons, false)
            val nameField = addonView.findViewById<EditText>(R.id.et_addon_name_dynamic)
            val descField = addonView.findViewById<EditText>(R.id.et_addon_description_dynamic)
            nameField.setText(addon.addonTitle)
            descField.setText(addon.addonDescription)
            containerAddons.addView(addonView)
        }

        updateImagesUI()
    }

    // Actualizar UI de imágenes
    private fun updateImagesUI() {
        containerImages.removeAllViews()
        containerImages.isVisible = selectedImages.isNotEmpty()

        // Mostrar las imágenes seleccionadas
        for ((index, uri) in selectedImages.withIndex()) {
            val imageViewLayout =
                layoutInflater.inflate(R.layout.item_deselected_image, containerImages, false)
            val iv = imageViewLayout.findViewById<ImageView>(R.id.iv_selected_image_1)
            val btnRemove = imageViewLayout.findViewById<ImageButton>(R.id.btn_remove_image)

            iv.setImageURI(uri)
            btnRemove.setOnClickListener {
                selectedImages.removeAt(index)
                updateImagesUI()
            }

            containerImages.addView(imageViewLayout)
        }

        flUploadImages.isVisible = selectedImages.size < 3
    }
}