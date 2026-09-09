package com.shopkeeper.mobileshop

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.shopkeeper.mobileshop.databinding.ActivityMainBinding
import com.shopkeeper.mobileshop.ui.auth.LockScreenActivity
import com.shopkeeper.mobileshop.utils.AppMode
import com.shopkeeper.mobileshop.utils.AppPreferences
import com.shopkeeper.mobileshop.utils.CurrencyManager
import com.shopkeeper.mobileshop.utils.ShopProfile

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        com.shopkeeper.mobileshop.utils.ThemeManager.applyNightMode(this)
        com.shopkeeper.mobileshop.utils.ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CurrencyManager.init(this)


        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val allDestinations = setOf(
            R.id.navigation_dashboard,
            R.id.navigation_new_sale,
            R.id.navigation_sales,
            R.id.navigation_inventory,
            R.id.navigation_repairs,
            R.id.navigation_purchases,
            R.id.navigation_imei,
            R.id.navigation_dues,
            R.id.navigation_expenses,
            R.id.navigation_reports,
            R.id.navigation_settings,
            R.id.navigation_sellers,
            R.id.navigation_customers,
            R.id.navigation_online_catalog,
            R.id.navigation_about
        )

        appBarConfiguration = AppBarConfiguration(allDestinations, binding.drawerLayout)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        val activeMode = AppPreferences.getActiveMode(this)
        configureDrawerForRole(activeMode)

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.navigation_lock_app) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                val intent = Intent(this, LockScreenActivity::class.java)
                startActivity(intent)
                finish()
                true
            } else {
                val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                if (handled) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
                handled
            }
        }

        // Modern back press callback (fully compatible with Android 13+ Predictive Back and Android 8-12)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    val currentDest = navController.currentDestination?.id
                    val isStartDest = when (activeMode) {
                        AppMode.OWNER, AppMode.SHOP_OWNER -> currentDest == R.id.navigation_dashboard
                        AppMode.SELLER_STAFF -> currentDest == R.id.navigation_sales || currentDest == R.id.navigation_new_sale
                        AppMode.REPAIR_TECH -> currentDest == R.id.navigation_repairs
                        else -> true
                    }
                    if (!isStartDest && navController.navigateUp()) {
                        // Navigated up
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                        isEnabled = true
                    }
                }
            }
        })
    }

    private fun configureDrawerForRole(mode: AppMode) {
        val headerView = binding.navView.getHeaderView(0)
        val tvHeaderRoleBadge = headerView?.findViewById<android.widget.TextView>(R.id.tvHeaderRoleBadge)
        val menu = binding.navView.menu

        when (mode) {
            AppMode.OWNER, AppMode.SHOP_OWNER -> {
                tvHeaderRoleBadge?.text = if (mode == AppMode.OWNER) "👑 App Owner" else "👑 Shop Owner (Master)"
                for (i in 0 until menu.size()) {
                    menu.getItem(i).isVisible = true
                }
            }


            AppMode.SELLER_STAFF -> {
                tvHeaderRoleBadge?.text = "💼 Seller / Staff Mode"
                // Hide sensitive store finances, purchases, expenses, reports & admin settings
                menu.findItem(R.id.navigation_dashboard)?.isVisible = false
                                menu.findItem(R.id.navigation_purchases)?.isVisible = false
                menu.findItem(R.id.navigation_expenses)?.isVisible = false
                menu.findItem(R.id.navigation_reports)?.isVisible = false
                menu.findItem(R.id.navigation_sellers)?.isVisible = false
                menu.findItem(R.id.navigation_settings)?.isVisible = false
                menu.findItem(R.id.navigation_repairs)?.isVisible = false

                // Show seller items
                menu.findItem(R.id.navigation_new_sale)?.isVisible = true
                menu.findItem(R.id.navigation_sales)?.isVisible = true
                menu.findItem(R.id.navigation_inventory)?.isVisible = true
                menu.findItem(R.id.navigation_online_catalog)?.isVisible = true
                menu.findItem(R.id.navigation_customers)?.isVisible = true
                menu.findItem(R.id.navigation_dues)?.isVisible = true
                menu.findItem(R.id.navigation_imei)?.isVisible = true
                menu.findItem(R.id.navigation_about)?.isVisible = true
                menu.findItem(R.id.navigation_lock_app)?.isVisible = true

                // Start on Sales
                navController.navigate(R.id.navigation_sales)
            }

            AppMode.REPAIR_TECH -> {
                tvHeaderRoleBadge?.text = "🔧 Repair Technician Mode"
                // Hide store finances, sales POS, purchases, expenses, reports, sellers & settings
                menu.findItem(R.id.navigation_dashboard)?.isVisible = false
                                menu.findItem(R.id.navigation_new_sale)?.isVisible = false
                menu.findItem(R.id.navigation_sales)?.isVisible = false
                menu.findItem(R.id.navigation_purchases)?.isVisible = false
                menu.findItem(R.id.navigation_expenses)?.isVisible = false
                menu.findItem(R.id.navigation_reports)?.isVisible = false
                menu.findItem(R.id.navigation_sellers)?.isVisible = false
                menu.findItem(R.id.navigation_settings)?.isVisible = false

                // Show repair technician items
                menu.findItem(R.id.navigation_repairs)?.isVisible = true
                menu.findItem(R.id.navigation_inventory)?.isVisible = true // for spare parts & repair devices
                menu.findItem(R.id.navigation_customers)?.isVisible = true
                menu.findItem(R.id.navigation_imei)?.isVisible = true
                menu.findItem(R.id.navigation_about)?.isVisible = true
                menu.findItem(R.id.navigation_lock_app)?.isVisible = true

                // Start on Repairs
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
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}
