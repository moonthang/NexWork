package com.example.nexwork.ui.users

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexwork.R
import com.example.nexwork.data.model.User

class UserListFragment : Fragment() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_list, container, false)
        rvUsers = view.findViewById(R.id.rvUsers)
        setupRecyclerView()
        return view
    }

    private fun setupRecyclerView() {
        // --- DATOS DE EJEMPLO ---
        val dummyUsers = listOf(
            User("1", "Ana López", "ana.lopez@email.com"),
            User("2", "Juan Pérez", "juan.perez@email.com"),
            User("3", "María García", "maria.garcia@email.com"),
            User("4", "Carlos Ruiz", "carlos.ruiz@email.com")
        )

        userAdapter = UserAdapter(dummyUsers) { user ->
            showUserOptionsDialog(user)
        }
        rvUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }
    }

    private fun showUserOptionsDialog(user: User) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_user_options)

        val title: TextView = dialog.findViewById(R.id.tvDialogTitle)
        val btnEdit: Button = dialog.findViewById(R.id.btnEditUser)
        val btnDetails: Button = dialog.findViewById(R.id.btnViewDetails)
        val btnDelete: Button = dialog.findViewById(R.id.btnDeleteUser)
        val btnCancel: Button = dialog.findViewById(R.id.btnCancel)

        title.text = "Opciones para ${user.firstName}"

        btnEdit.setOnClickListener {
          /*  // --- ¡NUEVA LÓGICA AQUÍ! ---
            // Inicia la EditProfileActivity cuando se pulsa el botón
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            // Opcional: pasar el ID del usuario a la actividad de edición
            // intent.putExtra("USER_ID", user.id)
            startActivity(intent)
            dialog.dismiss()*/
        }
        btnDetails.setOnClickListener {
            // Lógica para ver detalles
            dialog.dismiss()
        }
        btnDelete.setOnClickListener {
            // Lógica para eliminar usuario
            dialog.dismiss()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}