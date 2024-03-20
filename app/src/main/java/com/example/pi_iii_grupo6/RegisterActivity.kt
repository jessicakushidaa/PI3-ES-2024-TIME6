package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.pi_iii_grupo6.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var binding: ActivityRegisterBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth

        binding?.btnRegistrar?.setOnClickListener{
            var email = binding?.etEmail?.text.toString()
            var senha = binding?.etSenha?.text.toString()
            var senhaConfirmation = binding?.etSenhaConfirmation?.text.toString()

            if(email.isNotEmpty() && senha.isNotEmpty() && senhaConfirmation.isNotEmpty()){
                if(senha == senhaConfirmation){
                    auth.createUserWithEmailAndPassword(email, senha)
                }else{
                    Toast.makeText(this@RegisterActivity, "Senhas não coincidem", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this@RegisterActivity, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createUserWithEmailAndPassword(email: String, senha: String){
        auth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener{ task ->
            if(task.isSuccessful) {
                Log.d(TAG, "createUserWithEmailAndPassword:success")
                var voltarLogin = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(voltarLogin)
            }else{
                Log.w(TAG, "createUserWithEmailAndPAssword:failure", task.exception)
                Toast.makeText(baseContext, "Cadastro Falhou", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object{
        private var TAG = "EmailAndPassword"
    }
}