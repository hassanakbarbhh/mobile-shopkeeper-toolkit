package com.shopkeeper.mobileshop.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.entity.Product
import com.shopkeeper.mobileshop.databinding.ItemProductBinding
import com.shopkeeper.mobileshop.utils.money

class ProductAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onMoreClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductBrand.text = "${product.brand} • ${product.category.name.replace('_', ' ')}"
            binding.tvProductPrice.text = product.sellingPrice.money()
            binding.tvQuantity.text = "Stock: ${product.quantity}"

            val detailsList = mutableListOf<String>()
            if (product.ram.isNotBlank() || product.storage.isNotBlank()) {
                val mem = listOf(product.ram, product.storage).filter { it.isNotBlank() }.joinToString(" / ")
                detailsList.add(mem)
            }
            if (product.color.isNotBlank()) detailsList.add(product.color)
            if (product.warrantyMonths > 0) detailsList.add("${product.warrantyMonths}m warranty")
            if (product.notes.isNotBlank()) detailsList.add(product.notes)
            if (product.imei.isNotBlank()) detailsList.add("IMEI: ${product.imei}")

            if (detailsList.isNotEmpty()) {
                binding.tvProductDetails.visibility = android.view.View.VISIBLE
                binding.tvProductDetails.text = detailsList.joinToString(" • ")
            } else {
                binding.tvProductDetails.visibility = android.view.View.GONE
            }

            if (product.quantity <= 0) {
                binding.tvStockStatus.text = "Out of Stock"
                binding.tvStockStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.error))
                binding.tvStockStatus.setBackgroundResource(R.drawable.bg_badge_red)
            } else if (product.quantity <= 3) {
                binding.tvStockStatus.text = "Low Stock"
                binding.tvStockStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.warning))
                binding.tvStockStatus.setBackgroundResource(R.drawable.bg_badge_amber)
            } else {
                binding.tvStockStatus.text = "In Stock"
                binding.tvStockStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green_800))
                binding.tvStockStatus.setBackgroundResource(R.drawable.bg_badge_green)
            }

            binding.root.setOnClickListener { onItemClick(product) }
            binding.btnMore.setOnClickListener { onMoreClick(product) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}
