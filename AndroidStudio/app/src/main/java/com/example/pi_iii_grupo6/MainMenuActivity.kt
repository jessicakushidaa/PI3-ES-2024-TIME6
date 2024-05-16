package com.example.pi_iii_grupo6

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.pi_iii_grupo6.databinding.ActivityMainMenuBinding
import com.example.pi_iii_grupo6.MainViewActivity.Companion.locacoesPendentes
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.google.firebase.auth.auth
import com.google.gson.Gson

class Pendente(
    var pendente: Boolean,
    var preco: MainViewActivity.Preco,
    var idUnidade: String
)
class MainMenuActivity : AppCompatActivity() {
    private var binding: ActivityMainMenuBinding? = null
    private lateinit var functions: FirebaseFunctions
    private var gson = Gson()
    private lateinit var auth: FirebaseAuth

    //Declarando user como null, para depois atribuir o usuário do authenticator a ele (que pode ser null se for anonimo)
    var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        //inicializando variáveis
        auth = Firebase.auth
        user = auth.currentUser

        functions = Firebase.functions("southamerica-east1")


        checarLocacaoPendente()

        //pegando o id do docmuento do usuário no banco de dados
        pegarId().addOnSuccessListener { id->
            idDocumentPessoa = id
            Log.d("idrecebido","ID: $idDocumentPessoa")
            //Com o id, chamar a função que busca o cartão do usuário no banco, se tiver
            checarLocacaoPendente().addOnCompleteListener { task->
                if (task.isSuccessful){
                    val pendente = task.result
                    if (pendente.pendente){
                        mostrarDialogPendente(pendente)
                    }
                }else{
                    Log.e("FunPendente","Erro ao checar pendencia: ${task.exception}")
                    Log.e("ERROR","Erro ao checar pendencia: ${task.exception}")
                }
            }

            // Consultando o cartão do usuário a partir do ID da pessoa
            consultarCartao(idDocumentPessoa)
                .addOnCompleteListener { task->
                    if (task.isSuccessful){
                        val cartaoJson = task.result
                        if (cartaoJson != null) {
                            val cartao: CreateCardActivity.Cartao? = gson.fromJson(cartaoJson, CreateCardActivity.Cartao::class.java)
                            if (cartao != null) {
                                cartaoUsuario = cartao
                            }
                        } else {
                            Log.d("BUSCACARTAO", "Resultado da consulta é nulo")
                        }
                    }else{
                        Log.e("BUSCACARTAO","erro ao chamar function: ${task.exception}")
                    }
                }
        }




        //Chamar funcao que busca todos os armarios
        buscarArmarios().addOnCompleteListener { task->
            if (task.isSuccessful){
                val armariosGson = task.result
                //val unidadesLocacao = gson.fromJson(armariosGson, listOf<Place>()::class.java)
                Log.d("LOGARMARIOS", "$armariosGson")
            }else{
                Log.e("LOGARMARIOS", "Erro ao buscar armarios: ${task.exception}")
            }
        }


        //Setando o clique em MAPA que vai abrir o mapa
        binding?.llMapa?.setOnClickListener{
            var avancarMapa = Intent(this@MainMenuActivity, MainViewActivity::class.java)
            startActivity(avancarMapa)
        }

        //Ao clicar em CARTAO, abre a função de adicionar um cartao
        binding?.llCartao?.setOnClickListener{
            cartaoHandler(idDocumentPessoa)
        }
        //clicou no tv do RentManager
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


    private fun mostrarDialogPendente(locacao: Pendente){
        var dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_locacao_pendente)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvLocation: TextView = dialog.findViewById(R.id.tvLocation)
        val btnClose: Button = dialog.findViewById(R.id.btnClosePopup3)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        tvLocation.setOnClickListener {
            var intentQrCode = Intent(this@MainMenuActivity,CodeActivity::class.java)

            var infosJson = gson.toJson(locacao)

            intentQrCode.putExtra("infosJson",infosJson)
            startActivity(intentQrCode)
        }

        dialog.show()
    }

  
   //Função que busca as unidades no banco
    private fun buscarArmarios(): Task<String>{
        //Chamando function
        return functions
            .getHttpsCallable("getAllUnits")
            .call()
            .continueWith{ task->
                if(task.isSuccessful){
                    //Pegar os dados que vieram no result e colocar numa classe Place, que guarda uma unidade de locação
                    val data = task.result.data as Map<String, Any>
                    val payload = data["payload"] as Map<String, Any>
                    val unidades = payload["unidades"] as ArrayList<*>
                    val numUnidades = unidades.count()
                    var listaDeUnidades: MutableList<MainViewActivity.Place> = mutableListOf()
                    var i = 0
                    //Loop para percorrer todas as unidades recebidas
                    while (i < numUnidades){
                        Log.d("DEBUG UNIDADES","Entrou na unidade $i de $numUnidades")
                        val unidade = unidades[i] as Map<String, Any>
                        Log.d("DEBUG UNIDADES","$unidade")
                        val coordenadas = unidade["coordenadas"] as Map<String, Any>
                        val id = unidade["id"] as String
                        val latitude = coordenadas["latitude"] as Double
                        val longitude = coordenadas["longitude"] as Double
                        val nome = unidade["nome"] as String
                        val endereco = unidade["endereco"] as String
                        val descricao = unidade["descricao"] as String
                        val tabelaPrecos = unidade["tabelaPrecos"] as ArrayList<*>
                        val numPrecos = tabelaPrecos.count()
                        //Logica para pegar cada um dos precos, transformar na classe Preco e guardar em uma listOf<Preco>
                        var j = 0
                        //Criando uma lista que irá conter todos os preços da unidade
                        var listaPrecos: MutableList<MainViewActivity.Preco> = mutableListOf()
                        //Loop para pegar todos os precos da unidade
                        while (j < numPrecos){
                            Log.d("DEBUG UNIDADES","ENTROU NO WHILE DO PRECO")
                            var precoAtual = tabelaPrecos[j] as Map<*, *>
                            var preco = MainViewActivity.Preco(
                                precoAtual["tempo"],
                                precoAtual["preco"] as Double,
                            )
                            listaPrecos.add(preco)
                            j++
                        }

                        //Montar uma Place com os dados coletados e guardar na lugares: listOf<Places>
                        var unidadeLocacao = MainViewActivity.Place(
                            id,
                            latitude,
                            longitude,
                            nome,
                            endereco,
                            descricao,
                            listaPrecos
                        )
                        listaDeUnidades.add(unidadeLocacao)
                        i++
                    }
                    //colocando todas as unidades numa variável do companion object, para outras activities acessarem
                    MainViewActivity.places = listaDeUnidades
                    val unidadesJson = gson.toJson(listaDeUnidades)
                    unidadesJson
                }else{
                    throw task.exception ?: Exception("Unknown error occurred")
                }
            }
    }

    //Função que interpreta qual activity será aberta, baseando-se na existência ou não de um cartao registrado do usuário.
    fun cartaoHandler(id: String){
        //Se tiver cartão manda o cartao via intent, com put.extra
        if (cartaoUsuario != null){
            val intentShowCard = Intent(this@MainMenuActivity, ShowCardActivity::class.java)
            val cartaoGson = gson.toJson(cartaoUsuario)

            intentShowCard.putExtra("cartaoRecebido",cartaoGson)
            intentShowCard.putExtra("IDpessoa",id)

            startActivity(intentShowCard)

        }else{
            //Se nao tiver cartão, enviar apenas o id da pessoa via intent.
            val intentShowCard = Intent(this@MainMenuActivity, ShowCardActivity::class.java)
            intentShowCard.putExtra("IDpessoa",id)
            startActivity(intentShowCard)
        }

    }
    //Função que busca o id da pessoa, enviando o ID do user no auth
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
    //Função que consulta os cartões no banco de dados
    fun consultarCartao(id: String): Task<String>{

        //Setando os parâmetros
        Log.d("StringRecebida","Começou consultar Cartao")
        val data = hashMapOf(
            "documentId" to id,
            "collectionName" to "pessoas",
        )
        //Chamando a função
        return functions
            .getHttpsCallable("getDocumentFields")
            .call(data)
            .continueWith { task ->
                // Guardando os dados recebidos
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
                            gson.toJson(null)
                        }
                    } else {
                        Log.d("StringRecebida", "A lista de cartões é nula")
                        gson.toJson(null)
                    }
                } else {
                    Log.d("StringRecebida", "A subcoleção é nula")
                    gson.toJson(null)
                }

            }
    }
    //Variáveis que poderão ser acessadas por todas as activities
    companion object{
        var cartaoUsuario: CreateCardActivity.Cartao? = null
        var idDocumentPessoa = ""
    }

    private fun checarLocacaoPendente(): Task<Pendente> {
        val data = hashMapOf(
            "idPessoa" to idDocumentPessoa
        )

        return functions
            .getHttpsCallable("checkLocacao")
            .call(data)
            .continueWith{task->
                Log.d("FunPendente", "String recebida - iniciou função")
                val res = task.result.data as Map<*, *>
                val payload = res["payload"] as Map<*, *>
                val pendente = payload["pendente"] as Boolean

                Log.d("FunPendente", "Payload - $payload")
                if (pendente){
                    val snapshot = payload["locSnapshot"] as Map<String, Any>
                    val data = snapshot["data"] as Map<String, Any>
                    val precoEscolhido = data["precoTempoEscolhido"] as Map<String, Any>
                    val preco = precoEscolhido["preco"] as Double
                    val tempo = precoEscolhido["tempo"]
                    val armario = data["armario"] as Map<String, Any>
                    val path = armario["_path"] as Map<String, Any>
                    val segments= path["segments"] as ArrayList<*>
                    val idUnidade = segments[1] as String
                    var pendentetrue = Pendente(true, MainViewActivity.Preco(tempo, preco),idUnidade)
                    pendentetrue
                }else{
                    var pendentefalse = Pendente(false, MainViewActivity.Preco(0, 0.0), "")
                    pendentefalse
                }

            }
    }
}