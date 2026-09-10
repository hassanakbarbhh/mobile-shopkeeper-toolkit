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
import com.shopkeeper.mobileshop.databinding.FragmentRepairsBinding
import com.shopkeeper.mobileshop.utils.ExportManager
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
        val filtered = when (binding.chipGroupStatus.checkedChipId) {
            R.id.chipReceived -> allRepairs.filter { it.status == RepairStatus.RECEIVED }
            R.id.chipInRepair -> allRepairs.filter { it.status == RepairStatus.IN_REPAIR || it.status == RepairStatus.DIAGNOSING }
            R.id.chipCompleted -> allRepairs.filter { it.status == RepairStatus.COMPLETED }
            R.id.chipDelivered -> allRepairs.filter { it.status == RepairStatus.DELIVERED }
            else -> allRepairs
        }
        adapter.submitList(filtered)
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
        val options = arrayOf(
            "Update Status (${repair.status.name})",
            "🖨️ Print Thermal Claim & Phone Tag",
            "WhatsApp Status Update",
            "Call Customer",
            "Delete Repair Ticket"
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${repair.deviceBrand} ${repair.deviceModel} • ${repair.customerName}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showUpdateStatusDialog(repair)
                    1 -> com.shopkeeper.mobileshop.utils.ThermalPrintHelper.showRepairTagDialog(requireContext(), repair)
                    2 -> {
                        if (repair.customerPhone.isNotBlank()) {
                            val msg = "Assalam-o-Alaikum ${repair.customerName}, your ${repair.deviceBrand} ${repair.deviceModel} repair status is currently: ${repair.status.name.replace('_', ' ')}. Est: Rs.${repair.estimatedCost}. Mobile Shop."
                            ExportManager.shareWhatsApp(requireContext(), repair.customerPhone, msg)
                        } else {
                            Toast.makeText(requireContext(), "No phone recorded", Toast.LENGTH_SHORT).show()
                        }
                    }
                    3 -> {
                        if (repair.customerPhone.isNotBlank()) {
                            ExportManager.openDialer(requireContext(), repair.customerPhone)
                        } else {
                            Toast.makeText(requireContext(), "No phone recorded", Toast.LENGTH_SHORT).show()
                        }
                    }
                    4 -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Delete Repair Ticket #${repair.id}?")
                            .setMessage("Are you sure you want to permanently delete this repair ticket for ${repair.customerName}?")
                            .setPositiveButton("Delete") { _, _ ->
                                viewLifecycleOwner.lifecycleScope.launch {
                                    repository.deleteRepair(repair)
                                    Toast.makeText(requireContext(), "Repair ticket #${repair.id} deleted", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showUpdateStatusDialog(repair: Repair) {
        val statuses = RepairStatus.values().map { it.name.replace('_', ' ') }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Repair Status")
            .setItems(statuses) { _, which ->
                val newStatus = RepairStatus.values()[which]
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.updateRepair(repair.copy(status = newStatus))
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
