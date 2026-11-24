package com.example.lab5.data.database

data class FlightOption(
    val departureCode: String,
    val departureName: String,
    val destinationCode: String,
    val destinationName: String,
    val passengers: Int
)