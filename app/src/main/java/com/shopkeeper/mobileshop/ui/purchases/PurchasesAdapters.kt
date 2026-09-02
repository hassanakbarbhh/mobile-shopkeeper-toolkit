package com.shopkeeper.mobileshop.ui.purchases

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopkeeper.mobileshop.data.db.entity.Purchase
import com.shopkeeper.mobileshop.data.db.entity.Supplier
import com.shopkeeper.mobileshop.databinding.ItemPurchaseBinding
import com.shopkeeper.mobileshop.databinding.ItemSupplierBinding
import com.shopkeeper.mobileshop.utils.dateText
import com.shopkeeper.mobileshop.utils.money

class PurchaseAdapter(
    private val onItemClick: (Purchase) -> Unit
) : ListAdapter<Purchase, PurchaseAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPurchaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPurchaseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(p: Purchase) {
            binding.tvSupplier.text = p.supplierName
            binding.tvDate.text = p.purchaseDate.dateText()
            binding.tvTotal.text = p.totalCost.money()
            binding.tvStatus.text = p.paymentStatus.name
            binding.root.setOnClickListener { onItemClick(p) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Purchase>() {
        override fun areItemsTheSame(oldItem: Purchase, newItem: Purchase) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Purchase, newItem: Purchase) = oldItem == newItem
    }
}

class SupplierAdapter(
    private val onItemClick: (Supplier) -> Unit
) : ListAdapter<Supplier, SupplierAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSupplierBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSupplierBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(s: Supplier) {
            binding.tvName.text = s.name
            binding.tvCompany.text = "${s.company} • ${s.phone}"
            binding.tvInitials.text = s.name.take(1).uppercase()
            binding.root.setOnClickListener { onItemClick(s) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Supplier>() {
        override fun areItemsTheSame(oldItem: Supplier, newItem: Supplier) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Supplier, newItem: Supplier) = oldItem == newItem
    }
}
