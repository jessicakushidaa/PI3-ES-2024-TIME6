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
import com.google.firebase.auth.auth

internal class MainViewActivity : AppCompatActivity(), OnMapReadyCallback{
    private var binding: ActivityMainViewBinding? = null
    private lateinit var mMap: GoogleMap

    private lateinit var auth: FirebaseAuth

    var user = auth.currentUser


    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth


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
        }else{
            Toast.makeText(this@MainViewActivity, "Faça login para acessar essa função", Toast.LENGTH_SHORT).show()
        }

    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions()
            .position(sydney)
            .title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}