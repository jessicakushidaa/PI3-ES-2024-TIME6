package com.example.pi_iii_grupo6

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
import com.example.pi_iii_grupo6.MainMenuActivity.Companion.cartaoUsuario
import com.example.pi_iii_grupo6.MainViewActivity.Companion.locacoesPendentes
import com.example.pi_iii_grupo6.MainViewActivity.Companion.places
import com.example.pi_iii_grupo6.databinding.AlugarArmarioDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.gson.Gson
import java.util.Calendar

class RentActivity : AppCompatActivity() {
    private var binding: ActivityRentBinding? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var userLocation: LatLng
    private lateinit var actualLocker: MainViewActivity.Place
    private var precoSelecionado: MainViewActivity.Preco? = null
    private var gson = Gson()
    private var user: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var locAtual: MainViewActivity.Locacao


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
                locationHandler(flag)
            }
        }
    }

    private fun locationHandler(achou: Int){
        if(achou == 0){
            var textoTitulo = binding?.etTitleLocation
            textoTitulo?.text = "Você ainda não está em nenhum local"
        }else{
            //Usuário está em uma unidade de locação
            var textoTitulo = binding?.etTitleLocation
            var textoEndereco = binding?.etEndereco
            var textoReferencia = binding?.etReferencia

            textoTitulo?.text = "${actualLocker.nomeLocal}"
            textoEndereco?.text = "Endereço: ${actualLocker.enderecoLocal}"
            textoReferencia?.text = "Referência: ${actualLocker.referenciaLocal}"

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
        val sheetBinding: AlugarArmarioDialogBinding = AlugarArmarioDialogBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(sheetBinding.root)

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

        tempo1.text = "${actualLocker.precos[0].tempo} min"
        tempo2.text = "${actualLocker.precos[1].tempo} min"


        preco1.text = "R$ ${actualLocker.precos[0].preco}"
        preco2.text = "R$ ${actualLocker.precos[1].preco}"

        if(actualLocker.precos.size >= 3){
            preco3.text = "R$ ${actualLocker.precos[2].preco}"
            tempo3.text = "${actualLocker.precos[2].tempo} min"
        }else{
            preco3.text = ""
            tempo3.text = ""
        }


        if(actualLocker.precos.size >= 4){
            tempo4.text = "${actualLocker.precos[3].tempo}"
            preco4.text = "R$ ${actualLocker.precos[3].preco}"
        }

        //Inicializando a dialog
        dialog.show()

        opcao1.setOnClickListener{
            if (precoSelecionado?.tempo == actualLocker.precos[0].tempo){
                removerSelecionado()
                desmarcarOpcao(opcao1)
            }else if (precoSelecionado == null){
                addSelecionado(actualLocker.precos[0])
                marcarOpçcao(opcao1)
            }else{
                Toast.makeText(baseContext,"Selecione apenas uma opção",Toast.LENGTH_SHORT).show()
            }
        }
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

        if (!(somaHoraAtual >= 420 && somaHoraAtual <= 480)){
            opcao4.setBackgroundColor(Color.parseColor("#E7E7E7"))
        }
        Log.d("HORA","São $horaAtual:$minutoAtual")

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

        sheetBinding.btnConfirmarLoc.setOnClickListener {
            var userId = user?.uid
            locAtual = MainViewActivity.Locacao(userId,actualLocker,precoSelecionado)

            locacoesPendentes.add(locAtual)
            confirmacao(locAtual)
        }

        }

    fun confirmacao(locacao: MainViewActivity.Locacao){
        var intentQrCode = Intent(this@RentActivity, CodeActivity::class.java)

        if (precoSelecionado == null){
            Toast.makeText(baseContext,"Selecione uma opção",Toast.LENGTH_SHORT).show()
        }else{
            var infosJson = gson.toJson(locacao)

            intentQrCode.putExtra("infosJson",infosJson)
            startActivity(intentQrCode)
        }
    }

    fun addSelecionado(selecionado: MainViewActivity.Preco){
        precoSelecionado = selecionado
        Log.d("SELECTION", "PRECO SELECIONADO: ${precoSelecionado?.preco}")
    }
    fun removerSelecionado(){
        precoSelecionado = null
        Log.d("SELECTION","Preco removido")
    }

    fun marcarOpçcao(viewSelecionada: View){
        viewSelecionada.setBackgroundColor(Color.parseColor("#E7E7E7"))
    }
    fun desmarcarOpcao(viewSelecionada: View){
        viewSelecionada.setBackgroundColor(Color.parseColor("#ffffff"))
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



