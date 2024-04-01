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
            Toast.makeText(baseContext,"Funcao em desenvolvimento",Toast.LENGTH_SHORT).show()
            var voltarMenu = Intent(this@RentManagerActivity, MainMenuActivity::class.java)
            startActivity(voltarMenu)
        }

        binding?.btnRentals?.setOnClickListener {
            Toast.makeText(baseContext,"Funcao em desenvolvimento",Toast.LENGTH_SHORT).show()
            var voltarMenu = Intent(this@RentManagerActivity, MainMenuActivity::class.java)
            startActivity(voltarMenu)
        }
    }
}