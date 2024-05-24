package com.example.pi_iii_grupo6

import BasicaActivity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.pi_iii_grupo6.databinding.ActivityCreateCardBinding
import com.example.pi_iii_grupo6.databinding.DialogPopupBinding
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

class CreateCardActivity : BasicaActivity() {
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

        //Seta Voltar
        setSupportActionBar(binding?.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Remove o texto do nome do aplicativo

        // Define o ícone da seta como o drawable customizado
        supportActionBar?.setHomeAsUpIndicator(R.drawable.round_arrow_back_24)

        var idDocumentPessoa = ""
        idDocumentPessoa = receberId()

        /* Formatando o padrão de digitação da data de validade no formulário de
         cadastro */
        val dataVal = binding?.etValidade
        // Define um texto inicial com a barra (opcional)
        dataVal!!.setText("MM/AA")
        // Adicionando TextWatcher na edit text
        dataVal.addTextChangedListener(DateValTextFormat(dataVal))
        dataVal.setOnClickListener{
            // remove temporariamente o text Watcher
            dataVal.removeTextChangedListener(DateValTextFormat(dataVal))
            if (dataVal.text.toString() == "MM/AA") {
                dataVal.setText("") // limpa o edit text
                dataVal.setSelection(0) // posiciona cursor no início
            }
            // adiciona novamente o método
            dataVal.addTextChangedListener(DateValTextFormat(dataVal))
        }

        // O que fazer quando clicar em cadastrar
        binding?.btnCadastrar?.setOnClickListener {
            // Verifica se recebeu o ID corretamente
            if (idDocumentPessoa.isNotEmpty()) {
                // Chama a função verificarPreenchidos para cadastrar o cartão
                verificarPreenchidos(idDocumentPessoa)
            } else {
                // Exibe uma mensagem de erro caso o id nao tenha sido recebido
                Toast.makeText(this, "Ocorreu um erro ao cadastrar cartão. Recarregue a página",
                    Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun receberId(): String{
        Log.d(TAG,"entrou receber ID")
        var id = intent.getStringExtra("IDpessoa") as String
        Log.d("IDRECEBIDO", "$id")
        Log.d(TAG,"id recebido : $id")
        return id
    }
    private fun verificarPreenchidos(idPessoa: String) {
        val nomeTitular = binding?.etName?.text.toString()
        val numeroCartao = binding?.etCardNumber?.text.toString()
        val dataVal = binding?.etValidade?.text.toString()


        if(nomeTitular.isEmpty() || numeroCartao.isEmpty() || dataVal.isEmpty()){
            Toast.makeText(baseContext,"Preencha todos os campos!",Toast.LENGTH_SHORT).show()
        }else{
            if (dataVal.count() != 5){
                Toast.makeText(baseContext,"Preencha a data de validade corretamente: MM/AA",
                    Toast.LENGTH_SHORT).show()
            }else if(numeroCartao.count() != 16){
                Toast.makeText(baseContext,"Numero do cartão inválido, utilize apenas numeros",
                    Toast.LENGTH_SHORT).show()
            }
            else{
                // Mostra a ProgressBar
                val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                progressBar.isIndeterminate = true
                progressBar.visibility = View.VISIBLE

                //Campos preenchidos! criar instancia cartão e chamar function
                val cartao = Cartao(nomeTitular,numeroCartao,dataVal)
                cadastrarCartaoFirestore(cartao,idPessoa).addOnSuccessListener {
                    mostrarDialogCreated()
                    progressBar.visibility = View.GONE
                }
            }

        }
    }

    //Função que mostra a dialogBox para mostrar mensagem ao usuário
    private fun mostrarDialogCreated(){
        var dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_popup)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTitle: TextView = dialog.findViewById(R.id.tvTitle)
        val tvText: TextView = dialog.findViewById(R.id.tvText)
        val btnClose: Button = dialog.findViewById(R.id.btnClosePopup)

        // Adicionando o texto de cadastro do cartão à dialog padrão
        tvTitle.text = this.getString(R.string.dialogCadastradoTitle)
        tvText.text = this.getString(R.string.dialogCadastradoText)

        btnClose.setOnClickListener {
            dialog.dismiss()
            var voltarMostrarCartao = Intent(this@CreateCardActivity, ShowCardActivity::class.java)
            startActivity(voltarMostrarCartao)
        }

        dialog.show()
    }


    //Função que chama a function responsável por adicionar o cartão no banco de dados.
    private fun cadastrarCartaoFirestore(c: Cartao, userID: String): Task<String?>{

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

                    val id = payload["docId"] as String
                    id

                }else{
                    throw task.exception ?: Exception("Unknown error occurred")
                }
            }
            .addOnFailureListener{
                Log.e("CADASTRAR", "Erro ao chamar funcao: $data,  $it")
            }
    }
    companion object{
        var TAG = "DEBUGCARD"
    }
}