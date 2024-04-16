package com.example.pi_iii_grupo6

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.auth.User
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext

class MainViewActivity : AppCompatActivity(), OnMapReadyCallback{
    //Declarando as variáveis que serão utilizadas
    private var binding: ActivityMainViewBinding? = null
    private lateinit var mMap: GoogleMap

    private lateinit var auth: FirebaseAuth

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var userLocation: LatLng

    //Criando classe Place que representa cada Unidade de Locação
    class Place (
        var id: String,
        var latitude: Double,
        var longitude: Double,
        var nomeLocal: String,
        var descricaoLocal: String,
        var enderecoLocal: String,
        var referenciaLocal: String,
        var precos: List<Preco>
    )
    class Preco (
        var tempo: Int,
        var preco: Double
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

        //Direcionando o bottomNavigation
        val bottomNavigation : BottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                //tela Locações
                R.id.page_1 -> {
                    if (user != null) {
                        startActivity(Intent(this, RentManagerActivity::class.java))
                    }else{
                        Toast.makeText((baseContext), "Faça login para acessar essa função",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    true
                }
                //tela Mapa
                R.id.page_2 -> {
                    startActivity(Intent(this, MainViewActivity::class.java))
                    true
                }
                //tela Cartões
                R.id.page_3 -> {
                    if (user != null) {
                        startActivity(Intent(this, RentManagerActivity::class.java))
                    }else{
                        Toast.makeText((baseContext),"Faça login para acessar essa função",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    true
                }

                else -> false

            }
        }

        //Referenciando o Fragment do mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Ao clicar no botão logout, chamar a função de logout
        binding?.btnLogout?.setOnClickListener{
            singOutFun()
        }
    }

//Funçao que chama a dialog box das informações da unidade de Locação
    private fun showMarkerInfo(title: String?, adress: LatLng, reference: String?) {
        //Criando a dialog box
        val dialog = BottomSheetDialog(this)
        val sheetBinding:  DialogMarkerInfoBinding = DialogMarkerInfoBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(sheetBinding.root)

        //Referenciando os campos que serão preenchidos pelas informações da unidade de locação
        var titleTextView = sheetBinding.tvTitle
        var referenceTextView = sheetBinding.tvReference
        var adressTextView = sheetBinding.tvAdress

        //Preenchendo os campos com as informações da unidade
        titleTextView.setText("$title")
        referenceTextView.setText("$reference")
        adressTextView.setText("${adress.latitude} , ${adress.longitude}")

    //Inicializando a dialog
        dialog.show()

        // Função que chama o traçar rota ao clicar no botão
        sheetBinding.btnRoute.setOnClickListener{
            drawPath(mMap, adress)
        }
    }

    //Função que obtém a localização atual do usuário
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
                //Movendo o zoom do mapa para onde o usuário está
                moverMapa(mMap, userLocation)
                //addMarker(mMap, userLocation, "Your Location")
                mMap.isMyLocationEnabled = true
            }
        }
    }

    //Função que adiciona um marker no mapa
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
            //Voltando para a pagina de login
            var voltarLogin = Intent(this@MainViewActivity, LoginActivity::class.java)
            startActivity(voltarLogin)
            Toast.makeText(this@MainViewActivity, "Logout feito com sucesso", Toast.LENGTH_SHORT).show()
        //Se não fez, nao pode usar esta função, pede para fazer o login
        }else{
            Toast.makeText(this@MainViewActivity, "Faça login para acessar essa função", Toast.LENGTH_SHORT).show()
            //Abrindo tela de login
            var abrirLogin = Intent(this@MainViewActivity, LoginActivity::class.java)
            startActivity(abrirLogin)
        }

    }

    //Função que lida com a pós renderização do mapa. o que fazer quando ele estiver pronto?
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //Chamando função para adicionar os markers
        addAllMarkers()

        //Chamando função para obter a localização do usuário
        getCurrentLocation()

        //Setando um listener para quando clicar no marker, chamar função que mostra as informações do marker clicado
        mMap.setOnMarkerClickListener {
            showMarkerInfo(it.title,it.position, it.snippet)
            false
        }


    }

    //Função que desenha a rota entre dois pontos
    private fun drawPath(mMap: GoogleMap, adress: LatLng){
        getCurrentLocation()
        val geoApiContext = GeoApiContext.Builder()
            .apiKey("AIzaSyAol2dJabESlpiblrTmbN6XHeg8MyOKREM")
            .build()

        val directionsApi = DirectionsApi.newRequest(geoApiContext)
        val directionsResult = directionsApi.origin(com.google.maps.model.LatLng(userLocation.latitude,userLocation.longitude))
            .destination(com.google.maps.model.LatLng(adress.latitude, adress.longitude))
            .await()

        Log.d("ROUTE","${userLocation.latitude} ${userLocation.longitude}")

        val route = directionsResult.routes[0]
        val polylineOptions = PolylineOptions()
            .addAll(route.overviewPolyline.decodePath().map { convertToAndroidLatLng(it) })
            .color(Color.BLUE)
            .width(8f)

        mMap.addPolyline(polylineOptions)
    }

    private fun convertToAndroidLatLng(latLng: com.google.maps.model.LatLng): LatLng {
        return LatLng(latLng.lat, latLng.lng)
    }
    //Função que adiciona os markers da lista de unidades de locação
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

    companion object{
        //Criando uma lista de unidades de locação, pois rodará um looping nela para adicionar os markers
        var places = listOf<Place>(
            Place("A1",-22.835083, -47.047750, "Lockers Room 1","","Rua armando bonitão 213","Praia Pipa", listOf(Preco(30, 20.0), Preco(60, 40.0), Preco(120,55.0))),
            Place("A2",-22.912306,-47.060639, "Lockers Room 2","","Rua Hamilton jardão 185","Mercado Oxxo", listOf(Preco(15, 25.90), Preco(60, 45.0), Preco(125,65.0))),
            Place("A3",-22.969944,-46.990417, "Lockers Room 3","","Rua puc legal 021","Casa de shows Hallon", listOf(Preco(30, 20.0), Preco(60, 40.0), Preco(120,55.0))),
            Place("A4",-22.943614, -46.993418, "Lockers Room 4","","Rua José carlos ferrari 876","Supermercado Asp", listOf(Preco(30, 20.0), Preco(60, 40.0), Preco(120,55.0),Preco(160,130.50))),
            Place("A5",-22.944592, -46.995360, "Lockers Room 5","","Rua armando feiao 999","Colégio Inovati", listOf(Preco(30, 20.0), Preco(60, 40.0), Preco(120,55.0))),
            Place("A6",-22.943412, -47.000364,"Lockers Room 6", "","Rua josé carlos ferrari 65454", "casa 224", listOf(Preco(30, 35.0), Preco(60, 55.0), Preco(120,85.0)))

        )
    }

    //Função que volta o binding para null ao encerrar a activity
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}