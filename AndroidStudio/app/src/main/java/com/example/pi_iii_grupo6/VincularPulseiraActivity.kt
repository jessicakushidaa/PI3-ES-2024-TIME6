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
import android.nfc.NdefRecord
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat // classe do android framework
import com.example.pi_iii_grupo6.LiberarLocacaoActivity.Companion.atualLocacao


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

        // Receber o extra do intent
        val btnClicado = intent.getStringExtra("Activity")

        // Chamar função para mudar text view do título da tela
        definirTitulo(btnClicado)

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
                val activityExtra = intent.getStringExtra("Activity")
                //Checar se precisa entrar no modo leitura, modo escrever ou modo limpar TAG
                if (activityExtra == "buscar"){
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
                            if (intent.extras?.getString("Activity") == "vincular") atualLocacao.pulseiras.add(tagLida)
                            //Para atualizar a tela do usuário, precisa voltar para a thread principal (runOnUiThread)
                            runOnUiThread {
                                toastNaTela("Pulseira Lida")
                                avancarIntent(tagLida)
                            }
                        }
                    }
                }else if (activityExtra == "vincular"){
                    //LOGICA PARA GRAVAR NA TAG NFC
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Log.d("ESCREVER","Modo de escrever")
                        escreverTag(tag, atualLocacao.locId)
                        atualLocacao.pulseiras.add(atualLocacao.locId)
                        avancarIntent(atualLocacao.locId)
                    }else{
                        Log.e("ESCREVER","ERRO DE VERSAO")
                    }
                }else if (activityExtra == "limpar"){
                    //Lógica para limpar a tag
                    Log.d("LIMPAR","MODO LIMPAR")
                    limparTag(tag)
                    val duplaextra = intent.getStringExtra("dupla")
                    if (duplaextra != "true"){
                        runOnUiThread {
                            Toast.makeText(baseContext,"Locação encerrada com sucesso",Toast.LENGTH_SHORT).show()
                        }
                        val intent = Intent(this@VincularPulseiraActivity,MainViewGerenteActivity::class.java)
                        startActivity(intent)
                    }else if(duplaextra == "true"){
                        val intent = Intent(this@VincularPulseiraActivity,VincularPulseiraActivity::class.java)
                        intent.putExtra("Activity","limpar")
                        intent.putExtra("dupla","false")
                        startActivity(intent)
                    }
                }
            },
            flags,
            null
        )
    }

    // Função para definir o titulo e o drawable da text view
    private fun definirTitulo(btnClicado: String?){
        if (btnClicado == "buscar") {
            binding?.tvAcessarArmario!!.text = "Acessar Armário"
            binding?.tvAcessarArmario?.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.settings_1390_svgrepo_com,
                0,
                0,
                0
            )
        } else {
            binding?.tvAcessarArmario!!.text = "Liberar Locação"
            binding?.tvAcessarArmario?.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.open_padlock,
                0,
                0,
                0
            )
        }
    }
    //Função que pega a Activity que veio pela intent e avança, passando o id da pulseira também via intent;
    private fun avancarIntent(id: String) {
        //Verificando se são duas pessoas ou uma.
        val extra = intent.getStringExtra("dupla")
        val activity = intent.extras?.getString("Activity")

        if(extra == "true"){
            val intent = Intent(this@VincularPulseiraActivity, VincularPulseiraActivity::class.java)
            intent.putExtra("Activity", activity)
            intent.putExtra("dupla", "false")
            startActivity(intent)
        }else if(extra == "false"){
            if (activity == "vincular") {
                //Avançar para vincular pulseira (passando o id)
                val intent =
                    Intent(this@VincularPulseiraActivity, VincularPulseiraIdActivity::class.java)
                intent.putExtra("id", id)
                startActivity(intent)
            } else if (activity == "buscar") {
                //Avançar para buscar locação com esse id de pulseira (passando o id)
                val intent = Intent(this@VincularPulseiraActivity, BuscarLocIdActivity::class.java)
                intent.putExtra("id", id)
                startActivity(intent)
            }
        }
    }
    //Função que limpa a TAG NFC
    private fun limparTag(tag: Tag) {
        val ndef = Ndef.get(tag)
        //Verificando se a tag suporta NDEF
        if (ndef != null) {
            //Limpando a tag
            try {
                ndef.connect()
                // Criar um NdefRecord vazio
                val emptyRecord = NdefRecord(NdefRecord.TNF_EMPTY, byteArrayOf(), byteArrayOf(), byteArrayOf())
                // Criar um NdefMessage com o NdefRecord vazio
                val ndefMessage = NdefMessage(arrayOf(emptyRecord))
                ndef.writeNdefMessage(ndefMessage)
                ndef.close()
                runOnUiThread {
                    toastNaTela("Tag limpa com sucesso!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("LIMPAR","erro: $e")
                runOnUiThread {
                    toastNaTela("Falha ao limpar a tag!")
                }
            }
        } else {
            runOnUiThread {
                toastNaTela("Tag não suporta NDEF!")
            }
        }
    }

    //Função responsável por escrever string na tag de NFC
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun escreverTag(tag: Tag, data: String) {
        Log.d("ESCREVER","Entrou na escreverTag")
        val ndef = Ndef.get(tag)
        //Checando se a tag suporta NDEF
        if (ndef != null) {
            //Escrevendo na tag
            try {
                ndef.connect()
                val ndefRecord = NdefRecord.createTextRecord("en", data)
                val ndefMessage = NdefMessage(arrayOf(ndefRecord))
                ndef.writeNdefMessage(ndefMessage)
                ndef.close()
                runOnUiThread {
                    toastNaTela("Tag escrita com sucesso!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    toastNaTela("Falha ao escrever na tag!")
                }
                Log.e("ESCREVER","erro: $e")
            }
        } else {
            runOnUiThread {
                toastNaTela("Tag não suporta NDEF!")
            }
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

    companion object{
        var tagLida = ""
    }

}