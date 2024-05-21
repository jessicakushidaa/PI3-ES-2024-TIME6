package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pi_iii_grupo6.LiberarLocacaoActivity.Companion.atualLocacao
import com.example.pi_iii_grupo6.MainViewActivity.Companion.places
import com.example.pi_iii_grupo6.databinding.ActivityBuscarLocIdBinding
import com.google.gson.Gson

class BuscarLocIdActivity : AppCompatActivity() {
    private var binding: ActivityBuscarLocIdBinding? = null
    private lateinit var gson: Gson
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuscarLocIdBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        gson = Gson()

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

    companion object{
        //Variavel de TESTE
        var locRecebidaTESTE: MainViewActivity.Locacao = atualLocacao
        lateinit var latlocRecebida: MainViewActivity.Locacao
    }
}