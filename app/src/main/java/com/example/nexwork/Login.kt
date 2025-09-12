package com.example.nexwork

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val forgot_password = findViewById<TextView>(R.id.forgot_password)
        forgot_password.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }

        val txt_action_register = findViewById<TextView>(R.id.txt_action_register)
        txt_action_register.setOnClickListener {
            val intent = Intent(this, Registration::class.java)
            startActivity(intent)
        }

        val action_skip = findViewById<TextView>(R.id.action_skip)
        action_skip.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val hint_email = findViewById<EditText>(R.id.hint_email)
        val hint_password = findViewById<EditText>(R.id.hint_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)

        btnLogin.setOnClickListener {
            val email = hint_email.text.toString()
            val password = hint_password.text.toString()

            if (validarCampos( email, password)){
                loginUserInFirebase(email, password)
            }
        }
    }

    private fun loginUserInFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val intent = Intent(this, Home::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    handleLoginError(task.exception)
                }
            }
    }

    private fun handleLoginError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthInvalidUserException -> {
                Toast.makeText(
                    this,
                    "No existe una cuenta con este correo electrónico",
                    Toast.LENGTH_LONG
                ).show()
            }
            is FirebaseAuthInvalidCredentialsException -> {
                Toast.makeText(
                    this,
                    "Contraseña incorrecta",
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                Toast.makeText(
                    this,
                    "Error al iniciar sesión: ${exception?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun validarCampos(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }
    }
}