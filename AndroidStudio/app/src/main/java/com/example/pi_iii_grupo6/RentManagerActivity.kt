package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pi_iii_grupo6.databinding.ActivityRentManagerBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class RentManagerActivity : AppCompatActivity() {
    private var binding: ActivityRentManagerBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRentManagerBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnRent?.setOnClickListener{
            var irRent = Intent(this@RentManagerActivity, RentActivity::class.java)
            startActivity(irRent)
        }

        binding?.btnRentals?.setOnClickListener {
            var irMinhasLocs = Intent(this@RentManagerActivity, MinhasLocacoesActivity::class.java)
            startActivity(irMinhasLocs)
        }

        //Direcionando o bottomNavigation
        val bottomNavigation : BottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                //tela Locações
                R.id.page_1 -> {
                    startActivity(Intent(this, RentManagerActivity::class.java))
                    true
                }
                //tela Mapa
                R.id.page_2 -> {
                    startActivity(Intent(this, MainViewActivity::class.java))
                    true
                }
                //tela Cartões
                R.id.page_3 -> {
                    startActivity(Intent(this, ShowCardActivity::class.java))
                    true

                }

                else -> false

            }
        }

    }
}