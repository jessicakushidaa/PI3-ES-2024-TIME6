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
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityLoginBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth

        binding?.tvCadastrar?.setOnClickListener{
            var abrirCadastro = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(abrirCadastro)
        }

        binding?.btnLogin?.setOnClickListener{
            var email: String = binding?.etEmail?.text.toString()
            var senha: String = binding?.etSenha?.text.toString()

            if(email.isNotEmpty() && senha.isNotEmpty()){
                singInWithEmailAndPassword(email, senha)
            }else{
                Toast.makeText(this@LoginActivity, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun singInWithEmailAndPassword(email: String, senha: String){
        auth.signInWithEmailAndPassword(email,senha).addOnCompleteListener{ task ->
            if(task.isSuccessful){
                Log.d(TAG, "signInUserWithEmail:success")

                var avancarTelaInicial = Intent(this@LoginActivity, MainViewActivity::class.java)
                startActivity(avancarTelaInicial)
            }else{
                Log.w(TAG, "singInUserWithEmail:failure", task.exception)
                Toast.makeText(baseContext, "Autenticão Falhou", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object{
        private var TAG = "EmailAndPassword"
    }
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}