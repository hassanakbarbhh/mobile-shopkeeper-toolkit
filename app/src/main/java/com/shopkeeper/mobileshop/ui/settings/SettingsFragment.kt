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
import com.shopkeeper.mobileshop.databinding.DialogGoogleFirebaseAuthBinding
import com.shopkeeper.mobileshop.databinding.DialogManageRoleKeysBinding
import com.shopkeeper.mobileshop.databinding.FragmentSettingsBinding

import com.shopkeeper.mobileshop.sync.SyncState
import com.shopkeeper.mobileshop.ui.auth.LockScreenActivity
import com.shopkeeper.mobileshop.ui.role.RoleSelectActivity
import com.shopkeeper.mobileshop.utils.AppMode
import com.shopkeeper.mobileshop.utils.AppPreferences
import com.shopkeeper.mobileshop.utils.CurrencyManager
import com.shopkeeper.mobileshop.utils.ExportManager
import com.shopkeeper.mobileshop.utils.GoogleAuthManager
import com.shopkeeper.mobileshop.utils.ImportManager
import com.shopkeeper.mobileshop.utils.PasswordManager
import com.shopkeeper.mobileshop.utils.ShopProfile
import com.shopkeeper.mobileshop.utils.ThermalPrintHelper
import com.shopkeeper.mobileshop.utils.ThemeManager
import com.shopkeeper.mobileshop.utils.AppTheme
import com.shopkeeper.mobileshop.utils.NightModeOption

import com.shopkeeper.mobileshop.utils.FirebaseAuthDiagnosticHelper
import com.shopkeeper.mobileshop.databinding.DialogThemePickerBinding

import com.shopkeeper.mobileshop.databinding.DialogFirebaseAuthCheckBinding
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import com.shopkeeper.mobileshop.utils.DiagnosticStatus
import com.shopkeeper.mobileshop.utils.money
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import com.shopkeeper.mobileshop.utils.DatabaseBackupManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository

    private val backupLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val success = DatabaseBackupManager.performBackup(requireContext(), uri)
                    if (success) {
                        Toast.makeText(requireContext(), "Database backup saved successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to save backup", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val restoreLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val success = DatabaseBackupManager.performRestore(requireContext(), uri)
                    if (success) {
                        Toast.makeText(requireContext(), "Database restored successfully! Please restart the app.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to restore database", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

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
        updateThemeDisplay()

        binding.btnSelectCurrency.setOnClickListener { showCurrencySelectionDialog() }
        binding.btnSelectTheme.setOnClickListener { showThemePickerDialog() }

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etShopName.text.toString().trim()
            val phone = binding.etShopPhone.text.toString().trim()
            val addr = binding.etShopAddress.text.toString().trim()
            ShopProfile.save(requireContext(), name, phone, addr)
            Toast.makeText(requireContext(), "Shop profile saved!", Toast.LENGTH_SHORT).show()
        }

        binding.btnBackupDatabase.setOnClickListener {
            val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_TITLE, "MobileShop_Backup_$dateStr.db")
            }
            backupLauncher.launch(intent)
        }

        binding.btnRestoreDatabase.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            restoreLauncher.launch(intent)
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

        binding.btnCustomThermalPrint.setOnClickListener {
            ThermalPrintHelper.showCustomThermalPrintDialog(requireContext())
        }

        binding.btnThermalCustomOptions.setOnClickListener {
            ThermalPrintHelper.showThermalCustomOptionsDialog(requireContext())
        }

        binding.btnResetDefaultStock.setOnClickListener {
            showResetStockConfirmation()
        }

        binding.btnManageSellersSettings.setOnClickListener {
            findNavController().navigate(R.id.navigation_sellers)
        }

        updateGoogleAuthCard()
        binding.btnManageGoogleAuth.setOnClickListener {
            showGoogleAuthManagementDialog()
        }

        binding.btnCheckFirebaseAuth.setOnClickListener {
            showFirebaseAuthDiagnosticDialog()
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

    private fun showChangePasswordDialog() {
        val dBinding = DialogManageRoleKeysBinding.inflate(layoutInflater)
        var targetRole = AppMode.SHOP_OWNER

        fun updateRoleInfo() {
            when (targetRole) {
                AppMode.SHOP_OWNER -> {
                    dBinding.toggleKeyRole.check(R.id.btnKeyOwner)
                    dBinding.tvKeyRoleSubtitle.text = "Modifying security key for Shop Owner"
                }
                AppMode.SELLER_STAFF -> {
                    dBinding.toggleKeyRole.check(R.id.btnKeySeller)
                    dBinding.tvKeyRoleSubtitle.text = "Modifying security key for Seller / Staff"
                }
                AppMode.REPAIR_TECH -> {
                    dBinding.toggleKeyRole.check(R.id.btnKeyTech)
                    dBinding.tvKeyRoleSubtitle.text = "Modifying security key for Repair Tech"
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
            Toast.makeText(requireContext(), "All keys have been reset to factory defaults securely.", Toast.LENGTH_LONG).show()
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

    private fun updateGoogleAuthCard() {
        val ownerEmail = GoogleAuthManager.getOwnerEmail(requireContext())
        val user = GoogleAuthManager.getAuthenticatedUser(requireContext())

        if (user != null) {
            binding.tvSettingsGoogleAccount.text = "Logged In: ${user.email}"
            if (user.isOwner) {
                binding.tvSettingsGoogleSubtitle.text = "👑 Verified Shop Owner • Full Master Access Active"
            } else {
                binding.tvSettingsGoogleSubtitle.text = "💼 Verified Staff Account (Owner: $ownerEmail)"
            }
        } else {
            binding.tvSettingsGoogleAccount.text = "Owner: $ownerEmail"
            binding.tvSettingsGoogleSubtitle.text = "🛡️ Verified Google Identity • Anti-Leak Owner Protection"
        }
    }

    private fun showGoogleAuthManagementDialog() {
        val dBinding = DialogGoogleFirebaseAuthBinding.inflate(layoutInflater)
        val ownerEmail = GoogleAuthManager.getOwnerEmail(requireContext())
        val currentUser = GoogleAuthManager.getAuthenticatedUser(requireContext())

        dBinding.etAuthorizedOwnerEmail.setText(ownerEmail)

        if (currentUser != null) {
            dBinding.tvDialogCurrentGoogleEmail.text = currentUser.email
            if (currentUser.isOwner) {
                dBinding.tvDialogCurrentGoogleStatus.text = "👑 Verified Shop Owner"
                dBinding.tvDialogCurrentGoogleStatus.setBackgroundResource(R.drawable.bg_badge_green)
            } else {
                dBinding.tvDialogCurrentGoogleStatus.text = "💼 Verified Staff Account"
                dBinding.tvDialogCurrentGoogleStatus.setBackgroundResource(R.drawable.bg_badge_blue)
            }
            dBinding.btnDialogSignOutGoogle.text = "Sign Out Google Account"
            dBinding.btnDialogSignOutGoogle.isEnabled = true
        } else {
            dBinding.tvDialogCurrentGoogleEmail.text = "Not signed in yet (Default: $ownerEmail)"
            dBinding.tvDialogCurrentGoogleStatus.text = "🔒 Protection Active"
            dBinding.btnDialogSignOutGoogle.text = "Not Signed In"
            dBinding.btnDialogSignOutGoogle.isEnabled = false
        }

        val isFbInit = GoogleAuthManager.isFirebaseInitialized(requireContext())
        if (isFbInit) {
            dBinding.tvFirebaseStatusDetails.text = "• Firebase Cloud: Connected & Active (Spark Free Tier)\n• Google Sign-In: 100% Free forever\n• Firebase Authentication: Unlimited free logins\n• No credit card or paid plan required."
        } else {
            dBinding.tvFirebaseStatusDetails.text = "• Google Identity Services: Active & 100% Free\n• Firebase Spark Plan: 100% Free Forever\n• Anti-Leak Guarantee: Owner mode requires verified Gmail\n• To connect cloud Firestore, place free google-services.json in /app"
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Google & Firebase Login")
            .setView(dBinding.root)
            .setNegativeButton("Close", null)
            .create()

        dBinding.btnDialogSignOutGoogle.setOnClickListener {
            GoogleAuthManager.signOut(requireContext()) {
                Toast.makeText(requireContext(), "Signed out from Google account", Toast.LENGTH_SHORT).show()
                updateGoogleAuthCard()
                dialog.dismiss()
            }
        }

        dBinding.btnDialogSaveOwnerEmail.setOnClickListener {
            val newEmail = dBinding.etAuthorizedOwnerEmail.text?.toString().orEmpty().trim()
            if (newEmail.contains("@") && newEmail.contains(".")) {
                GoogleAuthManager.setOwnerEmail(requireContext(), newEmail)
                Toast.makeText(requireContext(), "Authorized Owner set to $newEmail", Toast.LENGTH_SHORT).show()
                updateGoogleAuthCard()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter a valid Gmail / email address", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun updateThemeDisplay() {
        val currentTheme = ThemeManager.getCurrentTheme(requireContext())
        binding.tvCurrentThemeName.text = "${currentTheme.badgeIcon} ${currentTheme.title} (Active)"
        binding.tvCurrentThemeDetails.text = "${currentTheme.subtitle} • 6 Trending Themes Available"
    }

    private fun showThemePickerDialog() {
        val dBinding = DialogThemePickerBinding.inflate(layoutInflater)
        val currentTheme = ThemeManager.getCurrentTheme(requireContext())
        val currentNight = ThemeManager.getCurrentNightMode(requireContext())

        // Set night mode toggle
        when (currentNight) {
            NightModeOption.SYSTEM -> dBinding.toggleNightMode.check(R.id.btnModeSystem)
            NightModeOption.LIGHT -> dBinding.toggleNightMode.check(R.id.btnModeLight)
            NightModeOption.DARK -> dBinding.toggleNightMode.check(R.id.btnModeDark)
        }

        dBinding.toggleNightMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val mode = when (checkedId) {
                    R.id.btnModeLight -> NightModeOption.LIGHT
                    R.id.btnModeDark -> NightModeOption.DARK
                    else -> NightModeOption.SYSTEM
                }
                ThemeManager.setNightMode(requireContext(), mode)
            }
        }

        // Set radio button selection
        when (currentTheme) {
            AppTheme.EMERALD -> dBinding.rbThemeEmerald.isChecked = true
            AppTheme.MIDNIGHT -> dBinding.rbThemeMidnight.isChecked = true
            AppTheme.SAPPHIRE -> dBinding.rbThemeSapphire.isChecked = true
            AppTheme.SUNSET -> dBinding.rbThemeSunset.isChecked = true
            AppTheme.PURPLE -> dBinding.rbThemePurple.isChecked = true
            AppTheme.SLATE -> dBinding.rbThemeSlate.isChecked = true
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dBinding.root)
            .create()

        val selectTheme = { theme: AppTheme ->
            ThemeManager.setTheme(requireActivity(), theme)
            updateThemeDisplay()
            dialog.dismiss()
        }

        dBinding.cardThemeEmerald.setOnClickListener { selectTheme(AppTheme.EMERALD) }
        dBinding.rbThemeEmerald.setOnClickListener { selectTheme(AppTheme.EMERALD) }

        dBinding.cardThemeMidnight.setOnClickListener { selectTheme(AppTheme.MIDNIGHT) }
        dBinding.rbThemeMidnight.setOnClickListener { selectTheme(AppTheme.MIDNIGHT) }

        dBinding.cardThemeSapphire.setOnClickListener { selectTheme(AppTheme.SAPPHIRE) }
        dBinding.rbThemeSapphire.setOnClickListener { selectTheme(AppTheme.SAPPHIRE) }

        dBinding.cardThemeSunset.setOnClickListener { selectTheme(AppTheme.SUNSET) }
        dBinding.rbThemeSunset.setOnClickListener { selectTheme(AppTheme.SUNSET) }

        dBinding.cardThemePurple.setOnClickListener { selectTheme(AppTheme.PURPLE) }
        dBinding.rbThemePurple.setOnClickListener { selectTheme(AppTheme.PURPLE) }

        dBinding.cardThemeSlate.setOnClickListener { selectTheme(AppTheme.SLATE) }
        dBinding.rbThemeSlate.setOnClickListener { selectTheme(AppTheme.SLATE) }

        dBinding.btnCloseThemePicker.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showFirebaseAuthDiagnosticDialog() {
        val dBinding = DialogFirebaseAuthCheckBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dBinding.root)
            .create()

        fun runDiagnostic() {
            dBinding.progressDiagnostic.visibility = View.VISIBLE
            dBinding.containerDiagnosticSteps.removeAllViews()

            viewLifecycleOwner.lifecycleScope.launch {
                val report = FirebaseAuthDiagnosticHelper.runFullDiagnostic(requireContext())
                dBinding.progressDiagnostic.visibility = View.GONE

                dBinding.containerDiagnosticSteps.removeAllViews()
                for (step in report.steps) {
                    val row = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 10, 0, 10)
                    }
                    val iconView = TextView(requireContext()).apply {
                        text = when (step.status) {
                            DiagnosticStatus.PASS -> "✅"
                            DiagnosticStatus.WARN -> "⚠️"
                            DiagnosticStatus.FAIL -> "❌"
                            DiagnosticStatus.INFO -> "ℹ️"
                        }
                        textSize = 16f
                    }
                    val textLayout = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                            marginStart = 16
                        }
                    }
                    val titleView = TextView(requireContext()).apply {
                        text = step.name
                        textSize = 13f
                        setTextColor(Color.parseColor("#111827"))
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }
                    val descView = TextView(requireContext()).apply {
                        text = step.details
                        textSize = 11.5f
                        setTextColor(Color.parseColor("#4B5563"))
                    }
                    textLayout.addView(titleView)
                    textLayout.addView(descView)
                    row.addView(iconView)
                    row.addView(textLayout)
                    dBinding.containerDiagnosticSteps.addView(row)
                }

                if (report.overallSuccess) {
                    dBinding.cardOverallStatus.setCardBackgroundColor(Color.parseColor("#F0FDF4"))
                    dBinding.cardOverallStatus.strokeColor = Color.parseColor("#86EFAC")
                    dBinding.tvStatusIcon.text = "✅"
                    dBinding.tvOverallStatusTitle.text = "ONLINE & VERIFIED (100% FREE)"
                    dBinding.tvOverallStatusTitle.setTextColor(Color.parseColor("#14532D"))
                    dBinding.tvOverallStatusSubtitle.text = "Account: ${report.currentUserEmail.ifEmpty { "Verified Shop Admin" }} • Anti-Leak Bound"
                } else {
                    dBinding.cardOverallStatus.setCardBackgroundColor(Color.parseColor("#FFFBEB"))
                    dBinding.cardOverallStatus.strokeColor = Color.parseColor("#FDE68A")
                    dBinding.tvStatusIcon.text = "⚠️"
                    dBinding.tvOverallStatusTitle.text = "LOCAL ANTI-LEAK SECURITY ACTIVE"
                    dBinding.tvOverallStatusTitle.setTextColor(Color.parseColor("#78350F"))
                    dBinding.tvOverallStatusSubtitle.text = "Owner protection is active locally with verified credentials."
                }

                dBinding.btnCopyDiagnosticReport.setOnClickListener {
                    val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("Firebase Auth Diagnostics", report.toShareableSummary()))
                    Toast.makeText(requireContext(), "Diagnostic report copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dBinding.btnRunDiagnostic.setOnClickListener { runDiagnostic() }
        dBinding.btnCloseDiagnostic.setOnClickListener { dialog.dismiss() }

        runDiagnostic()
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
