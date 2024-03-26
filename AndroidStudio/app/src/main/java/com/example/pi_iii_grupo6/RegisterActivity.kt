package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.pi_iii_grupo6.databinding.ActivityRegisterBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.functions

class RegisterActivity : AppCompatActivity() {
    //criando variável de autenticação do firebase
    private lateinit var auth: FirebaseAuth
    //criando variável de functions do firebase
    private lateinit var functions: FirebaseFunctions

    //criando variável do ViewBinding
    private var binding: ActivityRegisterBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Inflando Layout
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth
        //functions = Firebase.functions("southamerica-east1")
        functions = Firebase.functions("southamerica-east1")


        //Setando o que fazer ao clicar no botão registrar
        binding?.btnRegistrar?.setOnClickListener{
            var email = binding?.etEmail?.text.toString()
            var senha = binding?.etSenha?.text.toString()
            var senhaConfirmation = binding?.etSenhaConfirmation?.text.toString()

            var nome = binding?.etNome?.text.toString()
            var sobrenome = binding?.etSobrenome?.text.toString()
            var cpf = binding?.etCPF?.text.toString()
            var birth = binding?.etBirth?.text.toString()
            var phone = binding?.etPhone?.text.toString()

            //Checando se os campos foram preenchidos
            if(email.isNotEmpty() && senha.isNotEmpty() && senhaConfirmation.isNotEmpty() && nome.isNotEmpty() && sobrenome.isNotEmpty() && cpf.isNotEmpty() && birth.isNotEmpty() && phone.isNotEmpty()){
                //Checando se as senhas coincidem
                if(senha == senhaConfirmation){
                    //Se está tudo certo, chamar função de criação do usuário
                    createUserWithEmailAndPassword(email, senha)
                    //Chamando função para guardar infos no database
                    createUserDataBase(nome, sobrenome, cpf, birth, phone)
                }else{
                    Toast.makeText(this@RegisterActivity, "Senhas não coincidem", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this@RegisterActivity, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Função que cria um usuário no Firebase
    private fun createUserWithEmailAndPassword(email: String, senha: String){
        auth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener{ task ->
            if(task.isSuccessful){
                //Se a criação for um sucesso
                Log.d(TAG, "createUserWithEmail:success")
                Toast.makeText(baseContext, "Registro feito com sucesso", Toast.LENGTH_SHORT).show()
                //Volta para o usuário fazer o login
                var voltarLogin = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(voltarLogin)
            }else{
                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                Toast.makeText(baseContext, "Criação Falhou", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createUserDataBase(nome: String, sobrenome: String, cpf: String, birth: String, phone: String): Task<HttpsCallableResult> {
        val data = hashMapOf(
            "nome" to nome,
            "sobrenome" to sobrenome,
            "dataNascimento" to birth,
            "telefone" to phone,
            "cpfUsuario" to cpf,
        )
        return functions
            .getHttpsCallable("addPessoa")
            .call(data)

    }

    //Criação da TAG para logar no catlog
    companion object{
        private var TAG = "EmailAndPassword"
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}