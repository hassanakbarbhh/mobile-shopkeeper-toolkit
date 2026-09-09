# Section 2 Restore Missing Features Implementation Plan

1. **Scanner (ZXing)**: Ensure the ZXing dependency is added and create a ScannerActivity or helper that can be called from POS and Inventory. (Is it already there?)
2. **WhatsApp Integration**: Add a function to share invoices/receipts via WhatsApp.
3. **PDF Invoice**: Implement PDF generation for sales receipts.
4. **Backup/Restore (SAF)**: Provide local Storage Access Framework integration to backup the Room database to JSON or SQLite files.
5. **2FA TOTP**: Implement Time-based One-Time Password for sensitive operations using a standard TOTP algorithm.
6. **Themes/Lang (Urdu/RTL)**: Ensure `values-ur/strings.xml` exists and RTL support is enabled in the Manifest.
7. **Reports**: Add standard reporting fragments/activities (Sales, Profit, Dues) using MPAndroidChart.
