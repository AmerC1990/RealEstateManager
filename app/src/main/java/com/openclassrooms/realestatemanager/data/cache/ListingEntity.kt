package com.openclassrooms.realestatemanager.data.cache

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listing_info")
data class ListingEntity (
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id")val id: Int = 0,
        @ColumnInfo(name = "typeOfListing", defaultValue = "No Type") val typeOfListing:String?,
        @ColumnInfo(name = "price", defaultValue = "No Price") val price: String?,
        @ColumnInfo(name = "surfaceArea", defaultValue = "No Surface Area") val surfaceArea: Double?,
        @ColumnInfo(name = "numberOfRooms", defaultValue = "No Number of Rooms") val numberOfRooms: Int?,
        @ColumnInfo(name = "descriptionOfListing", defaultValue = "No Description") val descriptionOfListing: String?,
        @ColumnInfo(name = "photoUrl", defaultValue = "No Photo") val photoUrl: String?,
        @ColumnInfo(name = "photoDescription", defaultValue = "No Photo Description") val photoDescription: String?,
        @ColumnInfo(name = "address", defaultValue = "No Address") val address: String?,
        @ColumnInfo(name = "pointsOfInterest", defaultValue = "No Points of Interest") val pointsOfInterest: String?,
        @ColumnInfo(name = "status", defaultValue = "No Status") val status: String?,
        @ColumnInfo(name = "dateOnMarket", defaultValue = "No Date On Market") val dateOnMarket: String?,
        @ColumnInfo(name = "saleDate", defaultValue = "No Sale Date") val saleDate: String?,
        @ColumnInfo(name = "realEstateAgent", defaultValue = "No Agent") val realEstateAgent: String?
)