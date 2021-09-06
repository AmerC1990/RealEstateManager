package com.openclassrooms.realestatemanager.filter

import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PriceStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        return data.filter { property ->
            if (property.price.isNotEmpty()) {
                property.price.toDouble() <= param.maxPrice!! && property.price.toDouble() >= param.minPrice!!
            } else {
                property.id.toString().isNotEmpty()
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
        if (!param.onMarketSince!!.contains("date", ignoreCase = true)) {
            for (listing in data) {
                if (!listing.dateOnMarket.isNullOrEmpty()) {
                    return data.filter { property ->
                        return@filter (!property.dateOnMarket.isNullOrEmpty() && LocalDate.parse(property.dateOnMarket.filter { !it.isWhitespace() }, formatter).isAfter(LocalDate.parse(param.onMarketSince.filter { !it.isWhitespace() }, formatter)))
                    }
                }
            }
        } else if (param.onMarketSince.contains("date", ignoreCase = true)) {
            return data.filter { property ->
                return@filter !property.id.toString().isNullOrEmpty()
            }
        }
        return emptyList()
    }
}

class BeenSoldSinceStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        if (!param.soldSince!!.contains("date", ignoreCase = true)) {
            for (listing in data) {
                if (!listing.saleDate.isNullOrEmpty()) {
                    return data.filter { property ->
                        return@filter (!property.saleDate.isNullOrEmpty() && LocalDate.parse(property.saleDate.filter { !it.isWhitespace() }, formatter).isAfter(LocalDate.parse(param.soldSince.filter { !it.isWhitespace() }, formatter)))
                    }
                }
            }
        } else if (param.soldSince.contains("date", ignoreCase = true)) {
            return data.filter { property ->
                return@filter !property.id.toString().isNullOrEmpty()
            }
        }
        return emptyList()
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
                property.id.toString().isNotEmpty()
            }
        }
    }
}

class SearchStrategy : Search {
    override fun filterSearch(param: SearchParams, data: List<ListingEntity>): List<ListingEntity> {
        return data.filter { property ->
            property.descriptionOfListing.contains(
                    param.searchViewQuery.toString(),
                    ignoreCase = true)
                    ||
                    property.address.contains(param.searchViewQuery.toString(),
                            ignoreCase = true)
                    || property.pointsOfInterest.contains(param.searchViewQuery.toString(),
                    ignoreCase = true)
                    || property.typeOfListing.contains(param.searchViewQuery.toString(),
                    ignoreCase = true)
                    || property.price.contains(param.searchViewQuery.toString(),
                    ignoreCase = true)
                    || property.photoDescription.contains(param.searchViewQuery.toString(),
                    ignoreCase = true)
                    || property.surfaceArea.contains(param.searchViewQuery.toString(),
                    ignoreCase = true)
                    || property.status.contains(param.searchViewQuery.toString(),
                    ignoreCase = true)
                    || property.realEstateAgent.contains(param.searchViewQuery.toString(),
                    ignoreCase = true)
                    || property.dateOnMarket.contains(param.searchViewQuery.toString(),
                    ignoreCase = true)
                    || property.saleDate.contains(param.searchViewQuery.toString(),
                    ignoreCase = true)
        }
    }
}




