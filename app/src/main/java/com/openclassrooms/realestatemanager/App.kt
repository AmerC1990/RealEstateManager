package com.openclassrooms.realestatemanager

import android.app.Application
import androidx.room.Room
import com.openclassrooms.realestatemanager.data.cache.ListingsDatabase

class App: Application() {

    companion object {
        var database: ListingsDatabase? = null
    }

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(this,ListingsDatabase::class.java, "listing_db").build()

//        startKoin {
//            androidLogger()
//            androidContext(this@App)
//            modules(dataModule + appModule)
//        }
    }
}