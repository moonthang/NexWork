package com.example.nexwork.ui.users

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexwork.Home
import com.example.nexwork.R
import com.example.nexwork.data.model.User
import com.example.nexwork.databinding.FragmentUserListBinding

class UserListFragment : Fragment() {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!

    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeader()
        setupSearchView()
        setupRecyclerView()
    }

    private fun setupHeader() {

        // Creo que tocara usar Navigation Component para navegar entre fragmentos
        binding.header.txtTitle.text = getString(R.string.btn_users_menu_str)
        binding.header.btnBack.setOnClickListener {
            val intent = Intent(requireActivity(), Home::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun setupSearchView() {
        val searchEditText = binding.searchLayout.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
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
        binding.rvUsers.apply {
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
            // Lógica para editar usuario
            dialog.dismiss()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}