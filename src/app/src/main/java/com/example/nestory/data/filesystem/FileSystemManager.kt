package com.example.nestory.data.filesystem

import android.content.Context
import androidx.room.Room
import com.example.nestory.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileSystemManager(private val context: Context) {

    suspend fun createVaultStructure(): Unit = withContext(Dispatchers.IO) {
        // 1. Initialize 'files/' directory
        // Accessing filesDir ensures the system creates it if it doesn't exist
        context.filesDir
        
        // 2. Initialize 'cache/' directory
        context.cacheDir
        
        // 3. Initialize 'shared_prefs/' directory
        // Saving metadata ensures the 'shared_prefs/' folder is created
        val prefs = context.getSharedPreferences("nestory_vault_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("vault_initialized", true).apply()
        
        // 4. Initialize 'databases/' directory
        // Opening the Room database ensures the 'databases/' folder is created
        val db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "nestory_database"
        ).build()
        
        try {
            db.openHelper.writableDatabase
        } finally {
            db.close()
        }
    }
}
