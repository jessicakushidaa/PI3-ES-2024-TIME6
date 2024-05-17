package com.example.pi_iii_grupo6

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.pi_iii_grupo6.databinding.ActivityVincularPulseiraBinding
import java.nio.charset.Charset
import kotlin.properties.Delegates
import android.graphics.drawable.AnimatedVectorDrawable
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat // classe do android framework


class VincularPulseiraActivity : AppCompatActivity() {
    //Declaração do NFCAdapter (Conexão código -> hardware)
    private var nfcAdapter: NfcAdapter? = null
    private var binding: ActivityVincularPulseiraBinding? = null
    private lateinit var pendingIntent: PendingIntent
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVincularPulseiraBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        /** Iniciando animações drawable na tela - icons de conexao da pulseira
         * instanciando as variaveis com o id do layout
         * atribuindo à uma lista de animações
         * iniciando cada um dos elementos da lista
         */

        val drawableConn1 = binding?.imageView3?.drawable as? AnimatedVectorDrawable
        val drawableConn2 = binding?.imageView4?.drawable as? AnimatedVectorDrawable

        val animationList = listOf(drawableConn1, drawableConn2)
        animationList.forEach { it?.start() }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }

        pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), flags)

        //instanciando o Adapter do NFC (Conexão código -> hardware)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onResume() {
        super.onResume()

        //Declarando as flags necessárias para serem parâmetro do enableReaderMode
        val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V

        // "Ligando" a leitura de NFC
        nfcAdapter?.enableReaderMode(
            this,
            { tag ->
                //Se entrou, tag foi detectada
                Log.d("NFC", "Tag detectada: $tag")
                //Guardando o que tiver de NDEF dentro da tag
                val ndef = Ndef.get(tag)
                ndef?.let {
                    it.connect()
                    val ndefMessage = it.ndefMessage
                    it.close()
                    //Tendo o NDEF, guardar em uma variável e ldiar com as informações
                    val informacoes = ndefMessage.records
                    if (informacoes.isNotEmpty()) {
                        val firstRecord = informacoes[0]
                        val payload = firstRecord.payload
                        val text = String(payload, Charset.forName("UTF-8"))
                        //Remover o prefixo que vem com a gravação da tag
                        val textoesperado = text.substring(3)
                        Log.d("NFC", "Tag detectada: $textoesperado")
                        tagLida = textoesperado
                        //Para atualizar a tela do usuário, precisa voltar para a thread principal (runOnUiThread)
                        runOnUiThread {
                            toastNaTela(tagLida)
                            atualizarTela(tagLida)
                            avancarIntent(tagLida)
                        }
                    }
                }
            },
            flags,
            null
        )
    }

    //Função que pega a Activity que veio pela intent e avança, passando o id da pulseira também via intent;
    private fun avancarIntent(id: String) {
        val activity = intent.extras?.getString("Activity")
        if(activity == "vincular"){
            //Avançar para vincular pulseira (passando o id)
        }else if(activity == "buscar"){
            //Avançar para buscar locação com esse id de pulseira (passando o id)
        }
    }

    //Ao pausar a Activity, pausa também a procura por tags NFC
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }
    //Apresentar um Toast
    fun toastNaTela(string: String){
        Toast.makeText(baseContext,string,Toast.LENGTH_SHORT).show()
    }
    fun atualizarTela(string: String){
        var tvPulseira = binding?.tvPulseira

        tvPulseira?.text = "Id da pulseira: " + string
    }

    companion object{
        var tagLida = ""
    }

}