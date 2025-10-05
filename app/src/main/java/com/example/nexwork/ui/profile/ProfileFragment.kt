package com.example.nexwork.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.nexwork.ui.home.Home
import com.example.nexwork.R
import com.example.nexwork.data.model.User
import com.example.nexwork.data.repository.AuthRepository
import com.google.firebase.storage.FirebaseStorage
import com.example.nexwork.core.LoadingDialog
import com.example.nexwork.ui.profile.account.AccountFragment

class ProfileFragment : Fragment() {

    private val authRepository = AuthRepository()
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())
        loadingDialog.show()

        // Obtener el ID del usuario actual
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId != null) {
            // Obtener los detalles del usuario
            authRepository.getUserById(currentUserId) { result ->
                loadingDialog.dismiss()
                if (result.isSuccess) {
                    // Actualizar la interfaz de usuario con los detalles del usuario
                    val user = result.getOrNull()
                    if (user != null) {
                        setupUI(view, user)
                    }
                }
            }
        } else {
            loadingDialog.dismiss()
        }

        val sectionProfile = view.findViewById<View>(R.id.section_profile)
        sectionProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AccountFragment())
                .addToBackStack(null)
                .commit()
        }


        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)

        txtTitle.text = getString(R.string.profile_title)
        btnBack.setOnClickListener {
            val intent = Intent(requireActivity(), Home::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun setupUI(view: View, user: User) {
        val userName = view.findViewById<TextView>(R.id.user_name)
        val userRole = view.findViewById<TextView>(R.id.user_role)
        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        val sectionSavedList  = view.findViewById<LinearLayout>(R.id.section_saved_list)
        val sectionProviderPanel  = view.findViewById<LinearLayout>(R.id.section_provider_panel)

        userName.text = "${user.firstName} ${user.lastName}"
        userRole.text = user.role

        loadProfileImage(user.profileImageUrl, profileImage)

        // Mostrar u ocultar secciones según el rol del usuario
        when (user.role) {
            "client" -> {
                sectionSavedList.visibility = View.VISIBLE
                sectionProviderPanel.visibility = View.GONE
            }
            "provider" -> {
                sectionSavedList.visibility = View.GONE
                sectionProviderPanel.visibility = View.VISIBLE
            }
            "admin" -> {
                sectionSavedList.visibility = View.GONE
                sectionProviderPanel.visibility = View.GONE
            }
        }
    }

    private fun loadProfileImage(imagePath: String?, imageView: ImageView) {
        // Cargar la imagen de perfil
        if (imagePath.isNullOrEmpty()) {
            Glide.with(this)
                .load(R.drawable.ic_profile)
                .into(imageView)
            return
        }

        // Si la URL de la imagen contiene "http", cargarla directamente
        if (imagePath.startsWith("http")) {
            Glide.with(this)
                .load(imagePath)
                .placeholder(R.drawable.ic_profile)
                .into(imageView)
        } else {
            // Si la URL no contiene "http", descargarla de Firebase Storage
            val imageRef = FirebaseStorage.getInstance().reference.child(imagePath)
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_profile)
                    .into(imageView)
            }.addOnFailureListener {
                // Si falla al descargar la imagen, cargar una imagen predeterminada
                Glide.with(this)
                    .load(R.drawable.ic_profile)
                    .into(imageView)
            }
        }
    }
}