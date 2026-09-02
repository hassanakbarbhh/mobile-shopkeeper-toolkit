package com.shopkeeper.mobileshop.ui.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopkeeper.mobileshop.data.db.AppDatabase
import com.shopkeeper.mobileshop.data.db.entity.Expense
import com.shopkeeper.mobileshop.data.db.entity.ExpenseCategory
import com.shopkeeper.mobileshop.data.repository.ShopRepository
import com.shopkeeper.mobileshop.databinding.DialogExpenseBinding
import com.shopkeeper.mobileshop.databinding.FragmentExpensesBinding
import com.shopkeeper.mobileshop.utils.money
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: ShopRepository
    private lateinit var adapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        repository = ShopRepository(db)

        adapter = ExpenseAdapter { expense ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Expense?")
                .setMessage("${expense.title} - ${expense.amount.money()}")
                .setPositiveButton("Delete") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        db.expenseDao().delete(expense)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter

        binding.fabAddExpense.setOnClickListener { showAddExpenseDialog() }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.allExpenses.collectLatest { list ->
                adapter.submitList(list)
                val total = list.sumOf { it.amount }
                binding.tvMonthTotal.text = total.money()
            }
        }
    }

    private fun showAddExpenseDialog() {
        val dBinding = DialogExpenseBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Daily Expense")
            .setView(dBinding.root)
            .create()

        val cats = ExpenseCategory.values().map { it.name }
        dBinding.spExpCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, cats)

        dBinding.btnSaveExpense.setOnClickListener {
            val title = dBinding.etExpTitle.text.toString().trim()
            val amount = dBinding.etExpAmount.text.toString().toDoubleOrNull() ?: 0.0
            val notes = dBinding.etExpNotes.text.toString().trim()
            val cat = ExpenseCategory.valueOf(dBinding.spExpCategory.selectedItem.toString())

            if (title.isEmpty() || amount <= 0.0) {
                Toast.makeText(requireContext(), "Title & Amount required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                repository.insertExpense(
                    Expense(title = title, category = cat, amount = amount, notes = notes)
                )
                dialog.dismiss()
                Toast.makeText(requireContext(), "Expense logged", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
