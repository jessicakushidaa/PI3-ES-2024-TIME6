package com.example.pi_iii_grupo6

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.pi_iii_grupo6.databinding.ActivityAcessarArmarioBinding
import com.example.pi_iii_grupo6.databinding.ActivityLiberarLocacaoBinding
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class LiberarLocacaoActivity : AppCompatActivity() {
    private var binding: ActivityLiberarLocacaoBinding? = null
    private lateinit var gson: Gson


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
                isGranted: Boolean ->
            if (isGranted){
                openCamera()
            }else{
                //Explicar o motivo das permissões
            }
        }

    private val scanLauncher =
        registerForActivityResult(ScanContract()){ result: ScanIntentResult ->
            run {
                if (result.contents == null) {
                    Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
                } else {
                    setResult(result.contents)

                }
            }
        }

    //Função que recebe o conteúdo do codigo Qr e Passa para a variável resultado
    private fun setResult(string: String){
        resultado = string
        binding?.tvLiberarLocacao?.text = resultado.toEditable()

        checkQr(string)
    }

    //Função que verifica se o conteúdo do QrCode é válido e se for, transforma o conteudo de string Json para Classe Locação
    private fun checkQr(resultado: String) {
        try{
            var locacaoRecebida: MainViewActivity.Locacao = gson.fromJson(resultado,MainViewActivity.Locacao::class.java)
            atualLocacao = locacaoRecebida
            atualLocacao.foto = mutableListOf()
            atualLocacao.pulseiras = mutableListOf()
            Log.d("LOCACAO","Locação recebida, preco: ${locacaoRecebida.preco?.preco} tempo: ${locacaoRecebida.preco?.tempo}")

            val intentNext = Intent(this@LiberarLocacaoActivity, SelectPessoasActivity::class.java)
            startActivity(intentNext)
        }catch (e: Exception){
            Toast.makeText(baseContext,"QR code inválido",Toast.LENGTH_SHORT).show()
            Log.e("ERRO QR CODE","Erro ao ler QrCode: $e")
        }
    }

    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiberarLocacaoBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        gson = Gson()
        
        checkPermissionCamera(this)

    }

    //Função que checa as permissões da câmera do usuário
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissionCamera(context: android.content.Context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            openCamera()
        }else if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){
            Toast.makeText(context, "Permissões para Câmera necessárias", Toast.LENGTH_SHORT).show()
        }else{
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan QR code")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(false)
        options.setOrientationLocked(false)

        scanLauncher.launch(options)
    }

    companion object{
        lateinit var resultado: String
        lateinit var atualLocacao: MainViewActivity.Locacao
    }
}