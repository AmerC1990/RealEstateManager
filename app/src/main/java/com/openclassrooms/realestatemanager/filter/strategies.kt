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
                false
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
                false
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
            if (param.status?.replace("\\s".toRegex(), "")?.isNotEmpty() == true) {
                param.status.replace("\\s".toRegex(), "").contains(property.status.replace("\\s".toRegex(), ""), ignoreCase = true)
            } else {
                return data
            }

        }
    }
}

class TypeStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        return data.filter { property ->
            if (param.type?.replace("\\s".toRegex(), "")?.isNotEmpty() == true) {
                param.type.replace("\\s".toRegex(), "").contains(property.typeOfListing.replace("\\s".toRegex(), ""))
            } else {
                return data
            }
        }
    }
}

class BeenOnMarketSinceStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        return if (!param.onMarketSince!!.contains("date", ignoreCase = true) && param.onMarketSince.contains("/")) {
            val myList = mutableListOf<ListingEntity>()
            for (it in data) {
                if (it.dateOnMarket.isNotEmpty() && LocalDate.parse(it.dateOnMarket.filter { !it.isWhitespace() }, formatter).isAfter(LocalDate.parse(param.onMarketSince.filter { !it.isWhitespace() }, formatter))) {
                    myList.add(it)
                }
            }
            myList
        }
        else {
            data
        }
    }
}

class BeenSoldSinceStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        return if (!param.soldSince!!.contains("date", ignoreCase = true) && param.soldSince.contains("/")) {
            val myList = mutableListOf<ListingEntity>()
            for (it in data) {
                if (it.saleDate.isNotEmpty() && LocalDate.parse(it.saleDate.filter { !it.isWhitespace() }, formatter).isAfter(LocalDate.parse(param.soldSince.filter { !it.isWhitespace() }, formatter))) {
                    myList.add(it)
                }
            }
            myList
        }
        else {
            data
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
                    property.address.contains(param.location?.replace("\\s".toRegex(), "")!!, ignoreCase = true)
                } else {
                    property.id.toString().isNotEmpty()
                }
            }
        }
    }

    class SearchStrategy : Search {
        override fun filterSearch(param: SearchParams?, data: List<ListingEntity>): List<ListingEntity> {
            return data.filter { property ->
                property.descriptionOfListing.contains(
                        param?.searchViewQuery.toString(),
                        ignoreCase = true)
                        ||
                        property.address.contains(param?.searchViewQuery.toString(),
                                ignoreCase = true)
                        || property.pointsOfInterest.contains(param?.searchViewQuery.toString(),
                        ignoreCase = true)
                        || property.typeOfListing.contains(param?.searchViewQuery.toString(),
                        ignoreCase = true)
                        || property.price.contains(param?.searchViewQuery.toString(),
                        ignoreCase = true)
                        || property.photoDescription.contains(param?.searchViewQuery.toString(),
                        ignoreCase = true)
                        || property.surfaceArea.contains(param?.searchViewQuery.toString(),
                        ignoreCase = true)
                        || property.status.contains(param?.searchViewQuery.toString(),
                        ignoreCase = true)
                        || property.realEstateAgent.contains(param?.searchViewQuery.toString(),
                        ignoreCase = true)
                        || property.dateOnMarket.contains(param?.searchViewQuery.toString(),
                        ignoreCase = true)
                        || property.saleDate.contains(param?.searchViewQuery.toString(),
                        ignoreCase = true)
            }
        }
    }





