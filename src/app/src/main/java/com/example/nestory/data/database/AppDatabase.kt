package com.example.nestory.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nestory.data.dao.ContainerDao
import com.example.nestory.data.dao.DocumentDao
import com.example.nestory.data.entity.ContainerEntity
import com.example.nestory.data.entity.DocumentEntity

@Database(
    entities = [
        ContainerEntity::class,
        DocumentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun containerDao(): ContainerDao

    abstract fun documentDao(): DocumentDao
}