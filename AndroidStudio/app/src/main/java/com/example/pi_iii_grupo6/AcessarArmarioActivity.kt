package com.example.pi_iii_grupo6

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pi_iii_grupo6.databinding.ActivityAcessarArmarioBinding

class AcessarArmarioActivity : AppCompatActivity() {
    var binding: ActivityAcessarArmarioBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAcessarArmarioBinding.inflate(layoutInflater)
        setContentView(binding?.root)
    }
}