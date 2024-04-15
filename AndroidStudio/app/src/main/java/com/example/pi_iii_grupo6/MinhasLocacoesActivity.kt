package com.example.pi_iii_grupo6

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pi_iii_grupo6.databinding.ActivityMinhasLocacoesBinding
import com.example.pi_iii_grupo6.MainViewActivity.Companion.locacoesConfirmadas


class MinhasLocacoesActivity : AppCompatActivity() {
    private var binding: ActivityMinhasLocacoesBinding? = null
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
        if (numLocs > 0){
            Toast.makeText(baseContext,"Voce possui ${numLocs} locações",Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(baseContext,"Voce NAO possui locações",Toast.LENGTH_SHORT).show()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}