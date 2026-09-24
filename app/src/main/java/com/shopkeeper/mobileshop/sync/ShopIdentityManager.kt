package com.shopkeeper.mobileshop.sync

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.shopkeeper.mobileshop.data.db.entity.OutboxOperation
import com.shopkeeper.mobileshop.utils.UserAuthManager
import java.util.UUID

object ShopIdentityManager {
    private const val PREFS = "shop_identity_secure_prefs"
    private const val KEY_DEVICE_ID = "identity_device_id"
    private const val KEY_SHOP_ID = "identity_shop_id"
    private const val KEY_ORG_ID = "identity_org_id"

    @Synchronized
    fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId.isNullOrBlank()) {
            deviceId = "DEV_" + UUID.randomUUID().toString().substring(0, 8).uppercase()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }

    fun getShopId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val savedShopId = prefs.getString(KEY_SHOP_ID, null)
        if (!savedShopId.isNullOrBlank()) {
            return savedShopId
        }

        // Try getting from current user
        val currentUser = UserAuthManager.getCurrentUser(context)
        val candidate = if (!currentUser?.email.isNullOrBlank()) {
            "SHOP_" + Math.abs(currentUser.email.hashCode()).toString().take(6)
        } else {
            "SHOP_MAIN"
        }
        setShopId(context, candidate)
        return candidate
    }

    fun setShopId(context: Context, shopId: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SHOP_ID, shopId)
            .apply()
    }

    fun getOrganizationId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val savedOrgId = prefs.getString(KEY_ORG_ID, null)
        if (!savedOrgId.isNullOrBlank()) {
            return savedOrgId
        }
        val orgId = "ORG_" + getShopId(context).removePrefix("SHOP_")
        prefs.edit().putString(KEY_ORG_ID, orgId).apply()
        return orgId
    }

    fun getUserId(context: Context): String {
        val fbUid = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
        if (!fbUid.isNullOrBlank()) return fbUid

        val sessionUser = UserAuthManager.getCurrentUser(context)
        if (!sessionUser?.email.isNullOrBlank()) return sessionUser.email

        return "USER_LOCAL"
    }

    fun buildOutboxOperation(
        context: Context,
        operationType: String,
        entityType: String,
        entityId: String,
        payloadJson: String
    ): OutboxOperation {
        return OutboxOperation(
            eventId = UUID.randomUUID().toString(),
            operationType = operationType,
            entityType = entityType,
            entityId = entityId,
            payloadJson = payloadJson,
            status = OutboxOperation.STATUS_PENDING,
            retryCount = 0,
            createdAt = System.currentTimeMillis(),
            lastAttemptAt = 0L,
            errorMessage = null,
            deviceId = getDeviceId(context),
            shopId = getShopId(context),
            userId = getUserId(context)
        )
    }
}
