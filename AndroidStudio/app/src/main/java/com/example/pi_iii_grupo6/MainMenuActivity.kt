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

        auth = Firebase.auth
        user = auth.currentUser

        functions = Firebase.functions("southamerica-east1")


        checarLocacaoPendente()


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

    private fun mostrarDialogPendente(){
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
            //Abrir tela de qr code da locação
        }

        dialog.show()
    }

    private fun buscarArmarios(): Task<String>{
        return functions
            .getHttpsCallable("getAllUnits")
            .call()
            .continueWith{ task->
                if(task.isSuccessful){
                    val data = task.result.data as Map<String, Any>
                    val payload = data["payload"] as Map<String, Any>
                    val unidades = payload["unidades"] as ArrayList<*>
                    val numUnidades = unidades.count()
                    var listaDeUnidades: MutableList<MainViewActivity.Place> = mutableListOf()
                    var i = 0
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
                        var listaPrecos: MutableList<MainViewActivity.Preco> = mutableListOf()
                        while (j < numPrecos){
                            Log.d("DEBUG UNIDADES","ENTROU NO WHILE DO PRECO")
                            var precoAtual = tabelaPrecos[j] as Map<String, Any>
                            var preco = MainViewActivity.Preco(
                                precoAtual["tempo"],
                                precoAtual["preco"] as Double
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
        if (cartaoUsuario != null){
            val intentShowCard = Intent(this@MainMenuActivity, ShowCardActivity::class.java)
            val cartaoGson = gson.toJson(cartaoUsuario)

            intentShowCard.putExtra("cartaoRecebido",cartaoGson)
            intentShowCard.putExtra("IDpessoa",id)

            startActivity(intentShowCard)

        }else{
            val intentShowCard = Intent(this@MainMenuActivity, ShowCardActivity::class.java)
            intentShowCard.putExtra("IDpessoa",id)
            startActivity(intentShowCard)
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
        var idDocumentPessoa = ""
    }

    private fun checarLocacaoPendente() {
        if (locacoesPendentes.isNotEmpty()){
            Toast.makeText(baseContext,"Você Possui uma locação pendente! retorne para minhas locacoes",Toast.LENGTH_SHORT).show()
        }
    }
}