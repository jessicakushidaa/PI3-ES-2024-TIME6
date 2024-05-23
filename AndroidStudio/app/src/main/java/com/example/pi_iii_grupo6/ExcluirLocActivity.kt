package com.example.pi_iii_grupo6

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.pi_iii_grupo6.BuscarLocIdActivity.Companion.locRecebida
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions

class ExcluirLocActivity : AppCompatActivity() {
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_excluir_loc)
        functions = Firebase.functions("southamerica-east1")

        encerrarLocacao().addOnCompleteListener { task->
            if (task.isSuccessful){
                Log.i("ENCERRRARLOC",task.result)
                Toast.makeText(baseContext,"Locação encerrada com sucesso",Toast.LENGTH_SHORT).show()
            }else{
                Log.e("ENCERRRARLOC","Erro ao encerrar loc: ${task.exception}")
            }
        }

    }
    //Função que chama a function para encerrar a locação no banco de dados
    private fun encerrarLocacao(): Task<String> {
        val data = hashMapOf(
            "idLocacao" to locRecebida.locId,
            "idUnidade" to locRecebida.unidadeId
        )

        return functions
            .getHttpsCallable("encerrarLoc")
            .call(data)
            .continueWith{task->
                val res = task.result.data as Map<String, Any>
                val message = res["message"] as String
                message
            }
    }
}