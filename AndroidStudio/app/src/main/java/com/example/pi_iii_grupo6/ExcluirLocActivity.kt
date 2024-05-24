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

        val size = locRecebida.pulseiras.size
        Log.d("Tamanho","quantidade pulseiras: $size")

        encerrarLocacao().addOnCompleteListener { task->
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

        calcularTempo()

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

        //Pegando as horas e minutos escolhidos pelo usuário
        val minutosEscolhidosTotais = converterStringParaMinutos(locRecebida.preco?.tempo)
        Log.d("ESCOLHIDOS","$minutosEscolhidosTotais")
        if (minutosEscolhidosTotais == null){
            val diaria = true

        }
        val horasEscolhidas = minutosEscolhidosTotais?.div(60)
        val minutosEscolhidos = minutosEscolhidosTotais?.rem(60)

        mostrarDialogTempo(hourDiference,minuteDiference,horasEscolhidas,minutosEscolhidos)

    }

    private fun converterStringParaMinutos(string: Any?): Int? {
        //Identificando partes da string
        val regex = Regex("""(\d+)\s*[hH][rR]?\s*(\d+)?\s*[mM][iI]?[nN]?|(\d+)\s*[mM][iI]?[nN]?""")
        var totalMinutes = 0
        var stringRecebida = string.toString()

        val matchResults = regex.findAll(stringRecebida)
        for (matchResult in matchResults) {
            val (hours, minutes, onlyMinutes) = matchResult.destructured

            if (hours.isNotEmpty()) {
                totalMinutes += hours.toInt() * 60
            }
            if (minutes.isNotEmpty()) {
                totalMinutes += minutes.toInt()
            }
            if (onlyMinutes.isNotEmpty()) {
                totalMinutes += onlyMinutes.toInt()
            }
        }

        return totalMinutes
    }

    //Função que retorna o datetime atual
    private fun getCurrentTime(): Date {
        return Date()
    }

    //Função que mostra a dialog com o tempo de locação
    private fun mostrarDialogTempo(hora: Int, minutos: Int,horasEscolhidas: Int?,minutosEscolhidos: Int?) {
        var dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_tempo_usado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTempo: TextView = dialog.findViewById(R.id.tvTextoTempoLoc)
        val tvTempoEscolhido:TextView = dialog.findViewById(R.id.tvTextoTempoLocEscolhido)
        val btnClose: Button = dialog.findViewById(R.id.btnClosePopupAberto)

        if (hora == 1){
            tvTempo.text = "O cliente utilizou o armário por $hora hora e $minutos minutos."
        }else if (hora == 0){
            tvTempo.text = "O cliente utilizou o armário por $minutos minutos."
        }else{
            tvTempo.text = "O cliente utilizou o armário por $hora horas e $minutos minutos."
        }

        if (horasEscolhidas == 1){
            tvTempoEscolhido.text = "O cliente alugou $horasEscolhidas hora e $minutosEscolhidos minutos"
        }else if (horasEscolhidas == 0){
            tvTempoEscolhido.text = "O cliente alugou $minutosEscolhidos minutos"
        }else{
            tvTempoEscolhido.text = "O cliente alugou $horasEscolhidas horas e $minutosEscolhidos minutos"
        }



        btnClose.setOnClickListener {
            dialog.dismiss()
            encerrarLocacao().addOnCompleteListener { task->
                if (task.isSuccessful){
                    Log.i("ENCERRRARLOC",task.result)
                    Toast.makeText(baseContext,"Locação encerrada com sucesso",Toast.LENGTH_SHORT).show()
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
}