package com.shopkeeper.mobileshop.ui.sales

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.R
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.*
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.DialogProductSearchBinding
import com.shopkeeper.mobileshop.databinding.FragmentNewSaleBinding
import com.shopkeeper.mobileshop.ui.inventory.ProductAdapter
import com.shopkeeper.mobileshop.utils.InvoiceGenerator
import com.shopkeeper.mobileshop.utils.money
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NewSaleFragment : Fragment() {

    private var _binding: FragmentNewSaleBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository
    private lateinit var cartAdapter: CartAdapter
    private val cartItems = mutableListOf<SaleItem>()
    private var availableSellers: List<Seller> = emptyList()
    private var selectedSellerId: Long? = null
    private var selectedSellerName: String = "Owner"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewSaleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        repository = ShopRepository(db)

        setupCart()
        setupListeners()
        setupSellerSelector()
    }

    private fun setupSellerSelector() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.activeSellers.collect { sellers ->
                availableSellers = sellers
                val sellerNames = if (sellers.isEmpty()) {
                    listOf("Owner")
                } else {
                    sellers.map { it.name }
                }
                val sellerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sellerNames)
                binding.actvSeller.setAdapter(sellerAdapter)

                if (binding.actvSeller.text.isNullOrBlank() && sellerNames.isNotEmpty()) {
                    binding.actvSeller.setText(sellerNames[0], false)
                    selectedSellerName = sellerNames[0]
                    selectedSellerId = sellers.firstOrNull()?.id
                }

                binding.actvSeller.setOnItemClickListener { _, _, position, _ ->
                    if (position in sellers.indices) {
                        val sel = sellers[position]
                        selectedSellerId = sel.id
                        selectedSellerName = sel.name
                    } else {
                        selectedSellerId = null
                        selectedSellerName = "Owner"
                    }
                }
            }
        }
    }

    private fun setupCart() {
        cartAdapter = CartAdapter(
            onQtyChange = { item, newQty ->
                val index = cartItems.indexOfFirst { it.productId == item.productId }
                if (index != -1) {
                    val unit = cartItems[index].unitPrice
                    cartItems[index] = cartItems[index].copy(
                        quantity = newQty,
                        totalPrice = unit * newQty
                    )
                    cartAdapter.submitList(cartItems.toList())
                    recalculateTotals()
                }
            },
            onRemove = { item ->
                cartItems.removeAll { it.productId == item.productId }
                cartAdapter.submitList(cartItems.toList())
                recalculateTotals()
            }
        )
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = cartAdapter
    }

    private fun setupListeners() {
        binding.etProductSearch.setOnClickListener { showProductSearchDialog() }

        binding.etDiscount.doAfterTextChanged { recalculateTotals() }
        binding.etTax.doAfterTextChanged { recalculateTotals() }

        binding.btnCompleteSale.setOnClickListener { completeSale() }
    }

    private fun recalculateTotals() {
        val subtotal = cartItems.sumOf { it.totalPrice }
        val discount = binding.etDiscount.text.toString().toDoubleOrNull() ?: 0.0
        val tax = binding.etTax.text.toString().toDoubleOrNull() ?: 0.0
        val grandTotal = (subtotal - discount + tax).coerceAtLeast(0.0)

        binding.tvSubtotal.text = subtotal.money()
        binding.tvTotal.text = grandTotal.money()
    }

    private fun showProductSearchDialog() {
        val dialogBinding = DialogProductSearchBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        val searchAdapter = ProductAdapter(
            onItemClick = { product ->
                addToCart(product)
                dialog.dismiss()
            },
            onMoreClick = {}
        )
        dialogBinding.rvDialogProducts.layoutManager = LinearLayoutManager(requireContext())
        dialogBinding.rvDialogProducts.adapter = searchAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            val list = repository.allProducts.first()
            searchAdapter.submitList(list)

            dialogBinding.etDialogSearch.doAfterTextChanged { text ->
                val q = text?.toString().orEmpty()
                searchAdapter.submitList(
                    if (q.isBlank()) list else list.filter {
                        it.name.contains(q, true) || it.brand.contains(q, true) || it.imei.contains(q, true)
                    }
                )
            }
        }

        dialog.show()
    }

    private fun addToCart(product: Product) {
        val existingIndex = cartItems.indexOfFirst { it.productId == product.id }
        if (existingIndex != -1) {
            val cur = cartItems[existingIndex]
            cartItems[existingIndex] = cur.copy(
                quantity = cur.quantity + 1,
                totalPrice = cur.unitPrice * (cur.quantity + 1)
            )
        } else {
            cartItems.add(
                SaleItem(
                    saleId = 0,
                    productId = product.id,
                    productName = product.name,
                    quantity = 1,
                    unitPrice = product.sellingPrice,
                    totalPrice = product.sellingPrice,
                    imei = product.imei
                )
            )
        }
        cartAdapter.submitList(cartItems.toList())
        recalculateTotals()
    }

    private fun completeSale() {
        if (cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one product", Toast.LENGTH_SHORT).show()
            return
        }

        val custName = binding.actvCustomer.text?.toString()?.trim().orEmpty().ifEmpty { "Walk-in Customer" }
        val subtotal = cartItems.sumOf { it.totalPrice }
        val discount = binding.etDiscount.text.toString().toDoubleOrNull() ?: 0.0
        val tax = binding.etTax.text.toString().toDoubleOrNull() ?: 0.0
        val grandTotal = (subtotal - discount + tax).coerceAtLeast(0.0)

        val method = when (binding.chipGroupPayment.checkedChipId) {
            R.id.chipCard -> PaymentMethod.CARD
            R.id.chipUpi -> PaymentMethod.UPI
            R.id.chipCredit -> PaymentMethod.CREDIT
            else -> PaymentMethod.CASH
        }

        val status = if (method == PaymentMethod.CREDIT) PaymentStatus.PENDING else PaymentStatus.PAID

        val sellerNameInput = binding.actvSeller.text?.toString()?.trim().orEmpty().ifEmpty { selectedSellerName }

        val sale = Sale(
            customerName = custName,
            totalAmount = subtotal,
            discount = discount,
            taxAmount = tax,
            finalAmount = grandTotal,
            paymentMethod = method,
            paymentStatus = status,
            sellerId = selectedSellerId,
            sellerName = sellerNameInput
        )

        viewLifecycleOwner.lifecycleScope.launch {
            val saleId = repository.insertSale(sale, cartItems)
            val createdSale = sale.copy(id = saleId)
            val finalItems = cartItems.toList()
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sale Recorded • Invoice #$saleId")
                .setMessage("Total: ${grandTotal.money()} • $custName\n\nWould you like to print a thermal receipt or share invoice?")
                .setPositiveButton("🖨️ Thermal Receipt") { _, _ ->
                    com.shopkeeper.mobileshop.utils.ThermalPrintHelper.showSaleReceiptDialog(requireContext(), createdSale, finalItems)
                    findNavController().popBackStack()
                }
                .setNeutralButton("PDF Invoice") { _, _ ->
                    val file = InvoiceGenerator.generate(requireContext(), createdSale, finalItems)
                    val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
                    findNavController().popBackStack()
                }
                .setNegativeButton("Done") { _, _ ->
                    findNavController().popBackStack()
                }
                .setCancelable(false)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
