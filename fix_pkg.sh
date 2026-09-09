sed -i '1,6d' app/src/main/java/com/shopkeeper/mobileshop/utils/ThermalPrintHelper.kt
sed -i '1i\package com.shopkeeper.mobileshop.utils\n\nimport kotlinx.coroutines.launch\nimport kotlinx.coroutines.CoroutineScope\nimport kotlinx.coroutines.Dispatchers' app/src/main/java/com/shopkeeper/mobileshop/utils/ThermalPrintHelper.kt
