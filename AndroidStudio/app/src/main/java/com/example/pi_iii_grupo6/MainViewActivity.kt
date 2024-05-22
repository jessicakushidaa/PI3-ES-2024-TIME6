package com.example.pi_iii_grupo6

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.ScriptGroup.Binding
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.appcompat.widget.Toolbar
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
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.api.LogDescriptor
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.auth.User
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.google.gson.Gson
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext

class MainViewActivity : AppCompatActivity(), OnMapReadyCallback{
    //Declarando as variáveis que serão utilizadas
    private var binding: ActivityMainViewBinding? = null
    private lateinit var mMap: GoogleMap
    private lateinit var functions: FirebaseFunctions
    private var gson = Gson()



    private lateinit var auth: FirebaseAuth

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var userLocation: LatLng

    //Criando classe Place que representa cada Unidade de Locação
    class Place (
        var id: String,
        var latitude: Double,
        var longitude: Double,
        var nomeLocal: String,
        var enderecoLocal: String,
        var referenciaLocal: String,
        var precos: List<Preco>,
        var telefone: String
    )
    class Locacao (
        var userId: String?,
        var armario: Place?,
        var preco: Preco?,
        var foto: MutableList<String> = mutableListOf(),
        var pulseiras: MutableList<String> = mutableListOf(),
        var locId: String = "",
        var unidadeId: String = "",
    )
    class Preco (
        var tempo: Any?,
        var preco: Double
    )

    //Declarando user como null, para depois atribuir o usuário do authenticator a ele (que pode ser null se for anonimo)
    var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        //Atribuindo valor às variáveis criadas anteriormente
        auth = Firebase.auth
        user = auth.currentUser
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        functions = Firebase.functions("southamerica-east1")

        //Inflando o layout e colocando a activity na tela
        super.onCreate(savedInstanceState)
        binding = ActivityMainViewBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //Chamar funcao que busca todos os armarios
        buscarArmarios().addOnCompleteListener { task->
            if (task.isSuccessful){
                val armariosGson = task.result
                //val unidadesLocacao = gson.fromJson(armariosGson, listOf<Place>()::class.java)
                Log.d("LOGARMARIOS", "$armariosGson")
            }else{
                Log.e("LOGARMARIOS", "Erro ao buscar armarios: ${task.exception}")

            }
        }

        setSupportActionBar(binding?.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Remove o texto do nome do aplicativo

        //Seta Retorno aparece na ToolBar caso o usuário esteja logado
        if (user != null) {
            // Configurando a Toolbar para permitir voltar à tela anterior
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }else {
            // continua
        }

        //Direcionando o bottomNavigation
        val bottomNavigation : BottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                //tela Locações
                R.id.page_locacoes -> {
                    if (user != null) {
                        startActivity(Intent(this, RentManagerActivity::class.java))
                    }else{
                        Toast.makeText((baseContext), "Faça login para acessar essa função",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    true
                }
                //tela Mapa
                R.id.page_mapa -> {
                    startActivity(Intent(this, MainViewActivity::class.java))
                    true
                }
                //tela Cartões
                R.id.page_cartoes -> {
                    if (user != null) {
                        startActivity(Intent(this, ShowCardActivity::class.java))
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

    }

    private fun buscarArmarios(): Task<String>{
        return functions
            .getHttpsCallable("getAllUnits")
            .call()
            .continueWith{ task->
                if(task.isSuccessful){
                    val data = task.result.data as Map<String, Any>
                    val payload = data["payload"] as Map<String, Any>
                    val unidades = payload["unidades"] as ArrayList<*>
                    val numUnidades = unidades.count()
                    var listaDeUnidades: MutableList<Place> = mutableListOf()
                    var i = 0
                    while (i < numUnidades){
                        Log.d("DEBUG UNIDADES","Entrou na unidade $i de $numUnidades")
                        val unidade = unidades[i] as Map<String, Any>
                        Log.d("DEBUG UNIDADES","$unidade")
                        val coordenadas = unidade["coordenadas"] as Map<String, Any>
                        val id = unidade["id"] as String
                        val latitude = coordenadas["latitude"] as Double
                        val longitude = coordenadas["longitude"] as Double
                        val nome = unidade["nome"] as String
                        val endereco = unidade["endereco"] as String
                        val descricao = unidade["descricao"] as String
                        val tabelaPrecos = unidade["tabelaPrecos"] as ArrayList<*>
                        val telefone = unidade["telefone"] as String
                        val numPrecos = tabelaPrecos.count()
                        //Logica para pegar cada um dos precos, transformar na classe Preco e guardar em uma listOf<Preco>
                        var j = 0
                        var listaPrecos: MutableList<Preco> = mutableListOf()
                        while (j < numPrecos){
                            Log.d("DEBUG UNIDADES","ENTROU NO WHILE DO PRECO")
                            var precoAtual = tabelaPrecos[j] as Map<String, Any>
                            var preco = Preco(precoAtual["tempo"], precoAtual["preco"] as Double)
                            listaPrecos.add(preco)
                            j++
                        }

                        //Montar uma Place com os dados coletados e guardar na lugares: listOf<Places>
                        var unidadeLocacao = Place(id,latitude, longitude, nome, endereco, descricao, listaPrecos,telefone)
                        listaDeUnidades.add(unidadeLocacao)
                        i++
                    }
                    places = listaDeUnidades
                    val unidadesJson = gson.toJson(listaDeUnidades)
                    unidadesJson
                }else{
                    throw task.exception ?: Exception("Unknown error occurred")
                }
            }
    }

    //Funçao que chama a dialog box das informações da unidade de Locação
    private fun showMarkerInfo(title: String?, adress: Any?, reference: String?, position: LatLng) {
        //Criando a dialog box
        val dialog = BottomSheetDialog(this)
        val sheetBinding:  DialogMarkerInfoBinding = DialogMarkerInfoBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(sheetBinding.root)

        //Referenciando os campos que serão preenchidos pelas informações da unidade de locação
        var titleTextView = sheetBinding.tvTitle
        var referenceTextView = sheetBinding.tvReference

        //Preenchendo os campos com as informações da unidade
        titleTextView.setText("$title")
        referenceTextView.setText("$reference")

        //Inicializando a dialog
        dialog.show()

        // Ao clicar no botao chama Funcao de traçar rota e de substituir o botao
        sheetBinding.btnRoute.setOnClickListener{
            drawPath(mMap, position)
            substituirBotao(sheetBinding, title)
        }
    }
    //Função para substituir o botão, de "Ir até la" para "Alugar"
    fun substituirBotao(binding: DialogMarkerInfoBinding, nome:String?){
        val botaoIrAte = binding.btnRoute

        //Criando novo botão
        val btnNovo = Button(this)
        btnNovo.text = "Alugar"
        btnNovo.id = R.id.btnAlugarArmario
        btnNovo.setBackgroundColor(resources.getColor(R.color.blue_nav))
        btnNovo.setTextColor(resources.getColor(R.color.white))
        val layoutParams = ViewGroup.LayoutParams(
            300, // Largura
            120 // Altura
        )
        btnNovo.layoutParams = layoutParams


        val viewPai = botaoIrAte.parent as ViewGroup

        val index = viewPai.indexOfChild(botaoIrAte)

        viewPai.removeView(botaoIrAte)
        viewPai.addView(btnNovo, index)

        btnNovo.setOnClickListener {
            if (user != null) {
                abrirAlugarArmario(nome)
            } else {
                Toast.makeText((baseContext), "Faça login para acessar essa função",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }

    private fun abrirAlugarArmario(nome: String?) {
        val intentAlugar = Intent(this@MainViewActivity, RentActivity::class.java)
        intentAlugar.putExtra("nomeLocker",nome)
        startActivity(intentAlugar)
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
            Log.d(  "MAINVIEW","ENTROU NO COMPLETE LISTENER")
            //Atribuindo a localização a uma variável
            var location = task.result
            //Checando se a localização é nula
            if(location != null){
                Log.d("MAINVIEW","LOCATION NAO NULL")
                //Se nao for nula, atualiza a variável de localização do usuário
                userLocation = LatLng(location.latitude,location.longitude)
                //Movendo o zoom do mapa para onde o usuário está
                moverMapa(mMap, userLocation)
                //addMarker(mMap, userLocation, "Your Location")
                mMap.isMyLocationEnabled = true
            }else{
                Log.d("MAINVIEW","LOCATION NULL")
            }
        }
    }

    //Função que adiciona um marker no mapa
    private fun addMarker(mapa: GoogleMap, position: LatLng, title:String, reference: String, adressComing: String) {
        mapa.addMarker(
            MarkerOptions()
                .position(position)
                .title(title)
                .snippet(adressComing + "\n\n\n" + reference)
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
            showMarkerInfo(it.title,it.tag, it.snippet,it.position)
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
            Log.d("DebugMarker","Adicionando marker")
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
        var places = listOf<Place>()

        var locacoesPendentes = mutableListOf<Locacao>()
        var locacoesConfirmadas = mutableListOf<Locacao>()
    }

    //Função que volta o binding para null ao encerrar a activity
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}