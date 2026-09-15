with open("app/src/main/java/com/shopkeeper/mobileshop/data/catalog/OnlineCatalogRepository.kt", "r") as f:
    text = f.read()

replacement = """package com.shopkeeper.mobileshop.data.catalog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object OnlineCatalogRepository {
    var allOnlineModels = listOf<OnlinePhoneModel>()

    suspend fun fetchLiveModels() {
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://dummyjson.com/products/category/smartphones")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val jsonObject = JSONObject(response.toString())
                    val productsArray = jsonObject.getJSONArray("products")
                    val newModels = mutableListOf<OnlinePhoneModel>()

                    for (i in 0 until productsArray.length()) {
                        val productObj = productsArray.getJSONObject(i)
                        val model = OnlinePhoneModel(
                            id = productObj.getInt("id").toString(),
                            brand = productObj.optString("brand", "Unknown"),
                            modelName = productObj.optString("title", "Unknown"),
                            specs = productObj.optString("description", "No specs"),
                            ramOptions = "8GB RAM", // default dummy
                            storageOptions = "256GB", // default dummy
                            display = "Modern Display",
                            camera = "AI Camera",
                            battery = "5000 mAh",
                            colors = "Various Colors"
                        )
                        newModels.add(model)
                    }
                    
                    if (newModels.isNotEmpty()) {
                        allOnlineModels = newModels
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getBrands(): List<String> = listOf("All") + allOnlineModels.map { it.brand }.distinct()

    fun filter(brand: String, query: String): List<OnlinePhoneModel> {
        return allOnlineModels.filter { model ->
            val matchesBrand = brand == "All" || model.brand.equals(brand, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                model.fullName.contains(query, ignoreCase = true) ||
                model.specs.contains(query, ignoreCase = true)
            matchesBrand && matchesQuery
        }
    }
}
"""

with open("app/src/main/java/com/shopkeeper/mobileshop/data/catalog/OnlineCatalogRepository.kt", "w") as f:
    f.write(replacement)
