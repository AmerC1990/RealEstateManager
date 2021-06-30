package com.openclassrooms.realestatemanager.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.data.repository.ListingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ListingsViewModel constructor(private val repository: ListingRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ListingState>(ListingState.Loading)
    val uiState: StateFlow<ListingState> = _uiState

    init {
        fetchListings()
    }

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

    fun insertListings(entities: List<ListingEntity>) {
        viewModelScope.launch(IO) {
            _uiState.value = ListingState.Success(entities)
        }
    }

    sealed class ListingState {
        data class SuccessSingle(val listingId: Long) : ListingState()
        data class Success(val listing: List<ListingEntity>) : ListingState()
        data class Error(val message: String) : ListingState()
        object Loading : ListingState()
    }
}