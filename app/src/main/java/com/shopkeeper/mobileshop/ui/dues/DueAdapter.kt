package com.shopkeeper.mobileshop.ui.dues

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopkeeper.mobileshop.data.db.entity.Sale
import com.shopkeeper.mobileshop.databinding.ItemDueBinding
import com.shopkeeper.mobileshop.utils.dateTimeText

import android.graphics.Color
import com.shopkeeper.mobileshop.domain.UdhaarAgingAnalyzer
import com.shopkeeper.mobileshop.utils.money

class DueAdapter(
    private val onItemClick: (Sale) -> Unit
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
            binding.tvCustName.text = sale.customerName
            binding.tvBalance.text = "Due: ${sale.finalAmount.money()}"
            binding.tvDueDate.text = "Invoice #${sale.id} • ${sale.saleDate.dateTimeText()}"
            
            val analyzer = UdhaarAgingAnalyzer()
            val category = analyzer.getAgingCategory(sale.saleDate) // Assuming due date is sale date + some offset, or just sale date
            binding.tvPaidInfo.text = "Status: ${sale.paymentStatus} • $category"
            
            when {
                category.contains("Critical") -> binding.root.setBackgroundColor(Color.parseColor("#FFCDD2")) // Red
                category.contains("61-90") -> binding.root.setBackgroundColor(Color.parseColor("#FFE0B2")) // Orange
                category.contains("31-60") -> binding.root.setBackgroundColor(Color.parseColor("#FFF9C4")) // Yellow
                else -> binding.root.setBackgroundColor(Color.WHITE)
            }
            
            binding.root.setOnClickListener { onItemClick(sale) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Sale>() {
        override fun areItemsTheSame(oldItem: Sale, newItem: Sale) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Sale, newItem: Sale) = oldItem == newItem
    }
}
