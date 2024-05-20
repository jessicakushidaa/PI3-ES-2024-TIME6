package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pi_iii_grupo6.databinding.ActivityVincularPulseiraIdBinding

class VincularPulseiraIdActivity : AppCompatActivity() {
    private var binding: ActivityVincularPulseiraIdBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVincularPulseiraIdBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnAvancar?.setOnClickListener {
            //Avançar para tela de mostrar infos
            val intent = Intent(this@VincularPulseiraIdActivity,MostrarInfosActivity::class.java)
            startActivity(intent)
        }

    }

    //IMPLEMENTAR FUNCTION QUE VINCULA A PULSEIRA NO BANCO DE DADOS
}