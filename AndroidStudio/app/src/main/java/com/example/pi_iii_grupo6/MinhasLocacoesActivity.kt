package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pi_iii_grupo6.databinding.ActivityMinhasLocacoesBinding
import com.example.pi_iii_grupo6.MainViewActivity.Companion.locacoesConfirmadas
import com.example.pi_iii_grupo6.MainViewActivity.Companion.locacoesPendentes
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson


class MinhasLocacoesActivity : AppCompatActivity() {
    private var binding: ActivityMinhasLocacoesBinding? = null
    private var gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMinhasLocacoesBinding.inflate(layoutInflater)
        setContentView(binding?.root)

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

        //Seta Retorno
        val toolbar : Toolbar = findViewById(R.id.toolbar) //achando id da toolbar

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //Botão voltar
        supportActionBar?.setDisplayShowTitleEnabled(false) // Remove o texto do nome do aplicativo



        //Declarando user como null, para depois atribuir o usuário do authenticator a ele (que pode ser null se for anonimo)
        var user: FirebaseUser? = null

        //Direcionando o bottomNavigation
        val bottomNavigation : BottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                //tela Locações
                R.id.page_locacoes -> {
                    if (user != null) {
                        startActivity(Intent(this, RentManagerActivity::class.java))
                    }else{
                        Toast.makeText((baseContext), "Faça login para acessar essa função",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    true
                }
                //tela Mapa
                R.id.page_mapa -> {
                    startActivity(Intent(this, MainViewActivity::class.java))
                    true
                }
                //tela Cartões
                R.id.page_cartoes -> {
                    if (user != null) {
                        startActivity(Intent(this, ShowCardActivity::class.java))
                    }else{
                        Toast.makeText((baseContext),"Faça login para acessar essa função",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    true
                }

                else -> false

            }
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