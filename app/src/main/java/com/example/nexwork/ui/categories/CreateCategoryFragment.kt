package com.example.nexwork.ui.categories

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.nexwork.R
import com.example.nexwork.data.model.Category
import com.example.nexwork.ui.profile.ProfileFragment
import java.util.UUID
import com.example.nexwork.core.LoadingDialog

class CreateCategoryFragment : Fragment() {

    private lateinit var loadingDialog: LoadingDialog
    private lateinit var etCategoryTitle: EditText
    private lateinit var btnCreate: Button
    private lateinit var btnCancel: Button
    private lateinit var flUploadImage: FrameLayout
    private lateinit var previewImage: ImageView
    private val viewModel: CategoriesViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var categoryId: String = UUID.randomUUID().toString()
    private val imagePicker =
        // Manejar la selección de imágenes
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                previewImage.setImageURI(selectedImageUri)
                previewImage.visibility = View.VISIBLE
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_category, container, false)

        loadingDialog = LoadingDialog(requireContext())
        etCategoryTitle = view.findViewById(R.id.et_category_title)
        btnCreate = view.findViewById(R.id.btn_create_service)
        btnCancel = view.findViewById(R.id.btn_cancel)
        flUploadImage = view.findViewById(R.id.fl_upload_image_category)

        // Configurar la vista de la imagen
        previewImage = ImageView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            visibility = View.GONE
        }
        // Agregar la vista de la imagen al FrameLayout
        (flUploadImage as ViewGroup).addView(previewImage)

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

        txtTitle.setText(getString(R.string.create_category_title))
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setupListeners()
        observeViewModel()

        return view
    }

    private fun setupListeners() {
        // Configurar el clic en el FrameLayout para seleccionar una imagen
        flUploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            imagePicker.launch(intent)
        }

        btnCreate.setOnClickListener {
            val title = etCategoryTitle.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "Selecciona una imagen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loadingDialog.show()
            selectedImageUri?.let { uri ->
                viewModel.uploadCategoryImage(categoryId, uri)
            }
        }

        btnCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeViewModel() {
        // Observar el estado de la URL de la imagen
        viewModel.uploadImageUrl.observe(viewLifecycleOwner) { result ->
            result.onSuccess { url ->
                // Crear la categoría con la URL de la imagen
                val category = Category(
                    categoryId = categoryId,
                    name = etCategoryTitle.text.toString().trim(),
                    imageUrl = url
                )
                viewModel.createCategory(category)
            }
            result.onFailure {
                loadingDialog.dismiss()
                Toast.makeText(requireContext(), "Error al subir imagen", Toast.LENGTH_SHORT).show()
            }
        }

        // Observar el estado de la operación de creación de la categoría
        viewModel.operationStatus.observe(viewLifecycleOwner) { result ->
            loadingDialog.dismiss()
            result.onSuccess {
                Toast.makeText(requireContext(), "Categoría creada correctamente", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            result.onFailure {
                Toast.makeText(requireContext(), "Error al guardar categoría", Toast.LENGTH_SHORT).show()
            }
        }
    }
}