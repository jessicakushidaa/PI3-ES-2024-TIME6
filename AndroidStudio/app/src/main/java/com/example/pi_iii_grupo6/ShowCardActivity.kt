package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.pi_iii_grupo6.MainMenuActivity.Companion.cartaoUsuario
import com.example.pi_iii_grupo6.MainMenuActivity.Companion.idDocumentPessoa
import com.example.pi_iii_grupo6.databinding.ActivityShowCardBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.google.gson.Gson

class ShowCardActivity : AppCompatActivity() {
    private var binding: ActivityShowCardBinding? = null
    private lateinit var functions: FirebaseFunctions
    private var gson = Gson()



    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowCardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth
        functions = Firebase.functions("southamerica-east1")


        var idUser = auth.currentUser?.uid

        //Recebendo o ID do usuário
        receberId()

        consultarCartao(idPessoa)
            .addOnCompleteListener { task->
                if (task.isSuccessful){
                    val cartaoJson = task.result
                    val cartao: CreateCardActivity.Cartao = gson.fromJson(cartaoJson, CreateCardActivity.Cartao::class.java)
                    cartaoUsuario = cartao
                    consultarCartaoHandler()
                }else{
                    Log.e("BUSCACARTAO","erro ao chamar function: ${task.exception}")
                }
            }

        //Setando onclick para chamar função que abre a createCard
        binding?.btnAdicionarCartao?.setOnClickListener {
            abrirCreateCard(idPessoa)
        }

        //Seta Voltar
        val toolbar : Toolbar = findViewById(R.id.toolbar) //achando id da toolbar

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Remove o texto do nome do aplicativo


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

    private fun receberId() {
        idPessoa = idDocumentPessoa
    }

    private fun consultarCartaoHandler(){
        if (cartaoUsuario!=null){
            Log.d("CARTAO","USUARIO TEM CARTAO")
            mostrarInformacoes()
        }else{
            Log.d("CARTAO","USUARIO NAO TEM CARTAO")
        }
    }

    private fun mostrarInformacoes() {
        Log.d("StringRecebida","ENTROU MOSTRAR INFORMAÇÕES")
        var tvNome = binding?.tvNomeTitular
        var tvNumero = binding?.tvNumeroCartao
        var tvTitle = binding?.tvCartaoTitle


        var digitosOriginais = cartaoUsuario?.numeroCartao as String
        var ultimosDigitos = "${digitosOriginais[12]}${digitosOriginais[13]}${digitosOriginais[14]}${digitosOriginais[15]}"
        var numeroFormatado = "**** **** **** $ultimosDigitos"

        tvNome?.text = cartaoUsuario?.nomeTitular
        tvNumero?.text = numeroFormatado
        tvTitle?.text = "Meu cartão"
    }

    private fun abrirCreateCard(idUser: String?) {
        if (cartaoUsuario == null){
            val abrirCreateCard = Intent(this@ShowCardActivity, CreateCardActivity::class.java)
            abrirCreateCard.putExtra("IDpessoa",idUser)
            Log.d("debugcard","$idUser")
            startActivity(abrirCreateCard)
        }else{
            Toast.makeText(baseContext,"Você ja possui um cartão cadastrado",Toast.LENGTH_SHORT).show()
        }
    }
    fun consultarCartao(id: String?): Task<String> {
        Log.d("StringRecebida", "Começou consultar Cartao, id: $id")
        val data = hashMapOf(
            "documentId" to id,
            "collectionName" to "pessoas",
        )
        return functions
            .getHttpsCallable("getDocumentFields")
            .call(data)
            .continueWith { task ->
                val res = task.result.data as Map<String, Any>
                val payload = res["payload"] as Map<String, Any>
                val subcoletcions = payload["subCollectionsData"] as Map<String, Any>
                val cartoes = subcoletcions["cartoes"] as ArrayList<*>
                val cartao = cartoes[0] as Map<String, Any>
                val numeroCartao = cartao["numeroCartao"] as String
                val nomeTitular = cartao["nomeTitular"] as String
                val dataVal = cartao["dataVal"] as String
                val cartaoRecebido = CreateCardActivity.Cartao(nomeTitular, numeroCartao, dataVal)
                val cartoesgson = gson.toJson(cartaoRecebido)
                cartoesgson

            }
    }

    companion object{
        var idPessoa: String? = null
    }
}