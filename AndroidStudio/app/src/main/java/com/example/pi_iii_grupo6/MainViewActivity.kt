package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.pi_iii_grupo6.databinding.ActivityMainViewBinding

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
    private var binding: ActivityMainViewBinding? = null
    private lateinit var mMap: GoogleMap

    private lateinit var auth: FirebaseAuth

    var user: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        user = auth.currentUser

        super.onCreate(savedInstanceState)
        binding = ActivityMainViewBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding?.btnLogout?.setOnClickListener{
            singOutFun()
        }
    }

    private fun singOutFun(){
        if(user != null){
            Firebase.auth.signOut()
            var voltarLogin = Intent(this@MainViewActivity, LoginActivity::class.java)
            startActivity(voltarLogin)
            Toast.makeText(this@MainViewActivity, "Logout feito com sucesso", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this@MainViewActivity, "Faça login para acessar essa função", Toast.LENGTH_SHORT).show()
            var abrirLogin = Intent(this@MainViewActivity, LoginActivity::class.java)
            startActivity(abrirLogin)
        }

    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-22.835083, -47.047750)
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
        var move = LatLng(markers[0][0], markers[0][1])
        mMap.moveCamera(CameraUpdateFactory.newLatLng(move))

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}