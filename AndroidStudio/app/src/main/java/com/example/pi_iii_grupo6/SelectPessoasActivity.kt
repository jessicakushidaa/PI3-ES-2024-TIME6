package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.pi_iii_grupo6.databinding.ActivitySelectPessoasBinding

class SelectPessoasActivity : AppCompatActivity() {
    private var binding: ActivitySelectPessoasBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectPessoasBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //Seta Retorno
        val toolbar : Toolbar = findViewById(R.id.toolbar) //achando id da toolbar

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //Botão voltar
        supportActionBar?.setDisplayShowTitleEnabled(false) // Remove o texto do nome do aplicativo

        //Voltando o numero escolhido para 0
        numPessoas = 0

        //Chamando função que recebe os dados da locação via intent, esses dados precisam ser recebidos para posteriormente serem passados à proxima intent;
        receberDados()

        //Setando cliques do layout
        binding?.tvUmaPessoa?.setOnClickListener {
            adicionarPessoa(1)
        }

        binding?.tvDuasPessoas?.setOnClickListener {
            adicionarPessoa(2)
        }

        binding?.btnEnviar?.setOnClickListener {
            avancarIntent()
        }
    }


    //Função que recebe o número de pessoas que usarão a pulseira e guarda no companion object para acesso via outra Activity.
    fun adicionarPessoa(numero: Int){
        numPessoas = numero
    }

    //Função que avança para a intent do NFC, ja carregando a string dos dados que foram obtidos até esse momento (Locação + num de pessoas)
    fun avancarIntent(){
        if(numPessoas != 0){
            Toast.makeText(baseContext,"numero escolhido: $numPessoas",Toast.LENGTH_SHORT).show()
            val intentCamera = Intent(this@SelectPessoasActivity, TirarFotoActivity::class.java)
            startActivity(intentCamera)
        }else{
            Toast.makeText(baseContext,"Escolha uma opção",Toast.LENGTH_SHORT).show()
        }
    }

    //Função que recebe os dados vindos da Activity anterior, os guarda para depois serem enviados à próxima
    fun receberDados(){

    }

    companion object{
        var numPessoas = 0
    }
}