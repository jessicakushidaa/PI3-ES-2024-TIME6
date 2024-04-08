package com.example.pi_iii_grupo6

import android.content.pm.PackageManager
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
import com.example.pi_iii_grupo6.MainViewActivity.Companion.places
import com.example.pi_iii_grupo6.databinding.AlugarArmarioDialogBinding
import com.example.pi_iii_grupo6.databinding.DialogMarkerInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class RentActivity : AppCompatActivity() {
    private var binding: ActivityRentBinding? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var userLocation: LatLng
    private lateinit var actualLocker: MainViewActivity.Place
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRentBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()

        binding?.btnAlugarArmario?.setOnClickListener{
            dialogAlugarArmario()
        }
    }



    //Lógica que compara a localização do usuário com cada localização de locker
    private fun checkLocation(): Int {
        var flag = 0
        for (place in places) {

            var locPlace = LatLng(place.latitude, place.longitude)

            var distancia = calcularDistancia(locPlace)

            if (distancia <= 1.0) {
                //Está em alguma unidade!
                actualLocker = place
                flag = 1
                locationHandler(flag)
                return flag
            }
        }
        locationHandler(flag)
        return flag
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

            textoTitulo?.text = "Você está em: ${actualLocker.nomeLocal}"
            textoEndereco?.text = "Endereço: ${actualLocker.enderecoLocal}"
            textoReferencia?.text = "Referência: ${actualLocker.referenciaLocal}"

        }
    }
    private fun getCurrentLocation(){
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
                checkLocation()
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
        tempo3.text = "${actualLocker.precos[2].tempo} min"

        preco1.text = "R$ ${actualLocker.precos[0].preco}"
        preco2.text = "R$ ${actualLocker.precos[1].preco}"
        preco3.text = "R$ ${actualLocker.precos[2].preco}"

        if(actualLocker.precos.size >= 4){
            tempo4.text = "${actualLocker.precos[3].tempo} min"
            preco4.text = "R$ ${actualLocker.precos[3].preco}"
        }

        //Inicializando a dialog
        dialog.show()
        }



    //Função que volta o binding para null ao encerrar a activity
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}



