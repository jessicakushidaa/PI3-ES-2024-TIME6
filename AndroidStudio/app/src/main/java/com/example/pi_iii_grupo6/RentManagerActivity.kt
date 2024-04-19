package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.pi_iii_grupo6.databinding.ActivityRentManagerBinding

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

        //AÇÕES DO BOTTOM NAVIGATION
        binding?.btnHome?.setOnClickListener {
            var irHome = Intent(this@RentManagerActivity, MainViewActivity::class.java)
            startActivity(irHome)
        }
        binding?.btnCartoes?.setOnClickListener {
            //if()
            var irCartoes = Intent(this@RentManagerActivity, CreateCardActivity::class.java)
            startActivity(irCartoes)
        }
        binding?.btnLocacoes?.setOnClickListener {
            var irLocacoes = Intent(this@RentManagerActivity, RentManagerActivity::class.java)
            startActivity(irLocacoes)
        }
    }
}