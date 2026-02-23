package com.example.team16_mobile_team_project_1.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HighScore::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun highScoreDao(): HighScoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "high_score_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}