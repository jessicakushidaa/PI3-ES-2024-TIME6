package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.pi_iii_grupo6.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {
    //criando variável de autenticação do firebase
    private lateinit var auth: FirebaseAuth
    //criando variável do ViewBinding
    private var binding: ActivityLoginBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Inflando Layout
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth

        //Setando navegaçao para registro ao clicar no "nao tem uma conta?"
        binding?.tvCadastrar?.setOnClickListener{
            var abrirCadastro = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(abrirCadastro)
        }

        //Setando o que fazer quando clicar no botao login
        binding?.btnLogin?.setOnClickListener{
            var email: String = binding?.etEmail?.text.toString()
            var senha: String = binding?.etSenha?.text.toString()

            //verificando se dados foram preenchidos
            if(email.isNotEmpty() && senha.isNotEmpty()){
                //se sim, chama a funçao de login
                singInWithEmailAndPassword(email, senha)
            }else{
                //se nao, mensagem na tela para preencher todos os campos
                Toast.makeText(this@LoginActivity, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Função para login com authentication
    private fun singInWithEmailAndPassword(email: String, senha: String){
        auth.signInWithEmailAndPassword(email,senha).addOnCompleteListener{ task ->
            //Se o login for um sucesso
            if(task.isSuccessful){
                Log.d(TAG, "signInUserWithEmail:success")

                //Avançar para tela inicial
                var avancarTelaInicial = Intent(this@LoginActivity, MainViewActivity::class.java)
                startActivity(avancarTelaInicial)
            //se nao fro um sucesso, logar a falha no catlog
            }else{
                Log.w(TAG, "singInUserWithEmail:failure", task.exception)
                Toast.makeText(baseContext, "Autenticão Falhou", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Criação da TAG para log no catlog
    companion object{
        private var TAG = "EmailAndPassword"
    }
    //Função que volta o binding para null
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}