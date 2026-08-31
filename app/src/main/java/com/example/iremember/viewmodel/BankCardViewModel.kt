package com.example.iremember.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.iremember.IRememberApplication
import com.example.iremember.data.repository.BankCardRepository
import com.example.iremember.model.BankCard
import com.example.iremember.model.BankCardStyles
import com.example.iremember.util.CardFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BankCardViewModel(
    application: Application,
    private val repository: BankCardRepository
) : AndroidViewModel(application) {

    val allCards: StateFlow<List<BankCard>> = repository.allCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Combined balance across every saved card — the "Total balance" hero figure. */
    val totalBalance: StateFlow<Double> = repository.allCards
        .map { cards -> cards.sumOf { it.balance } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun cardById(id: Long): Flow<BankCard?> = repository.cardById(id)

    fun saveCard(
        cardId: Long,
        bankName: String,
        cardHolder: String,
        cardNumber: String,
        cardType: String,
        balance: Double,
        colorIndex: Int,
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val existing = if (cardId > 0L) repository.getByIdOnce(cardId) else null
            val card = BankCard(
                id = cardId.takeIf { it > 0L } ?: 0L,
                bankName = bankName.trim().ifBlank { "My card" },
                cardHolder = cardHolder.trim(),
                cardNumber = CardFormat.sanitizeNumber(cardNumber),
                cardType = cardType.ifBlank { BankCardStyles.DEFAULT_TYPE },
                balance = balance,
                colorIndex = BankCardStyles.clampColor(colorIndex),
                dateCreated = existing?.dateCreated ?: System.currentTimeMillis()
            )
            val id = repository.upsert(card)
            onSaved(id)
        }
    }

    fun deleteCard(cardId: Long, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            onDone(repository.delete(cardId))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as IRememberApplication
                BankCardViewModel(app, app.bankCardRepository)
            }
        }
    }
}
