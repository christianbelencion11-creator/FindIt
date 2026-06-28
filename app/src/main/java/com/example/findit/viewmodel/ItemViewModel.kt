package com.example.findit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.findit.data.repository.ItemRepository
import com.example.findit.model.Item
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ItemViewModel(
    private val repository: ItemRepository
) : ViewModel() {

    val allItems: StateFlow<List<Item>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Item>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allItems
            } else {
                repository.searchItems(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun itemById(id: Long): Flow<Item?> = repository.getItemById(id)

    fun saveItem(
        name: String,
        location: String,
        category: String,
        notes: String,
        imageUri: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            repository.insertItem(
                Item(
                    name = name.trim(),
                    location = location.trim(),
                    category = category.trim(),
                    notes = notes.trim(),
                    imageUri = imageUri
                )
            )
            onSaved()
        }
    }
}

class ItemViewModelFactory(
    private val repository: ItemRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemViewModel::class.java)) {
            return ItemViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
