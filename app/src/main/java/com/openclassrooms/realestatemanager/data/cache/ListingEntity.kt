package com.openclassrooms.realestatemanager.data.cache

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.storage.StorageReference
import okhttp3.internal.ignoreIoExceptions

@Entity(tableName = "listing_info")
data class ListingEntity(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,

        @ColumnInfo(name = "typeOfListing", defaultValue = "No Type")
        val typeOfListing: String,

        @ColumnInfo(name = "photoCount")
        val photoCount: Int,

        @ColumnInfo(name = "price", defaultValue = "No Price") val price: String,

        @ColumnInfo(name = "surfaceArea", defaultValue = "No Surface Area") val surfaceArea: String,

        @ColumnInfo(name = "numberOfRooms", defaultValue = "No Number of Rooms") val numberOfRooms: String,

        @ColumnInfo(name = "descriptionOfListing", defaultValue = "No Description") val descriptionOfListing: String,

        @ColumnInfo(name = "photoUrl", defaultValue = "No Photo") val photoReference: String,

        @ColumnInfo(name = "photoUrl2", defaultValue = "No Photo") val photoReference2: String,

        @ColumnInfo(name = "photoUrl3", defaultValue = "No Photo") val photoReference3: String,

        @ColumnInfo(name = "photoUrl4", defaultValue = "No Photo") val photoReference4: String,

        @ColumnInfo(name = "photoUrl5", defaultValue = "No Photo") val photoReference5: String,

        @ColumnInfo(name = "photoUrl6", defaultValue = "No Photo") val photoReference6: String,

        @ColumnInfo(name = "photoUrl7", defaultValue = "No Photo") val photoReference7: String,

        @ColumnInfo(name = "photoUrl8", defaultValue = "No Photo") val photoReference8: String,

        @ColumnInfo(name = "photoUrl9", defaultValue = "No Photo") val photoReference9: String,

        @ColumnInfo(name = "photoUrl10", defaultValue = "No Photo") val photoReference10: String,

        @ColumnInfo(name = "photoDescription", defaultValue = "No Photo Description") val photoDescription: String,

        @ColumnInfo(name = "address", defaultValue = "No Address") val address: String,

        @ColumnInfo(name = "pointsOfInterest", defaultValue = "No Points of Interest") val pointsOfInterest: String,

        @ColumnInfo(name = "status", defaultValue = "No Status") val status: String,

        @ColumnInfo(name = "dateOnMarket", defaultValue = "No Date On Market") val dateOnMarket: String,

        @ColumnInfo(name = "saleDate", defaultValue = "No Sale Date") val saleDate: String,

        @ColumnInfo(name = "realEstateAgent", defaultValue = "No Agent") val realEstateAgent: String
)