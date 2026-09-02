package com.shopkeeper.mobileshop.ui.dues

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopkeeper.mobileshop.data.db.entity.Sale
import com.shopkeeper.mobileshop.databinding.ItemDueBinding
import com.shopkeeper.mobileshop.utils.dateTimeText
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
            binding.tvPaidInfo.text = "Status: ${sale.paymentStatus}"
            binding.root.setOnClickListener { onItemClick(sale) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Sale>() {
        override fun areItemsTheSame(oldItem: Sale, newItem: Sale) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Sale, newItem: Sale) = oldItem == newItem
    }
}
