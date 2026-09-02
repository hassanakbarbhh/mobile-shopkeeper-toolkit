package com.shopkeeper.mobileshop.ui.sales

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shopkeeper.mobileshop.data.db.entity.SaleItem
import com.shopkeeper.mobileshop.databinding.ItemCartBinding
import com.shopkeeper.mobileshop.utils.money

class CartAdapter(
    private val onQtyChange: (SaleItem, Int) -> Unit,
    private val onRemove: (SaleItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private val items = mutableListOf<SaleItem>()

    fun submitList(newItems: List<SaleItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SaleItem) {
            binding.tvItemName.text = item.productName
            binding.tvUnitPrice.text = "${item.unitPrice.money()} each"
            binding.tvQty.text = "${item.quantity}"
            binding.tvLineTotal.text = item.totalPrice.money()

            binding.btnPlus.setOnClickListener { onQtyChange(item, item.quantity + 1) }
            binding.btnMinus.setOnClickListener {
                if (item.quantity > 1) onQtyChange(item, item.quantity - 1)
                else onRemove(item)
            }
            binding.btnRemove.setOnClickListener { onRemove(item) }
        }
    }
}
