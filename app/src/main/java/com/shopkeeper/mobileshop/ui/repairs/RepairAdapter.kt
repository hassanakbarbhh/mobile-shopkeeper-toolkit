package com.shopkeeper.mobileshop.ui.repairs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.entity.Repair
import com.shopkeeper.mobileshop.data.db.entity.RepairStatus
import com.shopkeeper.mobileshop.databinding.ItemRepairBinding
import com.shopkeeper.mobileshop.utils.dateText
import com.shopkeeper.mobileshop.utils.money

class RepairAdapter(
    private val onItemClick: (Repair) -> Unit
) : ListAdapter<Repair, RepairAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRepairBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemRepairBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(repair: Repair) {
            val ctx = binding.root.context
            binding.tvDevice.text = "${repair.deviceBrand} ${repair.deviceModel}"
            binding.tvCustomer.text = "${repair.customerName} • ${repair.customerPhone}"
            binding.tvIssue.text = repair.issueDescription
            binding.tvDate.text = repair.receivedDate.dateText()
            binding.tvCost.text = "Est: ${repair.estimatedCost.money()}"

            binding.tvStatus.text = repair.status.name.replace('_', ' ')
            when (repair.status) {
                RepairStatus.RECEIVED -> {
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_blue)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.blue_800))
                }
                RepairStatus.IN_REPAIR, RepairStatus.DIAGNOSING, RepairStatus.WAITING_PARTS -> {
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_amber)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.amber_800))
                }
                RepairStatus.COMPLETED, RepairStatus.DELIVERED -> {
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_green)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.green_800))
                }
                RepairStatus.CANCELLED -> {
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_red)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.error))
                }
            }

            binding.root.setOnClickListener { onItemClick(repair) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Repair>() {
        override fun areItemsTheSame(oldItem: Repair, newItem: Repair) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Repair, newItem: Repair) = oldItem == newItem
    }
}
