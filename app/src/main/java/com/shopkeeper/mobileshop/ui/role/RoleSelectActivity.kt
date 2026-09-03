package com.shopkeeper.mobileshop.ui.role

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.shopkeeper.mobileshop.MainActivity
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.databinding.ActivityRoleSelectBinding
import com.shopkeeper.mobileshop.utils.AppMode
import com.shopkeeper.mobileshop.utils.AppPreferences

class RoleSelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectBinding
    private var selectedMode: AppMode = AppMode.SHOP_OWNER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedMode = AppPreferences.getActiveMode(this)
        updateSelection()

        binding.cardShopOwner.setOnClickListener {
            selectedMode = AppMode.SHOP_OWNER
            updateSelection()
        }

        binding.cardSellerStaff.setOnClickListener {
            selectedMode = AppMode.SELLER_STAFF
            updateSelection()
        }

        binding.cardRepairTech.setOnClickListener {
            selectedMode = AppMode.REPAIR_TECH
            updateSelection()
        }

        binding.btnContinue.setOnClickListener {
            AppPreferences.saveMode(this, selectedMode, binding.cbRemember.isChecked)
            AppPreferences.setActiveMode(this, selectedMode)
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun updateSelection() {
        val green500 = ContextCompat.getColor(this, R.color.green_500)
        val transparent = ContextCompat.getColor(this, android.R.color.transparent)

        binding.cardShopOwner.strokeWidth = if (selectedMode == AppMode.SHOP_OWNER) 6 else 0
        binding.cardShopOwner.strokeColor = if (selectedMode == AppMode.SHOP_OWNER) green500 else transparent

        binding.cardSellerStaff.strokeWidth = if (selectedMode == AppMode.SELLER_STAFF) 6 else 0
        binding.cardSellerStaff.strokeColor = if (selectedMode == AppMode.SELLER_STAFF) green500 else transparent

        binding.cardRepairTech.strokeWidth = if (selectedMode == AppMode.REPAIR_TECH) 6 else 0
        binding.cardRepairTech.strokeColor = if (selectedMode == AppMode.REPAIR_TECH) green500 else transparent
    }
}
