package com.example.pi_iii_grupo6

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import com.example.pi_iii_grupo6.LiberarLocacaoActivity.Companion.atualLocacao
import com.example.pi_iii_grupo6.SelectPessoasActivity.Companion.numPessoas
import com.example.pi_iii_grupo6.TirarFotoActivity.Companion.images
import com.example.pi_iii_grupo6.databinding.ActivityMostrarInfosBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class MostrarInfosActivity : AppCompatActivity() {
    private var binding: ActivityMostrarInfosBinding? = null
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMostrarInfosBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        functions = Firebase.functions("southamerica-east1")

        //Ao clicar no botão de finalizar locação, chama a função confirmarLoc
        binding?.btnFinalizar?.setOnClickListener {
            confirmarLoc().addOnCompleteListener { task->
                if (task.isSuccessful){
                    val result = task.result
                    val status = result["status"]
                    val message = result["message"]
                    Log.d("FUN CONFIRMARLOC", "String recebida: status - $status ;  message " +
                            " - $message")
                    if (status == "ERROR"){
                        Toast.makeText(baseContext,message,Toast.LENGTH_LONG).show()
                        val intent = Intent(this@MostrarInfosActivity, MainViewGerenteActivity::class.java)
                        startActivity(intent)
                    }else{
                    Toast.makeText(baseContext,"Locação feita com sucesso",Toast.LENGTH_SHORT).show()
                    Log.i("CONFIRMAR LOC","loc confirmada!: ${message}")
                    //Limpando a lista de imagens transformadas em string
                    images.clear()
                    val intent = Intent(this@MostrarInfosActivity,MainViewGerenteActivity::class.java)
                    startActivity(intent)
                    }
                }else{
                    Toast.makeText(baseContext,"Houve um erro ao concluir locação",Toast.LENGTH_SHORT).show()
                    Log.e("CONFIRMAR LOC","erro ao confirmar loc: ${task.exception}")
                }
            }
        }
        //Chamar função que carrega as informações da locação que está sendo feita.
        carregarImagem()
    }
    //Função que chama a function de mudar o status da locação, e adiciona as fotos e as tags na locação do banco.
    private fun confirmarLoc(): Task<Map<String, String>> {
        val data = hashMapOf(
            "idLocacao" to atualLocacao.locId,
            "idUnidade" to atualLocacao.armario?.id,
            "idTag" to atualLocacao.pulseiras,
            "foto" to atualLocacao.foto
        )

        Log.i("CONFIRMAR LOC","INFOS: idLocacao: ${atualLocacao.locId}, idArmario: ${atualLocacao.armario?.id}, tags: ${atualLocacao.pulseiras}, fotos: ${atualLocacao.foto}")

        return functions
            .getHttpsCallable("confirmarLoc")
            .call(data)
            .continueWith{ task->
                //Lidar com o resultado retornado/
                val res = task.result.data as Map<String, Any>
                val status = res["status"] as String
                val message = res["message"] as String
                mapOf("status" to status, "message" to message)
            }
    }

    private fun carregarImagem() {
        val tvPreco = binding?.tvPreco1
        val tvTempo = binding?.tvTempo1
        val tvPulseiras = binding?.tvPulseiras

        if(numPessoas == 2){
            val string1 = atualLocacao.foto[0]
            val string2 = atualLocacao.foto[1]

            val bitmap1 = base64ToBitmap(string1)
            val bitmap2 = base64ToBitmap(string2)

            Log.d("RECEBIDA","duas: $string2")

            var ivImage1 = binding?.ivImagem1
            var ivImage2 = binding?.ivImagem2
            ivImage1?.setImageBitmap(bitmap1)
            ivImage2?.setImageBitmap(bitmap2)

            //Setando o texto da pulseira
            tvPulseiras?.text = " ${atualLocacao.pulseiras[0]}, ${atualLocacao.pulseiras[1]}"
        }else if(numPessoas == 1){
            //Excluir a ivImage2 (nao existe uma segunda foto)
            var ivImage2 = binding?.ivImagem2
            val parentViewGroup = ivImage2?.parent as ViewGroup

            parentViewGroup.removeView(ivImage2)
            //Carregar a única imagem

            val string = atualLocacao.foto[0]
            val bitmap = base64ToBitmap(string)

            Log.d("RECEBIDA",string)

            var ivImage = binding?.ivImagem1
            ivImage?.setImageBitmap(bitmap)

            //Setando o texto da pulseira
            tvPulseiras?.text = " ${atualLocacao.pulseiras[0]}"
        }
        tvPreco?.text = " ${atualLocacao.preco?.preco}"
        tvTempo?.text = " ${atualLocacao.preco?.tempo}"
    }
    //Função que recebe uma string base64 e decodifica a mesma para Bitmap

    @OptIn(ExperimentalEncodingApi::class)
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            // Decodifica a string Base64 para um array de bytes
            val decodedBytes = Base64.decode(base64String, android.util.Base64.DEFAULT)
            Log.d("Base64ToBitmap", "Base64 decodificado para byte array de tamanho: ${decodedBytes.size}")

            // Converte o array de bytes em um Bitmap
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            if (bitmap != null) {
                Log.d("Base64ToBitmap", "Bitmap criado com sucesso")
            } else {
                Log.e("Base64ToBitmap", "Falha ao criar Bitmap")
            }
            bitmap
        } catch (e: IllegalArgumentException) {
            Log.e("Base64ToBitmap", "String Base64 inválida", e)
            null
        } catch (e: OutOfMemoryError) {
            Log.e("Base64ToBitmap", "Memória insuficiente para criar Bitmap", e)
            null
        } catch (e: Exception) {
            Log.e("Base64ToBitmap", "Erro inesperado ao converter Base64 para Bitmap", e)
            null
        }
    }
}