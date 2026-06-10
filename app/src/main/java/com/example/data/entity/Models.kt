package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clients",
    indices = [Index(value = ["phone"], unique = true)]
)
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val address: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["clientId"])]
)
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int,
    val title: String, // e.g. "شراء بضاعة ومستلزمات"
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val dueDate: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isSettled: Boolean = false
)

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["clientId"])]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int,
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMethod: String = "نقدي", // نقدي (Cash)، محفظة إلكترونية (Wallet)، إلخ
    val notes: String = ""
)

@Entity(
    tableName = "reconciliations",
    foreignKeys = [
        ForeignKey(
            entity = Payment::class,
            parentColumns = ["id"],
            childColumns = ["paymentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Invoice::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["paymentId"]),
        Index(value = ["invoiceId"])
    ]
)
data class Reconciliation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val paymentId: Int,
    val invoiceId: Int,
    val amountAllocated: Double
)
