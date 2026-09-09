# Mobile Shop Manager

A comprehensive, offline-first Android application designed for shopkeepers to manage their mobile shop business. It includes inventory management, sales tracking, repair ticketing, customer management, due tracking, and expense logging, with optional real-time sync across devices using Firebase.

## Features Added

* **Multi-Role Authentication**: 
  * Login as **Shop Owner** or **Salesman** using Email/Password, Google Sign-In, or Phone (SMS OTP) authentication.
  * Local Biometric (Fingerprint/Face) unlock for quick subsequent access.
  * Role-based access control (Salesmen can be restricted from certain settings).
* **Inventory Management**: 
  * Add, edit, and delete products (mobiles, accessories, etc.).
  * Track stock levels, purchase prices, and sale prices.
  * Barcode scanning support for quick product lookup.
* **Point of Sale (Sales & Cart)**:
  * Create new sales with a shopping cart interface.
  * Add products to the cart and apply dynamic discounts.
  * Support for Cash, Card, and Due (Credit) payment methods.
  * Generate and print Bluetooth thermal receipts (using Thermal Printer ESC/POS).
* **Repair Management**:
  * Track customer devices submitted for repair.
  * Update statuses (Pending, In Progress, Completed, Delivered).
  * Record estimated costs, advance payments, and device issues.
* **Customer & Dues Tracker**:
  * Manage a customer database with contact details.
  * Track pending dues and log partial or full payments.
* **Purchases & Expense Logging**:
  * Track incoming stock purchases from suppliers.
  * Log daily operational expenses.
* **IMEI Tracking**:
  * Advanced search to track the complete lifecycle of a specific device (by IMEI/Serial) across purchases, inventory, and sales.
* **Online Catalog & Firebase Sync**:
  * Sync shop data in real-time using Firebase Realtime Database.
  * Publish products to an Online Catalog for customers to view.
* **Offline-First Architecture**:
  * Built heavily on Room Local Database to ensure the app works 100% offline and syncs in the background when online.

## Login Features & Authentication Workflow

The app features a robust `LockScreenActivity` that acts as the gateway. 

* **Email & Password**: Standard sign-up and login.
* **Forgot Password**: Fully implemented. Users can enter their email to receive a Firebase-generated password reset link.
* **Phone Auth (SMS OTP)**: Users can enter their phone number to receive a 6-digit OTP code for secure login.
* **Google Sign-In**: 1-tap Google login using the latest Google Auth APIs.
* **Email Verification**: Currently, Email/Password sign-ups are allowed instantly. For production, you may want to enforce `firebaseUser.isEmailVerified` before allowing entry.

### Firebase Authentication (Flaws & Limitations to Note)
While the code for OTP and Password Reset is complete, **Authentication will silently fail or throw errors if Firebase is not properly configured on your end**. 

* **OTP "Missing" / Fails to Send**: Firebase Phone Auth strictly requires a valid **SHA-1 and SHA-256 fingerprint** of your signing certificate to be added to the Firebase Console. Furthermore, the **Phone provider** must be explicitly enabled in the Firebase Auth settings. If these are missing, OTP requests are blocked by Firebase to prevent spam/abuse.
* **Forgot Password / Email**: The "Send Reset Link" will only work if the **Email/Password provider** is enabled in the Firebase Console.
* **Google Sign-In**: Requires the `Web Client ID` to be correctly placed in the code or synced via `google-services.json`.
* **Security Rules**: By default, Realtime Database might block reads/writes if the Firebase rules are left in "locked" mode.

## Work Needed From Your Side (Setup Instructions)

To get this app fully operational for production, you **must** perform the following steps:

1. **Create a Firebase Project**: Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
2. **Add Android App**: Register your app with the package name `com.shopkeeper.mobileshop`.
3. **Add SHA Fingerprints (CRITICAL for OTP & Google Login)**:
   * Generate your app's SHA-1 and SHA-256 keys (using `keytool` or Android Studio's Gradle signing report).
   * Add both keys to your Firebase Project settings.
4. **Download `google-services.json`**: Place the downloaded file in the `app/` directory of this codebase, overwriting the placeholder one.
5. **Enable Authentication Providers**:
   * Go to Firebase Authentication -> Sign-in method.
   * Enable **Email/Password**.
   * Enable **Phone**.
   * Enable **Google**.
6. **Configure Realtime Database**:
   * Create a Realtime Database in Firebase.
   * Update the Security Rules to allow authenticated users to read/write their shop's data. A basic rule could be:
     ```json
     {
       "rules": {
         ".read": "auth != null",
         ".write": "auth != null"
       }
     }
     ```
7. **Build and Release**: Once the Firebase config is injected, build the release APK/AAB and test the OTP and sync features on a physical device. Note: Android Emulators sometimes struggle with SafetyNet/Play Integrity checks required for Firebase Phone Auth.
