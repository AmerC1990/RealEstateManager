package com.openclassrooms.realestatemanager.data.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.openclassrooms.realestatemanager.Converters

@Database(entities = [ListingEntity::class], version = 4, exportSchema = false)
abstract class ListingsDatabase: RoomDatabase() {
    abstract fun listingDao(): ListingDao?
}