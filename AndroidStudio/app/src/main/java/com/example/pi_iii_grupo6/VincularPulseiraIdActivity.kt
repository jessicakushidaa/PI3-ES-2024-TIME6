package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.pi_iii_grupo6.databinding.ActivityVincularPulseiraIdBinding

class VincularPulseiraIdActivity : AppCompatActivity() {
    private var binding: ActivityVincularPulseiraIdBinding? = null
    private lateinit var meuHandler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVincularPulseiraIdBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        meuHandler = Handler(Looper.getMainLooper())

        meuHandler.postDelayed({
            //Avançar para tela de mostrar infos
            val intent = Intent(this@VincularPulseiraIdActivity,MostrarInfosActivity::class.java)
            startActivity(intent)
        }, 2500)
    }
}