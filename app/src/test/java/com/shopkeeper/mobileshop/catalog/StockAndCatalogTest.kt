package com.shopkeeper.mobileshop.catalog

import com.shopkeeper.mobileshop.data.catalog.OnlineCatalogRepository
import com.shopkeeper.mobileshop.data.db.entity.ProductCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StockAndCatalogTest {

    @Test
    fun testAllMajorPhoneBrandsIncludedInCatalog() {
        val brands = OnlineCatalogRepository.allOnlineModels.map { it.brand }.distinct()
        assertTrue("Catalog must include Apple", brands.contains("Apple"))
        assertTrue("Catalog must include Samsung", brands.contains("Samsung"))
        assertTrue("Catalog must include Xiaomi", brands.contains("Xiaomi"))
        assertTrue("Catalog must include Infinix", brands.contains("Infinix"))
        assertTrue("Catalog must include Tecno", brands.contains("Tecno"))
        assertTrue("Catalog must include Vivo", brands.contains("Vivo"))
        assertTrue("Catalog must include Oppo", brands.contains("Oppo"))
        assertTrue("Catalog must include Realme", brands.contains("Realme"))
        assertTrue("Catalog must include OnePlus", brands.contains("OnePlus"))
        assertTrue("Catalog must include Google", brands.contains("Google"))
        assertTrue("At least 10 major phone brands present", brands.size >= 10)
    }

    @Test
    fun testCatalogHasSubstantialPhoneModels() {
        val models = OnlineCatalogRepository.allOnlineModels
        assertTrue("Catalog should contain numerous phone models", models.size >= 30)
    }

    @Test
    fun testPhoneModelConvertsToProductWithNoPrice() {
        val models = OnlineCatalogRepository.allOnlineModels
        val firstModel = models.first()
        val product = firstModel.toProductNoPrice()

        assertEquals("Product name must match phone full name", firstModel.fullName, product.name)
        assertEquals("Selling price must be 0.0 (No Price)", 0.0, product.sellingPrice, 0.001)
        assertEquals("Purchase price must be 0.0 (No Cost)", 0.0, product.purchasePrice, 0.001)
        assertEquals("Stock quantity must be 0 (Default empty stock)", 0, product.quantity)
        assertEquals("Category must be SMARTPHONE", ProductCategory.SMARTPHONE, product.category)
        assertTrue("Brand must be non-blank", product.brand.isNotBlank())
        assertTrue("Model must be non-blank", product.model.isNotBlank())
    }

    @Test
    fun testAllModelsConvertWithZeroPrice() {
        val models = OnlineCatalogRepository.allOnlineModels
        val products = models.map { it.toProductNoPrice() }

        assertEquals(models.size, products.size)
        products.forEach { prod ->
            assertEquals("All seeded default phones must have 0.0 selling price", 0.0, prod.sellingPrice, 0.0)
            assertEquals("All seeded default phones must have 0.0 purchase price", 0.0, prod.purchasePrice, 0.0)
            assertEquals("All seeded default phones must have 0 stock quantity", 0, prod.quantity)
            assertFalse("Product name should not be blank", prod.name.isBlank())
            assertEquals("Category must be SMARTPHONE", ProductCategory.SMARTPHONE, prod.category)
        }
    }
}
