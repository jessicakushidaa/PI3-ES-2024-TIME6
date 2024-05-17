package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pi_iii_grupo6.databinding.ActivityBuscarLocIdBinding

class BuscarLocIdActivity : AppCompatActivity() {
    private var binding: ActivityBuscarLocIdBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuscarLocIdBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnSimular?.setOnClickListener {
            avancarTela()
        }
    }
    //TEMPORARIA
    private fun avancarTela() {
        var intent = Intent(this@BuscarLocIdActivity,MostrarLocacaoActivity::class.java)
        startActivity(intent)
    }

    //IMPLEMENTAR FUNCTION QUE BUSCA A LOCAÇÃO NO BANCO, BASEADO NO ID DA PULSEIRA.
}