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
        binding.btnVerify.isEnabled = false
        
        lifecycleScope.launch {
            try {
                // Try backend first
                Log.d(TAG, "verifyOtp: Attempting backend verification")
                val request = mapOf("phone" to phone, "otp" to otp)
                val response = RetrofitClient.getAuthApi(prefs).loginWithOtp(request)
                
                Log.d(TAG, "verifyOtp: Response - Success: ${response.success}, Message: ${response.message}, Data: ${response.data}")
                
                if (response.success && response.data != null) {
                    Log.d(TAG, "verifyOtp: OTP verified successfully - UserId: ${response.data.userId}")
                    prefs.token = response.data.token
                    prefs.userId = response.data.userId
                    
                    Log.d(TAG, "verifyOtp: Token and userId saved, navigating to main")
                    Toast.makeText(this@OtpActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@OtpActivity, MainActivity::class.java))
                    finish()
                } else {
                    Log.e(TAG, "verifyOtp: Verification failed - ${response.message}")
                    Toast.makeText(this@OtpActivity, response.message ?: "Invalid OTP", Toast.LENGTH_LONG).show()
                    binding.btnVerify.isEnabled = true
                }
            } catch (e: java.net.ConnectException) {
                // Backend is down - use offline mode with test OTP
                Log.w(TAG, "verifyOtp: Backend unavailable, using offline mode")
                handleOfflineOtpVerification(otp)
            } catch (e: java.net.SocketTimeoutException) {
                Log.w(TAG, "verifyOtp: Backend timeout, using offline mode")
                handleOfflineOtpVerification(otp)
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
                binding.btnVerify.isEnabled = true
            } catch (e: Exception) {
                Log.e(TAG, "verifyOtp: Exception occurred, trying offline mode", e)
                handleOfflineOtpVerification(otp)
            }
        }
    }
    
    private suspend fun handleOfflineOtpVerification(otp: String) {
        try {
            // Test OTP for development: 123456
            if (otp == "123456") {
                Log.d(TAG, "Offline mode: Test OTP accepted")
                
                // Find user by phone in local DB
                val app = application as com.dp.dawalo.MedNutriTrackApp
                val user = app.database.userDao().getCurrentUser()
                
                if (user != null) {
                    prefs.userId = user.id
                    prefs.token = "offline_mode"
                    
                    Toast.makeText(this@OtpActivity, "Login successful (Offline Mode)", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@OtpActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@OtpActivity, "No user found. Please register first.", Toast.LENGTH_LONG).show()
                    binding.btnVerify.isEnabled = true
                }
            } else {
                Toast.makeText(this@OtpActivity, "Backend unavailable. Use test OTP: 123456", Toast.LENGTH_LONG).show()
                binding.btnVerify.isEnabled = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Offline verification failed", e)
            Toast.makeText(this@OtpActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            binding.btnVerify.isEnabled = true
        }
    }
    
    private fun resendOtp() {
        Log.d(TAG, "resendOtp: Resending OTP to $phone")
        binding.btnResend.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val request = mapOf("phone" to phone)
                val response = RetrofitClient.getAuthApi(prefs).sendOtp(request)
                Log.d(TAG, "resendOtp: Response - Success: ${response.success}, Message: ${response.message}")
                
                if (response.success) {
                    Toast.makeText(this@OtpActivity, "OTP resent! Check backend logs for new OTP", Toast.LENGTH_LONG).show()
                    // Re-enable after 30 seconds
                    binding.btnResend.postDelayed({
                        binding.btnResend.isEnabled = true
                    }, 30000)
                } else {
                    Toast.makeText(this@OtpActivity, "Failed to resend OTP", Toast.LENGTH_SHORT).show()
                    binding.btnResend.isEnabled = true
                }
            } catch (e: java.net.ConnectException) {
                Log.w(TAG, "resendOtp: Backend unavailable (offline mode)")
                Toast.makeText(this@OtpActivity, "Backend unavailable. Use test OTP: 123456", Toast.LENGTH_LONG).show()
                binding.btnResend.isEnabled = true
            } catch (e: java.net.SocketTimeoutException) {
                Log.w(TAG, "resendOtp: Backend timeout")
                Toast.makeText(this@OtpActivity, "Backend unavailable. Use test OTP: 123456", Toast.LENGTH_LONG).show()
                binding.btnResend.isEnabled = true
            } catch (e: Exception) {
                Log.e(TAG, "resendOtp: Exception occurred", e)
                Toast.makeText(this@OtpActivity, "Backend unavailable. Use test OTP: 123456", Toast.LENGTH_LONG).show()
                binding.btnResend.isEnabled = true
            }
        }
    }
}
