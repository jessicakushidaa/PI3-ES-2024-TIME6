package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.pi_iii_grupo6.databinding.ActivityDetalhesBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class DetalhesActivity : AppCompatActivity() {

    private  var binding: ActivityDetalhesBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        //Inflando o layout e colocando a activity na tela
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val loc = intent.getParcelableExtra<LocacaoItem>("loc")
        if(loc!=null){
            val tvTag: TextView = findViewById(R.id.tvTagArmario)
            val tvNome: TextView = findViewById(R.id.tvNomeUnidade)
            val tvHorario: TextView = findViewById(R.id.tvHorarioLoc)
            val tvEscolhido: TextView = findViewById(R.id.tvTempoEscolhido)

            tvTag.text = loc.tagArmario
            tvNome.text = loc.nomeUnidade
            tvHorario.text = loc.horaLocacao
            tvEscolhido.text = loc.tempo
        }

        //Seta Voltar
        setSupportActionBar(binding?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Remove o texto do nome do aplicativo

        // Define o ícone da seta como o drawable customizado
        supportActionBar?.setHomeAsUpIndicator(R.drawable.round_arrow_back_24)

        //Direcionando o bottomNavigation
        val bottomNavigation : BottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                //tela Locações
                R.id.page_locacoes -> {
                    startActivity(Intent(this, RentManagerActivity::class.java))
                    true
                }
                //tela Mapa
                R.id.page_mapa -> {
                    startActivity(Intent(this, MainViewActivity::class.java))
                    true
                }
                //tela Cartões
                R.id.page_cartoes -> {
                    startActivity(Intent(this, ShowCardActivity::class.java))
                    true

                }

                else -> false

            }
        }
    }
}