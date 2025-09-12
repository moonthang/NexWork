package com.example.nexwork

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.TextView

class Welcome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btn_register_client = findViewById<Button>(R.id.btn_register_client)
        btn_register_client.setOnClickListener {
            val intent = Intent(this, Registration::class.java)
            intent.putExtra("userRole", "client")
            startActivity(intent)
        }

        val btn_register_provider = findViewById<Button>(R.id.btn_register_provider)
        btn_register_provider.setOnClickListener {
            val intent = Intent(this, Registration::class.java)
            intent.putExtra("userRole", "provider")
            startActivity(intent)
        }

        val actionLogin = findViewById<TextView>(R.id.action_login)
        actionLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}