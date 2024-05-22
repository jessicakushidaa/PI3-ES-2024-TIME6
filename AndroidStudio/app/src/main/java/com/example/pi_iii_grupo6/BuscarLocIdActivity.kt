package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.pi_iii_grupo6.LiberarLocacaoActivity.Companion.atualLocacao
import com.example.pi_iii_grupo6.MainViewActivity.Companion.places
import com.example.pi_iii_grupo6.databinding.ActivityBuscarLocIdBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.google.gson.Gson

class BuscarLocIdActivity : AppCompatActivity() {
    private var binding: ActivityBuscarLocIdBinding? = null
    private lateinit var gson: Gson
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuscarLocIdBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        gson = Gson()
        functions = Firebase.functions("southamerica-east1")


        binding?.btnSimular?.setOnClickListener {
            avancarTela()
        }

        buscarLocacao().addOnCompleteListener { task->
            if (task.isSuccessful){
                Log.d("BUSCARLOC", "DATA: ${task.result}")
            }else{
                Log.e("BUSCARLOC","ERRO AO BUSCAR: ${task.exception}")
            }
        }
    }
    //Função que recebe o id da Pulseira via intent e busca no banco a locação
    private fun buscarLocacao(): Task<Map<String, Any>> {
        val idRecebido = intent.getStringExtra("id")

        val data = hashMapOf(
            "idTag" to idRecebido
        )

        return functions
            .getHttpsCallable("buscarLoc")
            .call(data)
            .continueWith{task ->
                val res = task.result.data as Map<String, Any>
                val payload = res["payload"] as Map<String, Any>
                val id = payload["idLocacao"] as Map<String, Any>
                val idLoc = id["id"] as String
                val data = payload["data"] as Map<String, Any>
                //Pegando o preco escolhido
                val precoEscolhido = data["precoTempoEscolhido"] as Map<String, Any>
                val preco = precoEscolhido["preco"] as Double
                val tempo = precoEscolhido["tempo"]
                //Pegando as fotos
                val vetFotos = data["foto"] as ArrayList<String>
                val size = vetFotos.size
                val vetorFotos: MutableList<String> = mutableListOf()
                vetorFotos.add(vetFotos[0])
                if(size == 2) vetorFotos.add(vetFotos[1])
                //Pegando as tags
                val tags = data["tags"] as ArrayList<String>
                val vetorTags: MutableList<String> = mutableListOf()
                val sizeTags = tags.size
                vetorTags.add(tags[0])
                if(sizeTags == 2) vetorTags.add(tags[1])

                //Adicionando na classe
                locRecebida = MainViewActivity.Locacao(null,null,MainViewActivity.Preco(tempo,preco),vetorFotos,vetorTags,idLoc)

                data
            }
    }

    //TEMPORARIA
    private fun avancarTela() {
        var intent = Intent(this@BuscarLocIdActivity,MostrarLocacaoActivity::class.java)
        startActivity(intent)
    }

    //IMPLEMENTAR FUNCTION QUE BUSCA A LOCAÇÃO NO BANCO, BASEADO NO ID DA PULSEIRA.

    companion object{
        //Variavel de TESTE
        lateinit var locRecebida: MainViewActivity.Locacao
    }
}