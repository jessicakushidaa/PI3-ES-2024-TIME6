package com.example.pi_iii_grupo6

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.pi_iii_grupo6.databinding.ActivityMainViewBinding
import com.example.pi_iii_grupo6.databinding.DialogMarkerInfoBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.StyleSpan
import com.google.android.material.bottomsheet.BottomSheetDialog
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
    private lateinit var userLocation: LatLng

    private class Place (
        var latitude: Double,
        var longitude: Double,
        var nomeLocal: String,
        var descricaoLocal: String,
        var enderecoLocal: String,
        var referenciaLocal: String
    )

    private var places = listOf<Place>(
        Place(-22.835083, -47.047750, "Lockers Room 1","","Rua armando bonitão 213","Praia Pipa"),
        Place(-22.912306,-47.060639, "Lockers Room 2","","Rua Hamilton jardão 185","Mercado Oxxo"),
        Place(-22.969944,-46.990417, "Lockers Room 3","","Rua puc legal 021","Casa de shows Hallon"),
        Place(-22.943614, -46.993418, "Lockers Room 4","","Rua José carlos ferrari 876","Supermercado Asp"),
        Place(-22.944592, -46.995360, "Lockers Room 5","","Rua armando feiao 999","Colégio Inovati")
    )

    //Declarando user como null, para depois atribuir o usuário do authenticator a ele (que pode ser null se for anonimo)
    var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        //Atribuindo valor às variáveis criadas anteriormente
        auth = Firebase.auth
        user = auth.currentUser
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


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

    private fun showMarkerInfo(title: String?, adress: LatLng, reference: String?) {
        val dialog = BottomSheetDialog(this)
        val sheetBinding:  DialogMarkerInfoBinding = DialogMarkerInfoBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(sheetBinding.root)

        var titleTextView = sheetBinding.tvTitle
        var referenceTextView = sheetBinding.tvReference
        var adressTextView = sheetBinding.tvAdress

        titleTextView.setText("$title")
        referenceTextView.setText("$reference")
        adressTextView.setText("${adress.latitude} , ${adress.longitude}")

        dialog.show()

        sheetBinding.btnRoute.setOnClickListener{
            drawPath(mMap, adress)
        }
    }

    private fun getCurrentLocation(){
        Log.e("debug", "Entrou na getCurrentLocation")
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
            .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ){
                Log.e("debug", "Permissions")
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            }

        fusedLocationProviderClient.lastLocation.addOnCompleteListener {task ->
            Log.e("debug", "CompleteListener")
            var location = task.result
            if(location != null){
                Log.e("debug", "Sucess")
                userLocation = LatLng(location.latitude,location.longitude)
                //Movendo o zoom do mapa para onde o usuário está
                moverMapa(mMap, userLocation)
                //addMarker(mMap, userLocation, "Your Location")
                mMap.isMyLocationEnabled = true
            }
        }
        Log.e("debug", "Acabou GetLocation")
        userLocation = LatLng(2.0,2.0)
    }

    private fun addMarker(mapa: GoogleMap, position: LatLng, title:String, reference: String, adress: String) {
        mapa.addMarker(
            MarkerOptions()
            .position(position)
            .title(title)
            .snippet(reference)
        )

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
        Log.e("debug", "OnMapReady")

        //Lógica para adicionar os markers
        addAllMarkers()

        Log.e("debug", "Acabou OnMapReady")
        getCurrentLocation()

        mMap.setOnMarkerClickListener {
            showMarkerInfo(it.title,it.position, it.snippet)
            false
        }


    }

    private fun drawPath(mMap: GoogleMap, adress: LatLng){
        val line = mMap.addPolyline(
            PolylineOptions()
                .add(userLocation, adress)
                .addSpan(StyleSpan(Color.RED))
                .addSpan(StyleSpan(Color.GREEN))
        )
    }
    private fun addAllMarkers() {

        for(place in places){
            var position = LatLng(place.latitude, place.longitude)
            addMarker(mMap, position, place.nomeLocal,place.referenciaLocal,place.enderecoLocal)
        }
    }


    // ESTA CHAMANDO MOVER MAPA ANTES DE GET CURRENT LOCATION
    private fun moverMapa(mMap: GoogleMap, local: LatLng) {
        Log.e("debug", "MoverMapa")
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(local, 15.0f))
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}