package com.example.lab5.data.repository

import com.example.lab5.data.database.Airport
import com.example.lab5.data.database.AirportDao
import com.example.lab5.data.database.Favorite
import com.example.lab5.data.database.FavoriteDao
import com.example.lab5.data.database.FlightOption
import com.example.lab5.domain.FlightRepository
import kotlinx.coroutines.flow.Flow

class FlightRepositoryImpl(
    private val airportDao: AirportDao,
    private val favoriteDao: FavoriteDao
) : FlightRepository {

    override fun searchAirports(query: String): Flow<List<Airport>> =
        airportDao.searchAirports(query)

    override fun searchFlights(query: String): Flow<List<FlightOption>> =
        airportDao.searchFlights(query)

    override fun getFavoriteFlights(): Flow<List<FlightOption>> =
        favoriteDao.getFavorites()

    override fun isFavorite(
        departureCode: String,
        destinationCode: String
    ): Flow<Boolean> =
        favoriteDao.isFavorite(departureCode, destinationCode)

    override suspend fun addFavorite(
        departureCode: String,
        destinationCode: String
    ) {
        favoriteDao.addFavorite(
            Favorite(
                departureCode = departureCode,
                destinationCode = destinationCode
            )
        )
    }

    override suspend fun removeFavorite(
        departureCode: String,
        destinationCode: String
    ) {
        favoriteDao.removeFavorite(
            departureCode,
            destinationCode
        )
    }
}