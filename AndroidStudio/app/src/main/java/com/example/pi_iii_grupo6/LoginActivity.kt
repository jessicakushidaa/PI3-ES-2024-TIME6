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

    //instanciando o usuário atual do authenticator -> Não logado = null
    private var user = Firebase.auth.currentUser
    private var emailVerified: Boolean? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Inflando Layout
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth
        //Checando se o usuário verificou o email
        emailVerified = auth.currentUser?.isEmailVerified
        checkLogin()

        //Setando navegaçao para registro ao clicar no "nao tem uma conta?"
        binding?.tvCadastrar?.setOnClickListener{
            var abrirCadastro = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(abrirCadastro)
        }
        //Definindo o que fazer ao clicar no Esqueceu a senha?
        binding?.tvForgot?.setOnClickListener{
            //Pegando o email digitado
            var email = binding?.etEmail?.text.toString()

            //Checar se realmente digitou um email válido
            if(email.isNotEmpty()){
                //Se digitou um email, envia o email de recuperação
                resetPassword(email)
            }else{
                //Não digitou um email, comunicar
                Toast.makeText(this@LoginActivity, "Preencha o email para recuperar a senha", Toast.LENGTH_SHORT).show()
            }
        }

        //Avançar de tela ao clicar em entrar anonimamente
        //isso não altera o valor de user, continua como null, portanto nao conseguirá utilizar certas funções
        binding?.tvAnonimous?.setOnClickListener{
            var avancar = Intent(this@LoginActivity, MainViewActivity::class.java)
            startActivity(avancar)
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

    //Função que checa se o usuário está logado
    private fun checkLogin(){
        if(user == null || emailVerified == false){
            Log.d(TAG, "signInUserWithEmail:null")
        }else{
            Log.d(TAG, "signInUserWithEmail:success")
            //Avançar para tela inicial
            var avancarTelaInicial = Intent(this@LoginActivity, MainMenuActivity::class.java)
            startActivity(avancarTelaInicial)
        }
    }

    //Função para login com authentication
    private fun singInWithEmailAndPassword(email: String, senha: String){
        auth.signInWithEmailAndPassword(email,senha).addOnCompleteListener{ task ->
            //Se o login for um sucesso
            if(task.isSuccessful){
                emailVerified = auth.currentUser?.isEmailVerified
                Log.d("VERIFIED", "Emailverified: ${emailVerified}")
                Log.d(TAG, "signInUserWithEmail:success")
                if (emailVerified == false){
                    Toast.makeText(baseContext, "Por favor, verifique seu email!", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                    var reiniciarActivity = Intent(this@LoginActivity, LoginActivity::class.java)
                    startActivity(reiniciarActivity)
                }else{
                    //Avançar para tela inicial
                    var avancarTelaInicial = Intent(this@LoginActivity, MainMenuActivity::class.java)
                    startActivity(avancarTelaInicial)
                }

            //se nao fro um sucesso, logar a falha no catlog
            }else{
                Log.w(TAG, "singInUserWithEmail:failure", task.exception)
                Toast.makeText(baseContext, "Autenticão Falhou", Toast.LENGTH_SHORT).show()
            }
        }
    }
//Função que envia o email para resetar a senha
    private fun resetPassword(email: String){
        //chamando a função
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            //se o envio for um sucesso
            if(task.isSuccessful){
                Log.d(TAG, "sendPasswordResetEmail:success")
                Toast.makeText(baseContext, "Email de recuperação enviado, cheque seu email", Toast.LENGTH_SHORT).show()
            }else{
                Log.w(TAG, "sendPasswordResetEmail:failure", task.exception)
                Toast.makeText(baseContext, "Falha ao enviar email de recuperação", Toast.LENGTH_SHORT).show()
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