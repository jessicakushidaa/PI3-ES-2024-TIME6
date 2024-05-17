package com.example.pi_iii_grupo6

import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.pi_iii_grupo6.databinding.ActivityTirarFotoBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TirarFotoActivity : AppCompatActivity() {
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
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startCamera(){
        cameraProviderFuture.addListener({
            imageCapture = ImageCapture.Builder().build()

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
            //Nome do arquivo
            val fileName = "JPEG_CLIENT_${System.currentTimeMillis()}"
            val file = File(externalMediaDirs[0], fileName)

            it.takePicture(
                imageCaptureExecutor,
                object : ImageCapture.OnImageCapturedCallback(){
                    override fun onCaptureSuccess(image: ImageProxy) {
                        Log.d("FOTO","Entrou na capture Sucess")
                        super.onCaptureSuccess(image)
                        val bitmapImage = image.toBitmap()
                        val base64 = convertToBase64(bitmapImage)
                        runOnUiThread {
                            Toast.makeText(baseContext,"Convertida com sucesso!",Toast.LENGTH_SHORT).show()
                            Log.d("FOTO",base64)
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
        bitmapImage.compress(Bitmap.CompressFormat.JPEG,40,stream)
        val image = stream.toByteArray()
        val base64 =  Base64.encodeToString(image,Base64.DEFAULT)
        return base64
    }
}