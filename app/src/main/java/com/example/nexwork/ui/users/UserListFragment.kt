package com.example.nexwork.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexwork.R
import com.example.nexwork.core.OptionsDialogFragment
import com.example.nexwork.data.model.User
import com.example.nexwork.databinding.FragmentUserListBinding

class UserListFragment : Fragment(),
    UserAdapter.OnItemClickListener,
    OptionsDialogFragment.OptionsDialogListener {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserViewModel by viewModels()

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
        setupFabAddUserClickListener()
        setupHeader()
        setupSearchView()
        setupRecyclerView()
        observeViewModel()
        viewModel.getAllUsers()
    }

    private fun setupHeader() {
        binding.header.txtTitle.text = getString(R.string.btn_users_menu_str)
        binding.header.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.header.btnNotification.visibility = View.GONE
        binding.header.btnSearch.visibility = View.GONE
        binding.header.btnFilter.visibility = View.GONE
        binding.header.btnOptions.visibility = View.GONE
    }

    private fun setupSearchView() {
        val searchEditText = binding.searchLayout.searchView
            .findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.text_primary)
        )
        searchEditText.setHintTextColor(
            ContextCompat.getColor(requireContext(), R.color.text_secondary)
        )
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(emptyList(), this)
        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            userAdapter = UserAdapter(users, this)
            binding.rvUsers.adapter = userAdapter
        }


        viewModel.operationStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Operación exitosa", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFabAddUserClickListener() {
        binding.fabAddUser.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateUserFragment())
                .addToBackStack(null)
                .commit()
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
        val user = selectedUser ?: return
        when (option) {
            getString(R.string.edit_user_option) -> {
                Toast.makeText(requireContext(), "Editar usuario: ${user.firstName}", Toast.LENGTH_SHORT).show()
                // Aquí podrías navegar a un fragmento de edición con Navigation Component
            }

            getString(R.string.view_details_option) -> {
                Toast.makeText(requireContext(), "Detalles de: ${user.firstName}", Toast.LENGTH_SHORT).show()
                // Aquí podrías abrir un fragmento con detalles del usuario
            }

            getString(R.string.delete_user_option) -> {
                viewModel.deleteUser(user.userId)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
