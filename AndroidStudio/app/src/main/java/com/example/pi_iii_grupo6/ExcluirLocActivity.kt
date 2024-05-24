package com.example.pi_iii_grupo6

import android.content.Intent
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.pi_iii_grupo6.BuscarLocIdActivity.Companion.locRecebida
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import java.util.Date

class ExcluirLocActivity : AppCompatActivity() {
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_excluir_loc)
        functions = Firebase.functions("southamerica-east1")


        // Chama a função buscarVetorHorarios() que retorna uma Task e adiciona um listener para a conclusão da tarefa
        buscarVetorHorarios().addOnCompleteListener { task->
            if (task.isSuccessful){
                Log.i("BUSCARHORARIOS","${task.result}")
                calcularTempo()
            }else{
                Log.e("BUSCARHORARIOS","ERRO: ${task.exception}")
            }
        }


    }
    //Função que pega a hora da locação e a atual, e calcula o tempo
    private fun calcularTempo() {
        Log.d("TEMPO","hora: ${locRecebida.horaLocacao.hours} minuto: ${locRecebida.horaLocacao.minutes}")
        //Pegando a hora da locação
        val horaLocacao = locRecebida.horaLocacao.hours
        val minutoLocacao = locRecebida.horaLocacao.minutes

        val horaEmMinuto = horaLocacao*60
        val minutosTotais = horaEmMinuto + minutoLocacao

        //Pegando os minutos atuais
        val currentTime = getCurrentTime()

        val currentHour = currentTime.hours
        val currentMinutes = currentTime.minutes
        val currentHourToMinute = currentHour*60
        val totalCurrentMinutes = currentMinutes+currentHourToMinute

        val diference = totalCurrentMinutes - minutosTotais
        val minuteDiference = diference % 60
        val hourDiference = diference / 60
        Log.d("DIFERENCA","$diference")
        var indexPreco = 0
        if(diference in 31..90) indexPreco = 1
        else if(diference in 91..180) indexPreco = 2
        else if(diference > 180) indexPreco = 3

        val precoPagar = vetPrices[indexPreco]

        mostrarDialogTempo(hourDiference,minuteDiference, precoPagar)

    }

    private fun buscarVetorHorarios():Task<Any?> {
        val data = hashMapOf(
            "documentId" to locRecebida.unidadeId,
            "collectionName" to "unidadeLocacao"
        )

        return functions
            .getHttpsCallable("getDocumentFields")
            .call(data)
            .continueWith{task->
                val res = task.result.data as Map<String, Any>
                val payload = res["payload"] as Map<String, Any>
                val mainData = payload["mainData"] as Map<String, Any>
                val tabelaPrecos = mainData["tabelaPrecos"] as ArrayList<Map<String, Any>>

                for (price in tabelaPrecos){
                    vetPrices.add(price["preco"])
                }

                vetPrices
            }
    }

    //Função que retorna o datetime atual
    private fun getCurrentTime(): Date {
        return Date()
    }

    //Função que mostra a dialog com o tempo de locação
    private fun mostrarDialogTempo(hora: Int, minutos: Int, preco: Any?) {
        var dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_tempo_usado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTempo: TextView = dialog.findViewById(R.id.tvTextoTempoLoc)
        val textView: TextView = dialog.findViewById(R.id.tvTextoTempoLocEscolhido)
        val btnClose: Button = dialog.findViewById(R.id.btnClosePopupAberto)

        if (hora == 1){
            tvTempo.text = "O cliente utilizou o armário por $hora hora e $minutos minutos."
        }else if (hora == 0){
            tvTempo.text = "O cliente utilizou o armário por $minutos minutos."
        }else{
            tvTempo.text = "O cliente utilizou o armário por $hora horas e $minutos minutos."
        }

        textView.text = "O restante do valor será estornado, sendo cobrado apenas o valor de R$ $preco"


        btnClose.setOnClickListener {
            dialog.dismiss()
            encerrarLocacao().addOnCompleteListener { task->
                val size = locRecebida.pulseiras.size

                if (task.isSuccessful){
                    Log.i("ENCERRRARLOC",task.result)
                    val intent = Intent(this@ExcluirLocActivity, VincularPulseiraActivity::class.java)
                    intent.putExtra("Activity", "limpar")
                    if (size == 2) intent.putExtra("dupla","true") else intent.putExtra("dupla","false")
                    startActivity(intent)
                }else{
                    Log.e("ENCERRRARLOC","Erro ao encerrar loc: ${task.exception}")
                }
            }
        }

        dialog.show()
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

    companion object{
        val vetPrices: MutableList<Any?> = mutableListOf()
    }
}