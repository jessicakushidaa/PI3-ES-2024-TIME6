package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.pi_iii_grupo6.databinding.ActivityMostrarInfosBinding

class MostrarInfosActivity : AppCompatActivity() {
    private var binding: ActivityMostrarInfosBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMostrarInfosBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnFinalizar?.setOnClickListener {
            Toast.makeText(baseContext,"Locação feita com sucesso",Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MostrarInfosActivity,MainViewGerenteActivity::class.java)
            startActivity(intent)
        }

    }
}