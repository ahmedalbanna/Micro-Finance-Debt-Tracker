package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.Client
import com.example.data.entity.Invoice
import com.example.data.entity.Payment
import com.example.data.entity.Reconciliation
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    // Clients
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getClientById(id: Int): Client?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client): Long

    @Update
    suspend fun updateClient(client: Client)

    @Delete
    suspend fun deleteClient(client: Client)

    // Invoices / Debts
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getInvoicesByClient(clientId: Int): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE clientId = :clientId AND isSettled = 0 ORDER BY createdAt ASC")
    suspend fun getUnsettledInvoicesByClientDirect(clientId: Int): List<Invoice>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    // Payments
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE clientId = :clientId ORDER BY paymentDate DESC")
    fun getPaymentsByClient(clientId: Int): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Delete
    suspend fun deletePayment(payment: Payment)

    // Reconciliations
    @Query("SELECT * FROM reconciliations")
    fun getAllReconciliations(): Flow<List<Reconciliation>>

    @Query("SELECT * FROM reconciliations WHERE invoiceId = :invoiceId")
    fun getReconciliationsByInvoice(invoiceId: Int): Flow<List<Reconciliation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReconciliation(reconciliation: Reconciliation): Long
}
