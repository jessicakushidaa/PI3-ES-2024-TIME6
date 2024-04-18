package com.example.pi_iii_grupo6

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.pi_iii_grupo6.databinding.ActivityShowCardBinding
import com.google.gson.Gson

class ShowCardActivity : AppCompatActivity() {
    private var binding: ActivityShowCardBinding? = null
    private var gson = Gson()
    private lateinit var cartaoUsuario: CreateCardActivity.Cartao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowCardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        receberCartao()
        mostrarInformacoes()
    }

    private fun mostrarInformacoes() {
        var tvNome = binding?.tvNomeTitular
        var tvNumero = binding?.tvNumeroCartao
        var tvData = binding?.tvDataVal

        var digitosOriginais = cartaoUsuario.numeroCartao as String
        var ultimosDigitos = "${digitosOriginais[12]}${digitosOriginais[13]}${digitosOriginais[14]}${digitosOriginais[15]}"
        var numeroFormatado = "**** **** **** $ultimosDigitos"

        tvNome?.text = cartaoUsuario.nomeTitular
        tvNumero?.text = numeroFormatado
        tvData?.text = cartaoUsuario.dataVal
    }

    private fun receberCartao() {
        val stringCartao = intent.getStringExtra("cartaoRecebido")
        cartaoUsuario = gson.fromJson(stringCartao, CreateCardActivity.Cartao::class.java)
    }
}