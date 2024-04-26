package com.example.pi_iii_grupo6

import android.app.Dialog
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

class CodeActivity : AppCompatActivity() {
    private var binding: ActivityCodeBinding? = null
    private var gson = Gson()
    private lateinit var info: RentActivity.Info
    private var infoString: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCodeBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        
        receberDados()
        gerarQrCode()
    }

    //Função que diz para o usuário que a cobrança foi feita
    private fun mostrarDialog() {
        var dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_cobrar_cartao)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnClose: Button = dialog.findViewById(R.id.btnClosePopup)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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