package com.example.nexwork.ui.profile.account

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.nexwork.R
import com.example.nexwork.core.LoadingDialog
import com.example.nexwork.ui.auth.Login
import com.example.nexwork.ui.profile.ProfileFragment
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar

class AccountFragment : Fragment() {

    private val viewModel: AccountViewModel by viewModels()

    private lateinit var profileFields: List<EditText>
    private lateinit var profileImage: ImageView
    private lateinit var btnEdit: Button
    private lateinit var btnSave: Button
    private lateinit var btnChangePhoto: Button
    private lateinit var btnDeletePhoto: Button
    private lateinit var btnCancel: Button
    private lateinit var btnChangeRole: Button
    private lateinit var textDeleteAccount: TextView
    private lateinit var textCloseSession: TextView
    private lateinit var birthDateEditText: EditText
    private var isEditing = false
    private val originalFieldValues = mutableMapOf<Int, String>()
    private lateinit var loadingDialog: LoadingDialog

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri: Uri? = result.data?.data
                if (imageUri != null) {
                    viewModel.updateProfileImage(imageUri)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        loadingDialog = LoadingDialog(requireContext())

        bindViews(view)
        setupListeners()
        observeViewModel()

        loadingDialog.show()
        viewModel.loadUserData()
        setEditMode(false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sectionBankAccount = view.findViewById<LinearLayout>(R.id.section_bank_account)

        viewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                when (user.role) {
                    "client" -> sectionBankAccount.visibility = View.GONE
                    "provider" -> sectionBankAccount.visibility = View.VISIBLE
                    else -> sectionBankAccount.visibility = View.GONE
                }
            }
        }

        val sectionChangePassword = view.findViewById<LinearLayout>(R.id.section_change_password)
        sectionChangePassword.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChangePasswordFragment())
                .addToBackStack(null)
                .commit()
        }

        val sectionDeleteAccount = view.findViewById<LinearLayout>(R.id.section_delete_account)
        sectionDeleteAccount.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DeleteAccountFragment())
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<LinearLayout>(R.id.section_close_session).setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireContext(), Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)

        txtTitle.text = getString(R.string.account_title)
        btnBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }
    }


    private fun bindViews(view: View) {
        profileFields = listOf(
            view.findViewById(R.id.hint_first_name),
            view.findViewById(R.id.hint_last_name),
            view.findViewById(R.id.hint_email),
            view.findViewById(R.id.hint_birth_date),
            view.findViewById(R.id.hint_phone)
        )
        birthDateEditText = view.findViewById(R.id.hint_birth_date)

        profileImage = view.findViewById(R.id.profileImage)
        btnEdit = view.findViewById(R.id.btn_edit)
        btnSave = view.findViewById(R.id.btn_save)
        btnCancel = view.findViewById(R.id.btn_cancel)
        btnChangeRole = view.findViewById(R.id.btn_change_role)
        btnChangePhoto = view.findViewById(R.id.btn_change_photo)
        btnDeletePhoto = view.findViewById(R.id.btn_delete_photo)
        textDeleteAccount = view.findViewById(R.id.text_delete_account)
        textCloseSession = view.findViewById(R.id.text_close_session)
    }

    private fun setupListeners() {
        btnEdit.setOnClickListener {
            profileFields.forEach { originalFieldValues[it.id] = it.text.toString() }
            setEditMode(true)
        }

        btnSave.setOnClickListener {
            val current = viewModel.userData.value ?: return@setOnClickListener
            val updated = current.copy(
                firstName = profileFields[0].text.toString(),
                lastName = profileFields[1].text.toString(),
                email = profileFields[2].text.toString(),
                birthDate = profileFields[3].text.toString(),
                phone = profileFields[4].text.toString(),
                role = current.role,
                profileImageUrl = current.profileImageUrl
            )
            viewModel.updateUserWithEmailCheck(updated)
            setEditMode(false)
            originalFieldValues.clear()
        }

        btnCancel.setOnClickListener {
            revertChanges()
            setEditMode(false)
        }

        btnChangeRole.setOnClickListener {
            viewModel.toggleUserRole()
        }

        profileImage.setOnClickListener {
            if (isEditing) openGallery()
        }

        btnChangePhoto.setOnClickListener {
            openGallery()
        }

        btnDeletePhoto.setOnClickListener {
            val current = viewModel.userData.value ?: return@setOnClickListener
            val updated = current.copy(profileImageUrl = "")
            viewModel.updateUser(updated)
        }

        birthDateEditText.setOnClickListener {
            if (isEditing) {
                showDatePickerDialog()
            }
        }

        textCloseSession.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                birthDateEditText.setText(selectedDate)
            },
            year, month, day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun setEditMode(enableEdit: Boolean) {
        isEditing = enableEdit

        profileFields.forEach {
            val isBirthDateField = it.id == R.id.hint_birth_date
            if (isBirthDateField) {
                it.isFocusable = false
                it.isFocusableInTouchMode = false
                it.isClickable = enableEdit
            } else {
                it.isFocusable = enableEdit
                it.isClickable = enableEdit
                it.isFocusableInTouchMode = enableEdit
            }

            it.alpha = if (enableEdit) 1.0f else 0.6f
        }

        btnEdit.visibility = if (enableEdit) View.GONE else View.VISIBLE
        btnSave.visibility = if (enableEdit) View.VISIBLE else View.GONE
        btnCancel.visibility = if (enableEdit) View.VISIBLE else View.GONE

        btnChangeRole.isEnabled = !enableEdit
    }

    private fun revertChanges() {
        profileFields.forEach {
            val originalValue = originalFieldValues[it.id]
            if (originalValue != null) it.setText(originalValue)
        }
        originalFieldValues.clear()
    }

    private fun observeViewModel() {
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user == null) return@observe
            profileFields[0].setText(user.firstName)
            profileFields[1].setText(user.lastName)
            profileFields[2].setText(user.email)
            profileFields[3].setText(user.birthDate)
            profileFields[4].setText(user.phone)

            loadProfileImage(user.profileImageUrl)

            btnChangeRole.setText(
                if (user.role == "provider") R.string.btn_change_client
                else R.string.btn_change_provider
            )

            loadingDialog.dismiss()
        }

        viewModel.profileImageUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .into(profileImage)
            }
        }
    }

    private fun loadProfileImage(pathOrUrl: String?) {
        if (pathOrUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(R.drawable.ic_profile)
                .circleCrop()
                .into(profileImage)
            return
        }

        if (pathOrUrl.startsWith("http")) {
            Glide.with(this)
                .load(pathOrUrl)
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(profileImage)
            return
        }

        FirebaseStorage.getInstance().reference.child(pathOrUrl).downloadUrl
            .addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .into(profileImage)
            }
            .addOnFailureListener {
                Glide.with(this)
                    .load(R.drawable.ic_profile)
                    .circleCrop()
                    .into(profileImage)
            }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }
}