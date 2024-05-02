package com.example.pi_iii_grupo6

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pi_iii_grupo6.databinding.ActivityLiberarLocacaoBinding

class LiberarLocacaoActivity : AppCompatActivity() {
    var binding: ActivityLiberarLocacaoBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiberarLocacaoBinding.inflate(layoutInflater)
        setContentView(binding?.root)
    }
}