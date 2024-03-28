package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.pi_iii_grupo6.databinding.ActivityMainMenuBinding

class MainMenuActivity : AppCompatActivity() {
    private var binding: ActivityMainMenuBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //Setando o clique em MAPA que vai abrir o mapa
        binding?.llMapa?.setOnClickListener{
            var avancar = Intent(this@MainMenuActivity, MainViewActivity::class.java)
            startActivity(avancar)
        }

        //Ao clicar em CARTAO, abre a função de adicionar um cartao
        binding?.llCartao?.setOnClickListener{
            Toast.makeText(baseContext, "Função em desenvolvimento",Toast.LENGTH_SHORT ).show()
        }
    }
}