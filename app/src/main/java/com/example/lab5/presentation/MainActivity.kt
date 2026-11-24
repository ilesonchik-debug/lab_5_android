
package com.example.lab5.presentation

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.lab5.R
import com.example.lab5.data.database.Airport
import com.example.lab5.data.database.FlightDatabase
import com.example.lab5.data.database.FlightOption
import com.example.lab5.data.datastore.SearchPreferences
import com.example.lab5.data.repository.FlightRepositoryImpl
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: FlightViewModel

    private lateinit var departureInput: AutoCompleteTextView
    private lateinit var flightsContainer: LinearLayout
    private lateinit var favoritesContainer: LinearLayout

    private lateinit var emptyFlights: TextView
    private lateinit var emptyFavorites: TextView

    private var currentAirports = emptyList<Airport>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        departureInput = findViewById(R.id.etDeparture)
        flightsContainer = findViewById(R.id.llFlights)
        favoritesContainer = findViewById(R.id.llFavorites)

        emptyFlights = findViewById(R.id.tvEmptyFlights)
        emptyFavorites = findViewById(R.id.tvEmptyFavorites)

        val database = FlightDatabase.getDatabase(applicationContext)

        val repository = FlightRepositoryImpl(
            database.airportDao(),
            database.favoriteDao()
        )

        val preferences = SearchPreferences(applicationContext)

        val factory = FlightViewModelFactory(
            repository,
            preferences
        )

        viewModel = ViewModelProvider(this, factory)
            .get(FlightViewModel::class.java)

        setupSearch()
        observeData()
    }

    private fun setupSearch() {

        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf()
        )

        departureInput.setAdapter(adapter)
        departureInput.threshold = 1

        departureInput.addTextChangedListener(object :
            android.text.TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                viewModel.updateSearchQuery(s.toString())
            }

            override fun afterTextChanged(
                s: android.text.Editable?
            ) {}
        })

        departureInput.setOnItemClickListener { parent, _, position, _ ->

            val selectedText = parent.getItemAtPosition(position).toString()

            val airport = currentAirports.firstOrNull {
                "${it.iataCode} — ${it.name}" == selectedText
            }

            if (airport != null) {
                departureInput.setText(airport.iataCode, false)
                departureInput.setSelection(departureInput.text.length)

                viewModel.updateSearchQuery(airport.iataCode)
                viewModel.saveSearch(airport.iataCode)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.airports.collect { airports ->

                    currentAirports = airports

                    adapter.clear()

                    adapter.addAll(
                        airports.map {
                            "${it.iataCode} — ${it.name}"
                        }
                    )

                    adapter.notifyDataSetChanged()

                    if (airports.isNotEmpty() &&
                        departureInput.hasFocus()
                    ) {
                        departureInput.showDropDown()
                    }
                }
            }
        }
    }

    private fun observeData() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.flights.collect { flights ->
                        showFlights(flights)
                    }
                }

                launch {
                    viewModel.favoriteFlights.collect { favorites ->
                        showFavorites(favorites)
                        showFlights(viewModel.flights.value)
                    }
                }
            }
        }
    }

    private fun showFlights(flights: List<FlightOption>) {

        flightsContainer.removeAllViews()

        if (flights.isEmpty()) {
            emptyFlights.visibility = View.VISIBLE

            emptyFlights.text = if (
                viewModel.searchQuery.value.isBlank()
            ) {
                getString(R.string.empty_flights)
            } else {
                getString(R.string.no_flights)
            }

            return
        }

        emptyFlights.visibility = View.GONE

        flights.forEach { flight ->

            val row = createRow()
            val details = createFlightDetails(flight)

            val favoriteButton = Button(this).apply {
                text = if (
                    isFavorite(
                        flight.departureCode,
                        flight.destinationCode
                    )
                ) "★" else "☆"

                textSize = 22f
                minWidth = 48
                setPadding(4, 0, 4, 0)

                setOnClickListener {
                    viewModel.toggleFavorite(
                        flight.departureCode,
                        flight.destinationCode
                    )
                }
            }

            row.addView(details)
            row.addView(favoriteButton)

            flightsContainer.addView(row)
        }
    }

    private fun showFavorites(favorites: List<FlightOption>) {

        favoritesContainer.removeAllViews()

        if (favorites.isEmpty()) {
            emptyFavorites.visibility = View.VISIBLE
            return
        }

        emptyFavorites.visibility = View.GONE

        favorites.forEach { flight ->

            val row = createRow()
            val details = createFlightDetails(flight)

            val removeButton = Button(this).apply {
                text = "★"
                textSize = 22f
                minWidth = 48
                setPadding(4, 0, 4, 0)

                setOnClickListener {
                    viewModel.toggleFavorite(
                        flight.departureCode,
                        flight.destinationCode
                    )
                }
            }

            row.addView(details)
            row.addView(removeButton)

            favoritesContainer.addView(row)
        }
    }

    private fun createFlightDetails(
        flight: FlightOption
    ): LinearLayout {

        return LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

            val departure = TextView(this@MainActivity).apply {
                text = "Откуда: ${flight.departureCode} — ${flight.departureName}"
                textSize = 14f
                setTextColor(Color.BLACK)
            }

            val destination = TextView(this@MainActivity).apply {
                text = "Куда: ${flight.destinationCode} — ${flight.destinationName}"
                textSize = 14f
                setTextColor(Color.BLACK)
            }

            val passengers = TextView(this@MainActivity).apply {
                text = "Пассажиров: ${flight.passengers}"
                textSize = 12f
                setTextColor(Color.GRAY)
            }

            addView(departure)
            addView(destination)
            addView(passengers)
        }
    }

    private fun createRow(): LinearLayout {

        return LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            setPadding(12, 8, 8, 8)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }

            setBackgroundResource(
                android.R.drawable.dialog_holo_light_frame
            )
        }
    }

    private fun isFavorite(
        departureCode: String,
        destinationCode: String
    ): Boolean {

        return viewModel.favoriteFlights.value.any {
            it.departureCode == departureCode &&
                    it.destinationCode == destinationCode
        }
    }
}
