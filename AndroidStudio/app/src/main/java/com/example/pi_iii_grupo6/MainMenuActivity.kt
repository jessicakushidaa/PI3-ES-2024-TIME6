package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.pi_iii_grupo6.databinding.ActivityMainMenuBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class MainMenuActivity : AppCompatActivity() {
    private var binding: ActivityMainMenuBinding? = null

    //Declarando user como null, para depois atribuir o usuário do authenticator a ele (que pode ser null se for anonimo)
    var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //Setando o clique em MAPA que vai abrir o mapa
        binding?.llMapa?.setOnClickListener{
            var avancar = Intent(this@MainMenuActivity, MainViewActivity::class.java)
            startActivity(avancar)
        }

        //Ao clicar em CARTAO, abre a função de adicionar um cartao
        binding?.llCartao?.setOnClickListener{
            var avancarCartao = Intent(this@MainMenuActivity, CreateCardActivity::class.java)
            startActivity(avancarCartao)
        }

        binding?.llOpcao?.setOnClickListener {
            var avancarLocacao = Intent(this@MainMenuActivity, RentManagerActivity::class.java)
            startActivity(avancarLocacao)
        }

        //Ao clicar no botão logout, chamar a função de logout
        binding?.btnLogout?.setOnClickListener{
            singOutFun()
        }


    }

    //Criando a função para logout
    private fun singOutFun(){
        //Checando se o usuário fez login
        //Se fez, faz o logout e sai da acitivity principal
        if(user != null){
            Firebase.auth.signOut()
            //Voltando para a pagina de login
            var voltarLogin = Intent(this@MainMenuActivity, LoginActivity::class.java)
            startActivity(voltarLogin)
            Toast.makeText(this@MainMenuActivity, "Logout feito com sucesso", Toast.LENGTH_SHORT).show()
            //Se não fez, nao pode usar esta função, pede para fazer o login
        }else{
            Toast.makeText(this@MainMenuActivity, "Faça login para acessar essa função", Toast.LENGTH_SHORT).show()
            //Abrindo tela de login
            var abrirLogin = Intent(this@MainMenuActivity, LoginActivity::class.java)
            startActivity(abrirLogin)
        }

    }

}