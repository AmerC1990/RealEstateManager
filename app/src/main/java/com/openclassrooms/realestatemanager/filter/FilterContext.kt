package com.openclassrooms.realestatemanager.filter

import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import kotlinx.coroutines.flow.Flow

class FilterContext(private val strategy: Strategy) {
    fun executeStrategy(param: FilterParams, data: List<ListingEntity>): List<ListingEntity> {
        return strategy.filter(param = param, data = data)
    }
}