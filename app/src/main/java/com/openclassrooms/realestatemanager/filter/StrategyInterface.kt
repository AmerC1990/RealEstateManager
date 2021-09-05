package com.openclassrooms.realestatemanager.filter

import com.openclassrooms.realestatemanager.data.cache.ListingEntity

interface Strategy {
    fun filter(param: FilterParams, data: List<ListingEntity>): List<ListingEntity>
}