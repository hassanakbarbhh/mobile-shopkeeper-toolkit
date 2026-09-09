package com.shopkeeper.mobileshop.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shopkeeper.mobileshop.MainActivity
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.utils.UserAuthManager

class PendingApprovalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_approval)

        val btnCheckStatus = findViewById<Button>(R.id.btnCheckStatus)
        val btnSignOut = findViewById<Button>(R.id.btnSignOut)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            startActivity(Intent(this, LockScreenActivity::class.java))
            finish()
            return
        }

        tvStatus.text = "Account pending approval.\nEmail: ${user.email}"

        btnCheckStatus.setOnClickListener {
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists() && doc.getBoolean("approved") == true) {
                        val roleStr = doc.getString("role") ?: "BASIC_USER"
                        // Map and save to local
                        UserAuthManager.updateLocalRole(this, roleStr)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        tvStatus.text = "Still pending approval..."
                    }
                }
        }

        btnSignOut.setOnClickListener {
            UserAuthManager.signOut(this)
            startActivity(Intent(this, LockScreenActivity::class.java))
            finish()
        }
    }
}
