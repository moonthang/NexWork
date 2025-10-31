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
import com.example.nexwork.R.id.fragment_container
import com.example.nexwork.data.model.User
import com.example.nexwork.data.repository.AuthRepository
import com.google.firebase.storage.FirebaseStorage
import com.example.nexwork.core.LoadingDialog
import com.example.nexwork.ui.categories.CategoriesFragment
import com.example.nexwork.ui.home.HomeProviderFragment
import com.example.nexwork.ui.profile.account.AccountFragment
import com.example.nexwork.ui.services.MyServicesFragment
import com.example.nexwork.ui.users.UserListFragment

class ProfileFragment : Fragment() {

    private val authRepository = AuthRepository()
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val contentLayout = view.findViewById<View>(R.id.ProfileFragment)
        contentLayout.visibility = View.GONE

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val contentLayout = view.findViewById<View>(R.id.ProfileFragment)
        loadingDialog = LoadingDialog(requireContext())
        loadingDialog.show()

        // Obtener el ID del usuario actual
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId != null) {
            // Obtener los detalles del usuario
            authRepository.getUserById(currentUserId) { result ->
                loadingDialog.dismiss()
                contentLayout.visibility = View.VISIBLE
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
            contentLayout.visibility = View.VISIBLE
        }

        // redirije a account
        val sectionProfile = view.findViewById<View>(R.id.section_profile)
        sectionProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(fragment_container, AccountFragment())
                .addToBackStack(null)
                .commit()
        }

        //redirije hacia users
        val sectionManageUsers = view.findViewById<LinearLayout>(R.id.manage_users)
        sectionManageUsers.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(fragment_container, UserListFragment())
                .addToBackStack(null)
                .commit()
        }

        //redirije hacia el historial de servicios
        val sectionManageOrders = view.findViewById<LinearLayout>(R.id.section_manage_orders)
        sectionManageOrders.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(fragment_container, MyServicesFragment())
                .addToBackStack(null)
                .commit()
        }

        // redirije hacia las categorias
        val sectionCategories = view.findViewById<LinearLayout>(R.id.section_manage_categories)
        sectionCategories.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(fragment_container, CategoriesFragment())
                .addToBackStack(null)
                .commit()
        }

        // redirije hacia el panel de proveedores
        val sectionProviderPanel = view.findViewById<LinearLayout>(R.id.section_provider_panel)
        sectionProviderPanel.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(fragment_container, HomeProviderFragment())
                .addToBackStack(null)
                .commit()
        }

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

        txtTitle.setText(getString(R.string.profile_title))
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    //Manejo de visibilidad dependiendo el rol
    private fun setupUI(view: View, user: User) {
        val userName = view.findViewById<TextView>(R.id.user_name)
        val userRole = view.findViewById<TextView>(R.id.user_role)
        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        val sectionSavedList  = view.findViewById<LinearLayout>(R.id.section_saved_list)
        val sectionProviderPanel  = view.findViewById<LinearLayout>(R.id.section_provider_panel)
        val sectionManageUsers  = view.findViewById<LinearLayout>(R.id.manage_users)
        val sectionManageOrders  = view.findViewById<LinearLayout>(R.id.section_manage_orders)
        val sectionManageCategories  = view.findViewById<LinearLayout>(R.id.section_manage_categories)
        val sectionInviteFriends  = view.findViewById<LinearLayout>(R.id.section_invite_friends)


        userName.text = "${user.firstName} ${user.lastName}"
        userRole.text = user.role

        loadProfileImage(user.profileImageUrl, profileImage)

        // Mostrar u ocultar secciones según el rol del usuario
        when (user.role) {
            "client" -> {
                sectionSavedList.visibility = View.VISIBLE
                sectionProviderPanel.visibility = View.GONE
                sectionManageOrders.visibility = View.VISIBLE
                sectionManageCategories.visibility = View.GONE
                sectionManageUsers.visibility = View.GONE
            }
            "provider" -> {
                sectionSavedList.visibility = View.GONE
                sectionProviderPanel.visibility = View.VISIBLE
                sectionManageOrders.visibility = View.VISIBLE
                sectionManageCategories.visibility = View.GONE
                sectionManageUsers.visibility = View.GONE
            }
            "admin" -> {
                sectionSavedList.visibility = View.GONE
                sectionProviderPanel.visibility = View.VISIBLE
                sectionInviteFriends.visibility = View.GONE

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