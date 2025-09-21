package com.example.nexwork.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexwork.R

class UserListFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.users_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        userAdapter = UserAdapter(
            onEditClick = { user ->
                // TODO: Navigate to UserDetailFragment to edit user
            },
            onDeleteClick = { user ->
                viewModel.deleteUser(user.userId)
            }
        )
        recyclerView.adapter = userAdapter

        observeViewModel()
        viewModel.getAllUsers()
    }

    private fun observeViewModel() {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            userAdapter.submitList(users)
        }

        viewModel.operationStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(context, "Operation successful", Toast.LENGTH_SHORT).show()
                viewModel.getAllUsers() // Refresh the list
            }.onFailure {
                Toast.makeText(context, "Operation failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}