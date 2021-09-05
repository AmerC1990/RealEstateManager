package com.openclassrooms.realestatemanager.filter

import com.openclassrooms.realestatemanager.data.cache.ListingEntity

class FilterContext(private val priceStrategy: Strategy = PriceStrategy(),
                    private val surfaceStrategy: Strategy = SurfaceStrategy(),
                    private val photosStrategy: Strategy = PhotosStrategy(),
                    private val statusStrategy: Strategy = StatusStrategy(),
                    private val typeStrategy: Strategy = TypeStrategy(),
                    private val beenOnMarketSinceStrategy: Strategy = BeenOnMarketSinceStrategy(),
                    private val beenSoldSinceStrategy: Strategy = BeenSoldSinceStrategy(),
                    private val pointsOfInterestStrategy: PointsOfInterestStrategy = PointsOfInterestStrategy(),
                    private val locationStrategy: LocationStrategy = LocationStrategy()) {

    fun executeStrategy(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        val priceFilterParam = FilterParams(minPrice = param.minPrice.toString().toDouble(), maxPrice = param.maxPrice.toString().toDouble())
        val priceFilteredData = priceStrategy.filter(priceFilterParam, data)

        val surfaceAreaFilterParam = FilterParams(minSurfaceArea = param.minSurfaceArea.toString().toDouble(), maxSurfaceArea = param.maxSurfaceArea.toString().toDouble())
        val surfaceAreaFilteredData = surfaceStrategy.filter(surfaceAreaFilterParam, priceFilteredData)

        val photosFilterParam = FilterParams(minNumberOfPhotos = param.minNumberOfPhotos?.toString()?.toDouble(), maxNumberOfPhotos = param.maxNumberOfPhotos?.toString()?.toDouble())
        val photosFilteredData = photosStrategy.filter(photosFilterParam, surfaceAreaFilteredData)

        val statusFilterParam = FilterParams(status = param.status)
        val statusFilteredData = statusStrategy.filter(statusFilterParam, photosFilteredData)

        val typeFilterParam = FilterParams(type = param.type)
        val typeFilteredData = typeStrategy.filter(typeFilterParam, statusFilteredData)

        val onMarketFilterParam = FilterParams(onMarketSince = param.onMarketSince)
        val onMarketSinceFilteredData = beenOnMarketSinceStrategy.filter(onMarketFilterParam, typeFilteredData)

        val soldSinceFilterParam = FilterParams(soldSince = param.soldSince)
        val soldSinceFilteredData = beenSoldSinceStrategy.filter(soldSinceFilterParam, onMarketSinceFilteredData)

        val pointsOfInterestFilterParam = FilterParams(pointsOfInterest = param.pointsOfInterest)
        val pointsOfInterestFilteredData = pointsOfInterestStrategy.filter(pointsOfInterestFilterParam, soldSinceFilteredData)

        val locationFilterParam = FilterParams(location = param.location)
        val locationFilteredData = locationStrategy.filter(locationFilterParam, pointsOfInterestFilteredData)

        return locationStrategy.filter(locationFilterParam, locationFilteredData)
    }
}