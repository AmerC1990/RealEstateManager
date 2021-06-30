package com.openclassrooms.realestatemanager.data.di

import com.openclassrooms.realestatemanager.App
import com.openclassrooms.realestatemanager.data.cache.ListingCache
import com.openclassrooms.realestatemanager.data.repository.ListingRepository
import com.openclassrooms.realestatemanager.data.repository.ListingRepositoryImplementation
import com.openclassrooms.realestatemanager.data.store.ListingCacheImpl
import org.koin.dsl.module


val dataModule = module {

    single { App.database?.listingDao() }

    factory<ListingCache> { ListingCacheImpl(get()) }

    single<ListingRepository> { ListingRepositoryImplementation(get()) }

}