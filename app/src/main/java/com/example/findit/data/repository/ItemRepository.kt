package com.example.findit.data.repository

import com.example.findit.data.local.dao.ItemDao
import com.example.findit.data.local.entity.toItem
import com.example.findit.data.local.entity.toEntity
import com.example.findit.model.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ItemRepository(private val itemDao: ItemDao) {

    val allItems: Flow<List<Item>> = itemDao.getAllItems().map { entities ->
        entities.map { it.toItem() }
    }

    fun getItemById(id: Long): Flow<Item?> = itemDao.getItemById(id).map { it?.toItem() }

    fun searchItems(query: String): Flow<List<Item>> = itemDao.searchItems(query).map { entities ->
        entities.map { it.toItem() }
    }

    suspend fun insertItem(item: Item): Long = itemDao.insertItem(item.toEntity())

    init {
        CoroutineScope(Dispatchers.IO).launch {
            if (itemDao.getItemCount() == 0) {
                itemDao.insertItems(sampleItems.map { it.toEntity() })
            }
        }
    }

    companion object {
        private val sampleItems = listOf(
            Item(
                name = "House Keys",
                location = "Kitchen drawer",
                category = "Keys",
                notes = "Spare set with blue keychain",
                dateCreated = 1717977600000L
            ),
            Item(
                name = "Passport",
                location = "Office safe",
                category = "Documents",
                notes = "Expires 2028",
                dateCreated = 1718150400000L
            ),
            Item(
                name = "Wireless Earbuds",
                location = "Bedroom nightstand",
                category = "Electronics",
                notes = "Charging case is white",
                dateCreated = 1718323200000L
            ),
            Item(
                name = "Car Registration",
                location = "Glove compartment",
                category = "Documents",
                notes = "Renewal due next year",
                dateCreated = 1718409600000L
            ),
            Item(
                name = "Reading Glasses",
                location = "Living room bookshelf",
                category = "Personal",
                notes = "Black frame, +1.5 prescription",
                dateCreated = 1718668800000L
            ),
            Item(
                name = "USB-C Charger",
                location = "Home office desk",
                category = "Electronics",
                notes = "65W fast charger",
                dateCreated = 1718841600000L
            )
        )
    }
}
