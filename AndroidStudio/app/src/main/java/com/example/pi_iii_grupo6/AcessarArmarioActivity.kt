package com.example.pi_iii_grupo6

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory.Options
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.pi_iii_grupo6.databinding.ActivityAcessarArmarioBinding
import com.google.maps.android.Context
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions


class AcessarArmarioActivity : AppCompatActivity() {
    private var binding: ActivityAcessarArmarioBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAcessarArmarioBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnAbrirTemp?.setOnClickListener {
            //Abrir armario com dialog
            Toast.makeText(baseContext,"Armario aberto",Toast.LENGTH_SHORT).show()
        }
        binding?.btnEncerrarLoc?.setOnClickListener {
            //Abrir dialog para perguntar se deseja mesmo encerrar locacao
            mostrarDialog()
        }
    }

    private fun mostrarDialog() {
        var dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_encerrar_loc)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvEncerrar: TextView = dialog.findViewById(R.id.tvEncerrar)
        val tvCancelar: TextView = dialog.findViewById(R.id.tvCancelar)

        tvCancelar.setOnClickListener {
            dialog.dismiss()
        }

        tvEncerrar.setOnClickListener {
            var intentExcluir = Intent(this@AcessarArmarioActivity, ExcluirLocActivity::class.java)
            startActivity(intentExcluir)

        }

        dialog.show()
    }
}