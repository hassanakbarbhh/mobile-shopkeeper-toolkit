package com.shopkeeper.mobileshop.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.databinding.DialogChangePasswordBinding
import com.shopkeeper.mobileshop.databinding.FragmentSettingsBinding
import com.shopkeeper.mobileshop.ui.role.RoleSelectActivity
import com.shopkeeper.mobileshop.utils.AppPreferences
import com.shopkeeper.mobileshop.utils.PasswordManager
import com.shopkeeper.mobileshop.utils.ShopProfile

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etShopName.setText(ShopProfile.name(requireContext()))
        binding.etShopPhone.setText(ShopProfile.phone(requireContext()))
        binding.etShopAddress.setText(ShopProfile.address(requireContext()))

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etShopName.text.toString().trim()
            val phone = binding.etShopPhone.text.toString().trim()
            val addr = binding.etShopAddress.text.toString().trim()
            ShopProfile.save(requireContext(), name, phone, addr)
            Toast.makeText(requireContext(), "Shop profile saved!", Toast.LENGTH_SHORT).show()
        }

        binding.btnChangePassword.setOnClickListener { showChangePasswordDialog() }

        binding.btnSwitchRole.setOnClickListener {
            AppPreferences.clearMode(requireContext())
            startActivity(Intent(requireContext(), RoleSelectActivity::class.java))
            requireActivity().finish()
        }

        binding.btnAbout.setOnClickListener {
            findNavController().navigate(R.id.navigation_about)
        }
    }

    private fun showChangePasswordDialog() {
        val dBinding = DialogChangePasswordBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Lock Password")
            .setView(dBinding.root)
            .setPositiveButton("Change") { _, _ ->
                val curr = dBinding.etCurrentPassword.text.toString()
                val newP = dBinding.etNewPassword.text.toString()
                if (PasswordManager.changePassword(requireContext(), curr, newP)) {
                    Toast.makeText(requireContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Incorrect current password", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
