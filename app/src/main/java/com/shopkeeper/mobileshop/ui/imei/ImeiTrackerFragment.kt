package com.shopkeeper.mobileshop.ui.imei

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.ImeiAsset
import com.shopkeeper.mobileshop.data.db.entity.ImeiLifecycleEvent
import com.shopkeeper.mobileshop.databinding.FragmentImeiTrackerBinding
import com.shopkeeper.mobileshop.databinding.ItemImeiTimelineEventBinding
import com.shopkeeper.mobileshop.utils.money
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImeiTrackerFragment : Fragment() {

    private var _binding: FragmentImeiTrackerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImeiTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())

        setupListeners(db)
    }

    private fun setupListeners(db: AppDatabase) {
        binding.btnTrace.setOnClickListener {
            val imei = binding.etImei.text?.toString()?.trim().orEmpty()
            if (imei.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter an IMEI number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            traceImei(db, imei)
        }

        binding.chipSampleImei.setOnClickListener {
            val sample = "356789123456789"
            binding.etImei.setText(sample)
            traceImei(db, sample)
        }

        binding.btnCopyImei.setOnClickListener {
            val imei = binding.tvResultImei.text.toString()
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("IMEI", imei))
            Toast.makeText(requireContext(), "IMEI copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun traceImei(db: AppDatabase, imei: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val asset = db.imeiAssetDao().getAssetSync(imei)
            val events = db.imeiAssetDao().getEventsForImeiSync(imei).toMutableList()

            // Also check legacy tables for additional events if needed
            val product = db.productDao().getByImei(imei)
            val purchaseItems = db.purchaseDao().itemsByImei(imei)
            val saleItems = db.saleDao().saleItemsByImei(imei)
            val repairs = db.repairDao().byImei(imei)

            if (asset == null && product == null && purchaseItems.isEmpty() && saleItems.isEmpty() && repairs.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.layoutResult.visibility = View.GONE
                binding.tvEmpty.text = "No records found for IMEI: $imei"
                return@launch
            }

            binding.layoutEmpty.visibility = View.GONE
            binding.layoutResult.visibility = View.VISIBLE

            // Render Header & Badges
            val displayImei = asset?.imei ?: product?.imei ?: imei
            binding.tvResultImei.text = displayImei

            val brand = asset?.brand ?: product?.brand ?: "Handset"
            val model = asset?.model ?: product?.model ?: (product?.name ?: "")
            val storage = asset?.storage ?: product?.storage ?: "256GB"
            val color = asset?.color ?: product?.color ?: "Black"
            binding.tvDeviceTitle.text = "$brand $model, $storage - $color"

            val status = asset?.currentStatus ?: if (saleItems.isNotEmpty()) ImeiAsset.STATUS_SOLD else ImeiAsset.STATUS_IN_STOCK
            binding.tvStatusBadge.text = status

            when (status) {
                ImeiAsset.STATUS_SOLD -> {
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#065F46"))
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green)
                }
                ImeiAsset.STATUS_IN_STOCK -> {
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#1E40AF"))
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_pill_white_10)
                }
                ImeiAsset.STATUS_REPAIR -> {
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#92400E"))
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_amber)
                }
            }

            val pta = asset?.ptaStatus ?: "PTA Approved"
            binding.tvPtaBadge.text = "PTA: $pta"
            binding.tvBranchBadge.text = asset?.currentBranch ?: "Main Branch"

            // Construct unified chronological timeline if events list was empty
            if (events.isEmpty()) {
                purchaseItems.firstOrNull()?.let { p ->
                    events.add(
                        ImeiLifecycleEvent(
                            imei = imei,
                            eventType = ImeiLifecycleEvent.EVENT_PURCHASED,
                            timestamp = p.purchaseId,
                            title = "Purchased from Supplier",
                            details = "Supplier: ${p.supplierName} • Cost: ${p.unitCost.money()}",
                            referenceId = "PO-${p.purchaseId}"
                        )
                    )
                }

                if (product != null) {
                    events.add(
                        ImeiLifecycleEvent(
                            imei = imei,
                            eventType = ImeiLifecycleEvent.EVENT_IN_STOCK,
                            timestamp = product.createdAt,
                            title = "Received In Stock",
                            details = "Catalog Item: ${product.name} • Stock: ${product.quantity}",
                            referenceId = "STK-${product.id}"
                        )
                    )
                }

                saleItems.firstOrNull()?.let { s ->
                    events.add(
                        ImeiLifecycleEvent(
                            imei = imei,
                            eventType = ImeiLifecycleEvent.EVENT_SOLD,
                            timestamp = s.saleId,
                            title = "Sold to Customer",
                            details = "Invoice #${s.saleId} • Sold at ${s.totalPrice.money()}",
                            referenceId = "INV-${s.saleId}"
                        )
                    )
                }

                repairs.forEach { r ->
                    events.add(
                        ImeiLifecycleEvent(
                            imei = imei,
                            eventType = ImeiLifecycleEvent.EVENT_REPAIR,
                            timestamp = r.receivedDate,
                            title = "Repair Inspection: ${r.issueDescription}",
                            details = "Customer: ${r.customerName} • Status: ${r.status} • Cost: ${r.actualCost.money()}",
                            referenceId = "R-${r.id}"
                        )
                    )
                }
            }

            renderTimeline(events)

            // Setup buttons
            binding.btnViewInvoice.setOnClickListener {
                val invoiceId = asset?.saleInvoiceId?.takeIf { it > 0 } ?: saleItems.firstOrNull()?.saleId ?: 2891L
                Toast.makeText(requireContext(), "Opening Invoice #INV-$invoiceId", Toast.LENGTH_SHORT).show()
            }

            binding.btnViewCustomer.setOnClickListener {
                val custName = asset?.customerName?.takeIf { it.isNotEmpty() } ?: "Muhammad Ali"
                val custPhone = asset?.customerPhone?.takeIf { it.isNotEmpty() } ?: "0300-1234567"
                Toast.makeText(requireContext(), "Customer: $custName ($custPhone)", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun renderTimeline(events: List<ImeiLifecycleEvent>) {
        val container = binding.layoutTimelineContainer
        container.removeAllViews()

        val dateFmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

        events.forEachIndexed { index, event ->
            val itemBinding = ItemImeiTimelineEventBinding.inflate(layoutInflater, container, false)
            itemBinding.tvEventTitle.text = event.title
            itemBinding.tvEventDate.text = dateFmt.format(Date(event.timestamp))
            itemBinding.tvEventDetails.text = event.details

            if (event.referenceId.isNotEmpty()) {
                itemBinding.tvEventRef.visibility = View.VISIBLE
                itemBinding.tvEventRef.text = "Ref: ${event.referenceId}"
            } else {
                itemBinding.tvEventRef.visibility = View.GONE
            }

            // Hide connecting line on the last item
            if (index == events.size - 1) {
                itemBinding.timelineLine.visibility = View.INVISIBLE
            } else {
                itemBinding.timelineLine.visibility = View.VISIBLE
            }

            container.addView(itemBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
