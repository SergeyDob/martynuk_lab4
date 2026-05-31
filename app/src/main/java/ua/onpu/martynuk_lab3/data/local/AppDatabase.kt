package ua.onpu.martynuk_lab3.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ContactEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
