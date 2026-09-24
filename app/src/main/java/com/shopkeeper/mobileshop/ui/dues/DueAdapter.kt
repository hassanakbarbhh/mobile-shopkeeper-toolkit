package com.shopkeeper.mobileshop.ui.dues

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.entity.Sale
import com.shopkeeper.mobileshop.databinding.ItemDueBinding
import com.shopkeeper.mobileshop.domain.UdhaarAgingAnalyzer
import com.shopkeeper.mobileshop.utils.dateTimeText
import com.shopkeeper.mobileshop.utils.money
import android.graphics.Color

class DueAdapter(
    private val onItemClick: (Sale) -> Unit,
    private val onCallClick: ((Sale) -> Unit)? = null,
    private val onWhatsAppClick: ((Sale) -> Unit)? = null
) : ListAdapter<Sale, DueAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDueBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemDueBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sale: Sale) {
            val name = if (sale.customerName.isNotBlank()) sale.customerName else "Customer"
            binding.tvCustName.text = name
            binding.tvDueInitials.text = name.take(1).uppercase()
            binding.tvBalance.text = sale.finalAmount.money()
            binding.tvDueDate.text = "Invoice #${sale.invoiceNumber} • ${sale.saleDate.dateTimeText()}"
            
            val analyzer = UdhaarAgingAnalyzer()
            val category = analyzer.getAgingCategory(sale.saleDate)
            binding.tvAging.text = category
            binding.tvPaidInfo.text = "Status: ${sale.paymentStatus}"
            
            when {
                category.contains("Critical") -> {
                    binding.tvAging.setBackgroundResource(R.drawable.bg_badge_red)
                    binding.tvAging.setTextColor(Color.parseColor("#991B1B"))
                }
                category.contains("61-90") -> {
                    binding.tvAging.setBackgroundResource(R.drawable.bg_badge_amber)
                    binding.tvAging.setTextColor(Color.parseColor("#C2410C"))
                }
                category.contains("31-60") -> {
                    binding.tvAging.setBackgroundResource(R.drawable.bg_badge_amber)
                    binding.tvAging.setTextColor(Color.parseColor("#92400E"))
                }
                else -> {
                    binding.tvAging.setBackgroundResource(R.drawable.bg_badge_green)
                    binding.tvAging.setTextColor(Color.parseColor("#065F46"))
                }
            }
            
            binding.root.setOnClickListener { onItemClick(sale) }
            binding.btnReceiveDue.setOnClickListener { onItemClick(sale) }
            binding.btnCallDue.setOnClickListener { onCallClick?.invoke(sale) }
            binding.btnWhatsAppDue.setOnClickListener { onWhatsAppClick?.invoke(sale) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Sale>() {
        override fun areItemsTheSame(oldItem: Sale, newItem: Sale) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Sale, newItem: Sale) = oldItem == newItem
    }
}
