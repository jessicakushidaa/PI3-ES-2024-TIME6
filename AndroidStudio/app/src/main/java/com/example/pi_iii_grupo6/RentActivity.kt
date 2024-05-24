package com.example.pi_iii_grupo6

import BasicaActivity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.pi_iii_grupo6.databinding.ActivityRentBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import android.location.Location
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.pi_iii_grupo6.MainMenuActivity.Companion.cartaoUsuario
import com.example.pi_iii_grupo6.MainMenuActivity.Companion.idDocumentPessoa
import com.example.pi_iii_grupo6.MainViewActivity.Companion.locacoesPendentes
import com.example.pi_iii_grupo6.MainViewActivity.Companion.places
import com.example.pi_iii_grupo6.databinding.AlugarArmarioDialogBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.google.gson.Gson
import java.util.Calendar

class RentActivity : BasicaActivity() {
    private var binding: ActivityRentBinding? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var userLocation: LatLng
    private lateinit var actualLocker: MainViewActivity.Place
    private var precoSelecionado: MainViewActivity.Preco? = null
    private var gson = Gson()
    private var user: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var locAtual: MainViewActivity.Locacao
    private lateinit var functions: FirebaseFunctions

    class Info(
        var userId: String?,
        var preco: MainViewActivity.Preco
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRentBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = Firebase.auth
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        user = auth.currentUser
        functions = Firebase.functions("southamerica-east1")


        // Definindo a cor do text view "Locações" para ficar imutável no nightMode
        val tvLocTitle =binding?.tvLocTitle
        tvLocTitle!!.setTextColor(Color.parseColor("#FF000000")) // preto

        var nomeRecebido = getLockerName()
        getCurrentLocation(nomeRecebido)

        binding?.btnAlugarArmario?.setOnClickListener{
            if(cartaoUsuario!=null){
                dialogAlugarArmario()
            }else{
                dialogFaltaCartao()
            }
        }
    }
    private fun dialogNaoChegou(){
        var dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_nao_chegou)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvAbrirMapa: TextView = dialog.findViewById(R.id.tvAbrirMapa)

        tvAbrirMapa.setOnClickListener {
            val intentMapa = Intent(this@RentActivity, MainViewActivity::class.java)
            startActivity(intentMapa)
        }

        //Seta Voltar
        val toolbar : Toolbar = findViewById(R.id.toolbar) //achando id da toolbar

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Remove o texto do nome do aplicativo

        dialog.show()
    }

    //Se o usuário nao tiver um cartão, abrir uma dialog que diz que para alugar, precisa ter um cartão.
    private fun dialogFaltaCartao() {
        var dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_falta_cartao)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTextTwo: TextView = dialog.findViewById(R.id.tvTextTwo)
        val btnClose: Button = dialog.findViewById(R.id.btnClosePopup2)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        tvTextTwo.setOnClickListener {
            var intentCartao = Intent(this@RentActivity, ShowCardActivity::class.java)
            startActivity(intentCartao)
        }

        dialog.show()
    }

    fun getLockerName(): String?{
        val stringRecebida = intent.getStringExtra("nomeLocker")
        return stringRecebida
    }

    //Lógica que compara a localização do usuário com cada localização de locker
    private fun checkLocation(nomeRecebido: String?) {
        if (nomeRecebido == null){
            var flag = 0
            for (place in places) {
                var locPlace = LatLng(place.latitude, place.longitude)
                var distancia = calcularDistancia(locPlace)
                if (distancia <= 1.0) {
                    //Está em alguma unidade!
                    actualLocker = place
                    flag = 1
                    locationHandler(flag)
                    return
                }
            }
            locationHandler(flag)
        }
        else{
            var flag = 0
            for (place in places){
                if(place.nomeLocal == nomeRecebido){
                    var locPlace = LatLng(place.latitude, place.longitude)
                    var distancia = calcularDistancia(locPlace)
                    if (distancia <= 1.0){
                        actualLocker = place
                        flag = 1
                        locationHandler(flag)
                        return
                    }
                }else{
                    Log.d("stringrecebida",nomeRecebido)
                }
            }
            locationHandler(flag)
        }
    }

    private fun locationHandler(achou: Int){
        Log.d("HANDLER","ENTROU LOCATION HANDLER")
        if(achou == 0){
            Log.d("HANDLER","NAO ACHOU ARMARIO")
            dialogNaoChegou()
            var textoTitulo = binding?.etTitleLocation
            textoTitulo?.text = "Você ainda não está em nenhum local"
        }else{
            //Usuário está em uma unidade de locação
            val textoTitulo = binding?.etTitleLocation
            val textoEndereco = binding?.etEndereco
            val textoReferencia = binding?.etReferencia
            val textoTelefone = binding?.etTelefone

            textoTitulo?.text = "${actualLocker.nomeLocal}"
            textoEndereco?.text = "Endereço: ${actualLocker.enderecoLocal}"
            textoReferencia?.text = "Referência: ${actualLocker.referenciaLocal}"
            textoTelefone?.text = "Telefone: ${actualLocker.telefone}"

            val textViews = listOf(textoEndereco, textoReferencia)

            textoTitulo!!.setTextColor(Color.parseColor("#FF000000"))
            textViews.forEach {
                it!!.setTextColor(Color.parseColor("#757575")) // Define a cor de todos cinza
            }

        }
    }
    private fun getCurrentLocation(nomeRecebido: String?){
        //Checando permissões
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ){
            Log.e("debug", "Permissions")
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
        }

        //Após checar permissões, chamar a função que pega a localização do aparelho
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {task ->
            //Atribuindo a localização a uma variável
            var location = task.result
            //Checando se a localização é nula
            if(location != null){
                //Se nao for nula, atualiza a variável de localização do usuário
                userLocation = LatLng(location.latitude,location.longitude)
                checkLocation(nomeRecebido)
            }
        }
        //Voltando para um valor aleatorio apenas para inicializar
        userLocation = LatLng(0.0,0.0)
    }

    private fun calcularDistancia(segunda: LatLng):Double {
        var localizacaoAtual = Location("")
        localizacaoAtual.latitude = userLocation.latitude
        localizacaoAtual.longitude = userLocation.longitude

        var localizacaoSegunda = Location("")
        localizacaoSegunda.latitude = segunda.latitude
        localizacaoSegunda.longitude = segunda.longitude

        var distancia = localizacaoAtual.distanceTo(localizacaoSegunda) / 100.0

        return distancia
    }

    //APAGAR FUNCAO
    private fun dialogAlugarArmario() {
        //Criando a dialog box
        val dialog = BottomSheetDialog(this)
        //Criando uma variável que tera o binding não da activity, mas sim do layout da dialog
        val sheetBinding: AlugarArmarioDialogBinding = AlugarArmarioDialogBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(sheetBinding.root)

        //Referenciando as Views vindas do layout
        var opcao1 = sheetBinding.llPreco1
        var opcao2 = sheetBinding.llPreco2
        var opcao3 = sheetBinding.llPreco3
        var opcao4 = sheetBinding.llPreco4

        var tempo1 = sheetBinding.tvTempo1
        var tempo2 = sheetBinding.tvTempo2
        var tempo3 = sheetBinding.tvTempo3
        var tempo4 = sheetBinding.tvTempo4

        var preco1 = sheetBinding.tvPreco1
        var preco2 = sheetBinding.tvPreco2
        var preco3 = sheetBinding.tvPreco3
        var preco4 = sheetBinding.tvPreco4

        //Setando o texto dessas views, o valor to tempo e preço vindos do banco
        tempo1.text = "${actualLocker.precos[0].tempo} min"
        tempo2.text = "${actualLocker.precos[1].tempo} min"


        preco1.text = "R$ ${actualLocker.precos[0].preco}"
        preco2.text = "R$ ${actualLocker.precos[1].preco}"

        //Se a unidade tiver mais de 2 opções de preço
        if(actualLocker.precos.size >= 3){
            preco3.text = "R$ ${actualLocker.precos[2].preco}"
            tempo3.text = "${actualLocker.precos[2].tempo} min"
        }else{
            preco3.text = ""
            tempo3.text = ""
        }

        //Se houver mais de 3 opções de preço, sendo essa opção a da diária.
        if(actualLocker.precos.size >= 4){
            tempo4.text = "${actualLocker.precos[3].tempo}"
            preco4.text = "R$ ${actualLocker.precos[3].preco}"
        }

        //Inicializando a dialog
        dialog.show()

        //Setando os cliques nos elementos dentro da dialog.
        opcao1.setOnClickListener{
            //Se já estver selecionado, remover a seleção, tanto na lógica quanto visualmente.
            if (precoSelecionado?.tempo == actualLocker.precos[0].tempo){
                removerSelecionado()
                desmarcarOpcao(opcao1)
            }else if (precoSelecionado == null){
                //Selecionar
                addSelecionado(actualLocker.precos[0])
                marcarOpçcao(opcao1)
            }else{
                //Nessa opção, ja tem uma opção selecionada, portanto não é possível selecionar
                Toast.makeText(baseContext,"Selecione apenas uma opção",Toast.LENGTH_SHORT).show()
            }
        }
        //Mesmos comentários da opcao1
        opcao2.setOnClickListener{
            if (precoSelecionado?.tempo == actualLocker.precos[1].tempo){
                removerSelecionado()
                desmarcarOpcao(opcao2)
            }else if (precoSelecionado == null){
                addSelecionado(actualLocker.precos[1])
                marcarOpçcao(opcao2)
            }else{
                Toast.makeText(baseContext,"Selecione apenas uma opção",Toast.LENGTH_SHORT).show()
            }
        }
        //Mesmos comentários da opcao1
        opcao3.setOnClickListener{
            if (precoSelecionado?.tempo == actualLocker.precos[2].tempo){
                removerSelecionado()
                desmarcarOpcao(opcao3)
            }else if (precoSelecionado == null){
                addSelecionado(actualLocker.precos[2])
                marcarOpçcao(opcao3)
            }else{
                Toast.makeText(baseContext,"Selecione apenas uma opção",Toast.LENGTH_SHORT).show()
            }
        }

        //consultando a hora e setando a cor da seleção da diária caso não se encaixe no horário previsto.
        if (!(somaHoraAtual >= 420 && somaHoraAtual <= 480)){
            opcao4.setBackgroundColor(Color.parseColor("#E7E7E7"))
        }
        //Essa é a opção da diária, portanto não estará disponível em qualquer horário que não seja entre as 7:00 e 8:00
        opcao4.setOnClickListener{

            if(somaHoraAtual >= 420 && somaHoraAtual <= 480){
                if (precoSelecionado?.tempo == actualLocker.precos[3].tempo){
                    removerSelecionado()
                    desmarcarOpcao(opcao4)
                }else if (precoSelecionado == null){
                    addSelecionado(actualLocker.precos[3])
                    marcarOpçcao(opcao4)
                }else{
                    Toast.makeText(baseContext,"Selecione apenas uma opção",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(baseContext,"Disponível entre 7:00 e 8:00",Toast.LENGTH_SHORT).show()
            }
        }

        //Botão que confirma o envio da opção selecionada.
        sheetBinding.btnConfirmarLoc.setOnClickListener {
            //Setar a variável locAtual com uma classe Locacao para posteriormente enviar via QrCode.
            var userId = user?.uid
            locAtual = MainViewActivity.Locacao(userId,actualLocker,precoSelecionado)

            //Chamando funcao que retorna o id do armário.
            pegarIdArmario().addOnCompleteListener { task->
                //Ao pegar o id do armário, chama-se a função que adiciona a locação pendente no banco de dados.
                if(task.isSuccessful){
                    val idArmario = task.result
                    Log.d("PEGOU ARMARIO", idArmario)
                    //Chamando a function.
                    addLocacaoPendente(idArmario)
                        .addOnCompleteListener { task->
                            if (task.isSuccessful){
                                Log.d("ADDLOC","Locacao adicionada com sucesso! ${task.result}")
                                locAtual.locId = task.result
                                confirmacao(locAtual)
                            }else{
                                Log.e("ERROR","erro ao adicionar loc: ${task.exception}")
                            }
                        }
                }else{
                    Log.e("ERROR","${task.exception}")
                }
            }
            locacoesPendentes.add(locAtual)
        }
    }
    //Essa função passa o Id do documento e o nome da colecction, e retorna o ID do armário dessa unidade de locação.
    private fun pegarIdArmario(): Task<String> {
        Log.d("PEGAR ARMARIO", "id: ${actualLocker.id}")
        val data = hashMapOf(
            "documentId" to actualLocker.id,
            "collectionName" to "unidadeLocacao"
        )

        //Chamando a function
        return functions
            .getHttpsCallable("getDocumentFields")
            .call(data)
            .continueWith{ task->
                //Lidar com o resultado retornado/
                val res = task.result.data as Map<String, Any>
                val payload = res["payload"] as Map<String, Any>
                val subcoletcions = payload["subCollectionsData"] as Map<String, Any>
                val armarios = subcoletcions["armarios"] as ArrayList<*>
                val armarioItem = armarios[0] as Map<String, Any>
                val idArmario = armarioItem["id"] as String
                idArmario
            }
    }
    //FunçÃo que adiciona a locação pendente no banco de dados.
    private fun addLocacaoPendente(idArmario: String):Task<String> {
        //Setando os valores a serem enviados.
        val data = hashMapOf(
            "idUnidade" to actualLocker.id,
            "idArmario" to idArmario,
            "idPessoa" to mutableListOf(idDocumentPessoa),
            "tempoEscolhido" to precoSelecionado?.tempo,
            "precoEscolhido" to precoSelecionado?.preco,
        )
        //Chamando a function.
        return functions
            .getHttpsCallable("addLocacao")
            .call(data)
            .continueWith{task->
                //Lidar com o retorno, se houver.
                val res = task.result.data as Map<String, Any>
                val payload = res["payload"] as Map<String, Any>
                val docId = payload["locId"] as String

                docId
            }


    }
    //Função chamada ao clicar no botão de confirmar a solicitação de locação.
    fun confirmacao(locacao: MainViewActivity.Locacao){
        Log.i("LOCACAO","ADICIONAR LOC: ${locacao.locId}")
        var intentQrCode = Intent(this@RentActivity, CodeActivity::class.java)

        //Se a pessoa não tiver selecionado nenhuma das opções, avisar que é preciso selecionar ao menos uma.
        if (precoSelecionado == null){
            Toast.makeText(baseContext,"Selecione uma opção",Toast.LENGTH_SHORT).show()
        }else{
            //Se está tudo ok com a seleção, (ja validada anteriormente), passar para a activity do Qr Code
            var infosJson = gson.toJson(locacao)
            Log.d("LOCACAO","JSON da Locação: $infosJson")

            intentQrCode.putExtra("infosJson",infosJson)
            startActivity(intentQrCode)
        }
    }
//Funções que lidam com o layout das opções.
    fun addSelecionado(selecionado: MainViewActivity.Preco){
        precoSelecionado = selecionado
        Log.d("SELECTION", "PRECO SELECIONADO: ${precoSelecionado?.preco}")
    }
    fun removerSelecionado(){
        precoSelecionado = null
        Log.d("SELECTION","Preco removido")
    }

    fun marcarOpçcao(viewSelecionada: View){
        viewSelecionada.setBackgroundResource(R.drawable.time_price_box_pressed)
    }
    fun desmarcarOpcao(viewSelecionada: View){
        viewSelecionada.setBackgroundResource(R.drawable.time_price_box)
    }

    companion object{
        val calendar = Calendar.getInstance()
        val horaAtual = calendar.get(Calendar.HOUR_OF_DAY)
        val minutoAtual = calendar.get(Calendar.MINUTE)

        val somaHoraAtual = (horaAtual*60) + minutoAtual
    }

    //Função que volta o binding para null ao encerrar a activity
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}



