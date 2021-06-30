package com.openclassrooms.realestatemanager.data.di

import com.openclassrooms.realestatemanager.viewmodels.ListingsViewModel
import com.openclassrooms.realestatemanager.viewmodels.SingleListingViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    viewModel { ListingsViewModel(get()) }

    viewModel { SingleListingViewModel(get()) }

}