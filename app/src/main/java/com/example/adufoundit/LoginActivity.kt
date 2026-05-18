package com.example.adufoundit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.adufoundit.api.ApiClient
import com.example.adufoundit.api.ApiService
import com.example.adufoundit.api.LoginRequest
import com.example.adufoundit.api.LoginResponse
import retrofit2.Call
import android.widget.TextView
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val tvEmailNotLinked = findViewById<TextView>(R.id.tvEmailNotLinked)

        tvEmailNotLinked.setOnClickListener {
            startActivity(
                Intent(this, LinkEmailActivity::class.java)
            )
        }

        val prefs = getSharedPreferences("AUTH", MODE_PRIVATE)

        val fromLogout = intent.getBooleanExtra("FROM_LOGOUT", false)

        val existingUserId = prefs.getString("USER_ID", null)
        val existingEmail = prefs.getString("USER_EMAIL", null)
        Log.d(TAG, "onCreate - fromLogout: $fromLogout, existingUserId: $existingUserId, existingEmail: $existingEmail")

        if (!fromLogout && existingUserId != null) {
            Log.d(TAG, "Auto-login triggered for user: $existingEmail")
            goToHome()
            return
        }

        Log.d(TAG, "Showing login screen")

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnEnter = findViewById<Button>(R.id.btnEnter)

        btnEnter.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            Log.d(TAG, "Login attempt with email: $email")

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.edit().clear().apply()
            Log.d(TAG, "Cleared old session data")

            val api = ApiClient.retrofit.create(ApiService::class.java)

            api.loginUser(LoginRequest(email, password))
                .enqueue(object : Callback<LoginResponse> {

                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        val body = response.body()
                        val user = body?.user

                        Log.d(TAG, "API Response - Code: ${response.code()}, Body: $body")
                        Log.d(TAG, "User data - UserID: ${user?.UserID}, Email: ${user?.Email}")

                        if (response.isSuccessful && body?.ok == true && user != null) {

                            prefs.edit()
                                .putString("USER_ID", user.UserID)
                                .putString("USER_EMAIL", user.Email)
                                .putString("FULL_NAME", user.FullName)
                                .apply()

                            val savedId = prefs.getString("USER_ID", null)
                            val savedEmail = prefs.getString("USER_EMAIL", null)
                            Log.d(TAG, "Saved to prefs - UserID: $savedId, Email: $savedEmail")

                            Toast.makeText(
                                this@LoginActivity,
                                "Login successful!",
                                Toast.LENGTH_SHORT
                            ).show()

                            goToHome()

                        } else {
                            Log.e(TAG, "Login failed - Response code: ${response.code()}, ok: ${body?.ok}")
                            Toast.makeText(
                                this@LoginActivity,
                                "Invalid credentials",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Log.e(TAG, "Network error: ${t.message}", t)
                        Toast.makeText(
                            this@LoginActivity,
                            "Network error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }

    private fun goToHome() {
        Log.d(TAG, "Navigating to HomeActivity")
        startActivity(
            Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}
