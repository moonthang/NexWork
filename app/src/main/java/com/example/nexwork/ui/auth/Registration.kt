package com.example.nexwork.ui.auth

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nexwork.ui.home.Home
import com.example.nexwork.R
import com.example.nexwork.core.LoadingDialog
import java.util.Calendar

class Registration : AppCompatActivity() {

    private var userRole: String = "client"
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var birthDateEditText: EditText

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadingDialog = LoadingDialog(this)
        userRole = intent.getStringExtra("userRole") ?: "client"
        if (intent.getStringExtra("userRole") == null) {
            Toast.makeText(
                this,
                "Se te asignó automáticamente el rol de CLIENTE.\nSi quieres cambiarlo, dirígete a Perfil → Cuenta.",
                Toast.LENGTH_LONG
            ).show()
        }
        birthDateEditText = findViewById(R.id.hint_birth_date)

        setupOnClickListener()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.registrationState.observe(this) { state ->
            when (state) {
                is RegistrationState.Loading -> {
                    loadingDialog.show()
                }
                is RegistrationState.Success -> {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Home::class.java).apply {
                        putExtra(Login.EXTRA_USER_ROLE, userRole)
                    }
                    startActivity(intent)
                    finish()
                }
                is RegistrationState.Error -> {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupOnClickListener() {
        val hint_first_name = findViewById<EditText>(R.id.hint_first_name)
        val hint_last_name = findViewById<EditText>(R.id.hint_last_name)
        val hint_email = findViewById<EditText>(R.id.hint_email)
        val hint_phone = findViewById<EditText>(R.id.hint_phone)
        val hint_password = findViewById<EditText>(R.id.hint_password)
        val hint_confirm_password = findViewById<EditText>(R.id.hint_confirm_password)
        val checkbox_accept_terms = findViewById<CheckBox>(R.id.checkbox_accept_terms)
        val btn_register = findViewById<TextView>(R.id.btn_register)

        birthDateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        btn_register.setOnClickListener {
            val firstName = hint_first_name.text.toString()
            val lastName = hint_last_name.text.toString()
            val email = hint_email.text.toString()
            val birthDate = birthDateEditText.text.toString()
            val phone = hint_phone.text.toString()
            val password = hint_password.text.toString()
            val confirmPassword = hint_confirm_password.text.toString()
            val acceptTerms = checkbox_accept_terms.isChecked

            if (validateFields(
                    firstName,
                    lastName,
                    email,
                    birthDate,
                    phone,
                    password,
                    confirmPassword,
                    acceptTerms
                )
            ) {
                viewModel.registerUser(firstName, lastName, email, birthDate, phone, password, userRole)
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                birthDateEditText.setText(selectedDate)
            },
            year, month, day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun validateFields(
        firstName: String,
        lastName: String,
        email: String,
        birthDate: String,
        phone: String,
        password: String,
        confirmPassword: String,
        acceptTerms: Boolean
    ): Boolean {
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
            birthDate.isEmpty() || phone.isEmpty() || password.isEmpty() ||
            confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, todos los campos deben llenarse.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor, ingresa un correo eléctronico válido.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!acceptTerms) {
            Toast.makeText(this, "Debes aceptar los términos y condiciones.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}