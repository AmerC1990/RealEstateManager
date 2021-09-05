package com.openclassrooms.realestatemanager.viewmodels


import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.data.repository.ListingRepository
import com.openclassrooms.realestatemanager.filter.FilterContext
import com.openclassrooms.realestatemanager.filter.FilterParams
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ListingsViewModel constructor(private val repository: ListingRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ListingState>(ListingState.Loading)
    val uiState: StateFlow<ListingState> = _uiState

    fun fetchListings() {
        viewModelScope.launch(IO) {
            repository.getAllListings().collect { result ->
                _uiState.value = ListingState.Success(result)
            }
        }
    }

    fun insertListingInfo(entity: ListingEntity) {
        _uiState.value = ListingState.Loading
        viewModelScope.launch(IO) {
            val result = repository.saveListing(entity)
            _uiState.value = ListingState.SuccessSingle(result)
        }
    }

    fun filter(filterParams: FilterParams) {
        viewModelScope.launch(IO) {
            repository.getAllListings().collect { result ->
                val filteredData = FilterContext().executeStrategy(filterParams, result)
                _uiState.value = ListingState.Success(filteredData)
            }
        }


    }

    sealed class ListingState {
        data class SuccessSingle(val listingId: Long) : ListingState()
        data class Success(val listing: List<ListingEntity>) : ListingState()
        data class Error(val message: String) : ListingState()
        object Loading : ListingState()
    }
}