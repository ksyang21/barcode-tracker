package com.example.barcodetracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.AnimationUtils
import android.widget.ImageView

class SplashScreen: AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val logo = findViewById<ImageView>(R.id.app_logo)
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        logo.startAnimation(fadeInAnimation)

        Handler().postDelayed({
            // This method will be executed once the timer is over
            startActivity(Intent(this, LoginActivity ::class.java))

            // close this activity_add_item
            finish()
        }, SPLASH_TIME_OUT)
    }

}