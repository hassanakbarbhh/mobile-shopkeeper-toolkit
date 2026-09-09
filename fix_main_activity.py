import re

with open("app/src/main/java/com/shopkeeper/mobileshop/MainActivity.kt", "r") as f:
    content = f.read()

# Replace the broken navigation listener
old_nav_listener = """        binding.navView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.navigation_access_control) {
                binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
                startActivity(Intent(this@MainActivity, com.shopkeeper.mobileshop.ui.auth.AccessControlActivity::class.java))
                return@setNavigationItemSelectedListener true
                AppMode.BASIC_USER -> {
                tvHeaderRoleBadge?.text = "Pending Approval"
                for (i in 0 until menu.size()) {
                    menu.getItem(i).isVisible = false
                }
                menu.findItem(R.id.navigation_lock_app)?.isVisible = true
            }
        }
            if (menuItem.itemId == R.id.navigation_lock_app) {"""

new_nav_listener = """        binding.navView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.navigation_access_control) {
                binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
                startActivity(Intent(this@MainActivity, com.shopkeeper.mobileshop.ui.auth.AccessControlActivity::class.java))
                return@setNavigationItemSelectedListener true
            }
            if (menuItem.itemId == R.id.navigation_lock_app) {"""

content = content.replace(old_nav_listener, new_nav_listener)

# Fix configureDrawerForRole and add BASIC_USER
old_configure = """    private fun configureDrawerForRole(mode: AppMode) {
        when (mode) {"""

new_configure = """    private fun configureDrawerForRole(mode: AppMode) {
        val headerView = binding.navView.getHeaderView(0)
        val tvHeaderRoleBadge = headerView?.findViewById<android.widget.TextView>(R.id.tvHeaderRoleBadge)
        val menu = binding.navView.menu

        when (mode) {"""

content = content.replace(old_configure, new_configure)

# Add BASIC_USER to configureDrawerForRole when exhaustive
old_repair_tech = """                // Start on Repairs
                navController.navigate(R.id.navigation_repairs)
            }
        }
    }"""

new_repair_tech = """                // Start on Repairs
                navController.navigate(R.id.navigation_repairs)
            }
            AppMode.BASIC_USER -> {
                tvHeaderRoleBadge?.text = "Pending Approval"
                for (i in 0 until menu.size()) {
                    menu.getItem(i).isVisible = false
                }
                menu.findItem(R.id.navigation_lock_app)?.isVisible = true
            }
            else -> {}
        }
    }"""

content = content.replace(old_repair_tech, new_repair_tech)

# Fix exhaustive when in onBackPressedDispatcher
old_on_back = """                    val isStartDest = when (activeMode) {
                        AppMode.SHOP_OWNER -> currentDest == R.id.navigation_dashboard
                        AppMode.SELLER_STAFF -> currentDest == R.id.navigation_sales || currentDest == R.id.navigation_new_sale
                        AppMode.REPAIR_TECH -> currentDest == R.id.navigation_repairs
                    }"""

new_on_back = """                    val isStartDest = when (activeMode) {
                        AppMode.OWNER, AppMode.SHOP_OWNER -> currentDest == R.id.navigation_dashboard
                        AppMode.SELLER_STAFF -> currentDest == R.id.navigation_sales || currentDest == R.id.navigation_new_sale
                        AppMode.REPAIR_TECH -> currentDest == R.id.navigation_repairs
                        else -> true
                    }"""
content = content.replace(old_on_back, new_on_back)

with open("app/src/main/java/com/shopkeeper/mobileshop/MainActivity.kt", "w") as f:
    f.write(content)
