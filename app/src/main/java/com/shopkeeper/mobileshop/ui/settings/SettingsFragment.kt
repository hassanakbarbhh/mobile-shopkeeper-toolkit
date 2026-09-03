package com.shopkeeper.mobileshop.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.DialogChangePasswordBinding
import com.shopkeeper.mobileshop.databinding.FragmentSettingsBinding
import com.shopkeeper.mobileshop.sync.GitHubSyncManager
import com.shopkeeper.mobileshop.sync.SyncState
import com.shopkeeper.mobileshop.ui.role.RoleSelectActivity
import com.shopkeeper.mobileshop.utils.AppPreferences
import com.shopkeeper.mobileshop.utils.CurrencyManager
import com.shopkeeper.mobileshop.utils.ExportManager
import com.shopkeeper.mobileshop.utils.PasswordManager
import com.shopkeeper.mobileshop.utils.ShopProfile
import com.shopkeeper.mobileshop.utils.money
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        repository = ShopRepository(db)

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

        // Export Center Click Listeners with CSV & PDF Options
        binding.btnExportInventorySettings.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val list = repository.allProducts.first()
                if (list.isEmpty()) {
                    Toast.makeText(requireContext(), "No products to export", Toast.LENGTH_SHORT).show()
                } else {
                    ExportManager.showLedgerExportDialog(
                        context = requireContext(),
                        ledgerTitle = "Inventory & Stock Ledger",
                        onExportCsv = { ExportManager.exportProductsCsv(requireContext(), list) },
                        onExportPdf = { ExportManager.exportProductsPdf(requireContext(), list) }
                    )
                }
            }
        }

        binding.btnExportSalesSettings.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val list = repository.allSales.first()
                if (list.isEmpty()) {
                    Toast.makeText(requireContext(), "No sales to export", Toast.LENGTH_SHORT).show()
                } else {
                    ExportManager.showLedgerExportDialog(
                        context = requireContext(),
                        ledgerTitle = "Sales & Invoices Ledger",
                        onExportCsv = { ExportManager.exportSalesCsv(requireContext(), list) },
                        onExportPdf = { ExportManager.exportSalesPdf(requireContext(), list) }
                    )
                }
            }
        }

        binding.btnExportCustomersSettings.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val list = repository.allCustomers.first()
                if (list.isEmpty()) {
                    Toast.makeText(requireContext(), "No customers to export", Toast.LENGTH_SHORT).show()
                } else {
                    ExportManager.showLedgerExportDialog(
                        context = requireContext(),
                        ledgerTitle = "Customers Ledger",
                        onExportCsv = { ExportManager.exportCustomersCsv(requireContext(), list) },
                        onExportPdf = { ExportManager.exportCustomersPdf(requireContext(), list) }
                    )
                }
            }
        }

        binding.btnExportPurchasesSettings.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val list = repository.allPurchases.first()
                if (list.isEmpty()) {
                    Toast.makeText(requireContext(), "No purchases to export", Toast.LENGTH_SHORT).show()
                } else {
                    ExportManager.showLedgerExportDialog(
                        context = requireContext(),
                        ledgerTitle = "Daily Purchases Ledger",
                        onExportCsv = { ExportManager.exportPurchasesCsv(requireContext(), list) },
                        onExportPdf = { ExportManager.exportPurchasesPdf(requireContext(), list) }
                    )
                }
            }
        }

        binding.btnExportRepairsSettings.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val list = repository.allRepairs.first()
                if (list.isEmpty()) {
                    Toast.makeText(requireContext(), "No repairs to export", Toast.LENGTH_SHORT).show()
                } else {
                    ExportManager.showLedgerExportDialog(
                        context = requireContext(),
                        ledgerTitle = "Repair Tickets Ledger",
                        onExportCsv = { ExportManager.exportRepairsCsv(requireContext(), list) },
                        onExportPdf = { ExportManager.exportRepairsPdf(requireContext(), list) }
                    )
                }
            }
        }

        binding.btnExportMasterLedgerSettings.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val prods = repository.allProducts.first()
                val sales = repository.allSales.first()
                val purchases = repository.allPurchases.first()
                val customers = repository.allCustomers.first()
                val repairs = repository.allRepairs.first()

                val options = arrayOf(
                    "📄 Open Master Shop PDF Report",
                    "📤 Share Master Shop PDF Document"
                )
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Consolidated Master Shop Ledger")
                    .setItems(options) { _, which ->
                        val pdfFile = ExportManager.exportMasterLedgerPdf(
                            requireContext(), prods, sales, purchases, customers, repairs
                        )
                        if (which == 0) {
                            ExportManager.openFile(requireContext(), pdfFile, "application/pdf")
                        } else {
                            ExportManager.shareFile(requireContext(), pdfFile, "application/pdf", "Complete Shop Master Ledger")
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        binding.btnResetDefaultStock.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Stock & Clean Customer Data?")
                .setMessage("This will remove sample dummy customer/stock entries and seed all phone brands and models into inventory with No Price and 0 stock, as requested.")
                .setPositiveButton("Reset Now") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        val db = AppDatabase.getDatabase(requireContext())
                        db.customerDao().deleteAllCustomers()
                        db.productDao().deleteAllProducts()
                        val defaultPhoneProducts = com.shopkeeper.mobileshop.data.catalog.OnlineCatalogRepository.allOnlineModels.map {
                            it.toProductNoPrice()
                        }
                        db.productDao().insertAll(defaultPhoneProducts)
                        Toast.makeText(requireContext(), "Stock reset! All ${defaultPhoneProducts.size} phone models added with no price.", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnLockAppNow.setOnClickListener {
            val intent = Intent(requireContext(), com.shopkeeper.mobileshop.ui.auth.LockScreenActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.btnManageSellersSettings.setOnClickListener {
            findNavController().navigate(R.id.navigation_sellers)
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
