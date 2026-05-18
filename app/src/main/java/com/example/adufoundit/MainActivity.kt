package com.example.adufoundit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnLinkEmail = findViewById<Button>(R.id.btnLinkEmail)
        btnLinkEmail.setOnClickListener {
            val intent = Intent(this, LinkEmailActivity::class.java)
            startActivity(intent)
        }

        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        btnSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
