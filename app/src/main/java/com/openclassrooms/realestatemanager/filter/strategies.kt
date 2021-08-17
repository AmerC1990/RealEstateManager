package com.openclassrooms.realestatemanager.filter

import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.viewmodels.ListingsViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import java.time.LocalDate
import java.time.format.DateTimeFormatter


//    val filterParam = FilterParams(minPrice  = 100.0, maxPrice = 2000.0)
//    val filterContext = FilterContext(PriceStrategy())
//    filterContext.executeStrategy(filterParam, emptyList<ListingEntity>())

class PriceStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        return data.filter { property ->
            if (property.price.isNotEmpty()) {
                property.price.toDouble() <= param.maxPrice!! && property.price.toDouble() >= param.minPrice!!
            } else {
                property.price.isNotEmpty()
            }
        }
    }
}

class SurfaceStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        return data.filter { property ->
            if (property.surfaceArea.isNotEmpty()) {
                property.surfaceArea.toDouble() <= param.maxSurfaceArea!! && property.surfaceArea.toDouble() >= param.minSurfaceArea!!
            } else {
                property.surfaceArea.isNotEmpty()
            }
        }
    }
}

class PhotosStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        return data.filter { property ->
            property.photoCount.toDouble() <= param.maxNumberOfPhotos!! && property.photoCount.toDouble() >= param.minNumberOfPhotos!!
        }
    }
}

class StatusStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        return data.filter { property ->
            property.status.contains(param.status!!, ignoreCase = true)
        }
    }
}

class TypeStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        return data.filter { property ->
            property.typeOfListing.contains(param.type!!, ignoreCase = true)
        }
    }
}

class BeenOnMarketSinceStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        return data.filter { property ->
            if (property.dateOnMarket.isNotEmpty() && !param.onMarketSince!!.contains("date", ignoreCase = true)) {
                return@filter (LocalDate.parse(property.dateOnMarket.filter { !it.isWhitespace() }, formatter).isAfter(LocalDate.parse(param.onMarketSince.filter { !it.isWhitespace() }, formatter)))
            } else {
                return@filter property.dateOnMarket.isNotEmpty()
            }
        }
    }
}

class BeenSoldSinceStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        return data.filter { property ->
            if (property.saleDate.isNotEmpty() && !param.soldSince!!.contains("date", ignoreCase = true)) {
                return@filter (LocalDate.parse(property.saleDate.filter { !it.isWhitespace() }, formatter).isAfter(LocalDate.parse(param.soldSince.filter { !it.isWhitespace() }, formatter)))
            } else {
                return@filter property.saleDate.isNotEmpty()
            }
        }
    }
}

class PointsOfInterestStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        return data.filter { property ->
            property.pointsOfInterest.replace("[", "").replace("]", "").split(",").map { it.trim() }
                    .containsAll(param.pointsOfInterest!!)
        }

    }

}

class LocationStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        return data.filter { property ->
            if (param.location.toString().isNotEmpty()) {
                property.address.contains(param.location!!, ignoreCase = true)
            } else {
                property.address.isNotEmpty()
            }
        }
    }
}




