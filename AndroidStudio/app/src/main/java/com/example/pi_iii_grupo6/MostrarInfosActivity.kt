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
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class MostrarInfosActivity : AppCompatActivity() {
    private var binding: ActivityMostrarInfosBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMostrarInfosBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnFinalizar?.setOnClickListener {
            Toast.makeText(baseContext,"Locação feita com sucesso",Toast.LENGTH_SHORT).show()
            //Limpando a lista de imagens transformadas em string
            images.clear()
            val intent = Intent(this@MostrarInfosActivity,MainViewGerenteActivity::class.java)
            startActivity(intent)
        }

        carregarImagem()
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

            var ivImage1 = binding?.ivImage
            var ivImage2 = binding?.ivImage2
            ivImage1?.setImageBitmap(bitmap1)
            ivImage2?.setImageBitmap(bitmap2)

            //Setando o texto da pulseira
            tvPulseiras?.text = "Pulseiras: ${atualLocacao.pulseiras[0]}, ${atualLocacao.pulseiras[1]}"
        }else if(numPessoas == 1){
            //Excluir a ivImage2 (nao existe uma segunda foto)
            var ivImage2 = binding?.ivImage2
            val parentViewGroup = ivImage2?.parent as ViewGroup

            parentViewGroup.removeView(ivImage2)
            //Carregar a única imagem

            val string = atualLocacao.foto[0]
            val bitmap = base64ToBitmap(string)

            Log.d("RECEBIDA",string)

            var ivImage = binding?.ivImage
            ivImage?.setImageBitmap(bitmap)

            //Setando o texto da pulseira
            tvPulseiras?.text = "Pulseira: ${atualLocacao.pulseiras[0]}"
        }
        tvPreco?.text = "Preço: ${atualLocacao.preco?.preco}"
        tvTempo?.text = "Tempo: ${atualLocacao.preco?.tempo}"
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