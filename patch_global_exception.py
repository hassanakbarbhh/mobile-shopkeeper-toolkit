import re

with open("app/src/main/java/com/shopkeeper/mobileshop/utils/GlobalExceptionHandler.kt", "r") as f:
    content = f.read()

clear_logs = """    fun clearLogs(context: Context) {
        try {
            val logFile = File(context.filesDir, LOG_FILE_NAME)
            if (logFile.exists()) {
                logFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear logs", e)
        }
    }
}"""

content = content.replace("}", clear_logs)

with open("app/src/main/java/com/shopkeeper/mobileshop/utils/GlobalExceptionHandler.kt", "w") as f:
    f.write(content)
