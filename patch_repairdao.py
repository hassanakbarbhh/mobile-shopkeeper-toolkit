with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/dao/RepairDao.kt", "r") as f:
    text = f.read()

replacement = """    @Query("SELECT * FROM repairs ORDER BY dateReceived DESC")
    fun getAllRepairs(): Flow<List<Repair>>

    @Query("SELECT * FROM repairs ORDER BY dateReceived DESC")
    suspend fun getAllRepairsList(): List<Repair>"""

text = text.replace('    @Query("SELECT * FROM repairs ORDER BY dateReceived DESC")\n    fun getAllRepairs(): Flow<List<Repair>>', replacement)

with open("app/src/main/java/com/shopkeeper/mobileshop/data/db/dao/RepairDao.kt", "w") as f:
    f.write(text)
