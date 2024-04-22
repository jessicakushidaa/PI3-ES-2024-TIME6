package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
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

    }

    private fun mostrarInformacoes() {
        var tvNome = binding?.tvNomeTitular
        var tvNumero = binding?.tvNumeroCartao
        var tvData = binding?.tvDataVal
        var tvTitle = binding?.tvCartaoTitle


        var digitosOriginais = cartaoUsuario.numeroCartao as String
        var ultimosDigitos = "${digitosOriginais[12]}${digitosOriginais[13]}${digitosOriginais[14]}${digitosOriginais[15]}"
        var numeroFormatado = "**** **** **** $ultimosDigitos"

        tvNome?.text = cartaoUsuario.nomeTitular
        tvNumero?.text = numeroFormatado
        tvData?.text = cartaoUsuario.dataVal
        tvTitle?.text = "Meu cartão"

        binding?.btnAdicionarCartao?.setOnClickListener {
            abrirCreateCard()
        }
    }

    private fun abrirCreateCard() {
        if (temCartao == false){
            val abrirCreateCard = Intent(this@ShowCardActivity, CreateCardActivity::class.java)
            startActivity(abrirCreateCard)
        }else{
            Toast.makeText(baseContext,"Você ja possui um cartão cadastrado",Toast.LENGTH_SHORT).show()
        }
    }

    private fun receberCartao() {
        val stringCartao = intent.getStringExtra("cartaoRecebido")

        if (stringCartao != null){
            cartaoUsuario = gson.fromJson(stringCartao, CreateCardActivity.Cartao::class.java)
            temCartao = true
            mostrarInformacoes()
        }

    }

    companion object{
        var temCartao = false
    }
}