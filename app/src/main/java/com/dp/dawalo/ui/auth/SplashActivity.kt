package com.dp.dawalo.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.FontAssetDelegate
import com.dp.dawalo.MainActivity
import com.dp.dawalo.databinding.ActivitySplashBinding
import com.dp.dawalo.utils.PreferenceManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lottieAnimation.setFontAssetDelegate(object : FontAssetDelegate() {
            override fun getFontPath(fontFamily: String?): String? = null
            override fun fetchFont(fontFamily: String?): Typeface = Typeface.DEFAULT
        })

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = PreferenceManager(this)
            val next = if (prefs.isLoggedIn()) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(next)
            finish()
        }, 2500)
    }
}
