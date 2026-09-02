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

        updateSelection()

        binding.cardShopOwner.setOnClickListener {
            selectedMode = AppMode.SHOP_OWNER
            updateSelection()
        }

        binding.cardRepairTech.setOnClickListener {
            selectedMode = AppMode.REPAIR_TECH
            updateSelection()
        }

        binding.btnContinue.setOnClickListener {
            AppPreferences.saveMode(this, selectedMode, binding.cbRemember.isChecked)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun updateSelection() {
        val green500 = ContextCompat.getColor(this, R.color.green_500)
        val transparent = ContextCompat.getColor(this, android.R.color.transparent)

        if (selectedMode == AppMode.SHOP_OWNER) {
            binding.cardShopOwner.strokeWidth = 6
            binding.cardShopOwner.strokeColor = green500
            binding.cardRepairTech.strokeWidth = 0
            binding.cardRepairTech.strokeColor = transparent
        } else {
            binding.cardRepairTech.strokeWidth = 6
            binding.cardRepairTech.strokeColor = green500
            binding.cardShopOwner.strokeWidth = 0
            binding.cardShopOwner.strokeColor = transparent
        }
    }
}
