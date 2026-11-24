package com.example.lab5.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    // Получение избранных маршрутов вместе с данными аэропортов
    @Query("""
        SELECT
            departure.iata_code AS departureCode,
            departure.name AS departureName,
            destination.iata_code AS destinationCode,
            destination.name AS destinationName,
            destination.passengers AS passengers
        FROM favorite
        INNER JOIN airport AS departure
            ON favorite.departure_code = departure.iata_code
        INNER JOIN airport AS destination
            ON favorite.destination_code = destination.iata_code
        ORDER BY favorite.id DESC
    """)
    fun getFavorites(): Flow<List<FlightOption>>

    // Добавление маршрута в избранное
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: Favorite)

    // Удаление маршрута из избранного
    @Query("""
        DELETE FROM favorite
        WHERE departure_code = :departureCode
          AND destination_code = :destinationCode
    """)
    suspend fun removeFavorite(
        departureCode: String,
        destinationCode: String
    )

    // Проверка наличия маршрута в избранном
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM favorite
            WHERE departure_code = :departureCode
              AND destination_code = :destinationCode
        )
    """)
    fun isFavorite(
        departureCode: String,
        destinationCode: String
    ): Flow<Boolean>
}