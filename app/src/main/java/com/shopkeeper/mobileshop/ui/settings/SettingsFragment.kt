package com.shopkeeper.mobileshop.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.databinding.DialogChangePasswordBinding
import com.shopkeeper.mobileshop.databinding.FragmentSettingsBinding
import com.shopkeeper.mobileshop.sync.GitHubSyncManager
import com.shopkeeper.mobileshop.sync.SyncState
import com.shopkeeper.mobileshop.ui.role.RoleSelectActivity
import com.shopkeeper.mobileshop.utils.AppPreferences
import com.shopkeeper.mobileshop.utils.CurrencyManager
import com.shopkeeper.mobileshop.utils.PasswordManager
import com.shopkeeper.mobileshop.utils.ShopProfile
import com.shopkeeper.mobileshop.utils.money

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

        updateCurrencyDisplay()

        binding.btnSelectCurrency.setOnClickListener { showCurrencySelectionDialog() }

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etShopName.text.toString().trim()
            val phone = binding.etShopPhone.text.toString().trim()
            val addr = binding.etShopAddress.text.toString().trim()
            ShopProfile.save(requireContext(), name, phone, addr)
            Toast.makeText(requireContext(), "Shop profile saved!", Toast.LENGTH_SHORT).show()
        }

        // GitHub Sync Controls
        binding.btnTestSyncSuccess.setOnClickListener {
            GitHubSyncManager.setTestState(
                com.shopkeeper.mobileshop.sync.SyncState.Synced(
                    lastSyncTimestamp = System.currentTimeMillis(),
                    commitSha = "7e4b9a1",
                    syncedItemsCount = 28
                )
            )
            Toast.makeText(requireContext(), "Sync State set to: Synced", Toast.LENGTH_SHORT).show()
        }

        binding.btnTestSyncPending.setOnClickListener {
            GitHubSyncManager.setTestState(
                com.shopkeeper.mobileshop.sync.SyncState.Pending(
                    message = "Syncing local Room entities to GitHub repository...",
                    pendingCount = 6
                )
            )
            Toast.makeText(requireContext(), "Sync State set to: Pending", Toast.LENGTH_SHORT).show()
        }

        binding.btnTestSyncError.setOnClickListener {
            GitHubSyncManager.setTestState(
                com.shopkeeper.mobileshop.sync.SyncState.Error(
                    errorMessage = "GitHub API 403 / Network Timeout: Remote branch protection rejected commit without authentication token",
                    canRetry = true
                )
            )
            Toast.makeText(requireContext(), "Sync State set to: Error", Toast.LENGTH_SHORT).show()
        }

        binding.btnConfigureRepo.setOnClickListener {
            showConfigureRepoDialog()
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

    private fun updateCurrencyDisplay() {
        val currentCode = CurrencyManager.getCode(requireContext())
        val currentSymbol = CurrencyManager.getSymbol(requireContext())
        val matched = CurrencyManager.CURRENCIES.find { it.code == currentCode }
        val label = matched?.label ?: "Custom ($currentSymbol)"
        binding.tvCurrentCurrency.text = label
        binding.tvCurrencySymbolPreview.text = "Active symbol: $currentSymbol (Preview: ${45000.0.money()})"
    }

    private fun showCurrencySelectionDialog() {
        val options = CurrencyManager.CURRENCIES.map { it.label }.toTypedArray()
        val currentCode = CurrencyManager.getCode(requireContext())
        var selectedIndex = CurrencyManager.CURRENCIES.indexOfFirst { it.code == currentCode }
        if (selectedIndex == -1) selectedIndex = 0

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Currency (Default: PKR)")
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                val selected = CurrencyManager.CURRENCIES[which]
                dialog.dismiss()
                if (selected.code == "CUSTOM") {
                    showCustomCurrencyDialog()
                } else {
                    CurrencyManager.setCurrency(requireContext(), selected.code, selected.symbol)
                    updateCurrencyDisplay()
                    Toast.makeText(requireContext(), "Currency updated to ${selected.code} (${selected.symbol})", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomCurrencyDialog() {
        val input = EditText(requireContext()).apply {
            hint = "e.g. Rs, $, €, £, AED, SAR, ₨"
            setPadding(50, 40, 50, 40)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Enter Custom Currency Symbol")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val symbol = input.text.toString().trim()
                if (symbol.isNotEmpty()) {
                    CurrencyManager.setCurrency(requireContext(), "CUSTOM", symbol)
                    updateCurrencyDisplay()
                    Toast.makeText(requireContext(), "Custom currency symbol set to $symbol", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showConfigureRepoDialog() {
        val currentRepo = GitHubSyncManager.getRepoName(requireContext())
        val input = EditText(requireContext()).apply {
            setText(currentRepo)
            hint = "username/repository-name"
            setPadding(50, 40, 50, 40)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Configure GitHub Repository")
            .setMessage("Set the target remote GitHub repository to synchronize local Room database state:")
            .setView(input)
            .setPositiveButton("Save & Sync") { _, _ ->
                val repo = input.text.toString().trim()
                if (repo.isNotEmpty()) {
                    GitHubSyncManager.setRepoName(requireContext(), repo)
                    GitHubSyncManager.triggerSync(requireContext())
                    Toast.makeText(requireContext(), "Target repository updated to $repo", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
