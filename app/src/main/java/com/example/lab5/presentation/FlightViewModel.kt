package com.example.lab5.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lab5.data.database.Airport
import com.example.lab5.data.database.Favorite
import com.example.lab5.data.database.FlightOption
import com.example.lab5.data.datastore.SearchPreferences
import com.example.lab5.domain.FlightRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FlightViewModel(
    private val repository: FlightRepository,
    private val searchPreferences: SearchPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val airports: StateFlow<List<Airport>> =
        searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    flowOf(emptyList())
                } else {
                    repository.searchAirports(query)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val flights: StateFlow<List<FlightOption>> =
        searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    flowOf(emptyList())
                } else {
                    repository.searchFlights(query)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val favoriteFlights = repository.getFavoriteFlights()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveSearch(iataCode: String) {
        viewModelScope.launch {
            searchPreferences.saveSearch(iataCode)
        }
    }

    fun toggleFavorite(
        departureCode: String,
        destinationCode: String
    ) {
        viewModelScope.launch {
            val exists = repository
                .isFavorite(departureCode, destinationCode)
                .first()

            if (exists) {
                repository.removeFavorite(
                    departureCode,
                    destinationCode
                )
            } else {
                repository.addFavorite(
                    departureCode,
                    destinationCode
                )
            }
        }
    }

    fun clearSearch() {
        viewModelScope.launch {
            searchPreferences.clearSearch()
            _searchQuery.value = ""
        }
    }
}

class FlightViewModelFactory(
    private val repository: FlightRepository,
    private val searchPreferences: SearchPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlightViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FlightViewModel(
                repository,
                searchPreferences
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}