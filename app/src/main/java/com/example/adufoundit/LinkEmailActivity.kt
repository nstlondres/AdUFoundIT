package com.example.adufoundit

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.adufoundit.api.ApiClient
import com.example.adufoundit.api.ApiService
import com.example.adufoundit.api.RegisterRequest
import com.example.adufoundit.api.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LinkEmailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_link_email)

        val etStudentNumber = findViewById<EditText>(R.id.etStudentNumber)
        val etSchoolEmail = findViewById<EditText>(R.id.etSchoolEmail)
        val etPassword = findViewById<EditText>(R.id.etCreatePassword)
        val btnLinkEmail = findViewById<Button>(R.id.btnLinkEmail)

        btnLinkEmail.setOnClickListener {

            val studentNumber = etStudentNumber.text.toString().trim()
            val schoolEmail = etSchoolEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (studentNumber.isEmpty() || schoolEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val api = ApiClient.retrofit.create(ApiService::class.java)

            
            val request = RegisterRequest(
                FullName = studentNumber,        
                Email = schoolEmail,
                PasswordHash = password          
            )

            api.registerUser(request).enqueue(object : Callback<RegisterResponse> {

                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    if (response.isSuccessful && response.body()?.ok == true) {

                        Toast.makeText(
                            this@LinkEmailActivity,
                            "Registration successful!",
                            Toast.LENGTH_SHORT
                        ).show()

                        finish() 

                    } else {
                        Toast.makeText(
                            this@LinkEmailActivity,
                            "Registration failed: ${response.body()?.error}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    Toast.makeText(
                        this@LinkEmailActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }
}
