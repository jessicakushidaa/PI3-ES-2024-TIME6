package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pi_iii_grupo6.databinding.ActivityTirarFotoBinding

class TirarFotoActivity : AppCompatActivity() {
    private var binding: ActivityTirarFotoBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTirarFotoBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnDone?.setOnClickListener {
            val intentNFC = Intent(this@TirarFotoActivity, VincularPulseiraActivity::class.java)
            startActivity(intentNFC)
        }
    }
}