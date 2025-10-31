package com.example.nexwork.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.nexwork.R


class UserDetailFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Obtener argumento (si viene)
        arguments?.getString(ARG_USER_ID)?.let { userId ->
            viewModel.getUserById(userId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_user_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        val userName = view.findViewById<TextView>(R.id.user_name)
        val userRole = view.findViewById<TextView>(R.id.user_role)
        val email = view.findViewById<TextView>(R.id.tvEmail)
        val phone = view.findViewById<TextView>(R.id.tvPhone)
        val birthDate = view.findViewById<TextView>(R.id.tvBirthDate)

        // Observar los datos del ViewModel
        viewModel.user.observe(viewLifecycleOwner) { user ->
            userName.text = "${user.firstName} ${user.lastName}"
            userRole.text = user.role ?: ""
            email.text = user.email
            phone.text = "Teléfono: ${user.phone ?: "-"}"
            birthDate.text = "Fecha de nacimiento: ${user.birthDate ?: "-"}"

            // Cargar imagen si tienes URL (usando Glide/Picasso)
            // Glide.with(this).load(user.profileImageUrl).into(profileImage)
        }
    }

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: String): UserDetailFragment {
            val fragment = UserDetailFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_USER_ID, userId)
            }
            return fragment
        }
    }
}

