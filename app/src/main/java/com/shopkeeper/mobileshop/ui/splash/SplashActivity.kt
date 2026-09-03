package com.shopkeeper.mobileshop.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.shopkeeper.mobileshop.MainActivity
import com.shopkeeper.mobileshop.databinding.ActivitySplashBinding
import com.shopkeeper.mobileshop.ui.auth.LockScreenActivity
import com.shopkeeper.mobileshop.ui.role.RoleSelectActivity
import com.shopkeeper.mobileshop.utils.AppPreferences

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)

            Handler(Looper.getMainLooper()).postDelayed({
                navigateToNext()
            }, 1200)
        } catch (e: Exception) {
            // Safe fallback
            navigateToNext()
        }
    }

    private fun navigateToNext() {
        if (isFinishing || isDestroyed) return
        try {
            // Master security login interface (Key: Hassanisgreat)
            startActivity(Intent(this, LockScreenActivity::class.java))
        } catch (e: Exception) {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}
