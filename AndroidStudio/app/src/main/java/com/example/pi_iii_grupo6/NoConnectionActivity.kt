package com.example.pi_iii_grupo6

import BasicaActivity
import android.os.Bundle
import androidx.lifecycle.Observer

class NoConnectionActivity : BasicaActivity() {

    private lateinit var networkMonitor: InternetMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_connection)

        networkMonitor = InternetMonitor(this)
        networkMonitor.observe(this, Observer { isConnected ->
            if (isConnected) {
                finish()
            }
        })
    }
}