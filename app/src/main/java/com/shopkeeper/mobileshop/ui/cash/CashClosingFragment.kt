package com.shopkeeper.mobileshop.ui.cash

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.CashClosing
import com.shopkeeper.mobileshop.data.db.entity.OutboxOperation
import com.shopkeeper.mobileshop.data.db.entity.PaymentMethod
import com.shopkeeper.mobileshop.databinding.FragmentCashClosingBinding
import com.shopkeeper.mobileshop.sync.SyncEngine
import com.shopkeeper.mobileshop.utils.UserAuthManager
import com.shopkeeper.mobileshop.utils.money
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class CashClosingFragment : Fragment() {

    private var _binding: FragmentCashClosingBinding? = null
    private val binding get() = _binding!!

    private var selectedDate = Calendar.getInstance()
    private var openingCash = 0.0
    private var cashSales = 0.0
    private var cashExpenses = 0.0
    private var customerPayments = 0.0
    private var supplierPayments = 0.0
    private var expectedCash = 0.0
    private var actualCash = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCashClosingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateDateDisplay()
        loadCashData()
        setupListeners()
    }

    private fun updateDateDisplay() {
        val fmt = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        binding.tvClosingDate.text = fmt.format(selectedDate.time)
    }

    private fun loadCashData() {
        val startOfDay = selectedDate.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = selectedDate.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val db = AppDatabase.getDatabase(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            // Cash Sales
            val salesList = db.saleDao().getSalesByDateRange(startOfDay, endOfDay).first()
            cashSales = salesList.filter { it.paymentMethod == PaymentMethod.CASH }
                .sumOf { it.finalAmount }
            binding.tvCashSales.text = "+ " + cashSales.money()

            // Expenses
            val expensesSum = db.expenseDao().sumInRange(startOfDay, endOfDay).first() ?: 0.0
            cashExpenses = expensesSum
            binding.tvCashExpenses.text = "- " + cashExpenses.money()

            // Customer Payments
            val paymentsSum = db.paymentDao().sumInRange(startOfDay, endOfDay).first() ?: 0.0
            customerPayments = paymentsSum
            binding.tvCustomerPayments.text = "+ " + customerPayments.money()

            // Supplier Purchases (Cash Outward)
            val purchasesSum = db.purchaseDao().sumPaidInRange(startOfDay, endOfDay).first() ?: 0.0
            supplierPayments = purchasesSum
            binding.tvSupplierPayments.text = "- " + supplierPayments.money()

            recalculateExpectedCash()
        }
    }

    private fun recalculateExpectedCash() {
        openingCash = binding.etOpeningCash.text?.toString()?.toDoubleOrNull() ?: 0.0
        expectedCash = openingCash + cashSales + customerPayments - cashExpenses - supplierPayments
        binding.tvExpectedCash.text = expectedCash.money()
        updateVariance()
    }

    private fun updateVariance() {
        actualCash = binding.etActualCash.text?.toString()?.toDoubleOrNull() ?: 0.0
        val difference = actualCash - expectedCash

        when {
            abs(difference) < 0.01 -> {
                binding.tvDifference.text = "Rs 0 (Balanced)"
                binding.tvDifference.setTextColor(Color.parseColor("#059669"))
            }
            difference < 0 -> {
                binding.tvDifference.text = "- ${abs(difference).money()} (Shortage)"
                binding.tvDifference.setTextColor(Color.parseColor("#DC2626"))
            }
            else -> {
                binding.tvDifference.text = "+ ${difference.money()} (Surplus)"
                binding.tvDifference.setTextColor(Color.parseColor("#D97706"))
            }
        }
    }

    private fun setupListeners() {
        binding.etOpeningCash.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { recalculateExpectedCash() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etActualCash.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateVariance() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnChangeDate.setOnClickListener {
            val c = selectedDate
            val picker = android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDate.set(year, month, dayOfMonth)
                    updateDateDisplay()
                    loadCashData()
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            )
            picker.show()
        }

        binding.btnCloseRegister.setOnClickListener {
            confirmAndCloseRegister()
        }
    }

    private fun confirmAndCloseRegister() {
        val user = UserAuthManager.getCurrentUser(requireContext())
        val signedBy = user?.displayName ?: "Shopkeeper"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Daily Cash Closing")
            .setMessage("Expected: ${expectedCash.money()}\nActual Counted: ${actualCash.money()}\nVariance: ${(actualCash - expectedCash).money()}\n\nDo you want to seal and record the register?")
            .setPositiveButton("Seal Register") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val db = AppDatabase.getDatabase(requireContext())
                    val closing = CashClosing(
                        closingDate = selectedDate.timeInMillis,
                        openingCash = openingCash,
                        cashIn = cashSales + customerPayments,
                        cashOut = cashExpenses + supplierPayments,
                        countedCash = actualCash,
                        variance = actualCash - expectedCash,
                        signedBy = signedBy
                    )
                    db.cashClosingDao().insert(closing)

                    // Enqueue to Room Outbox for cloud synchronization
                    val payload = JSONObject().apply {
                        put("closingDate", closing.closingDate)
                        put("openingCash", closing.openingCash)
                        put("cashIn", closing.cashIn)
                        put("cashOut", closing.cashOut)
                        put("countedCash", closing.countedCash)
                        put("variance", closing.variance)
                        put("signedBy", closing.signedBy)
                        put("note", binding.etClosingNote.text?.toString().orEmpty())
                    }.toString()

                    val outboxOp = OutboxOperation(
                        operationType = OutboxOperation.OP_CASH_CLOSING,
                        entityType = "CashClosing",
                        entityId = closing.closingDate.toString(),
                        payloadJson = payload,
                        userId = user?.email ?: "UNKNOWN"
                    )
                    SyncEngine.enqueueOperation(requireContext(), outboxOp)

                    Toast.makeText(requireContext(), "Daily Cash Register successfully closed and saved!", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
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
