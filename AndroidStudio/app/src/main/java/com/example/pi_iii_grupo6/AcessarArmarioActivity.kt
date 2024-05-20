package com.example.pi_iii_grupo6

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory.Options
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
        }
    }
}