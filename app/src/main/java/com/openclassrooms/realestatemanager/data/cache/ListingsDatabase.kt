package com.openclassrooms.realestatemanager.data.cache

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ListingEntity::class], version = 1, exportSchema = false)
abstract class ListingsDatabase: RoomDatabase() {
    abstract fun listingDao(): ListingDao?
}