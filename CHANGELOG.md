# Changelog

## [Unreleased] — fix/p0-data-integrity

### Fixed (P0 data integrity)

- **Purchase flow is now atomic and complete** (`ShopRepository.insertPurchase`):
  records purchase + items, increases stock for every matched product
  (by explicit productId, non-blank IMEI, or name match), records the unit
  cost into the product's cost basis, and adds the unpaid remainder to the
  supplier's payable balance. Any failure rolls back the entire transaction.
- **`recordPayment` is now atomic** (`ShopRepository`): payment insert and the
  dependent sale payment-status recompute happen in one Room transaction.
- **Demo seed data removed** (`AppDatabase`): fresh installs no longer contain
  hardcoded demo sellers, a fake IMEI asset with a fake invoice, or personal
  phone numbers. Catalog seeding now happens only when the products table is
  completely empty.
- **Supplier payable tracking**: `Supplier.balance` field added (positive =
  shop owes supplier), with `MIGRATION_10_11` (DB v10 → v11). Existing
  suppliers start at 0.00; historical balances must be corrected manually
  rather than guessed.
- **Release signing no longer falls back to the debug keystore** (BUG-006,
  `app/build.gradle.kts`): `assembleRelease` now fails loudly when release
  credentials are absent instead of silently producing a debug-signed APK.

### Fixed (P1 business logic)

- **IMEI lifecycle integration**: purchases register/refresh `ImeiAsset`
  records (IN_STOCK + lifecycle event) for serial lines; sales mark matching
  assets SOLD with customer/invoice linkage and a SOLD lifecycle event.
  Blank/placeholder IMEIs never create assets.
- **`SyncEngine.scheduleSync` now honors `forceExpedited`**: manual
  "Sync Now" submits an expedited WorkManager request instead of silently
  queueing a normal background request.

### Fixed (P2 delete reversal)

- **`deleteSale`** now restocks, restores affected IMEI assets to RETURNED
  (only when still tied to that sale), and records RETURNED lifecycle events,
  all in one transaction.
- **New `deletePurchaseWithReversal`**: reduces stock (never below zero),
  tombstones assets registered by that purchase (only while IN_STOCK — sold
  devices are never touched), and decreases supplier payable without letting
  it go negative.

### Tests

- `P0DataIntegrityFixTest` (Robolectric), 14 tests: purchase stock/cost,
  credit payable, rollback on missing product/supplier, fully-paid purchase,
  partial/full payment transitions, payment without sale, no-demo-seed,
  purchase IMEI asset registration, blank-IMEI never creates assets,
  sale marks asset SOLD, sale rollback leaves no asset, purchase delete
  reversal (stock + payable).

### Known limitations

- Legacy purchase records are not retroactively applied to stock or supplier
  balances; new purchases behave correctly.
- Tests and build were not executed locally in this session (no Android SDK
  available); CI must verify compilation and test passage before merge.
- Repository-root legacy `patch_*`/`fix_*` scripts (~40 files) still need
  removal via `git rm` (connector cannot delete files).
