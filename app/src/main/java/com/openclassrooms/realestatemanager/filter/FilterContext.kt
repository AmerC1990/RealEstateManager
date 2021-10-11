package com.openclassrooms.realestatemanager.filter

import android.util.Log
import com.openclassrooms.realestatemanager.data.cache.ListingEntity

class FilterContext(
        private val priceStrategy: Strategy = PriceStrategy(),
        private val surfaceStrategy: SurfaceStrategy = SurfaceStrategy(),
        private val photosStrategy: PhotosStrategy = PhotosStrategy(),
        private val statusStrategy: StatusStrategy = StatusStrategy(),
        private val typeStrategy: TypeStrategy = TypeStrategy(),
        private val beenOnMarketSinceStrategy: BeenOnMarketSinceStrategy = BeenOnMarketSinceStrategy(),
        private val beenSoldSinceStrategy: BeenSoldSinceStrategy = BeenSoldSinceStrategy(),
        private val pointsOfInterestStrategy: PointsOfInterestStrategy = PointsOfInterestStrategy(),
        private val locationStrategy: LocationStrategy = LocationStrategy()
) {

    fun executeStrategy(param: FilterParams?, data: List<ListingEntity>): List<ListingEntity> {
        if (param?.minPrice == null
                && param?.maxPrice == null
                && param?.minSurfaceArea == null
                && param?.maxSurfaceArea == null
                && param?.minNumberOfPhotos == null
                && param?.maxNumberOfPhotos == null
                && param?.status == null
                && param?.type == null
                && param?.onMarketSince == null
                && param?.soldSince == null
                && param?.pointsOfInterest == null
                && param?.location == null) {
            return data
        } else {
            Log.d("filteredData: ", "alldata- " + data.size.toString())

            val priceFilterParam = FilterParams(minPrice = param.minPrice, maxPrice = param.maxPrice)
            val priceFilteredData = priceStrategy.filter(priceFilterParam, data)
            Log.d("filteredData: ", "price- " + priceFilteredData.size.toString())

            val surfaceAreaFilterParam = FilterParams(minSurfaceArea = param.minSurfaceArea, maxSurfaceArea = param.maxSurfaceArea)
            val surfaceAreaFilteredData = surfaceStrategy.filter(surfaceAreaFilterParam, priceFilteredData)
            Log.d("filteredData: ", "surface- " + surfaceAreaFilteredData.size.toString())

            val photosFilterParam = FilterParams(minNumberOfPhotos = param.minNumberOfPhotos, maxNumberOfPhotos = param.maxNumberOfPhotos)
            val photosFilteredData = photosStrategy.filter(photosFilterParam, surfaceAreaFilteredData)
            Log.d("filteredData: ", "photos- " + photosFilteredData.size.toString())

            val statusFilterParam = FilterParams(status = param.status)
            val statusFilteredData = statusStrategy.filter(statusFilterParam, photosFilteredData)
            Log.d("filteredData: ", "status- " + statusFilteredData.size.toString())


            val typeFilterParam = FilterParams(type = param.type)
            val typeFilteredData = typeStrategy.filter(typeFilterParam, statusFilteredData)
            Log.d("filteredData: ", "type- " + typeFilteredData.size.toString())


            val onMarketFilterParam = FilterParams(onMarketSince = param.onMarketSince)
            val onMarketSinceFilteredData = beenOnMarketSinceStrategy.filter(onMarketFilterParam, typeFilteredData)
            Log.d("filteredData: ", "onMarket- " + onMarketSinceFilteredData.size.toString())

            val soldSinceFilterParam = FilterParams(soldSince = param.soldSince)
            val soldSinceFilteredData = beenSoldSinceStrategy.filter(soldSinceFilterParam, onMarketSinceFilteredData)
            Log.d("filteredData: ", "soldsince- " + soldSinceFilteredData.size.toString())

            val pointsOfInterestFilterParam = FilterParams(pointsOfInterest = param.pointsOfInterest)
            val pointsOfInterestFilteredData = pointsOfInterestStrategy.filter(pointsOfInterestFilterParam, soldSinceFilteredData)
            Log.d("filteredData: ", "pointsinterest- " + pointsOfInterestFilteredData.size.toString())

            val locationFilterParam = FilterParams(location = param.location)
            val locationFilteredData = locationStrategy.filter(locationFilterParam, pointsOfInterestFilteredData)
            Log.d("filteredData: ", "location- " + locationFilteredData.size.toString())

            return locationStrategy.filter(locationFilterParam, locationFilteredData)
        }
    }
}