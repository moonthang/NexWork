package com.example.nexwork.ui.users

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexwork.Home
import com.example.nexwork.R
import com.example.nexwork.core.OptionsDialogFragment
import com.example.nexwork.data.model.User
import com.example.nexwork.databinding.FragmentUserListBinding

class UserListFragment : Fragment(), UserAdapter.OnItemClickListener, OptionsDialogFragment.OptionsDialogListener {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!

    private lateinit var userAdapter: UserAdapter
    private var selectedUser: User? = null

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

        userAdapter = UserAdapter(dummyUsers, this)
        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }
    }

    override fun onItemClick(user: User) {
        selectedUser = user
        val dialog = OptionsDialogFragment.newInstance(
            title = user.firstName,
            option1 = getString(R.string.edit_user_option),
            option2 = getString(R.string.view_details_option),
            option3 = getString(R.string.delete_user_option)
        )
        dialog.setOptionsDialogListener(this)
        dialog.show(parentFragmentManager, "OptionsDialogFragment")
    }

    override fun onOptionSelected(option: String) {
        val user = selectedUser?.firstName ?: ""
        when (option) {
            getString(R.string.edit_user_option) -> {
                // TODO: Implement edit logic
                android.widget.Toast.makeText(requireContext(), "Edit user: $user", android.widget.Toast.LENGTH_SHORT).show()
            }
            getString(R.string.view_details_option) -> {
                // TODO: Implement view details logic
                android.widget.Toast.makeText(requireContext(), "View details for: $user", android.widget.Toast.LENGTH_SHORT).show()
            }
            getString(R.string.delete_user_option) -> {
                // TODO: Implement delete logic
                android.widget.Toast.makeText(requireContext(), "Delete user: $user", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}