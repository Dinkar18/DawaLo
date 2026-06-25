package com.dp.dawalo.ui.auth

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.FontAssetDelegate
import com.dp.dawalo.MainActivity
import com.dp.dawalo.MedNutriTrackApp
import com.dp.dawalo.data.remote.RetrofitClient
import com.dp.dawalo.databinding.ActivityLoginBinding
import com.dp.dawalo.utils.PreferenceManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lottieLogin.setFontAssetDelegate(object : FontAssetDelegate() {
            override fun getFontPath(fontFamily: String?): String? = null
            override fun fetchFont(fontFamily: String?): Typeface = Typeface.DEFAULT
        })

        preferenceManager = PreferenceManager(this)

        if (preferenceManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        binding.btnLogin.setOnClickListener {
            val phone = binding.etPhone.text.toString()
            val password = binding.etPassword.text.toString()

            if (phone.isNotEmpty() && password.isNotEmpty()) {
                login(phone, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        
        binding.tvLoginWithOtp.setOnClickListener {
            loginWithOtp()
        }
    }
    
    private fun loginWithOtp() {
        val phone = binding.etPhone.text.toString()
        
        if (phone.isEmpty()) {
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.btnLogin.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val request = mapOf("phone" to phone)
                val api = RetrofitClient.getAuthApi(preferenceManager)
                val response = api.sendOtp(request)
                
                if (response.success) {
                    Toast.makeText(this@LoginActivity, "OTP sent to $phone", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, OtpActivity::class.java)
                    intent.putExtra("phone", phone)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@LoginActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: java.net.ConnectException) {
                // Backend is down - proceed to OTP screen with offline mode
                Toast.makeText(this@LoginActivity, "Backend unavailable. Use test OTP: 123456", Toast.LENGTH_LONG).show()
                val intent = Intent(this@LoginActivity, OtpActivity::class.java)
                intent.putExtra("phone", phone)
                startActivity(intent)
            } catch (e: java.net.SocketTimeoutException) {
                Toast.makeText(this@LoginActivity, "Backend unavailable. Use test OTP: 123456", Toast.LENGTH_LONG).show()
                val intent = Intent(this@LoginActivity, OtpActivity::class.java)
                intent.putExtra("phone", phone)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Backend unavailable. Use test OTP: 123456", Toast.LENGTH_LONG).show()
                val intent = Intent(this@LoginActivity, OtpActivity::class.java)
                intent.putExtra("phone", phone)
                startActivity(intent)
            } finally {
                binding.btnLogin.isEnabled = true
            }
        }
    }

    private fun login(phone: String, password: String) {
        binding.btnLogin.isEnabled = false
        
        lifecycleScope.launch {
            try {
                // Offline mode: Direct login with local DB
                val app = application as MedNutriTrackApp
                val user = app.database.userDao().getCurrentUser()
                
                if (user != null) {
                    preferenceManager.userId = user.id
                    preferenceManager.token = "offline_mode"
                    navigateToMain()
                } else {
                    Toast.makeText(this@LoginActivity, "No user found. Please register first.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnLogin.isEnabled = true
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
