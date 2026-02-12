package com.dp.dawalo.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dp.dawalo.MainActivity
import com.dp.dawalo.data.remote.RetrofitClient
import com.dp.dawalo.databinding.ActivityOtpBinding
import com.dp.dawalo.utils.PreferenceManager
import kotlinx.coroutines.launch

class OtpActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOtpBinding
    private lateinit var prefs: PreferenceManager
    private var phone: String = ""
    
    companion object {
        private const val TAG = "OtpActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting OtpActivity")
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefs = PreferenceManager(this)
        phone = intent.getStringExtra("phone") ?: ""
        Log.d(TAG, "onCreate: Phone = $phone")
        
        binding.tvPhone.text = "OTP sent to $phone"
        
        binding.btnVerify.setOnClickListener {
            val otp = binding.etOtp.text.toString()
            Log.d(TAG, "Verify button clicked - OTP length: ${otp.length}")
            
            if (otp.length == 6) {
                verifyOtp(otp)
            } else {
                Log.w(TAG, "Invalid OTP length: ${otp.length}")
            }
        }
        
        binding.btnResend.setOnClickListener {
            Log.d(TAG, "Resend OTP clicked")
            resendOtp()
        }
    }
    
    private fun verifyOtp(otp: String) {
        Log.d(TAG, "verifyOtp: Starting verification for phone: $phone, OTP: $otp")
        lifecycleScope.launch {
            try {
                Log.d(TAG, "verifyOtp: Making API call")
                val request = mapOf("phone" to phone, "otp" to otp)
                val response = RetrofitClient.getAuthApi(prefs).loginWithOtp(request)
                
                Log.d(TAG, "verifyOtp: Response - Success: ${response.success}, Message: ${response.message}, Data: ${response.data}")
                
                if (response.success && response.data != null) {
                    Log.d(TAG, "verifyOtp: OTP verified successfully - UserId: ${response.data.userId}")
                    prefs.token = response.data.token
                    prefs.userId = response.data.userId
                    
                    Log.d(TAG, "verifyOtp: Token and userId saved, navigating to main")
                    startActivity(Intent(this@OtpActivity, MainActivity::class.java))
                    finish()
                } else {
                    Log.e(TAG, "verifyOtp: Verification failed - ${response.message}")
                    Toast.makeText(this@OtpActivity, response.message ?: "Invalid OTP", Toast.LENGTH_LONG).show()
                }
            } catch (e: retrofit2.HttpException) {
                Log.e(TAG, "verifyOtp: HTTP error ${e.code()}", e)
                val errorMsg = try {
                    val errorBody = e.response()?.errorBody()?.string()
                    val gson = com.google.gson.Gson()
                    val errorResponse = gson.fromJson(errorBody, com.dp.dawalo.data.remote.dto.ApiResponse::class.java)
                    errorResponse.message ?: "Server error"
                } catch (ex: Exception) {
                    "Server error: ${e.code()}"
                }
                Toast.makeText(this@OtpActivity, errorMsg, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, "verifyOtp: Exception occurred", e)
                Toast.makeText(this@OtpActivity, "Connection error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun resendOtp() {
        Log.d(TAG, "resendOtp: Resending OTP to $phone")
        lifecycleScope.launch {
            try {
                val request = mapOf("phone" to phone)
                val response = RetrofitClient.getAuthApi(prefs).sendOtp(request)
                Log.d(TAG, "resendOtp: Response - Success: ${response.success}, Message: ${response.message}")
            } catch (e: Exception) {
                Log.e(TAG, "resendOtp: Exception occurred", e)
            }
        }
    }
}
