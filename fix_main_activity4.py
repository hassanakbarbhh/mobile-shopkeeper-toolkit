with open("app/src/main/java/com/shopkeeper/mobileshop/MainActivity.kt", "r") as f:
    content = f.read()

old_block = """            if (menuItem.itemId == R.id.navigation_access_control) {
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
        }"""
new_block = """            if (menuItem.itemId == R.id.navigation_access_control) {
                binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
                startActivity(Intent(this@MainActivity, com.shopkeeper.mobileshop.ui.auth.AccessControlActivity::class.java))
                return@setNavigationItemSelectedListener true
            }"""
content = content.replace(old_block, new_block)

with open("app/src/main/java/com/shopkeeper/mobileshop/MainActivity.kt", "w") as f:
    f.write(content)
