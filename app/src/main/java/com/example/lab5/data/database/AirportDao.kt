package com.example.lab5.data.database

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {

    @Query("""
        SELECT * FROM airport
        WHERE iata_code LIKE '%' || :query || '%'
           OR name LIKE '%' || :query || '%'
        ORDER BY passengers DESC
        LIMIT 10
    """)
    fun searchAirports(query: String): Flow<List<Airport>>

    @Query("""
        SELECT
            departure.iata_code AS departureCode,
            departure.name AS departureName,
            destination.iata_code AS destinationCode,
            destination.name AS destinationName,
            destination.passengers AS passengers
        FROM airport AS departure
        CROSS JOIN airport AS destination
        WHERE (
            departure.iata_code LIKE '%' || :query || '%'
            OR departure.name LIKE '%' || :query || '%'
        )
        AND departure.id != destination.id
        ORDER BY destination.passengers DESC
    """)
    fun searchFlights(query: String): Flow<List<FlightOption>>
}