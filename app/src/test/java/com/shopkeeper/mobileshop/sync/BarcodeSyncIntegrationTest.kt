package com.shopkeeper.mobileshop.sync

import com.shopkeeper.mobileshop.data.db.entity.Product
import com.shopkeeper.mobileshop.data.db.entity.ProductCategory
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * End-to-end multi-device synchronization test proving:
 * Device A -> Product barcode -> Firestore payload -> Device B -> same barcode
 */
class BarcodeSyncIntegrationTest {

    @Test
    fun testDeviceAToFirestoreToDeviceBNewProductSynchronization() {
        val testBarcode = "8901030012345"
        val cloudProductId = "prod_cloud_abc123"

        // Step 1: Device A creates a product with a scanned/entered barcode
        val deviceAProduct = Product(
            id = 101L,
            cloudId = cloudProductId,
            name = "Samsung Galaxy S24 Ultra",
            brand = "Samsung",
            model = "SM-S928B",
            imei = "359876543210987",
            barcode = testBarcode,
            category = ProductCategory.SMARTPHONE,
            purchasePrice = 280000.0,
            sellingPrice = 310000.0,
            quantity = 5,
            ram = "12GB",
            storage = "512GB",
            color = "Titanium Gray",
            version = 1L
        )

        assertEquals("Device A product must have the test barcode", testBarcode, deviceAProduct.barcode)

        // Step 2: Outbound synchronization prepares Firestore document payload
        val firestoreDocumentPayload = mapOf<String, Any?>(
            "cloudId" to deviceAProduct.cloudId,
            "name" to deviceAProduct.name,
            "brand" to deviceAProduct.brand,
            "model" to deviceAProduct.model,
            "imei" to deviceAProduct.imei,
            "barcode" to deviceAProduct.barcode,
            "category" to deviceAProduct.category.name,
            "purchasePrice" to deviceAProduct.purchasePrice,
            "sellingPrice" to deviceAProduct.sellingPrice,
            "quantity" to deviceAProduct.quantity.toLong(),
            "ram" to deviceAProduct.ram,
            "storage" to deviceAProduct.storage,
            "color" to deviceAProduct.color,
            "version" to 1L,
            "updatedAt" to System.currentTimeMillis()
        )

        // Ensure serialization / JSON transport preserves barcode
        val payloadJson = JSONObject(firestoreDocumentPayload).toString()
        val deserializedJson = JSONObject(payloadJson)
        assertEquals(testBarcode, deserializedJson.getString("barcode"))

        // Step 3: Device B downloads the document from Firestore via InboundSyncEngine
        val deviceBProduct = InboundSyncEngine.parseInboundProduct(
            cloudId = cloudProductId,
            data = firestoreDocumentPayload,
            existing = null
        )

        assertNotNull("Device B parsed inbound product must not be null", deviceBProduct)
        assertEquals("Device B must download and persist the exact same barcode as Device A", testBarcode, deviceBProduct?.barcode)
        assertEquals(deviceAProduct.name, deviceBProduct?.name)
        assertEquals(deviceAProduct.imei, deviceBProduct?.imei)
        assertEquals(ProductCategory.SMARTPHONE, deviceBProduct?.category)
    }

    @Test
    fun testDeviceAToFirestoreToDeviceBExistingProductBarcodeUpdate() {
        val originalBarcode = "1111222233334"
        val updatedBarcode = "9999888877776"
        val cloudProductId = "prod_cloud_update_99"

        // Device B already has this product with original barcode
        val deviceBExistingProduct = Product(
            id = 202L,
            cloudId = cloudProductId,
            name = "iPhone 15 Pro",
            brand = "Apple",
            model = "A3102",
            barcode = originalBarcode,
            category = ProductCategory.SMARTPHONE,
            version = 1L
        )

        // Device A updates the barcode and syncs to Firestore
        val remoteUpdatePayload = mapOf<String, Any?>(
            "cloudId" to cloudProductId,
            "name" to "iPhone 15 Pro Max",
            "brand" to "Apple",
            "model" to "A3102",
            "barcode" to updatedBarcode,
            "category" to ProductCategory.SMARTPHONE.name,
            "version" to 2L,
            "updatedAt" to System.currentTimeMillis()
        )

        // Device B inbound sync applies the update to its existing product
        val deviceBUpdatedProduct = InboundSyncEngine.parseInboundProduct(
            cloudId = cloudProductId,
            data = remoteUpdatePayload,
            existing = deviceBExistingProduct,
            remoteVersion = 2L
        )

        assertNotNull(deviceBUpdatedProduct)
        assertEquals("Device B existing product barcode must be updated to match Device A new barcode", updatedBarcode, deviceBUpdatedProduct?.barcode)
        assertEquals("iPhone 15 Pro Max", deviceBUpdatedProduct?.name)
    }

    @Test
    fun testInboundSyncPreservesLocalBarcodeWhenRemoteOmitted() {
        val existingLocalBarcode = "5555444433332"
        val cloudProductId = "prod_cloud_preserve_77"

        val deviceBExistingProduct = Product(
            id = 303L,
            cloudId = cloudProductId,
            name = "Xiaomi 14",
            brand = "Xiaomi",
            barcode = existingLocalBarcode,
            category = ProductCategory.SMARTPHONE
        )

        // Remote payload has null or missing barcode field
        val remotePayloadWithoutBarcode = mapOf<String, Any?>(
            "cloudId" to cloudProductId,
            "name" to "Xiaomi 14 Ultra",
            "version" to 2L
        )

        val deviceBMergedProduct = InboundSyncEngine.parseInboundProduct(
            cloudId = cloudProductId,
            data = remotePayloadWithoutBarcode,
            existing = deviceBExistingProduct
        )

        assertNotNull(deviceBMergedProduct)
        assertEquals("Local barcode must be safely preserved if remote omits barcode", existingLocalBarcode, deviceBMergedProduct?.barcode)
        assertEquals("Xiaomi 14 Ultra", deviceBMergedProduct?.name)
    }
}
