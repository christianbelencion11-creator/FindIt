package com.example.iremember.data.repository

import com.example.iremember.data.local.dao.BankCardDao
import com.example.iremember.data.local.entity.toBankCard
import com.example.iremember.data.local.entity.toEntity
import com.example.iremember.model.BankCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Owner-scoped store for saved bank cards / e-wallets. Mirrors [NoteRepository]:
 * flows are keyed to the signed-in Firebase UID and go empty when no user is set,
 * so one account never sees another's cards. Local-only — never synced.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BankCardRepository(
    private val bankCardDao: BankCardDao
) {
    private val _currentOwnerUid = MutableStateFlow("")
    val currentOwnerUid = _currentOwnerUid.asStateFlow()

    fun setOwnerUid(uid: String) {
        _currentOwnerUid.value = uid
    }

    val allCards: Flow<List<BankCard>> = _currentOwnerUid.flatMapLatest { ownerUid ->
        if (ownerUid.isBlank()) {
            flowOf(emptyList())
        } else {
            bankCardDao.observeAll(ownerUid).map { list -> list.map { it.toBankCard() } }
        }
    }

    fun cardById(id: Long): Flow<BankCard?> = _currentOwnerUid.flatMapLatest { ownerUid ->
        if (ownerUid.isBlank()) {
            flowOf(null)
        } else {
            bankCardDao.observeById(ownerUid, id).map { it?.toBankCard() }
        }
    }

    suspend fun getByIdOnce(id: Long): BankCard? = bankCardDao.getByIdOnce(id)?.toBankCard()

    suspend fun upsert(card: BankCard): Long {
        val ownerUid = _currentOwnerUid.value
        require(ownerUid.isNotBlank()) { "Cannot save cards without a signed-in user." }
        val now = System.currentTimeMillis()
        val entity = card.copy(
            ownerUid = ownerUid,
            dateUpdated = now,
            dateCreated = if (card.id == 0L) now else card.dateCreated
        ).toEntity()
        return if (card.id == 0L) {
            bankCardDao.insert(entity)
        } else {
            bankCardDao.update(entity)
            card.id
        }
    }

    suspend fun delete(cardId: Long): Boolean {
        val ownerUid = _currentOwnerUid.value
        if (ownerUid.isBlank()) return false
        return bankCardDao.delete(ownerUid, cardId) > 0
    }
}
