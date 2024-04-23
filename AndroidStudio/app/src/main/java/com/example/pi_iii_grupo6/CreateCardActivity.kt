package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.pi_iii_grupo6.databinding.ActivityCreateCardBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.auth.User
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.google.gson.Gson
import okhttp3.internal.format
import java.text.SimpleDateFormat
import java.util.Date
import java.util.logging.SimpleFormatter

class CreateCardActivity : AppCompatActivity() {
    //criando variável de autenticação do firebase
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityCreateCardBinding? = null
    private lateinit var functions: FirebaseFunctions
    private var gson = Gson()

    class Cartao(
        var nomeTitular: String,
        var numeroCartao: String,
        var dataVal: String,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth
        functions = Firebase.functions("southamerica-east1")

        val user = auth.currentUser
        val idPessoa = user?.uid
        var idDocumentPessoa = ""

        idDocumentPessoa = receberId()


        //O que fazer quando clicar em cadastrar
        binding?.btnCadastrar?.setOnClickListener{
            verificarPreenchidos(idDocumentPessoa)
        }

    /*    //AÇÕES DO BOTTOM NAVIGATION
        binding?.btnHome?.setOnClickListener {
            var irHome = Intent(this@CreateCardActivity, MainViewActivity::class.java)
            startActivity(irHome)
        }
        binding?.btnCartoes?.setOnClickListener {
            var irCartoes = Intent(this@CreateCardActivity, CreateCardActivity::class.java)
            startActivity(irCartoes)
        }
        binding?.btnLocacoes?.setOnClickListener {
            var irLocacoes = Intent(this@CreateCardActivity, RentManagerActivity::class.java)
            startActivity(irLocacoes)
        } */
    }
    private fun receberId(): String{
        var id = intent.getStringExtra("IDpessoa") as String
        Log.d("IDRECEBIDO", "$id")
        return id
    }
    private fun verificarPreenchidos(idPessoa: String) {
        val nomeTitular = binding?.etName?.text.toString()
        val numeroCartao = binding?.etCardNumber?.text.toString()
        val dataVal = binding?.etValidade?.text.toString()


        if(nomeTitular.isEmpty() || numeroCartao.isEmpty() || dataVal.isEmpty()){
            Toast.makeText(baseContext,"Preencha todos os campos!",Toast.LENGTH_SHORT).show()
        }else{
            if (dataVal.count() != 7){
                Toast.makeText(baseContext,"Preencha a data de validade corretamente",Toast.LENGTH_SHORT).show()
            }else if(numeroCartao.count() != 16){
                Toast.makeText(baseContext,"Numero do cartão inválido, utilize apenas numeros",Toast.LENGTH_SHORT).show()
            }
            else{
                //Campos preenchidos! criar instancia cartão e chamar function
                val cartao = Cartao(nomeTitular,numeroCartao,dataVal)
                cadastrarCartaoFirestore(cartao,idPessoa)
            }

        }
    }

    //Função que chama a function responsável por adicionar o cartão no banco de dados.
    private fun cadastrarCartaoFirestore(c: Cartao, userID: String): Task<String>{

        Log.d("CADASTRAR", "Entrou na cadastrar cartao")
        //Definindo os dados que serão passados aos parâmetros da function
        val data = hashMapOf(
            "nomeTitular" to c.nomeTitular,
            "numeroCartao" to c.numeroCartao,
            "dataVal" to c.dataVal,
            "idPessoa" to userID,
        )
        // Chamando a cloud function em si
        return functions
            //Nome da funcao
            .getHttpsCallable("addCartao")
            //Dados para os parâmetros
            .call(data)
            .continueWith { task ->
                if(task.isSuccessful){
                    val result = task.result?.data as Map<String, Any>
                    val payload = result["payload"] as Map<String, Any>

                    val id = payload["documentId"]
                    if (id is String) {
                        id
                    } else {
                        Log.e("ERROR","$id")
                        throw Exception("documentId não é uma String")
                    }

                }else{
                    throw task.exception ?: Exception("Unknown error occurred")
                }
            }
            .addOnFailureListener{
                Log.e("CADASTRAR", "Erro ao chamar funcao: $it")
            }
    }



}