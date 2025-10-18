package com.example.nexwork.ui.users

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.nexwork.R
import com.example.nexwork.data.model.User
import com.example.nexwork.databinding.FragmentCreateUserBinding
import com.example.nexwork.ui.auth.AuthViewModel
import com.example.nexwork.ui.auth.RegistrationState
import com.example.nexwork.ui.home.Home
import java.util.Calendar

class CreateUserFragment : Fragment() {

    private var _binding: FragmentCreateUserBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()
        setupListeners()
        setupSpinner()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener { registerUserAttempt() }
        binding.hintBirthDate.setOnClickListener { showDatePickerDialog() }

    }

    private fun setupHeader() {

        binding.header.txtTitle.text = getString(R.string.create_user_str)
        binding.header.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()

        }

        binding.header.btnNotification.visibility = View.GONE
        binding.header.btnSearch.visibility = View.GONE
        binding.header.btnFilter.visibility = View.GONE
        binding.header.btnOptions.visibility = View.GONE
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.user_roles,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerRole.adapter = adapter
        }
    }

    private fun observeViewModel() {
        userViewModel.registrationState.observe(viewLifecycleOwner, Observer {
            when (it) {
                is RegistrationState.Loading -> {
                    // Puedes mostrar un ProgressBar o un diálogo de carga aquí
                    Toast.makeText(requireContext(), "Registrando usuario...", Toast.LENGTH_SHORT).show()
                }
                is RegistrationState.Success -> {
                    Toast.makeText(requireContext(), "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                is RegistrationState.Error -> {
                    Toast.makeText(requireContext(), "Error al registrar usuario: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.hintBirthDate.setText(selectedDate)
            },
            year, month, day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun registerUserAttempt() {
        val firstName = binding.hintFirstName.text.toString()
        val lastName = binding.hintLastName.text.toString()
        val email = binding.hintEmail.text.toString()
        val birthDate = binding.hintBirthDate.text.toString()
        val phone = binding.hintPhone.text.toString()
        val password = binding.hintPassword.text.toString()
        val confirmPassword = binding.hintConfirmPassword.text.toString()
        val userRole = binding.spinnerRole.selectedItem.toString()

        if (validateFields(
                firstName,
                lastName,
                email,
                birthDate,
                phone,
                password,
                confirmPassword,
                userRole
            )
        ) {
            val newUser = User(
                firstName = firstName,
                lastName = lastName,
                email = email,
                birthDate = birthDate,
                phone = phone,
                password = password,
                role = userRole
            )

            userViewModel.registerUser(newUser)
        }
    }

    private fun validateFields(
        firstName: String,
        lastName: String,
        email: String,
        birthDate: String,
        phone: String,
        password: String,
        confirmPassword: String,
        spinnerRole: String
    ): Boolean {
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
            birthDate.isEmpty() || phone.isEmpty() || password.isEmpty() ||
            confirmPassword.isEmpty()|| spinnerRole.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, todos los campos deben llenarse.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Por favor, ingresa un correo eléctronico válido.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(requireContext(), "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
