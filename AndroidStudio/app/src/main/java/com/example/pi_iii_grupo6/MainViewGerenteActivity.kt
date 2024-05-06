package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.pi_iii_grupo6.databinding.ActivityMainViewGerenteBinding

class MainViewGerenteActivity : AppCompatActivity() {
    var binding: ActivityMainViewGerenteBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainViewGerenteBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.tvAcessarArmario?.setOnClickListener {
            //Abrir acessar armario
            val intentAcessarArmario = Intent(this@MainViewGerenteActivity,AcessarArmarioActivity::class.java)
            startActivity(intentAcessarArmario)
        }

        binding?.tvLiberarLocacao?.setOnClickListener {
            //Abrir Liberar Locação
            val intentLiberarLocacao = Intent(this@MainViewGerenteActivity,LiberarLocacaoActivity::class.java)
            startActivity(intentLiberarLocacao)
        }

        binding?.btnExit?.setOnClickListener{
            // Encerra a atividade atual
            finish()

            //Abrir a LoginActivity
            val intentExitGerente = Intent(this,LoginActivity::class.java)
            startActivity(intentExitGerente)
        }

    }
}