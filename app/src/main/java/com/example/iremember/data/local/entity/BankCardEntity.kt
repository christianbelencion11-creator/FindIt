package com.example.iremember.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.iremember.model.BankCard

@Entity(tableName = "bank_cards")
data class BankCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerUid: String,
    val bankName: String,
    val cardHolder: String = "",
    val cardNumber: String = "",
    val cardType: String = "Debit",
    val balance: Double = 0.0,
    val colorIndex: Int = 0,
    val dateCreated: Long = System.currentTimeMillis(),
    val dateUpdated: Long = System.currentTimeMillis()
)

fun BankCardEntity.toBankCard(): BankCard = BankCard(
    id = id,
    ownerUid = ownerUid,
    bankName = bankName,
    cardHolder = cardHolder,
    cardNumber = cardNumber,
    cardType = cardType,
    balance = balance,
    colorIndex = colorIndex,
    dateCreated = dateCreated,
    dateUpdated = dateUpdated
)

fun BankCard.toEntity(): BankCardEntity = BankCardEntity(
    id = id,
    ownerUid = ownerUid,
    bankName = bankName,
    cardHolder = cardHolder,
    cardNumber = cardNumber,
    cardType = cardType,
    balance = balance,
    colorIndex = colorIndex,
    dateCreated = dateCreated,
    dateUpdated = dateUpdated
)
