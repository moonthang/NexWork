package com.example.nexwork.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.nexwork.R
import com.example.nexwork.ui.auth.Registration
import com.example.nexwork.ui.auth.Login

class GuestProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guest_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val registerButton: Button = view.findViewById(R.id.button_register)
        val loginButton: Button = view.findViewById(R.id.button_login)

        txtTitle.text = getString(R.string.profile_title)

        registerButton.setOnClickListener {
            val intent = Intent(activity, Registration::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val intent = Intent(activity, Login::class.java)
            startActivity(intent)
        }
    }
}