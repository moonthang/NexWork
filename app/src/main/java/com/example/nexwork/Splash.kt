package com.example.nexwork

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Handler
import android.content.Intent
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

            val intent: Intent

            if (currentUser != null) {
                intent = Intent(this, Home::class.java)
            } else {
                intent = Intent(this, Welcome::class.java)
            }

            startActivity(intent)
            finish()
        }, splashTimeOut)
    }
}