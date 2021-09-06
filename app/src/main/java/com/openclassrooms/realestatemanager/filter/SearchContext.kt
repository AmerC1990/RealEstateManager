package com.openclassrooms.realestatemanager.filter

import com.openclassrooms.realestatemanager.data.cache.ListingEntity

class SearchContext(private val searchStrategy: Search = SearchStrategy()) {
    fun executeSearchStrategy(param: SearchParams, data: List<ListingEntity>): List<ListingEntity> {
        val searchViewQueryParam = SearchParams(searchViewQuery = param.searchViewQuery.toString())
        return searchStrategy.filterSearch(searchViewQueryParam, data)
    }
}