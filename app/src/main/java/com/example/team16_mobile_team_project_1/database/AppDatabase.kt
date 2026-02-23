package com.example.team16_mobile_team_project_1.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The Room database for the application.
 *
 * This database contains the [HighScore] table.
 */
@Database(entities = [HighScore::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Returns the Data Access Object for the [HighScore] table.
     */
    abstract fun highScoreDao(): HighScoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton instance of the [AppDatabase].
         *
         * @param context The application context.
         * @return The singleton instance of the [AppDatabase].
         */
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