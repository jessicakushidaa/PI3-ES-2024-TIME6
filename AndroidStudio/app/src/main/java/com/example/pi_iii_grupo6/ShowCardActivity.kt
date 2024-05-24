package com.example.pi_iii_grupo6

import BasicaActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.delay

class ShowCardActivity : BasicaActivity() {
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
                    if (cartaoJson != null) {
                        val cartao: CreateCardActivity.Cartao? = gson.fromJson(cartaoJson, CreateCardActivity.Cartao::class.java)
                        if (cartao != null) {
                            cartaoUsuario = cartao

                        } else {
                            Log.d("BUSCACARTAO", "Resultado da consulta é nulo")
                        }
                    } else {
                        Log.d("BUSCACARTAO", "Resultado da consulta é nulo")
                    }
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

    // Função para receber o ID da pessoa
    private fun receberId() {
        idPessoa = idDocumentPessoa
    }

    // Handler para exibir informações do cartão quando existem
    private fun consultarCartaoHandler(){
        if (cartaoUsuario!=null){
            Log.d("CARTAO","USUARIO TEM CARTAO")
            mostrarInformacoes()
        }else{
            Log.d("CARTAO","USUARIO NAO TEM CARTAO")
            removerCardInfos()
            mostrarNaoTem()
        }
    }

    // Função que simula botão desabilitado de adicionar cartão
    private fun desabilitarBtn(){
        binding?.btnAdicionarCartao?.let{
            // muda a cor do botao para cinza
            it.setColorFilter(ContextCompat.getColor(this, R.color.light_grey))
            it.setOnClickListener {
                it.isPressed = false
                Toast.makeText(baseContext,"Você já possui um cartão cadastrado",Toast.LENGTH_SHORT).show()
            }
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

    //Função que, quando nao tem um cartão, mostra a view falando que não tem cartao
    private fun mostrarNaoTem() {
        Log.d("CARTAO","Mostrando que nao tem")
        var tvInfo = binding?.tvInfoTitle

        tvInfo?.visibility = VISIBLE // Definindo a visibilidade de tvInfo
    }

    // Função para mostrar as informações do cartão
    private fun mostrarInformacoes() {
        Log.d("StringRecebida","ENTROU MOSTRAR INFORMAÇÕES")
        var tvInfo = binding?.linearInfo
        var tvNome = binding?.tvNomeTitular
        var tvNumero = binding?.tvNumeroCartao
        var tvTitle = binding?.tvCartaoTitle


        var digitosOriginais = cartaoUsuario?.numeroCartao as String
        var ultimosDigitos = "${digitosOriginais[12]}${digitosOriginais[13]}${digitosOriginais[14]}${digitosOriginais[15]}"
        var numeroFormatado = "**** **** **** $ultimosDigitos"

        tvInfo?.visibility = VISIBLE
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
            desabilitarBtn()
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
                val subcoletcions = payload["subCollectionsData"] as? Map<*, *>

                // Verifica se subCollectionsData não é nulo
                if (subcoletcions != null) {
                    val cartoes = subcoletcions["cartoes"] as? List<Map<String, Any>>

                    // Verifica se cartoes não é nulo
                    if (cartoes != null) {
                        // Verifica se cartoes não está vazio
                        if (cartoes.isNotEmpty()) {
                            val cartao = cartoes[0]
                            val numeroCartao = cartao["numeroCartao"] as String
                            val nomeTitular = cartao["nomeTitular"] as String
                            val dataVal = cartao["dataVal"] as String
                            val cartaoRecebido = CreateCardActivity.Cartao(nomeTitular, numeroCartao, dataVal)
                            gson.toJson(cartaoRecebido)
                        } else {
                            Log.d("StringRecebida", "A lista de cartões está vazia")
                            var cartaoRecebido = null
                            gson.toJson(cartaoRecebido)
                        }
                    } else {
                        Log.d("StringRecebida", "A lista de cartões é nula")
                        var cartaoRecebido = null
                        gson.toJson(cartaoRecebido)
                    }
                } else {
                    Log.d("StringRecebida", "A subcoleção é nula")
                    var cartaoRecebido = null
                    gson.toJson(cartaoRecebido)
                }

            }
    }

    //valor idpessoa
    companion object{
        var idPessoa: String? = null
    }
}