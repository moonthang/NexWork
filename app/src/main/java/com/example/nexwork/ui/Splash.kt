package com.example.nexwork.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nexwork.ui.home.Home
import com.example.nexwork.R
import com.example.nexwork.ui.auth.Login
import com.example.nexwork.ui.auth.Welcome
import com.google.firebase.auth.FirebaseAuth

class Splash : AppCompatActivity() {

    private val splashTimeOut: Long = 2000

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        Handler().postDelayed({
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("users").document(currentUser.uid).get()
                    .addOnSuccessListener { document ->
                        val role = document.getString("role") ?: "client"
                        val intent = Intent(this, Home::class.java).apply {
                            putExtra(Login.EXTRA_USER_ROLE, role)
                        }
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        val intent = Intent(this, Home::class.java).apply {
                            putExtra(Login.EXTRA_USER_ROLE, Login.ROLE_GUEST)
                        }
                        startActivity(intent)
                        finish()
                    }
            } else {
                startActivity(Intent(this, Welcome::class.java))
                finish()
            }
        }, splashTimeOut)
    }
}