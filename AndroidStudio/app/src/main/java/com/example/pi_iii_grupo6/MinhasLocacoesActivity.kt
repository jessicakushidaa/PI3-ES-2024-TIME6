package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pi_iii_grupo6.databinding.ActivityMinhasLocacoesBinding
import com.example.pi_iii_grupo6.MainViewActivity.Companion.locacoesConfirmadas
import com.example.pi_iii_grupo6.MainViewActivity.Companion.locacoesPendentes
import com.google.gson.Gson


class MinhasLocacoesActivity : AppCompatActivity() {
    private var binding: ActivityMinhasLocacoesBinding? = null
    private var gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMinhasLocacoesBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var recyclerView = binding?.recyclerviewLocacoes

        recyclerView?.layoutManager = LinearLayoutManager(this)

    checarLocacoes()
    }

    fun checarLocacoes(){
        var numLocs = locacoesConfirmadas.count()
        if (locacoesPendentes.isNotEmpty()){
            Toast.makeText(baseContext,"Voce possui uma locação pendente!",Toast.LENGTH_SHORT).show()
            var pendente = locacoesPendentes[0]
            confirmacao(pendente)
        } else if (numLocs > 0){
            Toast.makeText(baseContext,"Voce possui ${numLocs} locações",Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(baseContext,"Voce NAO possui locações concluídas",Toast.LENGTH_SHORT).show()
        }

    }

    fun confirmacao(locacao: MainViewActivity.Locacao){
        var intentQrCode = Intent(this@MinhasLocacoesActivity, CodeActivity::class.java)

        var infosJson = gson.toJson(locacao)

        intentQrCode.putExtra("infosJson",infosJson)
        startActivity(intentQrCode)
    }
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}