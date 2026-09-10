# Room
-keep class com.shopkeeper.mobileshop.data.db.entity.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }

# Firebase
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.firestore.** { *; }

# ZXing Barcode Scanner
-keep class com.journeyapps.barcodescanner.** { *; }

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.** { *; }
