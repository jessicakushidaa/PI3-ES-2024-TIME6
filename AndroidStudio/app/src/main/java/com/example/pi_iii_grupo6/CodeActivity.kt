package com.example.pi_iii_grupo6

import BasicaActivity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.pi_iii_grupo6.databinding.ActivityCodeBinding
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class CodeActivity : BasicaActivity() {
    private var binding: ActivityCodeBinding? = null

    private var infoString: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCodeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnQuit?.setOnClickListener {
            var intentVoltarMenu = Intent(this@CodeActivity,MainMenuActivity::class.java)
            startActivity(intentVoltarMenu)}

        receberDados()
        gerarQrCode()
    }


    fun receberDados(){
        infoString = intent.getStringExtra("infosJson")
    }
    fun gerarQrCode(){
        val writer = QRCodeWriter()

        try {

            //Variável que cria matriz ja transformando os dados em qr code
            val bitMatrix = writer.encode(infoString, BarcodeFormat.QR_CODE, 1024,1024)
            //Variável que cria o bitmap do Qr Code
            val bitmap = Bitmap.createBitmap(bitMatrix.width, bitMatrix.height, Bitmap.Config.RGB_565)
            for (x in 0 until bitMatrix.width){
                for (y in 0 until bitMatrix.height){
                    bitmap.setPixel(x,y, if (bitMatrix[x,y]) Color.BLACK else Color.WHITE)
                }
            }

            binding?.imQrCode?.setImageBitmap(bitmap)

        }catch (e: WriterException){
            e.printStackTrace()
        }
    }
}