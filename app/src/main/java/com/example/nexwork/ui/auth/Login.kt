package com.example.nexwork.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nexwork.R
import com.example.nexwork.core.LoadingDialog
import com.example.nexwork.ui.home.Home
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("DEPRECATION")
class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
        const val EXTRA_USER_ROLE = "USER_ROLE"
        const val ROLE_GUEST = "guest"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadingDialog = LoadingDialog(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("23096082016-9mm2n7evnrg1f2i24ptqcqsdkvmi2s58.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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
            navigateToHome(ROLE_GUEST)
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val hint_email = findViewById<EditText>(R.id.hint_email)
        val hint_password = findViewById<EditText>(R.id.hint_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnGoogle = findViewById<Button>(R.id.btn_action_google)

        btnLogin.setOnClickListener {
            val email = hint_email.text.toString()
            val password = hint_password.text.toString()

            if (validarCampos(email, password)) {
                loginUserInFirebase(email, password)
            }
        }

        btnGoogle.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        loadingDialog.show()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser!!
                    db.collection("users").document(user.uid).get()
                        .addOnCompleteListener { userTask ->
                            loadingDialog.dismiss()
                            if (userTask.isSuccessful) {
                                val document = userTask.result
                                if (document != null && document.exists()) {
                                    val role = document.getString("role") ?: "client"
                                    navigateToHome(role)
                                } else {
                                    Toast.makeText(
                                        this@Login,
                                        "No existe una cuenta con este correo electrónico.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    auth.signOut()
                                    googleSignInClient.signOut()
                                    user.delete()
                                }
                            } else {
                                Toast.makeText(
                                    this@Login,
                                    "Error al verificar el usuario: ${userTask.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                auth.signOut()
                                googleSignInClient.signOut()
                            }
                        }
                } else {
                    loadingDialog.dismiss()
                    handleLoginError(task.exception)
                }
            }
    }

    private fun loginUserInFirebase(email: String, password: String) {
        loadingDialog.show()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                loadingDialog.dismiss()
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        db.collection("users").document(user.uid).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val role = document.getString("role") ?: "client"
                                    navigateToHome(role)
                                } else {
                                    Toast.makeText(
                                        this,
                                        "No existe una cuenta con este correo electrónico.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    auth.signOut()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error al verificar usuario: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                navigateToHome(ROLE_GUEST)
                            }
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
                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_LONG).show()
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

    private fun navigateToHome(role: String) {
        val intent = Intent(this, Home::class.java).apply {
            putExtra(EXTRA_USER_ROLE, role)
        }
        startActivity(intent)
        finish()
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
            loadingDialog.show()
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    loadingDialog.dismiss()
                    if (document.exists()) {
                        val role = document.getString("role") ?: "client"
                        navigateToHome(role)
                    } else {
                        auth.signOut()
                    }
                }
                .addOnFailureListener {
                    loadingDialog.dismiss()
                    navigateToHome(ROLE_GUEST)
                }
        }
    }
}