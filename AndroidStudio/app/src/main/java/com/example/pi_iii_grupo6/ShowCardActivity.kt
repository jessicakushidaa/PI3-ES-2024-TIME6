package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
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

        // Inicialização do Firebase Authentication e Firebase Functions
        auth = Firebase.auth
        functions = Firebase.functions("southamerica-east1")

        // Obtendo o ID do usuário atualmente autenticado
        var idUser = auth.currentUser?.uid

        //Recebendo o ID do usuário
        receberId()

        // Consultando o cartão do usuário a partir do ID da pessoa
        consultarCartao(idPessoa)
            .addOnCompleteListener { task->
                if (task.isSuccessful){
                    val cartaoJson = task.result
                    val cartao: CreateCardActivity.Cartao = gson.fromJson(cartaoJson, CreateCardActivity.Cartao::class.java)
                    cartaoUsuario = cartao
                    consultarCartaoHandler()
                }else{
                    Log.e("BUSCACARTAO","erro ao chamar function: ${task.exception}")
                    removerCardInfos()
                }
            }

        //Setando onclick para chamar função que abre a createCard
        binding?.btnAdicionarCartao?.setOnClickListener {
            abrirCreateCard(idPessoa)
        }

        // Configurando a Toolbar para permitir voltar à tela anterior
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

    // Função para receber o ID da pessoa
    private fun receberId() {
        idPessoa = idDocumentPessoa
    }

    // Handler para exibir informações do cartão quando existem
    private fun consultarCartaoHandler(){
        if (cartaoUsuario!=null){
            Log.d("CARTAO","USUARIO TEM CARTAO")
            mostrarInformacoes()
            removerNaoTem()
        }else{
            Log.d("CARTAO","USUARIO NAO TEM CARTAO")
            removerCardInfos()
        }
    }

    //Função que remove a view que mostra as informações do cartão, quando não há um cartão
    private fun removerCardInfos() {
        Log.d("remove","Removendo informacoes do cartao")
        var tvInfo = binding?.linearInfo

        val viewPai = tvInfo?.parent as ViewGroup
        viewPai.removeView(tvInfo)
    }

    //Função que, quando tem um cartão, remove a view falando que não tem
    private fun removerNaoTem() {
        Log.d("remove","Removendo informacoes")
        var tvInfo = binding?.tvInfoTitle

        val viewPai = tvInfo?.parent as ViewGroup
        viewPai.removeView(tvInfo)
    }

    // Função para mostrar as informações do cartão
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

    // Função para abrir a tela de criação de cartão
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

    // Função para consultar o cartão do usuário no Firebase
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

    //valor idpessoa
    companion object{
        var idPessoa: String? = null
    }
}