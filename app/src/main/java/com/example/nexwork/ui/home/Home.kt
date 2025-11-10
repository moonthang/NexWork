package com.example.nexwork.ui.home

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.nexwork.R
import com.example.nexwork.core.LoadingDialog
import com.example.nexwork.data.model.User
import com.example.nexwork.data.repository.AuthRepository
import com.example.nexwork.ui.auth.Login
import com.example.nexwork.ui.categories.CategoriesDetailFragment
import com.example.nexwork.ui.categories.HomeCategoriesFragment
import com.example.nexwork.ui.chat.UserSearchFragment
import com.example.nexwork.ui.profile.GuestProfileFragment
import com.example.nexwork.ui.profile.ProfileFragment
import com.example.nexwork.ui.services.ImageCarouselAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexwork.data.model.Category
import com.example.nexwork.data.repository.CategoriesRepository
import com.example.nexwork.ui.categories.CategoryAdapter
import com.example.nexwork.ui.categories.ViewType

class Home : AppCompatActivity(), CategoryAdapter.OnItemClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var headerActivity: View
    private lateinit var mainScrollView: View
    private lateinit var fragmentContainer: View
    private val authRepository = AuthRepository()
    private var currentUserRole: String? = null
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var tabIndicator: TabLayout
    private lateinit var imageInformation: ImageView
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private val categoriesRepository = CategoriesRepository()

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
        tabIndicator = findViewById(R.id.tab_indicator)
        imageInformation = findViewById(R.id.image_information)
        categoriesRecyclerView = findViewById(R.id.rv_categories)
        setupCategoriesRecyclerView()

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

        if (currentUserRole != "provider") {
            loadCarouselImages()
            loadImageInformation()
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
        menu.findItem(R.id.btn_category).isVisible = true
        menu.findItem(R.id.btn_messages).isVisible = false
        menu.findItem(R.id.btn_notifications).isVisible = false

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btn_home -> {
                    showHomeContent()
                    true
                }
                R.id.btn_category -> {
                    loadFragment(HomeCategoriesFragment())
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
                R.id.btn_category -> {
                    loadFragment(HomeCategoriesFragment())
                    true
                }
                R.id.btn_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                R.id.btn_messages -> {
                    loadFragment(UserSearchFragment())
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

        if (currentUserRole != "provider") {
            loadCarouselImages()
            loadImageInformation()
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

    // Cargar imagenes del carrusel
    private fun loadCarouselImages() {
        loadingDialog.show()
        val storageRef = FirebaseStorage.getInstance().reference.child("banners")

        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                if (listResult.items.isEmpty()) {
                    loadingDialog.dismiss()
                    setupImageCarousel(emptyList())
                    return@addOnSuccessListener
                }

                val downloadUrlTasks = listResult.items.map { it.downloadUrl }

                Tasks.whenAllSuccess<Uri>(downloadUrlTasks)
                    .addOnSuccessListener { uris ->
                        loadingDialog.dismiss()
                        val imageUrls = uris.map { it.toString() }
                        setupImageCarousel(imageUrls)
                    }
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Toast.makeText(this, "Error al cargar las imágenes del carrusel", Toast.LENGTH_SHORT).show()
                setupImageCarousel(emptyList())
            }
    }

    private fun loadImageInformation() {
        val imageUrl = "https://firebasestorage.googleapis.com/v0/b/nexwork-e4866.firebasestorage.app/o/resources%2Finfo.jpg?alt=media&token=a785d8dc-7f58-44ff-b9d6-ab6cedfd704e"
        Glide.with(this)
            .load(imageUrl)
            .into(imageInformation)
    }

    // Configuracion carrusel
    private fun setupImageCarousel(imageUrls: List<String>) {
        val viewPager: ViewPager2 = findViewById(R.id.image_carousel)

        if (imageUrls.isNotEmpty()) {
            viewPager.visibility = View.VISIBLE
            tabIndicator.visibility = View.VISIBLE

            val adapter = ImageCarouselAdapter(imageUrls, this)
            viewPager.adapter = adapter

            TabLayoutMediator(tabIndicator, viewPager) { tab, position ->
            }.attach()

        } else {
            viewPager.visibility = View.GONE
            tabIndicator.visibility = View.GONE
        }
    }

    // Guardar datos de categoria seleccionada
    override fun onItemClick(category: Category) {
        val categoryId = category.categoryId
        val categoryName = category.name

        val fragment = CategoriesDetailFragment.newInstance(categoryId, categoryName)

        loadFragment(fragment)
    }

    // Configuracion categorias
    private fun setupCategoriesRecyclerView() {
        categoryAdapter = CategoryAdapter(
            categories = emptyList(),
            listener = this,
            viewType = ViewType.CARD
        )

        categoriesRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        categoriesRecyclerView.adapter = categoryAdapter

        loadCategories()
    }

    // Cargar categorias
    private fun loadCategories() {
        loadingDialog.show()

        categoriesRepository.getAllCategories { result ->
            loadingDialog.dismiss()

            if (result.isSuccess) {
                val realCategories = result.getOrThrow()
                categoryAdapter.updateCategories(realCategories)

            } else {
                val error = result.exceptionOrNull()
                Toast.makeText(this, "Error al cargar categorías: ${error?.message}", Toast.LENGTH_LONG).show()
                categoryAdapter.updateCategories(emptyList())
            }
        }
    }
}
