package com.example.nexwork.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.nexwork.R
import com.example.nexwork.core.LoadingDialog
import com.example.nexwork.data.model.User
import com.example.nexwork.data.repository.AuthRepository
import com.example.nexwork.ui.auth.Login
import com.example.nexwork.ui.profile.GuestProfileFragment
import com.example.nexwork.ui.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class Home : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var headerActivity: View
    private lateinit var mainScrollView: View
    private lateinit var fragmentContainer: View
    private val authRepository = AuthRepository()
    private var currentUserRole: String? = null
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentUserRole = intent.getStringExtra(Login.Companion.EXTRA_USER_ROLE)
        auth = FirebaseAuth.getInstance()
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        mainScrollView = findViewById(R.id.main)
        fragmentContainer = findViewById(R.id.fragment_container)
        headerActivity = findViewById(R.id.header)
        loadingDialog = LoadingDialog(this)

        if (currentUserRole == Login.Companion.ROLE_GUEST) {
            setupGuestMode()
        } else {
            setupAuthenticatedUser()
        }

        if (currentUserRole == "provider") {
            loadFragment(HomeProviderFragment())
            bottomNavigationView.selectedItemId = R.id.btn_home
        } else {
            showHomeContent()
            bottomNavigationView.selectedItemId = R.id.btn_home
        }
    }

    private fun setupGuestMode() {
        Toast.makeText(this, "Bienvenido como invitado", Toast.LENGTH_SHORT).show()
        setupBottomNavigationForGuest()
    }

    private fun setupAuthenticatedUser() {
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId != null) {
            loadingDialog.show()
            authRepository.getUserById(currentUserId) { result ->
                loadingDialog.dismiss()
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user != null) {
                        setupBottomNavigation(user)
                    }
                } else {
                    setupBottomNavigationForGuest()
                }
            }
        } else {
            setupBottomNavigationForGuest()
        }
    }

    private fun setupBottomNavigationForGuest() {
        val menu = bottomNavigationView.menu

        menu.findItem(R.id.btn_home).isVisible = true
        menu.findItem(R.id.btn_profile).isVisible = true

        menu.findItem(R.id.btn_messages).isVisible = false
        menu.findItem(R.id.btn_category).isVisible = false
        menu.findItem(R.id.btn_notifications).isVisible = false

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btn_home -> {
                    showHomeContent()
                    true
                }
                R.id.btn_profile -> {
                    loadFragment(GuestProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun setupBottomNavigation(user: User) {
        val menu = bottomNavigationView.menu

        menu.findItem(R.id.btn_home).isVisible = true
        menu.findItem(R.id.btn_messages).isVisible = true
        menu.findItem(R.id.btn_profile).isVisible = true

        when (user.role) {
            "provider" -> {
                menu.findItem(R.id.btn_category).isVisible = false
                menu.findItem(R.id.btn_notifications).isVisible = true
            }
            "client" -> {
                menu.findItem(R.id.btn_category).isVisible = true
                menu.findItem(R.id.btn_notifications).isVisible = false
            }
            "admin" -> {
                menu.findItem(R.id.btn_messages).isVisible = true
                menu.findItem(R.id.btn_category).isVisible = true
                menu.findItem(R.id.btn_notifications).isVisible = true
            }
            else -> {
                setupBottomNavigationForGuest()
                return
            }
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btn_home -> {
                    if (user.role == "provider") {
                        loadFragment(HomeProviderFragment())
                    } else {
                        showHomeContent()
                    }
                    true
                }
                R.id.btn_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }


    private fun showHomeContent() {
        fragmentContainer.visibility = View.GONE
        mainScrollView.visibility = View.VISIBLE
        headerActivity.visibility = View.VISIBLE

        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        mainScrollView.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE
        headerActivity.visibility = View.GONE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}