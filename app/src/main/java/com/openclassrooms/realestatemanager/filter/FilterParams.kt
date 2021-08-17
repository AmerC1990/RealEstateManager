package com.openclassrooms.realestatemanager.filter

//data class FilterParams(
//        val minPrice: Double? = null,
//        val maxPrice: Double? = null,
//        val minSurfaceArea: Double? = null,
//        val maxSurfaceArea: Double? = null,
//        val minNumberOfPhotos: Double? = null,
//        val maxNumberOfPhotos: Double? = null,
//        val status: String? = null,
//        val type: String? = null,
//        val location: String? = null,
//        val onMarketSince: String? = null,
//        val soldSince: String? = null,
//        val pointsOfInterest: List<String?>? = null
//)

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
) {

    private constructor(builder: Builder) : this(builder.minPrice,
            builder.maxPrice,
            builder.minSurfaceArea,
            builder.maxSurfaceArea,
            builder.minNumberOfPhotos,
            builder.maxNumberOfPhotos,
            builder.status,
            builder.type,
            builder.location,
            builder.onMarketSince,
            builder.soldSince,
            builder.pointsOfInterest)

    class Builder {
        var minPrice: Double? = null
            private set

        var maxPrice: Double? = null
            private set

        var minSurfaceArea: Double? = null
            private set

        var maxSurfaceArea: Double? = null
            private set

        var minNumberOfPhotos: Double? = null
            private set

        var maxNumberOfPhotos: Double? = null
            private set

        var status: String? = null
            private set

        var type: String? = null
            private set

        var location: String? = null
            private set

        var onMarketSince: String? = null
            private set

        var soldSince: String? = null
            private set

        var pointsOfInterest: List<String?>? = null
            private set


        fun minPrice(minPrice: Double?) = apply { this.minPrice = minPrice }

        fun maxPrice(maxPrice: Double?) = apply { this.maxPrice = maxPrice }

        fun minSurfaceArea(minSurfaceArea: Double?) = apply { this.minSurfaceArea = minSurfaceArea }

        fun maxSurfaceArea(maxSurfaceArea: Double?) = apply { this.maxSurfaceArea = maxSurfaceArea }

        fun minNumberOfPhotos(minNumberOfPhotos: Double?) = apply { this.minNumberOfPhotos = minNumberOfPhotos }

        fun maxNumberOfPhotos(maxNumberOfPhotos: Double?) = apply { this.maxNumberOfPhotos = maxNumberOfPhotos }

        fun status(status: String?) = apply { this.status = status }

        fun type(type: String?) = apply { this.type = type }

        fun location(location: String?) = apply { this.location = location }

        fun onMarketSince(onMarketSince: String?) = apply { this.onMarketSince = onMarketSince }

        fun soldSince(soldSince: String?) = apply { this.soldSince = soldSince }

        fun pointsOfInterest(pointsOfInterest: List<String?>?) = apply { this.pointsOfInterest = pointsOfInterest }


        fun build() = FilterParams(this)
    }
}

