package com.example.pi_iii_grupo6

import BasicaActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.pi_iii_grupo6.databinding.ActivitySelectPessoasBinding

class SelectPessoasActivity : BasicaActivity() {
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

        // Define o ícone da seta como o drawable customizado
        supportActionBar?.setHomeAsUpIndicator(R.drawable.round_arrow_back_24)

        //Voltando o numero escolhido para 0
        numPessoas = 0

        /*
        Chamando função que recebe os dados da locação via intent, esses dados precisam ser
        recebidos para posteriormente serem passados à proxima intent;
        */
        receberDados()

        /** Verificar se as CheckBoxes não são nulas antes de adicionar os listeners
        Setando listeners para quando a checkbox for selecionada, desmarcar a outra
        e chamar função de adicionarPessoa, passando seu respectivo valor como parametro
        **/
        binding?.tvUmaPessoa?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Se "Uma Pessoa" está selecionada, desmarcar "Duas Pessoas" (se não for nulo)
                binding?.tvDuasPessoas?.isChecked = false
                adicionarPessoa(1)
                Log.d("adicionarPessoa","quantidade de pessoas selecionadas: $numPessoas")
            } else if (!binding?.tvDuasPessoas?.isChecked!!) {
                // Se ambos os CheckBoxes estiverem desmarcados, define numPessoas como 0
                numPessoas = 0
            }
        }

        // Verificar se as CheckBoxes não são nulas antes de adicionar os listeners
        binding?.tvDuasPessoas?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Se "Uma Pessoa" está selecionada, desmarcar "Duas Pessoas" (se não for nulo)
                binding?.tvUmaPessoa?.isChecked = false
                adicionarPessoa(2)
                Log.d("adicionarPessoa","quantidade de pessoas selecionadas: $numPessoas")
            } else if (!binding?.tvUmaPessoa?.isChecked!!) {
                // Se ambos os CheckBoxes estiverem desmarcados, define numPessoas como 0
                numPessoas = 0
            }
        }

        binding?.btnEnviar?.setOnClickListener {
            avancarIntent()
        }

        //Seta Voltar
        setSupportActionBar(binding?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Remove o texto do nome do aplicativo

        // Define o ícone da seta como o drawable customizado
        supportActionBar?.setHomeAsUpIndicator(R.drawable.round_arrow_back_24)
    }


    //Função que recebe o número de pessoas que usarão a pulseira e guarda no companion object para acesso via outra Activity.
    private fun adicionarPessoa(numero: Int){
        numPessoas = numero
    }

    //Função que avança para a intent do NFC, ja carregando a string dos dados que foram obtidos até esse momento (Locação + num de pessoas)
    fun avancarIntent(){
        if(numPessoas != 0){
            Toast.makeText(baseContext,"numero escolhido: $numPessoas",Toast.LENGTH_SHORT).show()
            if (numPessoas == 2){
                val intentCamera = Intent(this@SelectPessoasActivity, TirarFotoActivity::class.java)
                intentCamera.putExtra("dupla","true")
                intentCamera.putExtra("primeiro","true")
                startActivity(intentCamera)
            }else if (numPessoas == 1){
                val intentCamera = Intent(this@SelectPessoasActivity, TirarFotoActivity::class.java)
                intentCamera.putExtra("dupla","false")
                intentCamera.putExtra("primeiro","true")
                startActivity(intentCamera)
            }else{
                Toast.makeText(baseContext,"Erro, tente novamente mais tarde.",Toast.LENGTH_SHORT).show()
            }
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