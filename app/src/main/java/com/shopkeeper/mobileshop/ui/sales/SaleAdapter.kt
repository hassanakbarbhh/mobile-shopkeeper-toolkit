package com.shopkeeper.mobileshop.ui.sales

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopkeeper.mobileshop.data.db.entity.Sale
import com.shopkeeper.mobileshop.databinding.ItemSaleBinding
import com.shopkeeper.mobileshop.utils.dateTimeText
import com.shopkeeper.mobileshop.utils.money

class SaleAdapter(
    private val onItemClick: (Sale) -> Unit
) : ListAdapter<Sale, SaleAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSaleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSaleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sale: Sale) {
            binding.tvInvoice.text = "INV-#${sale.id.toString().padStart(4, '0')}"
            binding.tvCustomer.text = sale.customerName
            binding.tvDate.text = sale.saleDate.dateTimeText()
            binding.tvPayment.text = "${sale.paymentMethod} • ${sale.paymentStatus}"
            binding.tvTotal.text = sale.finalAmount.money()
            binding.root.setOnClickListener { onItemClick(sale) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Sale>() {
        override fun areItemsTheSame(oldItem: Sale, newItem: Sale) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Sale, newItem: Sale) = oldItem == newItem
    }
}
