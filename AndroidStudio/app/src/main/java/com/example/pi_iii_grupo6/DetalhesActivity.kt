package com.example.pi_iii_grupo6

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class DetalhesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes)

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
    }
}