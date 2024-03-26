package com.example.pi_iii_grupo6

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.pi_iii_grupo6.databinding.ActivityMainViewBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.auth.User

internal class MainViewActivity : AppCompatActivity(), OnMapReadyCallback{
    //Declarando as variáveis que serão utilizadas
    private var binding: ActivityMainViewBinding? = null
    private lateinit var mMap: GoogleMap

    private lateinit var auth: FirebaseAuth

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    //Declarando user como null, para depois atribuir o usuário do authenticator a ele (que pode ser null se for anonimo)
    var user: FirebaseUser? = null

    var userLatitude = 0.0
    var userLongitude = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        //Atribuindo valor às variáveis criadas anteriormente
        auth = Firebase.auth
        user = auth.currentUser
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()

        //Inflando o layout e colocando a activity na tela
        super.onCreate(savedInstanceState)
        binding = ActivityMainViewBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Ao clicar no botão logout, chamar a função de logout
        binding?.btnLogout?.setOnClickListener{
            singOutFun()
        }


    }

    private fun getCurrentLocation() {
        val task = fusedLocationProviderClient.lastLocation

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
            .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
                return
            }

        task.addOnSuccessListener {
            if(it != null){
                userLatitude = it.latitude
                userLongitude = it.longitude
                Toast.makeText(this@MainViewActivity, "$userLatitude $userLongitude", Toast.LENGTH_SHORT).show()
            }
        }

    }

    //Criando a função para logout
    private fun singOutFun(){
        //Checando se o usuário fez login
        //Se fez, faz o logout e sai da acitivity principal
        if(user != null){
            Firebase.auth.signOut()
            var voltarLogin = Intent(this@MainViewActivity, LoginActivity::class.java)
            startActivity(voltarLogin)
            Toast.makeText(this@MainViewActivity, "Logout feito com sucesso", Toast.LENGTH_SHORT).show()
        //Se não fez, nao pode usar esta função, pede para fazer o login
        }else{
            Toast.makeText(this@MainViewActivity, "Faça login para acessar essa função", Toast.LENGTH_SHORT).show()
            var abrirLogin = Intent(this@MainViewActivity, LoginActivity::class.java)
            startActivity(abrirLogin)
        }

    }
    //Função que lida com a pós renderização do mapa. o que fazer quando ele estiver pronto?
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Criando array de markers
        val markers = listOf(listOf(-22.835083, -47.047750), listOf(-22.912306,-47.060639), listOf(-22.969944,-46.990417))
        //22°50'06.3"S 47°02'51.9"W
        //Lógica para adicionar os markers
        var count = 0
        for(marker in markers){
            var position = LatLng(marker[0], marker[1])
            mMap.addMarker(MarkerOptions()
                .position(position)
                .title("Marker ${count+1}"))
            count++
       }
        //Movendo o zoom do mapa para onde o usuário está
        var move = LatLng(userLatitude,userLongitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(move))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(5.0F))

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object{

    }
}