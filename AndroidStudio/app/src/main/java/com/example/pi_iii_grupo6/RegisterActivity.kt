package com.example.pi_iii_grupo6

import BasicaActivity
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
import java.text.SimpleDateFormat
import java.util.Date

class RegisterActivity : BasicaActivity() {
    //criando variável de autenticação do firebase
    private lateinit var auth: FirebaseAuth
    //criando variável de functions do firebase
    private lateinit var functions: FirebaseFunctions

    //criando variável do ViewBinding
    private var binding: ActivityRegisterBinding? = null

    //Criar classe pessoa, que envia os dados para a function
    data class Pessoa(
        val userId: String,
        val nome: String,
        val sobrenome: String,
        val dataNascimento: String,
        val cpf: String,
        val telefone: String,
        var gerente: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Inflando Layout
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //Inicializando varáveis do firebase
        auth = Firebase.auth
        functions = Firebase.functions("southamerica-east1")

        /**
         * Formatando padrão de digitação de data de nascimento
         */
        val dataNascimentoET = binding?.etBirth
        // Definindo texto guia para o usuário
        dataNascimentoET!!.setText("DD/MM/AAAA")
        // Adicionando o TextWatcher na editText
        // Define um texto inicial com a barra
        // Adicionando TextWatcher na edit text
        dataNascimentoET.addTextChangedListener(DateTextFormat(dataNascimentoET))
        dataNascimentoET.setOnClickListener{
            // remove temporariamente o text Watcher
            dataNascimentoET.removeTextChangedListener(DateTextFormat(dataNascimentoET))
            if (dataNascimentoET.text.toString() == "MM/AA") {
                dataNascimentoET.setText("") // limpa o edit text
                dataNascimentoET.setSelection(0) // posiciona cursor no início
            }
            // adiciona novamente o método
            dataNascimentoET.addTextChangedListener(DateTextFormat(dataNascimentoET))
        }

        //Setando o que fazer ao clicar no botão registrar
        binding?.btnRegistrar?.setOnClickListener{
            //Referenciar variáveis às EditText
            var email = binding?.etEmail?.text.toString()
            var senha = binding?.etSenha?.text.toString()
            var senhaConfirmation = binding?.etSenhaConfirmation?.text.toString()

            var nome = binding?.etNome?.text.toString()
            var sobrenome = binding?.etSobrenome?.text.toString()
            var cpf = binding?.etCPF?.text.toString()
            var dataNascimento = dataNascimentoET.text.toString()
            var phone = binding?.etPhone?.text.toString()


            if(dataNascimento.isEmpty()){
                Toast.makeText(this@RegisterActivity, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }else{
                var formatoRecebido = SimpleDateFormat("dd/MM/yyyy")
                var dataRecebida = formatoRecebido.parse(dataNascimento)
                var formatoAmericano = SimpleDateFormat("yyyy-MM-dd")

                var dataNascimentoFinal = formatoAmericano.format(dataRecebida);
                if (possuiLetras(dataNascimento)){
                    Toast.makeText(this@RegisterActivity, "Data de Nascimento não aceita", Toast.LENGTH_SHORT).show()
                }else{
                    Log.d("DATAFINAL","$dataNascimentoFinal")
                    val dataNascimentoSplit = dataNascimento.split("/")
                    if(dataNascimentoSplit[0].toInt() > 31 || dataNascimentoSplit[1].toInt() > 12 || dataNascimentoSplit[2].toInt() > 2024){
                        Toast.makeText(this@RegisterActivity, "Data de Nascimento não aceita", Toast.LENGTH_SHORT).show()
                    }else{
                        //Checando se os campos foram preenchidos
                        if(checkValues(email,senha,senhaConfirmation,nome,sobrenome,cpf,dataNascimentoFinal,phone)){
                            //Checando se as senhas coincidem
                            if(senha == senhaConfirmation){
                                //Se está tudo certo, chamar função de criação do usuário, retorna o userId
                                createUserWithEmailAndPassword(email, senha) { userId ->
                                    if (userId != null) {
                                        // Criando instância da classe Pessoa com os dados do usuário
                                        var p = Pessoa(userId,nome,sobrenome,dataNascimentoFinal,cpf,phone,false)

                                        // Lidar com a criação do usuário bem-sucedida
                                        Log.d(TAG, "Usuário criado com sucesso. UID: $userId")

                                        //Chamando a função que verifica se o usuário é um gerente, se for, muda
                                        // isGerente para true
                                        checarGerente(email, p)

                                        //Chamando função para guardar infos no database
                                        createUserDataBase(p, userId).addOnCompleteListener { task ->
                                            if(!task.isSuccessful){
                                                Log.e("Register","Error on Function: ${task.exception}")
                                            }
                                        }
                                    } else {
                                        // Lidar com a falha na criação do usuário
                                        Log.e(TAG, "Falha ao criar o usuário.")
                                    }
                                }
                            }else{
                                Toast.makeText(this@RegisterActivity, "Senhas não coincidem", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            Toast.makeText(this@RegisterActivity, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }



        }
    }

    private fun possuiLetras(dataNascimento: String): Boolean {
        return dataNascimento.any { it.isLetter() }
    }

    //Função para validar se campos foram preenchidos
    private fun checkValues(email: String, senha:String, senhaConfirmation:String, nome:String, sobrenome:String,cpf:String,birth:String,phone:String): Boolean {
        return email.isNotEmpty() && senha.isNotEmpty() && senhaConfirmation.isNotEmpty() && nome.isNotEmpty() && sobrenome.isNotEmpty() && cpf.isNotEmpty() && birth.isNotEmpty() && phone.isNotEmpty()
    }

    //Função para checar se, de acordo com o email cadastrado, a pessoa será gerente ou não
    private fun checarGerente(email: String, p: Pessoa) {
        if("gerente@gmail.com" == email){
            //Se tiver email de gerente, mudar gerente para true
            p.gerente = true
        }
    }

    //Função que cria um usuário no Firebase
    private fun createUserWithEmailAndPassword(email: String, senha: String, callback: (String?) -> Unit){
        //Chamando função do authentication para criar usuário
        auth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener{ task ->
            if(task.isSuccessful){
                // Mandando email de verificação para o usuário
                sendEmailVerification()

                // Obtendo o usuário autenticado
                val user = auth.currentUser
                // Obtendo o ID do usuário
                val userId = user?.uid

                if (userId != null) {
                    //Se a criação for um sucesso
                    Log.d(TAG, "createUserWithEmail:success.UID: $ UID: $userId")
                    callback(userId)
                    Toast.makeText(baseContext, "Registro feito com sucesso.", Toast.LENGTH_SHORT)
                        .show()
                    //Volta para o usuário fazer o login
                    var voltarLogin = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(voltarLogin)
                } else {
                    Log.e(TAG,"Erro ao obter Id do usuário.",task.exception)
                    callback(null)
                }

            }else{

                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                Toast.makeText(baseContext, "Criação Falhou", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Função que envia um email de verificação para o novo usuário
    private fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                Toast.makeText(baseContext,"Foi enviado um link de verificação para seu email",Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Função que chama a function responsável por adicionar o usuário no banco de dados (dados de pessoa)
    private fun createUserDataBase(p: Pessoa, userId:String): Task<String> {
        //Definindo os dados que serão passados aos parâmetros da function
        val data = hashMapOf(
            "userId" to p.userId,
            "nome" to p.nome,
            "sobrenome" to p.sobrenome,
            "dataNascimento" to p.dataNascimento,
            "telefone" to p.telefone,
            "cpf" to p.cpf,
            "isGerente" to p.gerente,
        )
        // Chamando a cloud function em si
        return functions
            //Nome da funcao
            .getHttpsCallable("addPessoa")
            //Dados para os parâmetros
            .call(data)
            .continueWith { task ->
                val result = task.result?.data as String
                result
            }
    }

    //Criação da TAG para logar no catlog
    companion object{
        private var TAG = "EmailAndPassword"
    }

    //Função que quando encerrar activity, volta o binding para null
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}