package com.example.nexwork.auth

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nexwork.Home
import com.example.nexwork.R
import com.example.nexwork.core.LoadingDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class Registration : AppCompatActivity() {

    private lateinit var birthDateEditText: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userRole: String = "user"
    private lateinit var loadingDialog: LoadingDialog

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
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        userRole = intent.getStringExtra("userRole") ?: "user"

        val action_login = findViewById<TextView>(R.id.action_login)
        action_login.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        birthDateEditText = findViewById(R.id.hint_birth_date)
        birthDateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        setupOnClickListener()
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

    private fun setupOnClickListener(){
        val hint_first_name = findViewById<EditText>(R.id.hint_first_name)
        val hint_last_name = findViewById<EditText>(R.id.hint_last_name)
        val hint_email = findViewById<EditText>(R.id.hint_email)
        val hint_birth_date = findViewById<EditText>(R.id.hint_birth_date)
        val hint_phone = findViewById<EditText>(R.id.hint_phone)
        val hint_password = findViewById<EditText>(R.id.hint_password)
        val hint_confirm_password = findViewById<EditText>(R.id.hint_confirm_password)
        val checkbox_accept_terms = findViewById<CheckBox>(R.id.checkbox_accept_terms)
        val btn_register = findViewById<TextView>(R.id.btn_register)

        btn_register.setOnClickListener {
            val firstName = hint_first_name.text.toString()
            val lastName = hint_last_name.text.toString()
            val email = hint_email.text.toString()
            val birthDate = hint_birth_date.text.toString()
            val phone = hint_phone.text.toString()
            val password = hint_password.text.toString()
            val confirmPassword = hint_confirm_password.text.toString()
            val acceptTerms = checkbox_accept_terms.isChecked

            if (validarCampos(firstName, lastName, email, birthDate, phone, password, confirmPassword, acceptTerms)){
                registerUserInFirebase(firstName, lastName, email, birthDate, phone, password)
            }
        }
    }

    private fun registerUserInFirebase(
        firstName: String,
        lastName: String,
        email: String,
        birthDate: String,
        phone: String,
        password: String
    ) {
        loadingDialog.show()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName("$firstName $lastName")
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                saveUserDataToFirestore(user.uid, firstName, lastName, email, birthDate, phone)
                            } else {
                                loadingDialog.dismiss()
                                Toast.makeText(
                                    this,
                                    "Error al actualizar perfil: ${profileTask.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Error en registro: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserDataToFirestore(
        userId: String,
        firstName: String,
        lastName: String,
        email: String,
        birthDate: String,
        phone: String
    ) {
        val user = hashMapOf(
            "userId" to userId,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "birthDate" to birthDate,
            "phone" to phone,
            "role" to userRole,
            "createdAt" to Calendar.getInstance().time
        )

        db.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                loadingDialog.dismiss()
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                loadingDialog.dismiss()
                Toast.makeText(
                    this,
                    "Error al guardar datos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

                auth.currentUser?.delete()
            }
    }

    private fun validarCampos(
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