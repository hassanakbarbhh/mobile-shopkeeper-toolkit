# Changelog

## [Unreleased] — fix/p0-data-integrity

### Fixed (P0 data integrity)

- **Purchase flow is now atomic and complete** (`ShopRepository.insertPurchase`):
  records purchase + items, increases stock for every matched product
  (by explicit productId, non-blank IMEI, or name match), records the unit
  cost into the product's cost basis, and adds the unpaid remainder to the
  supplier's payable balance. Any failure rolls back the entire transaction —
  no purchase record, payable, or stock change is left half-applied.
- **`recordPayment` is now atomic** (`ShopRepository`): payment insert and the
  dependent sale payment-status recompute happen in one Room transaction.
- **Demo seed data removed** (`AppDatabase`): fresh installs no longer contain
  hardcoded demo sellers, a fake IMEI asset with a fake invoice, or personal
  phone numbers. Catalog seeding now happens only when the products table is
  completely empty.
- **Supplier payable tracking**: `Supplier.balance` field added (positive =
  shop owes supplier), with `MIGRATION_10_11` (DB v10 → v11). Existing
  suppliers start at 0.00; legacy purchases were never tracked, so historical
  balances must be corrected manually rather than guessed.

### Tests

- New `P0DataIntegrityFixTest` (Robolectric): purchase stock/cost recording,
  credit purchase payable, rollback on missing product, rollback on missing
  supplier, fully-paid purchase leaves balance untouched, partial payment
  status, full payment clears dues, payment without sale, no demo seed data.

### Known limitations

- Legacy purchase records are not retroactively applied to stock or supplier
  balances (they were recorded without those effects); new purchases behave
  correctly.
- Tests and build were not executed locally in this session (no Android SDK
  available); CI must verify compilation and test passage before merge.
