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
                val data = payload["data"] as Map<String, Any>
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
        var locRecebidaTESTE: MainViewActivity.Locacao = atualLocacao
        lateinit var latlocRecebida: MainViewActivity.Locacao
    }
}