package com.example.lab5.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Airport::class,
        Favorite::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FlightDatabase : RoomDatabase() {

    abstract fun airportDao(): AirportDao

    abstract fun favoriteDao(): FavoriteDao

    companion object {

        @Volatile
        private var INSTANCE: FlightDatabase? = null

        fun getDatabase(context: Context): FlightDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlightDatabase::class.java,
                    "flight_search.db"
                )
                    .createFromAsset("databases/flight_search.db")
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}