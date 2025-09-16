package com.example.nexwork.core

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import com.example.nexwork.R

class LoadingDialog(context: Context) {
    private val dialog: Dialog = Dialog(context).apply {
        setContentView(LayoutInflater.from(context).inflate(R.layout.activity_loading_dialog, null))
        setCancelable(false)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}