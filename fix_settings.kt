        binding.btnAdvancedDataMenu.setOnClickListener { view ->
            val popup = android.widget.PopupMenu(requireContext(), view)
            popup.menu.add(0, 1, 0, "Export Database File")
            popup.menu.add(0, 2, 0, "Import & Restore Database")
            popup.menu.add(0, 3, 0, "Export Inventory (CSV)")
            popup.menu.add(0, 4, 0, "Export Sales (CSV)")
            popup.menu.add(0, 5, 0, "Export Master Ledger (PDF)")
            
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> binding.btnBackupDatabase.performClick()
                    2 -> binding.btnRestoreDatabase.performClick()
                    3 -> Toast.makeText(requireContext(), "Exporting Inventory...", Toast.LENGTH_SHORT).show()
                    4 -> Toast.makeText(requireContext(), "Exporting Sales...", Toast.LENGTH_SHORT).show()
                    5 -> Toast.makeText(requireContext(), "Generating Master Ledger PDF...", Toast.LENGTH_SHORT).show()
                }
                true
            }
            popup.show()
        }
