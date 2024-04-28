package com.example.smart_shoe

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private val SPLASH_DELAY: Long = 4000 // Splash screen delay in milliseconds (4 seconds)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay the transition to the main activity
        Handler().postDelayed({
            // Start main activity
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Close splash activity to prevent going back
        }, SPLASH_DELAY)
    }
}
