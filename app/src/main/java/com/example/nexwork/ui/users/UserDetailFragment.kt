package com.example.nexwork.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.nexwork.R
import com.example.nexwork.data.model.User

class UserDetailFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(ARG_USER_ID)?.let { userId ->
            viewModel.getUserById(userId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val headerView = view.findViewById<View>(R.id.header)
        val titleTextView = headerView.findViewById<TextView>(R.id.txtTitle)
        
        val firstNameEditText = view.findViewById<EditText>(R.id.first_name_edit_text)
        val lastNameEditText = view.findViewById<EditText>(R.id.last_name_edit_text)
        val emailEditText = view.findViewById<EditText>(R.id.email_edit_text)
        val phoneEditText = view.findViewById<EditText>(R.id.phone_edit_text)
        val saveButton = view.findViewById<Button>(R.id.save_button)

        observeViewModel(titleTextView, firstNameEditText, lastNameEditText, emailEditText, phoneEditText)

        saveButton.setOnClickListener {
            val updatedUser = (currentUser ?: User()).copy(
                firstName = firstNameEditText.text.toString(),
                lastName = lastNameEditText.text.toString(),
                email = emailEditText.text.toString(),
                phone = phoneEditText.text.toString()
            )

            if (currentUser?.userId.isNullOrEmpty()) {
                 // viewModel.createUser(updatedUser)
            } else {
                viewModel.updateUser(updatedUser)
            }
        }
    }

    private fun observeViewModel(
        titleTextView: TextView,
        firstNameEditText: EditText,
        lastNameEditText: EditText,
        emailEditText: EditText,
        phoneEditText: EditText
    ) {
        if (arguments?.getString(ARG_USER_ID) == null) {
            titleTextView.text = "Crear Usuario"
            // Puedes establecer valores predeterminados o dejar los campos vacíos
        } else {
            viewModel.user.observe(viewLifecycleOwner) { user ->
                currentUser = user
                titleTextView.text = "Editar Usuario"
                firstNameEditText.setText(user.firstName)
                lastNameEditText.setText(user.lastName)
                emailEditText.setText(user.email)
                phoneEditText.setText(user.phone)
            }
        }

        viewModel.operationStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(context, "Guardado con éxito", Toast.LENGTH_SHORT).show()
                // TODO: Navegar hacia atrás
            }.onFailure {
                Toast.makeText(context, "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: String?) =
            UserDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
    }
}