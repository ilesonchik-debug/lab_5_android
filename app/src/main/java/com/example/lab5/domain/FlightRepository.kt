package com.example.lab5.domain

import com.example.lab5.data.database.Airport
import com.example.lab5.data.database.Favorite
import com.example.lab5.data.database.FlightOption
import kotlinx.coroutines.flow.Flow

interface FlightRepository {

    fun searchAirports(query: String): Flow<List<Airport>>

    fun searchFlights(query: String): Flow<List<FlightOption>>

    fun getFavoriteFlights(): Flow<List<FlightOption>>

    fun isFavorite(
        departureCode: String,
        destinationCode: String
    ): Flow<Boolean>

    suspend fun addFavorite(
        departureCode: String,
        destinationCode: String
    )

    suspend fun removeFavorite(
        departureCode: String,
        destinationCode: String
    )
}