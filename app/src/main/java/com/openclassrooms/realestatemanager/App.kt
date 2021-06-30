package com.openclassrooms.realestatemanager

import android.app.Application
import androidx.room.Room
import com.openclassrooms.realestatemanager.data.cache.ListingsDatabase
import com.openclassrooms.realestatemanager.data.di.appModule
import com.openclassrooms.realestatemanager.data.di.dataModule
import com.openclassrooms.realestatemanager.data.repository.ListingRepository
import com.openclassrooms.realestatemanager.viewmodels.SingleListingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


class App : Application() {

    companion object {
        var database: ListingsDatabase? = null
    }

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(this, ListingsDatabase::class.java, "listing_db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(dataModule + appModule)
        }
    }
}