package ua.onpu.martynuk_lab3.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var database: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "contacts_database"
            ).build()

            database = instance
            instance
        }
    }
}
