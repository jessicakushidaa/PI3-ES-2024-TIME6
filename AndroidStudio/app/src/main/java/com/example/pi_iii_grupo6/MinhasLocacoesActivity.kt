package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pi_iii_grupo6.databinding.ActivityMinhasLocacoesBinding
import com.example.pi_iii_grupo6.MainViewActivity.Companion.locacoesConfirmadas
import com.example.pi_iii_grupo6.MainViewActivity.Companion.locacoesPendentes
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson


class MinhasLocacoesActivity : AppCompatActivity() {
    private var binding: ActivityMinhasLocacoesBinding? = null
    private var gson = Gson()
    private lateinit var recyclerView: RecyclerView
    private lateinit var locList: ArrayList<LocacaoItem>
    private lateinit var locacaoAdapter: LocacaoAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMinhasLocacoesBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //Setar as variáveis da Recycler View
        recyclerView = findViewById(R.id.RecyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        locList = ArrayList()
        //Adicionando exemplo
        locList.add(LocacaoItem("Guarde e Deus te Guarde"))
        adicionarElementosLista(locList)
        if (locList.size != 0){
            //Excluir a mensagem de nenhuma loc se tiver alguma
            var tvNenhum:TextView = findViewById(R.id.appCompatTextView)
            val parentViewGroup = tvNenhum?.parent as ViewGroup

            parentViewGroup.removeView(tvNenhum)
        }
        locacaoAdapter = LocacaoAdapter(locList)
        recyclerView.adapter = locacaoAdapter

        //Seta Retorno
        val toolbar : Toolbar = findViewById(R.id.toolbar) //achando id da toolbar

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //Botão voltar
        supportActionBar?.setDisplayShowTitleEnabled(false) // Remove o texto do nome do aplicativo


        //Direcionando o bottomNavigation
        val bottomNavigation : BottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                //tela Locações
                R.id.page_locacoes -> {
                    startActivity(Intent(this, RentManagerActivity::class.java))
                    true
                }
                //tela Mapa
                R.id.page_mapa -> {
                    startActivity(Intent(this, MainViewActivity::class.java))
                    true
                }
                //tela Cartões
                R.id.page_cartoes -> {
                    startActivity(Intent(this, ShowCardActivity::class.java))
                    true

                }

                else -> false

            }
        }
    }
    //Função que chama a function que retorna as locações confirmadas do cliente
    private fun adicionarElementosLista(lista: ArrayList<LocacaoItem>) {
        //Implementar function que retorna as locações confirmadas do cliente
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