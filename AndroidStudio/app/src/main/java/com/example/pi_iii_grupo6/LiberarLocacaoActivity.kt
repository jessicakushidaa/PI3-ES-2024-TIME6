package com.example.pi_iii_grupo6

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.pi_iii_grupo6.databinding.ActivityAcessarArmarioBinding
import com.example.pi_iii_grupo6.databinding.ActivityLiberarLocacaoBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class LiberarLocacaoActivity : AppCompatActivity() {
    var binding: ActivityLiberarLocacaoBinding? = null
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

        val intentNext = Intent(this@LiberarLocacaoActivity, SelectPessoasActivity::class.java)
        startActivity(intentNext)
    }
    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiberarLocacaoBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        
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
    }
}