package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.pi_iii_grupo6.databinding.ActivityMainMenuBinding
import com.example.pi_iii_grupo6.MainViewActivity.Companion.locacoesPendentes
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.google.firebase.auth.auth
import com.google.gson.Gson
import com.google.firebase.auth.FirebaseUser

class MainMenuActivity : AppCompatActivity() {
    private var binding: ActivityMainMenuBinding? = null
    private lateinit var functions: FirebaseFunctions
    private var gson = Gson()

    //Declarando user como null, para depois atribuir o usuário do authenticator a ele (que pode ser null se for anonimo)
    var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        functions = Firebase.functions("southamerica-east1")

        checarLocacaoPendente()
        var idDocumentPessoa = ""

        pegarId().addOnSuccessListener { id->
            idDocumentPessoa = id
            Log.d("idrecebido","ID: $idDocumentPessoa")
            consultarCartao(idDocumentPessoa)
                .addOnCompleteListener { task->
                    if (task.isSuccessful){
                        val cartaoJson = task.result
                        val cartao: CreateCardActivity.Cartao = gson.fromJson(cartaoJson, CreateCardActivity.Cartao::class.java)
                        cartaoUsuario = cartao
                    }else{
                        Log.e("StringRecebida","erro ao chamar function: ${task.exception}")
                    }
                }
        }


        //Setando o clique em MAPA que vai abrir o mapa
        binding?.llMapa?.setOnClickListener{
            var avancar = Intent(this@MainMenuActivity, MainViewActivity::class.java)
            startActivity(avancar)
        }

        //Ao clicar em CARTAO, abre a função de adicionar um cartao
        binding?.llCartao?.setOnClickListener{
            cartaoHandler(idDocumentPessoa)
        }

        binding?.llOpcao?.setOnClickListener {
            var avancarLocacao = Intent(this@MainMenuActivity, RentManagerActivity::class.java)
            startActivity(avancarLocacao)
        }

        //Ao clicar no botão logout, chamar a função de logout
        binding?.btnLogout?.setOnClickListener{
            singOutFun()
        }


    }

    //Criando a função para logout
    private fun singOutFun(){
        //Checando se o usuário fez login
        //Se fez, faz o logout e sai da acitivity principal
        if(user != null){
            Firebase.auth.signOut()
            //Voltando para a pagina de login
            var voltarLogin = Intent(this@MainMenuActivity, LoginActivity::class.java)
            startActivity(voltarLogin)
            Toast.makeText(this@MainMenuActivity, "Logout feito com sucesso", Toast.LENGTH_SHORT).show()
            //Se não fez, nao pode usar esta função, pede para fazer o login
        }else{
            Toast.makeText(this@MainMenuActivity, "Faça login para acessar essa função", Toast.LENGTH_SHORT).show()
            //Abrindo tela de login
            var abrirLogin = Intent(this@MainMenuActivity, LoginActivity::class.java)
            startActivity(abrirLogin)
        }

    }

    //Função que interpreta qual activity será aberta, baseando-se na existência ou não de um cartao registrado do usuário.
    fun cartaoHandler(id: String){
        if (cartaoUsuario != null){
            val intentShowCard = Intent(this@MainMenuActivity, ShowCardActivity::class.java)
            val cartaoGson = gson.toJson(cartaoUsuario)

            intentShowCard.putExtra("cartaoRecebido",cartaoGson)

            startActivity(intentShowCard)

        }else{
            val intentCriarCartao = Intent(this@MainMenuActivity, CreateCardActivity::class.java)
            intentCriarCartao.putExtra("IDpessoa",id)
            startActivity(intentCriarCartao)
        }

    }
    fun pegarId(): Task<String> {
        return functions
            .getHttpsCallable("getDocumentId")
            .call()
            .continueWith { task ->
                if (task.isSuccessful) {
                    // Acesse os dados retornados
                    val data = task.result?.data as Map<String, Any>
                    val payload = data["payload"] as Map<String, Any> // Se houver payload
                    // Obtendo o ID do documento
                    val id = payload["documentId"] as String
                    // Retornando o ID como uma String
                    id
                } else {
                    // Se houver uma falha, lançar a exceção associada
                    throw task.exception ?: Exception("Unknown error occurred")
                }
            }
    }
    fun consultarCartao(id: String): Task<String>{
        Log.d("StringRecebida","Começou consultar Cartao")
        val data = hashMapOf(
            "documentId" to id,
            "collectionName" to "pessoas",
        )
        return functions
            .getHttpsCallable("getDocumentFields")
            .call(data)
            .continueWith{task ->
                val res = task.result.data as Map<String, Any>
                val payload = res["payload"] as Map<String, Any>
                val subcoletcions = payload["subCollectionsData"] as Map<String, Any>
                val cartoes = subcoletcions["cartoes"] as ArrayList<*>
                val cartao = cartoes[0] as Map<String, Any>
                val numeroCartao = cartao["numeroCartao"] as String
                val nomeTitular = cartao["nomeTitular"] as String
                val dataVal = cartao["dataVal"] as String
                val cartaoRecebido = CreateCardActivity.Cartao(nomeTitular,numeroCartao,dataVal)
                val cartoesgson = gson.toJson(cartaoRecebido)
                cartoesgson

            }
    }
    companion object{
        var cartaoUsuario: CreateCardActivity.Cartao? = null
    }

    private fun checarLocacaoPendente() {
        if (locacoesPendentes.isNotEmpty()){
            Toast.makeText(baseContext,"Você Possui uma locação pendente! retorne para minhas locacoes",Toast.LENGTH_SHORT).show()
        }
    }
}