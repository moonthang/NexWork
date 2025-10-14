package com.example.nexwork.core

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.nexwork.R

class LoadingDialog(context: Context) {
    private val dialog: Dialog = Dialog(context).apply {
        setContentView(LayoutInflater.from(context).inflate(R.layout.activity_loading_dialog, null))
        setCancelable(false)

        window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawable(
                ColorDrawable(ContextCompat.getColor(context, R.color.background_primary))
            )
        }
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}