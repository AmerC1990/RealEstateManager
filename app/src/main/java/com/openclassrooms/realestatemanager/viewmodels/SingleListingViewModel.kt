package com.openclassrooms.realestatemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.data.repository.ListingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SingleListingViewModel constructor(private val repository: ListingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<SingleListingState>(SingleListingState.Loading)
    val uiState: StateFlow<SingleListingState> = _uiState

    fun getSingleListing(id: Int) {
        _uiState.value = SingleListingState.Loading
        viewModelScope.launch(IO) {
            repository.getSingleItem(id).take(1).collect { result ->
                _uiState.value = SingleListingState.Success(result)
            }
        }
    }

    fun putPhotoReference(photoCount: Int, photoReference: String) {
        viewModelScope.launch(IO) {
            _uiState.value = SingleListingState.SuccessPhoto(photoReference, photoCount)
        }
    }

    sealed class SingleListingState {
        data class SuccessPhoto(val photoReference: String, val photoCount: Int) : SingleListingState()
        data class Success(val listing: ListingEntity) : SingleListingState()
        data class Error(val message: String) : SingleListingState()
        object Loading : SingleListingState()
    }

}