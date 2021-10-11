package com.openclassrooms.realestatemanager.filter

import com.openclassrooms.realestatemanager.Utils.convertDateFromUSAToWorld
import com.openclassrooms.realestatemanager.Utils.convertDollarToEuro
import com.openclassrooms.realestatemanager.Utils.doesLocaleSubscribeToEuroCurrency
import com.openclassrooms.realestatemanager.Utils.isLocaleInAmerica
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PriceStrategy : Strategy {
        override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
            if(!doesLocaleSubscribeToEuroCurrency()) {
                return data.filter { property ->
                    if (property.price.isNotEmpty()) {
                        property.price.toDouble() <= param.maxPrice!! && property.price.toDouble() >= param.minPrice!!
                    } else {
                        false
                    }
                }
            }
            else {
                return data.filter { property ->
                    if (property.price.isNotEmpty()) {
                        convertDollarToEuro(property.price.toInt()).toDouble() <= param.maxPrice!! && convertDollarToEuro(property.price.toInt()).toDouble() >= param.minPrice!!
                    } else {
                        false
                    }
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
            return if (param.status?.isNotEmpty() == true) {
                val myList = mutableListOf<ListingEntity>()
                for (it in data) {
                    if (!it.status.isNullOrEmpty() && it.status.contains(param.status.toString())) {
                        myList.add(it)
                    }
                }
                myList
            } else {
                data
            }
        }
    }
}

class TypeStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        return data.filter { property ->
            return if (param.type?.isNotEmpty() == true) {
                val myList = mutableListOf<ListingEntity>()
                for (it in data) {
                    if (!it.typeOfListing.isNullOrEmpty() && it.typeOfListing.contains(param.type.toString())) {
                        myList.add(it)
                    }
                }
                myList
            } else {
                data
            }
        }
    }
}

class BeenOnMarketSinceStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        if (isLocaleInAmerica()) {
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
        else {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            return if (!param.onMarketSince!!.contains("date", ignoreCase = true) && param.onMarketSince.contains("/")) {
                val myList = mutableListOf<ListingEntity>()
                for (it in data) {
                    if (it.dateOnMarket.isNotEmpty() && LocalDate.parse(convertDateFromUSAToWorld(it.dateOnMarket).filter { !it.isWhitespace() }, formatter).isAfter(LocalDate.parse(param.onMarketSince.filter { !it.isWhitespace() }, formatter))) {
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
}

class BeenSoldSinceStrategy : Strategy {
    override fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        if (isLocaleInAmerica()) {
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
        else {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            return if (!param.soldSince!!.contains("date", ignoreCase = true) && param.soldSince.contains("/")) {
                val myList = mutableListOf<ListingEntity>()
                for (it in data) {
                    if (it.saleDate.isNotEmpty() && LocalDate.parse(convertDateFromUSAToWorld(it.saleDate).filter { !it.isWhitespace() }, formatter).isAfter(LocalDate.parse(param.soldSince.filter { !it.isWhitespace() }, formatter))) {
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





