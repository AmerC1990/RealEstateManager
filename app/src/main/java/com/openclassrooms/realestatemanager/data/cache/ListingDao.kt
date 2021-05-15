package com.openclassrooms.realestatemanager.data.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ListingDao {
    @Query("SELECT * FROM listing_info ORDER BY id DESC")
    fun getAllListingInfo(): List<ListingEntity>

    @Insert
    fun insertListing(listing: ListingEntity)

    @Update
    fun updateListing(listing: ListingEntity)
}