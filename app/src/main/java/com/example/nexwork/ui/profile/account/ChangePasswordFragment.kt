package com.example.nexwork.ui.profile.account

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

class ChangePasswordFragment : Fragment() {

    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmNewPasswordEditText: EditText
    private lateinit var changePasswordButton: Button

    private val accountViewModel: AccountViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentPasswordEditText = view.findViewById(R.id.current_password)
        newPasswordEditText = view.findViewById(R.id.new_password)
        confirmNewPasswordEditText = view.findViewById(R.id.confirm_new_password)
        changePasswordButton = view.findViewById(R.id.btn_send)

        changePasswordButton.setOnClickListener {
            accountViewModel.changePassword(
                currentPasswordEditText.text.toString(),
                newPasswordEditText.text.toString(),
                confirmNewPasswordEditText.text.toString()
            )
        }

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

        txtTitle.setText(getString(R.string.change_password_title))
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        accountViewModel.passwordChangeResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                currentPasswordEditText.setText("")
                newPasswordEditText.setText("")
                confirmNewPasswordEditText.setText("")
            }
            result.onFailure {
                Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}