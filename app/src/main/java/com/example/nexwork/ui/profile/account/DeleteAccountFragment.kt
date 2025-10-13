package com.example.nexwork.ui.profile.account

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.nexwork.R
import com.example.nexwork.ui.auth.Welcome

class DeleteAccountFragment : Fragment() {

    private val accountViewModel: AccountViewModel by activityViewModels()
    private lateinit var btnDelete: Button
    private lateinit var btnCancel: Button
    private lateinit var txtTitle: TextView
    private lateinit var btnBack: ImageView
    private lateinit var currentPasswordEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_delete_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnDelete = view.findViewById(R.id.btn_delete_account)
        btnCancel = view.findViewById(R.id.btn_cancel)
        btnBack = view.findViewById(R.id.btnBack)
        currentPasswordEditText = view.findViewById(R.id.current_password)

        val basic_header = view.findViewById<View>(R.id.header)
        val btnNotification = basic_header.findViewById<ImageView>(R.id.btnNotification)
        val btnSearch = basic_header.findViewById<ImageView>(R.id.btnSearch)
        val btnFilter = basic_header.findViewById<ImageView>(R.id.btnFilter)
        val btnOptions = basic_header.findViewById<ImageView>(R.id.btnOptions)
        val txtTitle = basic_header.findViewById<TextView>(R.id.txtTitle)
        val btnBack = basic_header.findViewById<ImageView>(R.id.btnBack)

        btnNotification.visibility = View.GONE
        btnSearch.visibility = View.GONE
        btnFilter.visibility = View.GONE
        btnOptions.visibility = View.GONE

        txtTitle.setText(getString(R.string.delete_account_title))
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        btnDelete.setOnClickListener {
            val password = currentPasswordEditText.text.toString()

            if (password.isNotEmpty()) {
                accountViewModel.deleteUserWithReauthentication(password)
            } else {
                Toast.makeText(context, "Debe ingresar su contraseña para confirmar la eliminación.", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        accountViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            btnDelete.isEnabled = !isLoading
            btnCancel.isEnabled = !isLoading
            currentPasswordEditText.isEnabled = !isLoading
        }

        accountViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                Toast.makeText(context, "Cuenta eliminada correctamente", Toast.LENGTH_LONG).show()

                val intent = Intent(requireContext(), Welcome::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                startActivity(intent)
                requireActivity().finish()
            }
        }

        accountViewModel.deleteAccountError.observe(viewLifecycleOwner) { exception ->
            if (exception != null) {
                Toast.makeText(context, exception.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}