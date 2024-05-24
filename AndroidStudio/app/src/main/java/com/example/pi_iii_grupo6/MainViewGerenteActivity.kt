package com.example.pi_iii_grupo6

import BasicaActivity
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.pi_iii_grupo6.databinding.ActivityMainViewGerenteBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainViewGerenteActivity : BasicaActivity() {
    var binding: ActivityMainViewGerenteBinding? = null
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainViewGerenteBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth

        binding?.tvAcessarArmario?.setOnClickListener {
            //Abrir acessar armario
            val intentVincular = Intent(this@MainViewGerenteActivity,VincularPulseiraActivity::class.java)
            intentVincular.putExtra("Activity","buscar")
            intentVincular.putExtra("dupla","false")
            startActivity(intentVincular)
        }

        binding?.tvLiberarLocacao?.setOnClickListener {
            //Abrir Liberar Locação
            val intentLiberarLocacao = Intent(this@MainViewGerenteActivity,LiberarLocacaoActivity::class.java)
            startActivity(intentLiberarLocacao)
        }

        binding?.btnExit?.setOnClickListener{
            Firebase.auth.signOut()
            //Voltando para a pagina de login
            var voltarLogin = Intent(this@MainViewGerenteActivity, LoginActivity::class.java)
            startActivity(voltarLogin)
            Toast.makeText(baseContext, "Logout feito com sucesso", Toast.LENGTH_SHORT).show()
        }

    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
    }
}