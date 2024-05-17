package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pi_iii_grupo6.databinding.ActivityMostrarLocacaoBinding

class MostrarLocacaoActivity : AppCompatActivity() {
    private var binding: ActivityMostrarLocacaoBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMostrarLocacaoBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnAvancar?.setOnClickListener {
            val intent = Intent(this@MostrarLocacaoActivity, AcessarArmarioActivity::class.java)
            startActivity(intent)
        }
    }
}