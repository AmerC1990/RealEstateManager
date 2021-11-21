package com.openclassrooms.realestatemanager

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.openclassrooms.realestatemanager.data.cache.ListingCache
import com.openclassrooms.realestatemanager.data.cache.ListingDao
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.data.cache.ListingsDatabase
import com.openclassrooms.realestatemanager.data.repository.ListingRepository
import com.openclassrooms.realestatemanager.data.repository.ListingRepositoryImplementation
import com.openclassrooms.realestatemanager.data.store.ListingCacheImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.mockito.MockitoAnnotations


class ListingRepositoryImplTestTwo {

    private lateinit var userDao: ListingDao
    private lateinit var db: ListingsDatabase
    private lateinit var listingRepository: ListingRepository
    private lateinit var listingCache: ListingCache

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
            val context = ApplicationProvider.getApplicationContext<Context>()
            db = Room.inMemoryDatabaseBuilder(context, ListingsDatabase::class.java).build()
            userDao = db.listingDao()!!

        listingCache = ListingCacheImpl(userDao)
        listingRepository = ListingRepositoryImplementation(listingCache)
    }

    @After
    fun tearDown() {
            db.close()
    }

    @InternalCoroutinesApi
    @Test
    @ExperimentalCoroutinesApi
    fun saveListingAndRetrieveSameListingTest() {
        runBlocking {
            val listing = ListingEntity(
                    id = 8000,
                    photoCount = 0,
                    photoReference = "",
                    photoReference2 = "",
                    photoReference3 = "",
                    photoReference4 = "",
                    photoReference5 = "",
                    photoReference6 = "",
                    photoReference7 = "",
                    photoReference8 = "",
                    photoReference9 = "",
                    photoReference10 = "",
                    photoDescription = "",
                    typeOfListing = "",
                    price = "",
                    surfaceArea = "",
                    numberOfRooms = "",
                    descriptionOfListing = "",
                    address = "",
                    pointsOfInterest = "",
                    status = "",
                    dateOnMarket = "",
                    saleDate = "",
                    realEstateAgent = ""
            )

            listingRepository.saveListing(listing)
            listingRepository.getSingleItem(8000).take(1).collect { result ->
                Assert.assertEquals(listing.id, result.id)
                Assert.assertEquals(8000, result.id)
            }
        }
    }

    @InternalCoroutinesApi
    @Test
    @ExperimentalCoroutinesApi
    fun saveListingAndRetrieveDifferentListingTest() {
        runBlocking {
            val listing = ListingEntity(
                    id = 5000,
                    photoCount = 0,
                    photoReference = "",
                    photoReference2 = "",
                    photoReference3 = "",
                    photoReference4 = "",
                    photoReference5 = "",
                    photoReference6 = "",
                    photoReference7 = "",
                    photoReference8 = "",
                    photoReference9 = "",
                    photoReference10 = "",
                    photoDescription = "",
                    typeOfListing = "",
                    price = "",
                    surfaceArea = "",
                    numberOfRooms = "",
                    descriptionOfListing = "",
                    address = "",
                    pointsOfInterest = "",
                    status = "",
                    dateOnMarket = "",
                    saleDate = "",
                    realEstateAgent = ""
            )

            val wrongListing = ListingEntity(
                    id = 777,
                    photoCount = 0,
                    photoReference = "",
                    photoReference2 = "",
                    photoReference3 = "",
                    photoReference4 = "",
                    photoReference5 = "",
                    photoReference6 = "",
                    photoReference7 = "",
                    photoReference8 = "",
                    photoReference9 = "",
                    photoReference10 = "",
                    photoDescription = "",
                    typeOfListing = "",
                    price = "",
                    surfaceArea = "",
                    numberOfRooms = "",
                    descriptionOfListing = "",
                    address = "",
                    pointsOfInterest = "",
                    status = "",
                    dateOnMarket = "",
                    saleDate = "",
                    realEstateAgent = ""
            )
            listingRepository.saveListing(listing)
            listingRepository.getSingleItem(5000).take(1).collect { result ->
                Assert.assertNotEquals(wrongListing.id, result.id)
            }
        }
    }

    @InternalCoroutinesApi
    @Test
    @ExperimentalCoroutinesApi
    fun saveAndUpdateListingTest() {
        runBlocking {
            val listing = ListingEntity(
                    id = 997,
                    photoCount = 0,
                    photoReference = "",
                    photoReference2 = "",
                    photoReference3 = "",
                    photoReference4 = "",
                    photoReference5 = "",
                    photoReference6 = "",
                    photoReference7 = "",
                    photoReference8 = "",
                    photoReference9 = "",
                    photoReference10 = "",
                    photoDescription = "",
                    typeOfListing = "",
                    price = "",
                    surfaceArea = "",
                    numberOfRooms = "",
                    descriptionOfListing = "",
                    address = "",
                    pointsOfInterest = "",
                    status = "",
                    dateOnMarket = "",
                    saleDate = "",
                    realEstateAgent = ""
            )
            listingRepository.saveListing(listing)
            val updatedListing = ListingEntity(
                    id = 997,
                    photoCount = 0,
                    photoReference = "",
                    photoReference2 = "",
                    photoReference3 = "",
                    photoReference4 = "",
                    photoReference5 = "",
                    photoReference6 = "",
                    photoReference7 = "",
                    photoReference8 = "",
                    photoReference9 = "",
                    photoReference10 = "",
                    photoDescription = "test",
                    typeOfListing = "",
                    price = "",
                    surfaceArea = "",
                    numberOfRooms = "",
                    descriptionOfListing = "",
                    address = "",
                    pointsOfInterest = "",
                    status = "",
                    dateOnMarket = "",
                    saleDate = "",
                    realEstateAgent = ""
            )
            listingRepository.updateListing(updatedListing)

            listingRepository.getSingleItem(997).take(1).collect { result ->
                Assert.assertEquals("test", result.photoDescription)
            }
        }
    }
}