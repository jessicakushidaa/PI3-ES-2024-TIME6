package com.example.pi_iii_grupo6

import BasicaActivity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.pi_iii_grupo6.LiberarLocacaoActivity.Companion.atualLocacao
import com.example.pi_iii_grupo6.databinding.ActivityConfirmarFotosBinding

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class ConfirmarFotosActivity : BasicaActivity() {
    private var binding: ActivityConfirmarFotosBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmarFotosBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        carregarInfos()

        binding?.btnRefazer?.setOnClickListener {
            if (atualLocacao.foto.size == 2){
                val intentCamera = Intent(this@ConfirmarFotosActivity, TirarFotoActivity::class.java)
                intentCamera.putExtra("dupla","true")
                atualLocacao.foto.clear()
                startActivity(intentCamera)
            }else if (atualLocacao.foto.size == 1){
                val intentCamera = Intent(this@ConfirmarFotosActivity, TirarFotoActivity::class.java)
                intentCamera.putExtra("dupla","false")
                atualLocacao.foto.clear()
                startActivity(intentCamera)
            }
        }

        binding?.btnConfirmar?.setOnClickListener {
            val intent = Intent(
                this@ConfirmarFotosActivity,
                VincularPulseiraActivity::class.java
            )
            intent.putExtra("Activity", "vincular")
            if(SelectPessoasActivity.numPessoas == 2) intent.putExtra("dupla","true") else intent.putExtra("dupla","false")
            startActivity(intent)
        }
    }
    //Função que carrega as fotos de acordo com o nuero de fotos
    private fun carregarInfos() {
        if (atualLocacao.foto.size == 1){
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
        }else if(atualLocacao.foto.size == 2){
            val string1 = atualLocacao.foto[0]
            val string2 = atualLocacao.foto[1]

            val bitmap1 = base64ToBitmap(string1)
            val bitmap2 = base64ToBitmap(string2)

            Log.d("RECEBIDA","duas: $string2")

            var ivImage1 = binding?.ivImagem1
            var ivImage2 = binding?.ivImagem2
            ivImage1!!.setImageBitmap(bitmap1)
            ivImage2!!.setImageBitmap(bitmap2)
        }
    }

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