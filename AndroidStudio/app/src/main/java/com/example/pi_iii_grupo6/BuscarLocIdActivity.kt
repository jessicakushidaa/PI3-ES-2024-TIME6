package com.example.pi_iii_grupo6

import BasicaActivity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.example.pi_iii_grupo6.LiberarLocacaoActivity.Companion.atualLocacao
import com.example.pi_iii_grupo6.MainViewActivity.Companion.places
import com.example.pi_iii_grupo6.databinding.ActivityBuscarLocIdBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.google.gson.Gson
import io.opencensus.stats.View
import java.util.Date

class BuscarLocIdActivity : BasicaActivity() {
    private var binding: ActivityBuscarLocIdBinding? = null
    private lateinit var gson: Gson
    private lateinit var functions: FirebaseFunctions
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuscarLocIdBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        gson = Gson()
        functions = Firebase.functions("southamerica-east1")

        progressBar = findViewById(R.id.progressBar)

        showProgressBar(true)
        buscarLocacao()?.addOnCompleteListener { task->
            if(task.result == null){
                Log.e("BUSCARLOC","ERRO AO BUSCAR: ${task.exception}")
                mostrarDialogNaoEncontrou()
            }
            else{
                var loc: MainViewActivity.Locacao = task.result
                Toast.makeText(baseContext,"Encontrada com sucesso",Toast.LENGTH_SHORT).show()
                showProgressBar(false)
                avancarTela()
            }
        }
    }
    // Função para controlar a visibilidade da ProgressBar
    private fun showProgressBar(show: Boolean) {
        progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }
    //Função que recebe o id da Pulseira via intent e busca no banco a locação
    private fun buscarLocacao(): Task<MainViewActivity.Locacao>? {
        val idRecebido = intent.getStringExtra("id")
        Log.d("BUSCARLOC","Entrou no buscar")
        val data = hashMapOf(
            "idTag" to idRecebido
        )

        return functions
            .getHttpsCallable("buscarLoc")
            .call(data)
            .continueWith{task ->
                Log.d("BUSCARLOC","Dentro da task")
                if (task.result.data == null){
                    Log.d("BUSCARLOC","Resultado null")
                    null
                }else {
                    Log.d("BUSCARLOC","Resultado nao null")
                    Log.d("BUSCARLOC","pegou res")
                    val res = task.result.data as Map<String, Any>
                    val status = res["status"] as String
                    if (status == "ERROR"){
                        Log.d("BUSCARLOC","NAO TEM")
                        null
                    }else{
                        Log.d("BUSCARLOC","payload")
                        val payload = res["payload"] as Map<String, Any>
                        //Id da locação
                        Log.d("BUSCARLOC","idloc")
                        val id = payload["idLocacao"] as String
                        //Informações da locação
                        Log.d("BUSCARLOC","infosLoc")
                        val data = payload["data"] as Map<String, Any>

                        //Pegando o preco escolhido
                        Log.d("BUSCARLOC","precotempo")
                        val precoEscolhido = data["precoTempoEscolhido"] as Map<String, Any>
                        val preco = precoEscolhido["preco"] as Double
                        val tempo = precoEscolhido["tempo"]

                        //Pegando as fotos
                        Log.d("BUSCARLOC","fotos")
                        val vetFotos = data["foto"] as ArrayList<String>
                        val size = vetFotos.size
                        val vetorFotos: MutableList<String> = mutableListOf()
                        vetorFotos.add(vetFotos[0])
                        if (size == 2) vetorFotos.add(vetFotos[1])

                        //Pegando a hora da locacao
                        Log.d("BUSCARLOC","lcoacao")
                        val horaLoc = data["horaLocacao"] as Map<String, Any>
                        val segundos = (horaLoc["_seconds"] as Number).toLong()
                        val nanosegundos = horaLoc["_nanoseconds"] as Int
                        val timestamp = Timestamp(segundos,nanosegundos)
                        val datetime = timestamp.toDate()

                        //Pegando as tags
                        Log.d("BUSCARLOC","tags")
                        val tags = data["tags"] as ArrayList<String>
                        val vetorTags: MutableList<String> = mutableListOf()
                        val sizeTags = tags.size
                        vetorTags.add(tags[0])
                        if (sizeTags == 2) vetorTags.add(tags[1])

                        //Pegando o id do usuário
                        Log.d("BUSCARLOC","usuario")
                        val cliente = data["cliente"] as ArrayList<*>
                        val cliente1 = cliente[0] as Map<String, Any>
                        val path = cliente1["_path"] as Map<String, Any>
                        val segments = path["segments"] as ArrayList<*>
                        val userId = segments[1] as String

                        //Pegando o id da Unidade de Locação
                        Log.d("BUSCARLOC","armario")
                        val armario = data["armario"] as Map<String, Any>
                        val pathArmario = armario["_path"] as Map<String, Any>
                        val segmentsArmario = pathArmario["segments"] as ArrayList<*>
                        val idUnidade = segmentsArmario[1] as String

                        //Adicionando na classe
                        locRecebida = MainViewActivity.Locacao(
                            userId,
                            null,
                            MainViewActivity.Preco(tempo, preco),
                            vetorFotos,
                            vetorTags,
                            id,
                            idUnidade,
                            datetime
                        )

                        locRecebida
                    }


                }
            }
    }
    private fun mostrarDialogNaoEncontrou() {
        var dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_nao_encontrou_pulseira)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnClose: Button = dialog.findViewById(R.id.btnClosePopupAberto)

        btnClose.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }

    //TEMPORARIA
    private fun avancarTela() {
        var intent = Intent(this@BuscarLocIdActivity,MostrarLocacaoActivity::class.java)
        startActivity(intent)
    }

    //IMPLEMENTAR FUNCTION QUE BUSCA A LOCAÇÃO NO BANCO, BASEADO NO ID DA PULSEIRA.

    companion object{
        //Variavel de TESTE
        lateinit var locRecebida: MainViewActivity.Locacao
    }
}