package com.example.nexwork.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.nexwork.R

class ForgotPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        val basic_header = findViewById<View>(R.id.header)
        val txtTitle = basic_header.findViewById<TextView>(R.id.txtTitle)
        val btnBack = basic_header.findViewById<ImageView>(R.id.btnBack)

        txtTitle.setText(getString(R.string.forgot_password_title))
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}