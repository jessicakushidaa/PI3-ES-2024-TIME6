package com.example.pi_iii_grupo6

import BasicaActivity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.pi_iii_grupo6.LiberarLocacaoActivity.Companion.atualLocacao
import com.example.pi_iii_grupo6.SelectPessoasActivity.Companion.numPessoas
import com.example.pi_iii_grupo6.databinding.ActivityTirarFotoBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TirarFotoActivity : BasicaActivity() {
    private var binding: ActivityTirarFotoBinding? = null
    //Variável que controla as intâncias da câmera que estão abertas
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    //Variável que dirá qual câmera será utilizada: Frontal ou traseira.
    private lateinit var cameraSelector: CameraSelector
    //Variável para guardar nossa imagem
    private var imageCapture: ImageCapture? = null
    //Variável que será responsável pela execução em outra trhead
    private lateinit var imageCaptureExecutor: ExecutorService


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTirarFotoBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imageCaptureExecutor = Executors.newSingleThreadExecutor()

        startCamera()

        binding?.btnFoto?.setOnClickListener {
            takePicture()
        }

        //Seta Retorno
        val toolbar : Toolbar = findViewById(R.id.toolbar) //achando id da toolbar

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //Botão voltar
        supportActionBar?.setDisplayShowTitleEnabled(false) // Remove o texto do nome do aplicativo
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startCamera(){
        cameraProviderFuture.addListener({
            imageCapture = ImageCapture.Builder().setTargetResolution(Size(250,400)).build()

            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding?.cameraPreview?.surfaceProvider)
            }

            try{
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this,cameraSelector,preview, imageCapture)
            }catch (e:Exception){

            }
        }, ContextCompat.getMainExecutor(this))
    }

    //Função que tira foto ao clicar no botao de tirar foto.
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun takePicture(){
        imageCapture?.let {

            it.takePicture(
                imageCaptureExecutor,
                object : ImageCapture.OnImageCapturedCallback(){
                    override fun onCaptureSuccess(image: ImageProxy) {
                        Log.d("FOTO","Entrou na capture Sucess")
                        super.onCaptureSuccess(image)
                        val bitmapImage = image.toBitmap()
                        val rotatedBitmap = rotateBitmap(bitmapImage,90)
                        val base64 = convertToBase64(rotatedBitmap)
                        images.add(base64)
                        atualLocacao.foto.add(base64)
                        runOnUiThread {
                            Toast.makeText(
                                baseContext,
                                "Convertida com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("FOTO", base64)

                            //Se tiver selecionado duas pessoas, repetir o processo, se não, apenas uma vez.
                            val extra = intent.getStringExtra("dupla")
                            if (extra == "true") {
                                val intent =
                                    Intent(this@TirarFotoActivity, TirarFotoActivity::class.java)
                                intent.putExtra("dupla", "false")
                                startActivity(intent)
                            } else {
                                val intent = Intent(
                                    this@TirarFotoActivity,
                                    ConfirmarFotosActivity::class.java
                                )
                                intent.putExtra("Activity", "vincular")
                                if(numPessoas == 2) intent.putExtra("dupla","true") else intent.putExtra("dupla","false")
                                startActivity(intent)
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        super.onError(exception)
                    }
                }
            )
        }
    }

    private fun convertToBase64(bitmapImage: Bitmap): String {
            val stream = ByteArrayOutputStream()
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, stream)
            val byteArray = stream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        if (degree == 0) {
            return bitmap
        }
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    companion object{
        var images: MutableList<String> = mutableListOf()
    }
}