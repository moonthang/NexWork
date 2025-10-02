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
import com.example.nexwork.Home
import com.example.nexwork.R
import com.example.nexwork.R.id.fragment_container
import com.example.nexwork.data.model.User
import com.example.nexwork.data.repository.AuthRepository
import com.google.firebase.storage.FirebaseStorage
import com.example.nexwork.core.LoadingDialog
import com.example.nexwork.ui.categories.CategoriesFragment
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
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())
        loadingDialog.show()

        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId != null) {
            authRepository.getUserById(currentUserId) { result ->
                loadingDialog.dismiss()
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user != null) {
                        setupUI(view, user)
                    }
                }
            }
        } else {
            loadingDialog.dismiss()
        }

        // redirije a la account
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

        //redirije hacia orders
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




        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)

        txtTitle.text = getString(R.string.profile_title)
        btnBack.setOnClickListener {
            val intent = Intent(requireActivity(), Home::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    //Manejo de visibilidad dependiendo el rol
    private fun setupUI(view: View, user: User) {
        val userName = view.findViewById<TextView>(R.id.user_name)
        val userRole = view.findViewById<TextView>(R.id.user_role)
        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        val sectionSavedList  = view.findViewById<LinearLayout>(R.id.section_saved_list)
        val sectionProviderPanel  = view.findViewById<LinearLayout>(R.id.section_provider_panel)
        val sectionInviteFriends  = view.findViewById<LinearLayout>(R.id.section_invite_friends)


        userName.text = "${user.firstName} ${user.lastName}"
        userRole.text = user.role

        loadProfileImage(user.profileImageUrl, profileImage)

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
                sectionInviteFriends.visibility = View.GONE

            }
        }
    }

    private fun loadProfileImage(imagePath: String?, imageView: ImageView) {
        if (imagePath.isNullOrEmpty()) {
            Glide.with(this)
                .load(R.drawable.ic_profile)
                .into(imageView)
            return
        }

        if (imagePath.startsWith("http")) {
            Glide.with(this)
                .load(imagePath)
                .placeholder(R.drawable.ic_profile)
                .into(imageView)
        } else {
            val imageRef = FirebaseStorage.getInstance().reference.child(imagePath)
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_profile)
                    .into(imageView)
            }.addOnFailureListener {
                Glide.with(this)
                    .load(R.drawable.ic_profile)
                    .into(imageView)
            }
        }
    }
}