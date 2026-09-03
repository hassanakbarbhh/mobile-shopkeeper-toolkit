package com.shopkeeper.mobileshop.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.catalog.OnlineCatalogRepository
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.PaymentMethod
import com.shopkeeper.mobileshop.data.db.entity.Repair
import com.shopkeeper.mobileshop.data.db.entity.RepairStatus
import com.shopkeeper.mobileshop.data.db.entity.Sale
import com.shopkeeper.mobileshop.data.db.entity.SaleItem
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.DialogManageRoleKeysBinding
import com.shopkeeper.mobileshop.databinding.FragmentSettingsBinding
import com.shopkeeper.mobileshop.sync.GitHubSyncManager
import com.shopkeeper.mobileshop.sync.SyncState
import com.shopkeeper.mobileshop.ui.auth.LockScreenActivity
import com.shopkeeper.mobileshop.ui.role.RoleSelectActivity
import com.shopkeeper.mobileshop.utils.AppMode
import com.shopkeeper.mobileshop.utils.AppPreferences
import com.shopkeeper.mobileshop.utils.CurrencyManager
import com.shopkeeper.mobileshop.utils.ExportManager
import com.shopkeeper.mobileshop.utils.ImportManager
import com.shopkeeper.mobileshop.utils.PasswordManager
import com.shopkeeper.mobileshop.utils.ShopProfile
import com.shopkeeper.mobileshop.utils.ThermalPrintHelper
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
                        ledgerTitle = "Repairs & Workshop Ledger",
                        onExportCsv = { ExportManager.exportRepairsCsv(requireContext(), list) },
                        onExportPdf = { ExportManager.exportRepairsPdf(requireContext(), list) }
                    )
                }
            }
        }

        binding.btnExportMasterLedgerSettings.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val p = repository.allProducts.first()
                val s = repository.allSales.first()
                val r = repository.allRepairs.first()
                val c = repository.allCustomers.first()
                val pur = repository.allPurchases.first()
                val file = ExportManager.exportMasterLedgerPdf(requireContext(), p, s, pur, c, r)
                ExportManager.openFile(requireContext(), file, "application/pdf")
            }
        }

        // CSV Import & Export
        binding.btnImportCsvSettings.setOnClickListener {
            ImportManager.showImportDialog(requireContext(), repository) {
                Toast.makeText(requireContext(), "Data refreshed successfully!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnExportAllCsvSettings.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val products = repository.allProducts.first()
                ExportManager.exportProductsCsv(requireContext(), products)
            }
        }

        // Thermal Receipt Testing
        binding.btnTestThermalPrint.setOnClickListener {
            val dummySale = Sale(
                id = 42,
                customerName = "Ali Raza Khan",
                totalAmount = 86600.0,
                discount = 1000.0,
                finalAmount = 85600.0,
                paymentMethod = PaymentMethod.CASH,
                sellerName = "Ahmed (Counter)",
                saleDate = System.currentTimeMillis()
            )
            val dummyItems = listOf(
                SaleItem(saleId = 42, productId = 1, productName = "Samsung Galaxy A54", quantity = 1, unitPrice = 85000.0, totalPrice = 85000.0, imei = "354892019283741"),
                SaleItem(saleId = 42, productId = 2, productName = "Type-C Fast Cable", quantity = 2, unitPrice = 800.0, totalPrice = 1600.0)
            )
            ThermalPrintHelper.showSaleReceiptDialog(requireContext(), dummySale, dummyItems)
        }

        binding.btnTestRepairTag.setOnClickListener {
            val dummyRepair = Repair(
                id = 15,
                customerName = "Usman Tariq",
                customerPhone = "0312-9876543",
                deviceBrand = "Apple",
                deviceModel = "iPhone 13 Pro (Blue)",
                imei = "352910293847561",
                issueDescription = "Screen glass cracked & battery draining fast",
                notes = "Passcode: 4892. Test cameras after screen replacement.",
                estimatedCost = 14500.0,
                actualCost = 5000.0,
                status = RepairStatus.IN_REPAIR,
                receivedDate = System.currentTimeMillis()
            )
            ThermalPrintHelper.showRepairTagDialog(requireContext(), dummyRepair)
        }

        // GitHub Sync Controls
        binding.btnTestSyncSuccess.setOnClickListener {
            GitHubSyncManager.setTestState(SyncState.Synced(System.currentTimeMillis()))
            Toast.makeText(requireContext(), "Sync status marked as Synced", Toast.LENGTH_SHORT).show()
        }

        binding.btnTestSyncPending.setOnClickListener {
            GitHubSyncManager.setTestState(SyncState.Pending("Changes pending sync...", 3))
            Toast.makeText(requireContext(), "Sync status marked as Changes Pending", Toast.LENGTH_SHORT).show()
        }

        binding.btnTestSyncError.setOnClickListener {
            GitHubSyncManager.setTestState(SyncState.Error("Simulated sync error", System.currentTimeMillis()))
            Toast.makeText(requireContext(), "Sync status marked as Error", Toast.LENGTH_SHORT).show()
        }

        binding.btnConfigureRepo.setOnClickListener {
            showConfigureRepoDialog()
        }

        binding.btnResetDefaultStock.setOnClickListener {
            showResetStockConfirmation()
        }

        binding.btnManageSellersSettings.setOnClickListener {
            findNavController().navigate(R.id.navigation_sellers)
        }

        binding.btnLockAppNow.setOnClickListener {
            val intent = Intent(requireContext(), LockScreenActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
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

    private fun showResetStockConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reset Catalog & Clean Customers?")
            .setMessage("This will wipe customer list and reset all phone models in stock with quantity 0 and empty prices so you can enter your actual shop pricing.")
            .setPositiveButton("Reset Now") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val db = AppDatabase.getDatabase(requireContext())
                    db.customerDao().deleteAllCustomers()
                    db.productDao().deleteAllProducts()
                    val defaultPhoneProducts = OnlineCatalogRepository.allOnlineModels.map {
                        it.toProductNoPrice()
                    }
                    db.productDao().insertAll(defaultPhoneProducts)
                    Toast.makeText(requireContext(), "Stock catalog reset! Ready for custom pricing.", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
        val dBinding = DialogManageRoleKeysBinding.inflate(layoutInflater)
        var targetRole = AppMode.SHOP_OWNER

        fun updateRoleInfo() {
            when (targetRole) {
                AppMode.SHOP_OWNER -> {
                    dBinding.toggleKeyRole.check(R.id.btnKeyOwner)
                    dBinding.tvKeyRoleSubtitle.text = "Modifying key for Shop Owner (Default: Hassanisgreat)"
                }
                AppMode.SELLER_STAFF -> {
                    dBinding.toggleKeyRole.check(R.id.btnKeySeller)
                    dBinding.tvKeyRoleSubtitle.text = "Modifying key for Seller / Staff (Default: seller123)"
                }
                AppMode.REPAIR_TECH -> {
                    dBinding.toggleKeyRole.check(R.id.btnKeyTech)
                    dBinding.tvKeyRoleSubtitle.text = "Modifying key for Repair Tech (Default: repair123)"
                }
            }
        }

        updateRoleInfo()

        dBinding.toggleKeyRole.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                targetRole = when (checkedId) {
                    R.id.btnKeyOwner -> AppMode.SHOP_OWNER
                    R.id.btnKeySeller -> AppMode.SELLER_STAFF
                    else -> AppMode.REPAIR_TECH
                }
                updateRoleInfo()
            }
        }

        var dialog: AlertDialog? = null

        dBinding.btnResetKeys.setOnClickListener {
            PasswordManager.resetToDefaults(requireContext())
            Toast.makeText(requireContext(), "All keys reset to defaults (Hassanisgreat, seller123, repair123)!", Toast.LENGTH_LONG).show()
            dialog?.dismiss()
        }

        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Manage Role Access Keys")
            .setView(dBinding.root)
            .setPositiveButton("Save Key") { _, _ ->
                val newKey = dBinding.etNewRoleKey.text?.toString().orEmpty().trim()
                if (newKey.length < 3) {
                    Toast.makeText(requireContext(), "Key must be at least 3 characters", Toast.LENGTH_SHORT).show()
                } else {
                    PasswordManager.changeKeyForMode(requireContext(), targetRole, newKey)
                    Toast.makeText(requireContext(), "Access key for ${targetRole.displayName} updated!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
