package com.example.adufoundit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.adufoundit.api.ApiClient
import com.example.adufoundit.api.ApiService
import com.example.adufoundit.api.SimpleResponse
import com.example.adufoundit.api.UpdateProfileRequest
import com.example.adufoundit.api.UpdatePasswordRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnEditPicture = findViewById<Button>(R.id.btnEditPicture)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnProfileTab = findViewById<Button>(R.id.btnProfileTab)
        val btnSecurityTab = findViewById<Button>(R.id.btnSecurityTab)

        val layoutProfileForm = findViewById<LinearLayout>(R.id.layoutProfileForm)
        val layoutSecurityForm = findViewById<LinearLayout>(R.id.layoutSecurityForm)

        val etName = findViewById<EditText>(R.id.etName)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etContact = findViewById<EditText>(R.id.etContact)
        val btnSaveProfile = findViewById<Button>(R.id.btnSaveProfile)

        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSavePassword = findViewById<Button>(R.id.btnSavePassword)

        val prefs = getSharedPreferences("AUTH", MODE_PRIVATE)

        val userId = prefs.getString("USER_ID", null)
        val fullName = prefs.getString("FULL_NAME", "")
        val email = prefs.getString("USER_EMAIL", "")
        val tvUserFullName = findViewById<TextView>(R.id.tvUserFullName)
        val tvUserUsername = findViewById<TextView>(R.id.tvUserUsername)

        val username = prefs.getString("USERNAME", "")

        tvUserFullName.text = fullName?.takeIf { it.isNotBlank() } ?: "Unnamed User"
        tvUserUsername.text = username?.takeIf { it.isNotBlank() } ?: ""

        etName.setText(fullName)
        etEmail.setText(email)

        
        btnProfileTab.setOnClickListener {
            layoutProfileForm.visibility = View.VISIBLE
            layoutSecurityForm.visibility = View.GONE
            btnProfileTab.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue))
            btnSecurityTab.setBackgroundColor(ContextCompat.getColor(this, R.color.light_blue))
        }

        btnSecurityTab.setOnClickListener {
            layoutProfileForm.visibility = View.GONE
            layoutSecurityForm.visibility = View.VISIBLE
            btnSecurityTab.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue))
            btnProfileTab.setBackgroundColor(ContextCompat.getColor(this, R.color.light_blue))
        }

        
        btnEditPicture.setOnClickListener {
            Toast.makeText(this, "Image picker will be implemented", Toast.LENGTH_SHORT).show()
        }

        
        btnSaveProfile.setOnClickListener {

            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val contact = etContact.text.toString().trim()

            val userId = prefs.getString("USER_ID", null)

            if (userId == null) {
                Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val api = ApiClient.retrofit.create(ApiService::class.java)

            api.updateProfile(
                UpdateProfileRequest(
                    UserID = userId,
                    FullName = name,
                    Email = email,
                    ContactNumber = contact.ifEmpty { null }
                )
            ).enqueue(object : Callback<SimpleResponse> {

                override fun onResponse(
                    call: Call<SimpleResponse>,
                    response: Response<SimpleResponse>
                ) {
                    if (response.isSuccessful && response.body()?.ok == true) {

                        
                        prefs.edit()
                            .putString("FULL_NAME", name)
                            .putString("USER_EMAIL", email)
                            .apply()

                        
                        tvUserFullName.text = name

                        Toast.makeText(
                            this@SettingsActivity,
                            "Profile updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@SettingsActivity,
                            "Update failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                    Toast.makeText(
                        this@SettingsActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }

        
        btnSavePassword.setOnClickListener {

            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val userId = prefs.getString("USER_ID", null)

            if (userId == null) {
                Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val api = ApiClient.retrofit.create(ApiService::class.java)

            api.updatePassword(
                UpdatePasswordRequest(
                    UserID = userId,
                    NewPassword = password
                )
            ).enqueue(object : Callback<SimpleResponse> {

                override fun onResponse(
                    call: Call<SimpleResponse>,
                    response: Response<SimpleResponse>
                ) {
                    if (response.isSuccessful && response.body()?.ok == true) {
                        Toast.makeText(this@SettingsActivity, "Password updated", Toast.LENGTH_SHORT).show()
                        etPassword.text.clear()
                        etConfirmPassword.text.clear()
                    } else {
                        Toast.makeText(this@SettingsActivity, "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                    Toast.makeText(this@SettingsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
        }

        
        btnLogout.setOnClickListener {

            
            getSharedPreferences("AUTH", MODE_PRIVATE)
                .edit()
                .clear()
                .apply()

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            
            val intent = Intent(this, LoginActivity::class.java).apply {
                putExtra("FROM_LOGOUT", true)  
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}