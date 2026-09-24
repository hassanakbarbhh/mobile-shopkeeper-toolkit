package com.shopkeeper.mobileshop.ui.repairs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.Repair
import com.shopkeeper.mobileshop.data.db.entity.RepairStatus
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.DialogRepairBinding
import com.shopkeeper.mobileshop.databinding.DialogRepairDetailsBinding
import com.shopkeeper.mobileshop.databinding.FragmentRepairsBinding
import com.shopkeeper.mobileshop.utils.ExportManager
import com.shopkeeper.mobileshop.utils.money
import kotlinx.coroutines.flow.collectLatest

import com.shopkeeper.mobileshop.domain.WarrantyExpirationTracker
import com.shopkeeper.mobileshop.domain.RepairStatusStateRouter
import kotlinx.coroutines.launch

class RepairsFragment : Fragment() {

    private var _binding: FragmentRepairsBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository
    private lateinit var adapter: RepairAdapter
    private var allRepairs: List<Repair> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRepairsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        repository = ShopRepository(db)

        adapter = RepairAdapter({ repair -> showRepairActions(repair) }, { repair ->
            viewLifecycleOwner.lifecycleScope.launch {
                val router = com.shopkeeper.mobileshop.domain.RepairStatusStateRouter()
                val nextStatus = router.getNextStatus(repair.status)
                if (nextStatus != repair.status) {
                    val updated = repair.copy(status = nextStatus)
                    repository.updateRepair(updated)
                    android.widget.Toast.makeText(requireContext(), "Advanced to ${nextStatus.name}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        })
        binding.rvRepairs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRepairs.adapter = adapter

        binding.fabAddRepair.setOnClickListener { showAddRepairDialog() }

        binding.btnExportRepairs.setOnClickListener {
            if (allRepairs.isEmpty()) {
                Toast.makeText(requireContext(), "No repair records to export", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ExportManager.showLedgerExportDialog(
                context = requireContext(),
                ledgerTitle = "Repair Jobs Ledger",
                onExportCsv = { ExportManager.exportRepairsCsv(requireContext(), allRepairs) },
                onExportPdf = { ExportManager.exportRepairsPdf(requireContext(), allRepairs) }
            )
        }

        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, _ ->
            filterRepairs()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.allRepairs.collectLatest { list ->
                allRepairs = list
                filterRepairs()
            }
        }
    }

    private fun filterRepairs() {
        val filtered = when ((_binding?.chipGroupStatus?.checkedChipId ?: -1)) {
            R.id.chipReceived -> allRepairs.filter { it.status == RepairStatus.RECEIVED }
            R.id.chipInRepair -> allRepairs.filter { it.status == RepairStatus.IN_REPAIR || it.status == RepairStatus.DIAGNOSING }
            R.id.chipCompleted -> allRepairs.filter { it.status == RepairStatus.COMPLETED }
            R.id.chipDelivered -> allRepairs.filter { it.status == RepairStatus.DELIVERED }
            else -> allRepairs
        }
        adapter.submitList(filtered)
        _binding?.layoutEmptyRepairs?.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        _binding?.rvRepairs?.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showAddRepairDialog() {
        val dBinding = DialogRepairBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Register Device Repair")
            .setView(dBinding.root)
            .create()

        dBinding.btnSaveRepair.setOnClickListener {
            val custName = dBinding.etCustomerName.text.toString().trim()
            val phone = dBinding.etCustomerPhone.text.toString().trim()
            val brand = dBinding.etDeviceBrand.text.toString().trim()
            val model = dBinding.etDeviceModel.text.toString().trim()
            val imei = dBinding.etImei.text.toString().trim()
            val issue = dBinding.etIssue.text.toString().trim()
            val cost = dBinding.etEstCost.text.toString().toDoubleOrNull() ?: 0.0

            if (custName.isEmpty() || brand.isEmpty() || issue.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill mandatory fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val r = Repair(
                customerName = custName,
                customerPhone = phone,
                deviceBrand = brand,
                deviceModel = model,
                imei = imei,
                issueDescription = issue,
                estimatedCost = cost,
                status = RepairStatus.RECEIVED
            )

            viewLifecycleOwner.lifecycleScope.launch {
                val id = repository.insertRepair(r)
                val saved = r.copy(id = id)
                dialog.dismiss()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Repair Ticket #$id Created")
                    .setMessage("Device: ${r.deviceBrand} ${r.deviceModel}\nCustomer: ${r.customerName}\n\nPrint thermal claim receipt & phone tag?")
                    .setPositiveButton("🖨️ Print Thermal Tag") { _, _ ->
                        com.shopkeeper.mobileshop.utils.ThermalPrintHelper.showRepairTagDialog(requireContext(), saved)
                    }
                    .setNegativeButton("Done", null)
                    .show()
            }
        }

        dialog.show()
    }

    private fun showRepairActions(repair: Repair) {
        showRepairDetailsDialog(repair)
    }

    private fun showRepairDetailsDialog(repair: Repair) {
        val dBinding = DialogRepairDetailsBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dBinding.root)
            .create()

        var current = repair

        fun renderState() {
            dBinding.tvTicketNumber.text = "Repair Ticket #${current.id}"
            dBinding.tvRepairDevice.text = "${current.deviceBrand} ${current.deviceModel}"
            dBinding.tvDetailCustomerName.text = if (current.customerName.isNotBlank()) current.customerName else "Walk-in Customer"
            dBinding.tvDetailCustomerPhone.text = if (current.customerPhone.isNotBlank()) current.customerPhone else "No phone"
            dBinding.tvDetailImei.text = if (current.imei.isNotBlank()) current.imei else "Not provided"
            dBinding.tvDetailIssue.text = current.issueDescription
            dBinding.tvDetailCost.text = current.estimatedCost.money()
            dBinding.tvDetailStatusBadge.text = current.status.name.replace('_', ' ')

            val router = RepairStatusStateRouter()
            val next = router.getNextStatus(current.status)

            if (current.status == RepairStatus.DELIVERED || current.status == RepairStatus.CANCELLED) {
                dBinding.btnAdvanceStatus.visibility = View.GONE
            } else {
                dBinding.btnAdvanceStatus.visibility = View.VISIBLE
                dBinding.btnAdvanceStatus.text = "Next: ${next.name.replace('_', ' ')} →"
            }

            val s = current.status
            val isStep1 = true
            val isStep2 = s == RepairStatus.DIAGNOSING || s == RepairStatus.WAITING_PARTS || s == RepairStatus.IN_REPAIR || s == RepairStatus.COMPLETED || s == RepairStatus.DELIVERED
            val isStep3 = s == RepairStatus.IN_REPAIR || s == RepairStatus.COMPLETED || s == RepairStatus.DELIVERED
            val isStep4 = s == RepairStatus.COMPLETED || s == RepairStatus.DELIVERED
            val isStep5 = s == RepairStatus.DELIVERED

            dBinding.tvTimelineStep1.text = if (isStep1) "✓ 1. Intake Received" else "○ 1. Intake Received"
            dBinding.tvTimelineStep1.setTextColor(if (isStep1) android.graphics.Color.parseColor("#15803D") else android.graphics.Color.parseColor("#94A3B8"))

            dBinding.tvTimelineStep2.text = if (isStep2) "✓ 2. Diagnosing & Parts Check" else "○ 2. Diagnosing & Parts Check"
            dBinding.tvTimelineStep2.setTextColor(if (isStep2) android.graphics.Color.parseColor("#15803D") else android.graphics.Color.parseColor("#94A3B8"))

            dBinding.tvTimelineStep3.text = if (isStep3) "✓ 3. Bench Repair in Progress" else "○ 3. Bench Repair in Progress"
            dBinding.tvTimelineStep3.setTextColor(if (isStep3) android.graphics.Color.parseColor("#15803D") else android.graphics.Color.parseColor("#94A3B8"))

            dBinding.tvTimelineStep4.text = if (isStep4) "✓ 4. Tested & Ready for Pickup" else "○ 4. Tested & Ready for Pickup"
            dBinding.tvTimelineStep4.setTextColor(if (isStep4) android.graphics.Color.parseColor("#15803D") else android.graphics.Color.parseColor("#94A3B8"))

            dBinding.tvTimelineStep5.text = if (isStep5) "✓ 5. Delivered to Customer" else "○ 5. Delivered to Customer"
            dBinding.tvTimelineStep5.setTextColor(if (isStep5) android.graphics.Color.parseColor("#15803D") else android.graphics.Color.parseColor("#94A3B8"))
        }

        renderState()

        dBinding.btnAdvanceStatus.setOnClickListener {
            val router = RepairStatusStateRouter()
            val nextStatus = router.getNextStatus(current.status)
            if (nextStatus != current.status) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val updated = current.copy(status = nextStatus)
                    repository.updateRepair(updated)
                    current = updated
                    renderState()
                    Toast.makeText(requireContext(), "Advanced to ${nextStatus.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dBinding.btnRepairCall.setOnClickListener {
            if (current.customerPhone.isNotBlank()) {
                ExportManager.openDialer(requireContext(), current.customerPhone)
            } else {
                Toast.makeText(requireContext(), "No phone recorded", Toast.LENGTH_SHORT).show()
            }
        }

        dBinding.btnRepairWhatsApp.setOnClickListener {
            if (current.customerPhone.isNotBlank()) {
                val msg = "Assalam-o-Alaikum ${current.customerName}, your ${current.deviceBrand} ${current.deviceModel} repair status is currently: ${current.status.name.replace('_', ' ')}. Est: ${current.estimatedCost.money()}."
                ExportManager.shareWhatsApp(requireContext(), current.customerPhone, msg)
            } else {
                Toast.makeText(requireContext(), "No phone recorded", Toast.LENGTH_SHORT).show()
            }
        }

        dBinding.btnRepairPrintTag.setOnClickListener {
            com.shopkeeper.mobileshop.utils.ThermalPrintHelper.showRepairTagDialog(requireContext(), current)
        }

        dBinding.btnRepairChangeStatus.setOnClickListener {
            showUpdateStatusDialog(current) { updated ->
                current = updated
                renderState()
            }
        }

        dBinding.btnRepairClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showUpdateStatusDialog(repair: Repair, onUpdated: (Repair) -> Unit) {
        val statuses = RepairStatus.values().map { it.name.replace('_', ' ') }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Repair Status")
            .setItems(statuses) { _, which ->
                val newStatus = RepairStatus.values()[which]
                viewLifecycleOwner.lifecycleScope.launch {
                    val updated = repair.copy(status = newStatus)
                    repository.updateRepair(updated)
                    onUpdated(updated)
                    Toast.makeText(requireContext(), "Status updated to ${newStatus.name}", Toast.LENGTH_SHORT).show()
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
// Block B: UI Wiring complete
