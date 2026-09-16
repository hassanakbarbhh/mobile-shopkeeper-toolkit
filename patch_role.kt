    private fun setRole(mode: AppMode) {
        currentRole = mode
        when (mode) {
            AppMode.OWNER, AppMode.SHOP_OWNER -> {
                binding.toggleRoleMode.check(R.id.btnRoleOwner)
                binding.tvRoleHint.text = "👑 Shop Owner: Full master access, profits, purchases & settings"
                binding.btnLoginSubmit.text = "Sign In as Shop Owner"
                binding.btnRegisterSubmit.text = "Register as Shop Owner"
                binding.layoutShopOwnerFields.visibility = View.VISIBLE
                binding.layoutSellerFields.visibility = View.GONE
            }
            AppMode.SELLER_STAFF -> {
                binding.toggleRoleMode.check(R.id.btnRoleSeller)
                binding.tvRoleHint.text = "💼 Seller / Staff: Fast counter checkout, sales & receipts"
                binding.btnLoginSubmit.text = "Sign In as Seller / Staff"
                binding.btnRegisterSubmit.text = "Register as Seller / Staff"
                binding.layoutShopOwnerFields.visibility = View.GONE
                binding.layoutSellerFields.visibility = View.VISIBLE
            }
            AppMode.REPAIR_TECH -> {
                binding.toggleRoleMode.check(R.id.btnRoleTech)
                binding.tvRoleHint.text = "🔧 Repair Technician: Intake jobs, diagnosis & parts tracking"
                binding.btnLoginSubmit.text = "Sign In as Repair Tech"
                binding.btnRegisterSubmit.text = "Register as Repair Tech"
                binding.layoutShopOwnerFields.visibility = View.VISIBLE
                binding.layoutSellerFields.visibility = View.GONE
            }
            AppMode.BASIC_USER -> {
                binding.tvRoleHint.text = "Basic User"
                binding.btnLoginSubmit.text = "Sign In"
                binding.layoutShopOwnerFields.visibility = View.GONE
                binding.layoutSellerFields.visibility = View.GONE
            }
            else -> {}
        }
        binding.tvAuthError.visibility = View.GONE
    }
