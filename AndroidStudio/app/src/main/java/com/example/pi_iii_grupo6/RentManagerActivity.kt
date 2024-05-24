package com.example.pi_iii_grupo6

import BasicaActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pi_iii_grupo6.databinding.ActivityMainMenuBinding
import com.example.pi_iii_grupo6.databinding.ActivityRentManagerBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class RentManagerActivity : BasicaActivity() {
    private var binding: ActivityRentManagerBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar o layout
        binding = ActivityRentManagerBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Configurar um clique no botão "Alugar armário" - inicia tela de alugar
        binding?.btnRent?.setOnClickListener{
            var irRent = Intent(this@RentManagerActivity, RentActivity::class.java)
            startActivity(irRent)
        }

        // Configurar um clique no botão "Minhas Locações" - inicia tela "minhas Locações"
        binding?.btnRentals?.setOnClickListener {
            var irMinhasLocs = Intent(this@RentManagerActivity, MinhasLocacoesActivity::class.java)
            startActivity(irMinhasLocs)
        }

        binding?.btnHome?.setOnClickListener{
            //botão home volta pro menu do gerente
            val intentHome = Intent(this@RentManagerActivity, ActivityMainMenuBinding::class.java)
            startActivity(intentHome)
        }

        //Direcionando o bottomNavigation
        val bottomNavigation : BottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                //tela Locações
                R.id.page_locacoes -> {
                    startActivity(Intent(this, RentManagerActivity::class.java))
                    true
                }
                //tela Mapa
                R.id.page_mapa -> {
                    startActivity(Intent(this, MainViewActivity::class.java))
                    true
                }
                //tela Cartões
                R.id.page_cartoes -> {
                    startActivity(Intent(this, ShowCardActivity::class.java))
                    true

                }

                else -> false

            }
        }

    }
}