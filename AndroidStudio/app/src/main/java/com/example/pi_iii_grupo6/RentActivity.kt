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
            var texto = binding?.etTitleLocation
            texto?.text = "Você ainda não está em nenhum local"
        }else{
            //Implementar continuacao
            var texto = binding?.etTitleLocation
            texto?.text = "Você está em: ${actualLocker.nomeLocal}"

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

    //Função que volta o binding para null ao encerrar a activity
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}



