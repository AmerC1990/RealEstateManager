package com.openclassrooms.realestatemanager.filter

data class FilterParams(
        val minPrice: Double? = null,
        val maxPrice: Double? = null,
        val minSurfaceArea: Double? = null,
        val maxSurfaceArea: Double? = null,
        val minNumberOfPhotos: Double? = null,
        val maxNumberOfPhotos: Double? = null,
        val status: String? = null,
        val type: String? = null,
        val location: String? = null,
        val onMarketSince: String? = null,
        val soldSince: String? = null,
        val pointsOfInterest: List<String?>? = null
)

