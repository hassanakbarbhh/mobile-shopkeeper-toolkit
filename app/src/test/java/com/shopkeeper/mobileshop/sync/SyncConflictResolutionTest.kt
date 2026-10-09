package com.shopkeeper.mobileshop.sync

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncConflictResolutionTest {

    @Test
    fun testStaleRemoteVersionNeverOverwritesNewerLocalVersion() {
        // Local has version 10 (updated at 1000). Remote has stale version 2 with a drifting/future timestamp 2000.
        val shouldApply = InboundSyncEngine.shouldApplyRemote(
            hasPendingLocal = false,
            localVersion = 10L,
            remoteVersion = 2L,
            localUpdatedAt = 1000L,
            remoteUpdatedAt = 2000L
        )
        assertFalse("Stale remote version must NEVER overwrite a newer local version even if remote clock is ahead", shouldApply)
    }

    @Test
    fun testStaleRemoteTimestampNeverOverwritesNewerLocalWhenVersionTied() {
        // Both local and remote have version 5. Local was updated at 1500; remote was updated at 1200 (stale edit).
        val shouldApply = InboundSyncEngine.shouldApplyRemote(
            hasPendingLocal = false,
            localVersion = 5L,
            remoteVersion = 5L,
            localUpdatedAt = 1500L,
            remoteUpdatedAt = 1200L
        )
        assertFalse("Stale remote timestamp must not overwrite newer local edit when versions are identical", shouldApply)
    }

    @Test
    fun testIdenticalVersionAndTimestampDoesNotTriggerRedundantApply() {
        val shouldApply = InboundSyncEngine.shouldApplyRemote(
            hasPendingLocal = false,
            localVersion = 3L,
            remoteVersion = 3L,
            localUpdatedAt = 1000L,
            remoteUpdatedAt = 1000L
        )
        assertFalse("Identical version and timestamp does not require redundant re-apply", shouldApply)
    }

    @Test
    fun testNewerRemoteVersionAppliesSuccessfully() {
        val shouldApply = InboundSyncEngine.shouldApplyRemote(
            hasPendingLocal = false,
            localVersion = 2L,
            remoteVersion = 3L,
            localUpdatedAt = 1000L,
            remoteUpdatedAt = 1100L
        )
        assertTrue("Newer remote version must apply", shouldApply)
    }

    @Test
    fun testSameVersionWithNewerRemoteTimestampApplies() {
        val shouldApply = InboundSyncEngine.shouldApplyRemote(
            hasPendingLocal = false,
            localVersion = 4L,
            remoteVersion = 4L,
            localUpdatedAt = 1000L,
            remoteUpdatedAt = 1200L
        )
        assertTrue("Same version with newer remote timestamp must apply as tie-breaker", shouldApply)
    }

    @Test
    fun testPendingLocalMutationsProtectedUnlessRemoteIsStrictlyNewerInBoth() {
        // Local has uncommitted mutation in Outbox (version 2, updated at 1000)
        // Case A: Remote has newer timestamp (1200) but same version (2) -> must protect local uncommitted mutation
        val shouldApplySameVer = InboundSyncEngine.shouldApplyRemote(
            hasPendingLocal = true,
            localVersion = 2L,
            remoteVersion = 2L,
            localUpdatedAt = 1000L,
            remoteUpdatedAt = 1200L
        )
        assertFalse("Pending local mutation in outbox must be protected if remote version is not strictly higher", shouldApplySameVer)

        // Case B: Remote has strictly higher version (3) and strictly newer timestamp (1500) -> safe to merge/apply
        val shouldApplyStrictlyNewer = InboundSyncEngine.shouldApplyRemote(
            hasPendingLocal = true,
            localVersion = 2L,
            remoteVersion = 3L,
            localUpdatedAt = 1000L,
            remoteUpdatedAt = 1500L
        )
        assertTrue("Strictly newer remote (version + timestamp) overrides pending local mutation", shouldApplyStrictlyNewer)
    }
}
