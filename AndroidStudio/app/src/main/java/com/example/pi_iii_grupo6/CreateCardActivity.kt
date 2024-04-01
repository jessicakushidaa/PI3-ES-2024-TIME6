package com.example.pi_iii_grupo6

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.pi_iii_grupo6.databinding.ActivityCreateCardBinding

class CreateCardActivity : AppCompatActivity() {
    private var binding: ActivityCreateCardBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //O que fazer quando clicar em cadastrar
        binding?.btnCadastrar?.setOnClickListener{
            Toast.makeText(baseContext, "Funcao em desenvolvimento", Toast.LENGTH_SHORT).show()
            //Voltando pro menu
            var voltarMenu = Intent(this@CreateCardActivity, MainMenuActivity::class.java)
            startActivity(voltarMenu)
        }

        //AÇÕES DO BOTTOM NAVIGATION
        binding?.btnHome?.setOnClickListener {
            var irHome = Intent(this@CreateCardActivity, MainViewActivity::class.java)
            startActivity(irHome)
        }
        binding?.btnCartoes?.setOnClickListener {
            var irCartoes = Intent(this@CreateCardActivity, CreateCardActivity::class.java)
            startActivity(irCartoes)
        }
        binding?.btnLocacoes?.setOnClickListener {
            var irLocacoes = Intent(this@CreateCardActivity, RentManagerActivity::class.java)
            startActivity(irLocacoes)
        }
    }


}