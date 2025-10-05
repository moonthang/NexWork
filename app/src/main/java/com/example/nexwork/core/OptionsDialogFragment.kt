
package com.example.nexwork.core

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.nexwork.R

class OptionsDialogFragment : DialogFragment() {

    interface OptionsDialogListener {
        fun onOptionSelected(option: String)
    }

    private var listener: OptionsDialogListener? = null
    private var dialogTitle: String? = null
    private var option1: String? = null
    private var option2: String? = null
    private var option3: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_item_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialogTitle = arguments?.getString(ARG_TITLE)
        option1 = arguments?.getString(ARG_OPTION_1)
        option2 = arguments?.getString(ARG_OPTION_2)
        option3 = arguments?.getString(ARG_OPTION_3)

        val tvDialogTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val btnOption1 = view.findViewById<Button>(R.id.btnOption1)
        val btnOption2 = view.findViewById<Button>(R.id.btnOption2)
        val btnOption3 = view.findViewById<Button>(R.id.btnOption3)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        tvDialogTitle.text = dialogTitle

        setupButton(btnOption1, option1)
        setupButton(btnOption2, option2)
        setupButton(btnOption3, option3)

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun setupButton(button: Button, text: String?) {
        if (text != null) {
            button.text = text
            button.visibility = View.VISIBLE
            button.setOnClickListener {
                listener?.onOptionSelected(text)
                dismiss()
            }
        } else {
            button.visibility = View.GONE
        }
    }

    fun setOptionsDialogListener(listener: OptionsDialogListener) {
        this.listener = listener
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_OPTION_1 = "arg_option_1"
        private const val ARG_OPTION_2 = "arg_option_2"
        private const val ARG_OPTION_3 = "arg_option_3"

        fun newInstance(
            title: String,
            option1: String? = null,
            option2: String? = null,
            option3: String? = null
        ): OptionsDialogFragment {
            val fragment = OptionsDialogFragment()
            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_OPTION_1, option1)
                putString(ARG_OPTION_2, option2)
                putString(ARG_OPTION_3, option3)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
