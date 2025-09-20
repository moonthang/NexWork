package com.example.nexwork

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nexwork.ui.orders.AdministrarPedidosFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, AdministrarPedidosFragment())
                .commitNow()
        }
    }
}